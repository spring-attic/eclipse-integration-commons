package org.springsource.ide.eclipse.commons.quicksearch.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;

@SuppressWarnings("restriction")
public class QuickTextSearcher extends ResourceWalker {
		private final QuickTextSearchRequestor requestor;
		private final QuickTextQuery query;

		public QuickTextSearcher(QuickTextQuery query, QuickTextSearchRequestor requestor) {
			this.requestor = requestor;
			this.query = query;
		}

		@Override
		protected void visit(IFile f) {
			System.out.println("visit: "+f);
			FileTextSearchScope scope = FileTextSearchScope.newSearchScope(new IResource[] {f}, new String[] {"*"}, false);
			FileSearchQuery search = new FileSearchQuery(query.getPattern(), false, true, scope);
			search.run(new NullProgressMonitor());
			FileSearchResult result = (FileSearchResult) search.getSearchResult();
			for (Object el : result.getElements()) {
				for (Match _match : result.getMatches(el)) {
					FileMatch match = (FileMatch) _match;
					requestor.add(match.getLineElement());
				}
			}
			System.out.println(result);
			
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
	}