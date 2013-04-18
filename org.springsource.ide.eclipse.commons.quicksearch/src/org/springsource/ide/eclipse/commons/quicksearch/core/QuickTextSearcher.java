package org.springsource.ide.eclipse.commons.quicksearch.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;
import org.springsource.ide.eclipse.commons.quicksearch.util.JobUtil;

@SuppressWarnings("restriction")
public class QuickTextSearcher {
	private final QuickTextSearchRequestor requestor;
	private QuickTextQuery query;
	
	/**
	 * Keeps track of currently found matches. Items are added as they are found and may also
	 * be removed when the query changed and they become invalid.
	 */
	private LinkedHashSet<LineItem> matches;
	
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
	
	public QuickTextSearcher(QuickTextQuery query, QuickTextSearchRequestor requestor) {
		this.requestor = requestor;
		this.query = query;
		this.walker = createWalker();
		this.matches = new LinkedHashSet<LineItem>(2000);
	}
	
	private SearchInFilesWalker createWalker() {
		final SearchInFilesWalker job = new SearchInFilesWalker();
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
			System.out.println("visit: "+f);
			FileTextSearchScope scope = FileTextSearchScope.newSearchScope(new IResource[] {f}, new String[] {"*"}, false);
			FileSearchQuery search = new FileSearchQuery(query.getPattern(), false, true, scope);
			search.run(new NullProgressMonitor());
			FileSearchResult result = (FileSearchResult) search.getSearchResult();
			for (Object el : result.getElements()) {
				for (Match _match : result.getMatches(el)) {
					if (checkCanceled(mon)) {
						return;
					}
					FileMatch match = (FileMatch) _match;
					LineItem line = new LineItem(match);
					add(line);
				}
			}

			//				InputStream content = null;
			//				try {
			//					content = f.getContents();
			//					BufferedReader lines = new BufferedReader(new InputStreamReader(content));
			//					String line = lines.readLine();
			//					int lineNumber = 0;
			//					while (line!=null) {
			//						contentProvider.add(new LineItem(f, line, lineNumber++), itemsFilter);
			//						line = lines.readLine();
			//					}
			//				} catch (CoreException e) {
			//					Activator.log(e);
			//				} catch (IOException e) {
			//					Activator.log(e);
			//				} finally {
			//					if (content!=null) {
			//						try {
			//							content.close();
			//						} catch (IOException e) {
			//						}
			//					}
			//				}
		}

		private boolean checkCanceled(IProgressMonitor mon) {
			return mon.isCanceled();
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
			//since we are inside Job here that uses same scheduling rule as walker, we 
			//know walker is not currently executing. so walker cancel should be instantenous
			matches.clear();
			requestor.clear();
			walker.cancel();
			walker.init(); //Reinitialize the walker work queue to its starting state
			walker.resume(); //Ensure walker is going to start again when we release the scheduling rule.
		}

	}
	
	private void add(LineItem line) {
		if (matches.add(line)) {
			requestor.add(line);
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
		// to allready found query results.
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



}