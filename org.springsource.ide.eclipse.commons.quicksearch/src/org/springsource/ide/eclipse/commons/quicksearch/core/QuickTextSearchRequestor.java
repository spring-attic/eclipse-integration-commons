/*******************************************************************************
 * Copyright (c) 2012 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.core;

import org.eclipse.search.internal.ui.text.LineElement;

/**
 * Plays a similar role than SearchReqeustor in eclipse Searches. I.e. a search requestor
 * is some entity accepting the results of a search. Typically the requestor displays the
 * result to the user.
 * <p>
 * This API differs a little from the Eclipse SearchRequestor in that searches are 'live'
 * in the sense that they update while the user is typing. As the user is typing the
 * query is changing. This may result in results already results being changed or revoked.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class QuickTextSearchRequestor {

	/**
	 * Called when a line of text containing the search text is found.
	 */
	public void add(LineElement line) {};
	
	/**
	 * Called when a previously added line of text needs to be redisplayed (this happens if
	 * the query has changed but still matches the line. The line is still a match, but
	 * the highlighting of the search term is different.
	 */
	public void update(LineElement line) {}
	
	/**
	 * Called when a line of text previously added is no longer a match for the current query
	 */
	public void revoke(LineElement line) {}
}
