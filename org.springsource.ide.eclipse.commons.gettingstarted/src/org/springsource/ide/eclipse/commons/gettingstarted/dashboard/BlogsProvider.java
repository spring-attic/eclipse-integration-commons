/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (https://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (https://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * @author Miles Parker
 * 
 */
public class BlogsProvider extends FeedProvider {

	protected static final int FEEDS_DESCRIPTION_MAX = 200;

	private Date lastUpdated = null;

	private Date currentUpdated = null;

	/**
	 * @param manager
	 */
	public BlogsProvider() {
		super(FeedMonitor.RESOURCE_DASHBOARD_FEEDS_BLOGS);
		IPreferenceStore prefStore = IdeUiPlugin.getDefault().getPreferenceStore();
		long lastUpdateLong = prefStore.getLong(IIdeUiConstants.PREF_FEED_ENTRY_LAST_UPDATE_DISPLAYED);
		lastUpdated = new Date(lastUpdateLong);
		currentUpdated = lastUpdated;
	}

	private String buildDescription(SyndEntry entry) {
		SyndContent content = entry.getDescription();
		if (content == null) {
			List<?> nestedContent = entry.getContents();
			if (!nestedContent.isEmpty()) {
				Object obj = nestedContent.get(0);
				if (obj instanceof SyndContent) {
					content = (SyndContent) obj;
				}
			}
		}
		if (content == null) {
			return "";
		}

		String value = content.getValue();
		if (value == null) {
			return "";
		}

		if (value.startsWith("<form>")) {
			return value;
		}
		return removeHtmlEntities(value);
	}

	private String buildFeed(SyndEntry entry) {
		String html = "";
		Date entryDate = new Date(0);
		if (entry.getUpdatedDate() != null) {
			entryDate = entry.getUpdatedDate();
		}
		else {
			entryDate = entry.getPublishedDate();
		}

		String dateString = "";
		if (entryDate != null) {
			dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(entryDate);
		}

		String entryAuthor = "";
		if (entry.getAuthor() != null && entry.getAuthor().trim() != "") {
			entryAuthor = entry.getAuthor();
		}
		html += "<div class=\"blog--container blog-preview\">";
		html += "	<div class=\"blog--title\">";
		if (lastUpdated.before(entryDate)) {
			html += "<i class=\"fa fa-star new-star\"></i>";
		}
		if (currentUpdated.before(entryDate)) {
			currentUpdated = entryDate;
		}
		html += "	<a href=\"\" onclick=\"return ide.call('openWebPage', '" + entry.getLink() + "')\">" + entry.getTitle()
				+ "</a>";
		html += "	</div>";
		html += "	<div class=\"blog--post\">";
		html += "		<div>";
		html += "			<p>" + trimText(buildDescription(entry));
		html += "		<span class=\"author\">" + entryAuthor + " <i>" + dateString + "</i></span></p>";
		html += "		</div>";
		html += "	</div>";
		html += "</div>";
		return html;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunctionCall
	 * .Adapter#getDynamicArgumentValue(java.lang.String)
	 */
	@Override
	public String getFeedHtml() {
		Set<SyndEntry> feedEntries = FeedMonitor.getInstance().getFeedEntries();
		if (feedEntries == null || feedEntries.isEmpty()) {
			return null;
		}
		String html = "";
		List<SyndEntry> sortedEntries = new ArrayList<SyndEntry>(feedEntries);
		Collections.sort(sortedEntries, new Comparator<SyndEntry>() {
			public int compare(SyndEntry o1, SyndEntry o2) {
				Date o1Date = o1.getPublishedDate() != null ? o1.getPublishedDate() : o1.getUpdatedDate();
				Date o2Date = o2.getPublishedDate() != null ? o2.getPublishedDate() : o2.getUpdatedDate();
				if (o1Date == null && o2Date == null) {
					return 0;
				}
				else if (o1Date == null) {
					return -1;
				}
				else if (o2Date == null) {
					return 1;
				}
				else {
					return o2Date.compareTo(o1Date);
				}
			}
		});

		for (SyndEntry entry : sortedEntries) {
			html += buildFeed(entry);
		}
		return html;
	}

	private String trimText(String s) {
		// Remove html encoded entities
		s = StringEscapeUtils.unescapeHtml(s);

		// Remove line breaks and tabs
		s = s.replace("\n", " ");
		s = s.replace("\t", " ");

		// Remove whitespace between text
		String[] vals = s.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String v : vals) {
			if (v.trim().length() > 0) {
				sb.append(v).append(" ");
			}
		}

		if (sb.length() > FEEDS_DESCRIPTION_MAX) {
			return sb.substring(0, findEndOfWord(sb, FEEDS_DESCRIPTION_MAX)) + " ...";
			// return sb.substring(0, FEEDS_DESCRIPTION_MAX) + " ...";
		}
		return sb.toString();
	}

	private int findEndOfWord(StringBuilder sb, int pos) {
		Pattern pattern = Pattern.compile("\\w");
		while (pos < sb.length()) {
			if (pattern.matcher(sb.subSequence(pos, pos + 1)).matches()) {
				pos++;
			}
			else {
				return pos;
			}
		}
		return pos;
	}

	private String removeHtmlEntities(String value) {
		StringBuilder result = new StringBuilder();
		boolean tagOpened = false;
		for (char currChar : value.toCharArray()) {
			if (currChar == '<') {
				tagOpened = true;
			}
			else if (currChar == '>') {
				tagOpened = false;
			}
			else {
				if (!tagOpened) {
					result.append(currChar);
				}
			}
		}
		return StringEscapeUtils.unescapeHtml(result.toString());

	}

	@Override
	public boolean isFeedReady() {
		Set<SyndEntry> entries = FeedMonitor.getInstance().getFeedEntries();
		return entries!=null && !entries.isEmpty();
	}

}
