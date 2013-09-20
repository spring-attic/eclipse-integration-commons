/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Content provider for displaying RSS feeds on the dashboard
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class FeedsContentProvider implements IStructuredContentProvider {

	public static String removeTags(SyndContent content) {
		if (content == null) {
			return null;
		}

		String value = content.getValue();
		if (value == null) {
			return null;
		}

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
		// try {
		return StringEscapeUtils.unescapeHtml(result.toString());
		// }
		// catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// return "";
	}

	private Set<SyndEntry> entries = new HashSet<SyndEntry>();

	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		return null;
	}

	public Object[] getElements(Object inputElement) {
		List<Object> result = new ArrayList<Object>();
		for (SyndEntry entry : entries) {
			result.add(entry);

			String description = removeTags(entry.getDescription());
			if (description != null && description.length() > 0) {
				result.add(new StubSyndEntryImpl(description));
				// result.add(new StubSyndEntryImpl(""));
			}
		}

		return result.toArray();
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public boolean isUpToDate(Set<SyndEntry> newEntries) {
		return entries.equals(newEntries);
	}

	public void setFeeds(Set<SyndEntry> entries) {// , Map<SyndEntry, SyndFeed>
		// feedsMap) {
		this.entries = entries;
		// this.feedsMap = feedsMap;
	}

}
