/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.core;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.springsource.ide.eclipse.commons.quicksearch.core.priority.PriorityFunction;
import org.springsource.ide.eclipse.commons.quicksearch.util.JobUtil;
import org.springsource.ide.eclipse.commons.quicksearch.util.LineReader;

public class QuickTextSearcher {
	private final QuickTextSearchRequestor requestor;
	private QuickTextQuery query;

	/**
	 * Keeps track of currently found matches. Items are added as they are found and may also
	 * be removed when the query changed and they become invalid.
	 */
	private Set<LineItem> matches = new HashSet<LineItem>(2000);

	/**
	 * Scheduling rule used by Jobs that work on the matches collection.
	 */
	private ISchedulingRule matchesRule = JobUtil.lightRule("QuickSearchMatchesRule");

	private SearchInFilesWalker walker = null;
	private IncrementalUpdateJob incrementalUpdate;

	/**
	 * This field gets set to request a query change. The new query isn't stuffed directly
	 * into the query field because the query is responded to by the updater job which needs
	 * access to both the original query and the newQuery to decide on an efficient strategy.
	 */
	private QuickTextQuery newQuery;

	/**
	 * If number of accumulated results reaches maxResults the search will be suspended.
	 * <p>
	 * Note that more results may still arrive beyond the limit since the searcher does not (yet) have the
	 * capability to suspend/resume a search in the middle of a file.
	 */
	private int maxResults = 200;

	/**
	 * If a line of text is encountered longer than this, the searcher will stop searching
	 * that file (this rule avoids searching machine generated text files, like minified javascript).
	 */
	private int MAX_LINE_LEN;

	/**
	 * While searching in a file, this field will be set. This can be used to show the name
	 * of the 'current file' in the progress area of the quicksearch dialog.
	 */
	private IFile currentFile = null;

	/**
	 * Retrieves the current result limit.
	 */
	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public QuickTextSearcher(QuickTextQuery query, PriorityFunction priorities, int maxLineLen, QuickTextSearchRequestor requestor) {
		this.MAX_LINE_LEN = maxLineLen;
		this.requestor = requestor;
		this.query = query;
		this.walker = createWalker(priorities);
	}

	private SearchInFilesWalker createWalker(PriorityFunction priorities) {
		final SearchInFilesWalker job = new SearchInFilesWalker();
		job.setPriorityFun(priorities);
		job.setRule(matchesRule);
		job.schedule();
		return job;
	}

	private final class SearchInFilesWalker extends ResourceWalker {


		@Override
		protected void visit(IFile f, IProgressMonitor mon) {
			if (checkCanceled(mon)) {
				return;
			}


			LineReader lr = null;
			currentFile = f;
			try {
				lr = new LineReader(new InputStreamReader(f.getContents(true), f.getCharset()), MAX_LINE_LEN);
				String line = null;
				int lineIndex = 1;
				while ((line = lr.readLine()) != null) {
					int offset = lr.getLastLineOffset();
					if (checkCanceled(mon)) {
						return;
					}

					boolean found = query.matchItem(line);
					if (found) {
						LineItem lineItem = new LineItem(f, line, lineIndex, offset);
						add(lineItem);
					}

					lineIndex++;
				}
			} catch (Exception e) {
			} finally {
				currentFile = null;
				if (lr != null) {
					lr.close();
				}
			}
		}

//		@Override
//		protected void visit(IFile f, IProgressMonitor mon) {
//			if (checkCanceled(mon)) {
//				return;
//			}
////			System.out.println("visit: "+f);
//			FileTextSearchScope scope = FileTextSearchScope.newSearchScope(new IResource[] {f}, new String[] {"*"}, false);
//			FileSearchQuery search = new FileSearchQuery(query.getPatternString(), false, query.isCaseSensitive(), scope);
//			search.run(new NullProgressMonitor());
//			FileSearchResult result = (FileSearchResult) search.getSearchResult();
//			for (Object el : result.getElements()) {
//				for (Match _match : result.getMatches(el)) {
//					if (checkCanceled(mon)) {
//						return;
//					}
//					FileMatch match = (FileMatch) _match;
//					LineItem line = new LineItem(match);
//					add(line);
//				}
//			}
//		}

