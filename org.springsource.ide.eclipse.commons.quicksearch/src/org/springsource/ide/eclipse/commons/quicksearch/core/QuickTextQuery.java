package org.springsource.ide.eclipse.commons.quicksearch.core;

import org.eclipse.search.internal.ui.text.LineElement;

/**
 * Represents something you can search for with a 'quick search' text searcher. 
 * Typically just a case-insensitive String. But maybe could be a regular expression and be case
 * sensitive or not.
 * 
 * @author Kris De Volder
 */
public class QuickTextQuery {

	private String pattern;

	/**
	 * A query that matches anything.
	 */
	public QuickTextQuery() {
		this("");
	}
	
	public QuickTextQuery(String substring) {
		this.pattern = substring;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean equalsFilter(QuickTextQuery o) {
		return this.pattern.equals(o.pattern);
	}

	/**
	 * Returns true if the other query is a specialization of this query. I.e. any results matching the other
	 * query must also match this query. If this method returns true then we can optimize the search for other
	 * re-using already found results for this query. 
	 * <p>
	 * If it is hard or impossible to decide whether other query is a specialization of this query then this
	 * method is allowed to 'punt' and just return false. However, the consequence of this is that the query 
	 * will be re-run and instead of incrementally updated.
	 */
	public boolean isSubFilter(QuickTextQuery other) {
		return other.pattern.contains(this.pattern);
	}

	public boolean matchItem(LineElement item) {
		return item.getContents().contains(this.pattern);
	}

}
