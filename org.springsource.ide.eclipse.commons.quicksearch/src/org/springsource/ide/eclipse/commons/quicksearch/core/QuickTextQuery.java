package org.springsource.ide.eclipse.commons.quicksearch.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextQuery.TextRange;


/**
 * Represents something you can search for with a 'quick search' text searcher. 
 * Typically just a case-insensitive String. But maybe could be a regular expression and be case
 * sensitive or not.
 * 
 * @author Kris De Volder
 */
public class QuickTextQuery {

	public class TextRange {
		public final int start;
		public final int len;
		public TextRange(int start, int len) {
			this.start = start;
			this.len = len;
		}
	}

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

	public boolean matchItem(LineItem item) {
		return item.getText().contains(this.pattern);
	}

	/**
	 * A trivial query is one that either 
	 *   - matches anything
	 *   - matches nothing
	 * In other words, if a query is 'trivial' then it returns either nothing or all the text in the scope
	 * of the search.
	 */
	public boolean isTrivial() {
		return "".equals(this.pattern);
	}

	@Override
	public String toString() {
		return "QTQuery("+pattern+")";
	}

	public List<TextRange> findAll(String text) {
		if (isTrivial()) {
			return Arrays.asList();
		} else {
			int len = pattern.length();
			LinkedList<TextRange> ranges = new LinkedList<TextRange>();
			int pos = text.indexOf(pattern);
			while (pos>=0) {
				ranges.add(new TextRange(pos, len));
				pos = text.indexOf(pattern, pos+1);
			}
			return ranges;
		}
	}

	public TextRange findFirst(String str) {
		//TODO: more efficient implementation, just search the first one 
		// no need to find all matches then toss away everything except the
		// first one.
		List<TextRange> all = findAll(str);
		if (all!=null && !all.isEmpty()) {
			return all.get(0);
		}
		return null;
	}
	
}
