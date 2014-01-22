/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

/**
 * Reads RSS feeds for dashboard
 * @author Terry Denney
 */
public class FeedsReader {

	private class SyndEntryComparator implements Comparator<SyndEntry> {

		public int compare(SyndEntry o1, SyndEntry o2) {
			Date date1 = new Date(0);
			Date date2 = new Date(0);

			if (o1.getUpdatedDate() != null) {
				date1 = o1.getUpdatedDate();
			}
			else if (o1.getPublishedDate() != null) {
				date1 = o1.getPublishedDate();
			}

			if (o2.getUpdatedDate() != null) {
				date2 = o2.getUpdatedDate();
			}
			else if (o2.getPublishedDate() != null) {
				date2 = o2.getPublishedDate();
			}

			return -1 * date1.compareTo(date2);
		}

	}

	private final List<SyndFeed> feeds;

	private final Map<SyndFeed, String> feedsWithIcons;

	private final Map<SyndEntry, SyndFeed> feedsWithEntries;

	public FeedsReader() {
		this.feeds = new ArrayList<SyndFeed>();
		this.feedsWithIcons = new HashMap<SyndFeed, String>();
		this.feedsWithEntries = new TreeMap<SyndEntry, SyndFeed>(new SyndEntryComparator());
	}

	public List<SyndFeed> getFeeds() {
		return feeds;
	}

	public Map<SyndEntry, SyndFeed> getFeedsWithEntries() {
		return feedsWithEntries;
	}

	public ImageDescriptor getImageDescriptorForFeed(SyndFeed feed) {
		if (feedsWithIcons.containsKey(feed)) {
			return IdeUiPlugin.getImageDescriptor(feedsWithIcons.get(feed));
		}
		else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void readFeeds(Reader reader, SyndFeedInput input, String iconPath) throws IllegalArgumentException,
			FeedException {
		SyndFeed feed = input.build(reader);
		if (feed != null) {
			feeds.add(feed);
			feedsWithIcons.put(feed, iconPath);

			List<SyndEntry> articlesList = feed.getEntries();
			if (!articlesList.isEmpty()) {
				for (SyndEntry article : articlesList) {
					feedsWithEntries.put(article, feed);
				}
			}
		}
	}

}