		@Override
		public void resume() {
			//Only resume if we don't already exceed the maxResult limit.
			if (matches.size()<maxResults) {
				super.resume();
			}
		}

		private boolean checkCanceled(IProgressMonitor mon) {
			return mon.isCanceled();
		}

		public void requestMoreResults() {
			int currentSize = matches.size();
			maxResults = Math.max(maxResults, currentSize + currentSize/10);
			resume();
		}

	}

	/**
	 * This job updates already found matches when the query is changed.
	 * Both the walker job and this job share the same scheduling rule so
	 * only one of them can be executing at the same time.
	 * <p>
	 * This is to avoid problems with concurrent modification of the
	 * matches collection.
	 */
	private class IncrementalUpdateJob extends Job {
		public IncrementalUpdateJob() {
			super("Update matches");
			this.setRule(matchesRule);
			//This job isn't started automatically. It should be schedule every time
			// there's a 'newQuery' set by the user/client.
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			QuickTextQuery nq = newQuery; //Copy into local variable to avoid
										  // problems if another thread changes newQuery while we
										  // are still mucking with it.
			if (query.isSubFilter(nq)) {
				query = nq;
				performIncrementalUpdate(monitor);
			} else {
				query = nq;
				performRestart(monitor);
			}
			return monitor.isCanceled()?Status.CANCEL_STATUS:Status.OK_STATUS;
		}

		private void performIncrementalUpdate(IProgressMonitor mon) {
			Iterator<LineItem> items = matches.iterator();
			while (items.hasNext() && !mon.isCanceled()) {

				LineItem item = items.next();
				if (query.matchItem(item)) {
					//Match still valid but may need updating highlighted text in the UI:
					requestor.update(item);
				} else {
					items.remove();
					requestor.revoke(item);
				}
			}
			if (!mon.isCanceled()) {
				//Resume searching remaining files, if any.
				walker.resume();
			}
		}

		private void performRestart(IProgressMonitor mon) {
			//walker may be null if dialog got closed already before we managed to
			// 'performRestart'.
			if (walker!=null) {
				//since we are inside Job here that uses same scheduling rule as walker, we
				//know walker is not currently executing. so walker cancel should be instantenous
				matches.clear();
				requestor.clear();
				walker.cancel();
				if (!query.isTrivial()) {
					walker.init(); //Reinitialize the walker work queue to its starting state
					walker.resume(); //Allow walker to resume when we release the scheduling rule.
				}
			}
		}

	}

	private void add(LineItem line) {
		if (matches.add(line)) {
			requestor.add(line);
			if (matches.size() >= maxResults) {
				walker.suspend();
			}
		}
	}

	public void setQuery(QuickTextQuery newQuery) {
		if (newQuery.equalsFilter(query)) {
			return;
		}
		this.newQuery = newQuery;
		walker.suspend(); //The walker must be suspended so the update job can run, they share scheduling rule
						 // so only one job can run at any time.
		scheduleIncrementalUpdate();
	}

	public QuickTextQuery getQuery() {
		//We return the newQuery as soon as it was set, even if it has not yet been effectively applied
		// to previously found query results. Most logical since when you call 'setQuery' you would
		// expect 'getQuery' to return the query you just set.
		return newQuery!=null ? newQuery : query;
	}

	private synchronized void scheduleIncrementalUpdate() {
		//Any outstanding incremental update should be canceled since the query has changed again.
		if (incrementalUpdate!=null) {
			incrementalUpdate.cancel();
		}
		incrementalUpdate = new IncrementalUpdateJob();
		incrementalUpdate.schedule();
	}

	public boolean isDone() {
		//Walker can be null if job was canceled because dialog closed. But stuff like
		//the job that shows 'Searching ...' doesn't instantly stop and may still
		//be asking the incremental update job whether its done.
		return walker!=null && walker.isDone();
	}

	public void requestMoreResults() {
		if (walker!=null && !walker.isDone()) {
			walker.requestMoreResults();
		}
	}

	public void cancel() {
		if (walker!=null) {
			walker.cancel();
			walker = null;
		}
	}

	public IFile getCurrentFile() {
		return currentFile;
	}

}
