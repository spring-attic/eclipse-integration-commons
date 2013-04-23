package org.springsource.ide.eclipse.commons.quicksearch.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.eclipse.ui.internal.misc.StringMatcher.Position;


/**
 * Represents something you can search for with a 'quick search' text searcher. 
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class QuickTextQuery {

	//TODO: delete and use jface Region class instead.
	public class TextRange implements IRegion {
		public final int start;
		public final int len;
		public TextRange(int start, int len) {
			this.start = start;
			this.len = len;
		}
		public int getLength() {
			return len;
		}
		public int getOffset() {
			return start;
		}
	}

	private boolean caseSensitive;
	private String orgPattern; //Original pattern case preserved even if search is case insensitive.
	private StringMatcher pattern;

	/**
	 * A query that matches anything.
	 */
	public QuickTextQuery() {
		this("", true);
	}
	
	public QuickTextQuery(String substring, boolean caseSensitive) {
		this.orgPattern = substring;
		this.pattern = new StringMatcher(orgPattern, !caseSensitive, false);
		this.caseSensitive = caseSensitive;
	}

//	public StringMatcher getPattern() {
//		return pattern;
//	}

	public boolean equalsFilter(QuickTextQuery o) {
		return this.caseSensitive == o.caseSensitive && this.pattern.equals(o.pattern);
	}

	/**
	 * Returns true if the other query is a specialisation of this query. I.e. any results matching the other
	 * query must also match this query. If this method returns true then we can optimise the search for other
	 * re-using already found results for this query. 
	 * <p>
	 * If it is hard or impossible to decide whether other query is a specialisation of this query then this
	 * method is allowed to 'punt' and just return false. However, the consequence of this is that the query 
	 * will be re-run and instead of incrementally updated.
	 */
	public boolean isSubFilter(QuickTextQuery other) {
		if (this.caseSensitive==other.caseSensitive) {
			if (this.caseSensitive) {
				return other.orgPattern.contains(this.orgPattern);
			} else {
				return other.orgPattern.toLowerCase().contains(this.orgPattern.toLowerCase());
			}
		}
		return false;
	}

	public boolean matchItem(LineItem item) {
		String text = item.getText();
		Position pos = pattern.find(item.getText(), 0, text.length());
		return pos!=null;
	}

	/**
	 * A trivial query is one that either 
	 *   - matches anything
	 *   - matches nothing
	 * In other words, if a query is 'trivial' then it returns either nothing or all the text in the scope
	 * of the search.
	 */
	public boolean isTrivial() {
		return "".equals(this.orgPattern);
	}

	@Override
	public String toString() {
		return "QTQuery("+pattern+", "+(caseSensitive?"caseSens":"caseInSens")+")";
	}

	public List<TextRange> findAll(String text) {
		if (isTrivial()) {
			return Arrays.asList();
		} else {
			if (!caseSensitive) {
				text = text.toLowerCase();
			}
			Position pos = pattern.find(text, 0, text.length());
			List<TextRange> ranges = new ArrayList<QuickTextQuery.TextRange>();
			while (pos!=null) {
				ranges.add(new TextRange(pos.getStart(), pos.getEnd()-pos.getStart()));
				pos = pattern.find(text, Math.max(pos.getEnd(), pos.getStart()+1), text.length());
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
	
	public String getPatternString() {
		return orgPattern;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
}
