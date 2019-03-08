/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardMainPage.FeedType;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Label provider for displaying RSS feeds on the dashboard
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class FeedsLabelProvider implements IFontProvider, IColorProvider, ITableLabelProvider {

	private final IPreferenceStore prefStore;

	private FeedsReader feedReader;

	private Map<SyndEntry, SyndFeed> feedsMap;

	// private final FormToolkit toolkit;

	private static final String ICON_BLOG_BLANK = "rss/blank.png";

	private static final String ICON_BLOG_INCOMING = "rss/overlay-incoming.png";

	private final FeedType feedType;

	private final Color feedColor;

	public FeedsLabelProvider(IPreferenceStore prefStore, DashboardMainPage.FeedType feedType, Color feedColor) {
		this.prefStore = prefStore;
		// this.toolkit = toolkit;
		this.feedType = feedType;
		this.feedColor = feedColor;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Color getBackground(Object element) {
		return null;
	}

	public Image getColumnImage(Object element, int index) {
		// // TODO Auto-generated method stub
		// return super.getImage(element);
		// }
		// public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof SyndEntry) {
			SyndEntry entry = (SyndEntry) element;
			SyndFeed feed = feedsMap.get(entry);
			if (feed == null) {
				return CommonImages.getImage(IdeUiPlugin.getImageDescriptor(ICON_BLOG_BLANK));
			}
			//
			// ImageDescriptor incoming = null;
			ImageDescriptor feedImageDesc = feedReader.getImageDescriptorForFeed(feed);

			if (feedType.equals(DashboardMainPage.FeedType.BLOG)) {
				if (!prefStore.getBoolean(IIdeUiConstants.PREF_FEED_ENTRY_READ_STATE + ":" + entry.getLink())) {
					return IdeUiPlugin.getImage(ICON_BLOG_INCOMING);
					// incoming =
					// IdeUiPlugin.getImageDescriptor(ICON_BLOG_INCOMING);
				}
			}

			if (feedImageDesc == null) {
				return IdeUiPlugin.getImage(ICON_BLOG_BLANK);
			}
			return CommonImages.getImage(feedImageDesc);

			// if (incoming != null) {
			// return CommonImages.getImageWithOverlay(feedImageDesc, incoming,
			// true, true);
			// }
			// return CommonImages.getImage(feedImageDesc);
		}

		return null;
	}

	public String getColumnText(Object element, int index) {
		// // TODO Auto-generated method stub
		// return super.getText(element);
		// }
		// public String getColumnText(Object element, int columnIndex) {
		if (element instanceof StubSyndEntryImpl) {
			return removeHtmlEntities(((StubSyndEntryImpl) element).getText());
		}
		if (element instanceof SyndEntry) {
			SyndEntry entry = (SyndEntry) element;
			SyndFeed feed = feedsMap.get(entry);
			if (feed == null) {
				return null;
			}

			String title = entry.getTitle();

			Date entryDate = new Date(0);
			if (entry.getUpdatedDate() != null) {
				entryDate = entry.getUpdatedDate();
			}
			else {
				entryDate = entry.getPublishedDate();
			}

			String dateString = "";
			if (entryDate != null) {
				dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(entryDate);
			}

			String entryAuthor = "";
			if (entry.getAuthor() != null && entry.getAuthor().trim() != "") {
				entryAuthor = " by " + entry.getAuthor();
			}

			if (feedType.equals(DashboardMainPage.FeedType.BLOG) && dateString.length() > 0 && entryAuthor.length() > 0) {
				return removeHtmlEntities(title + " (" + dateString + entryAuthor + ")");
			}
			return removeHtmlEntities(title);
		}

		return null;
	}

	public Font getFont(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		if (!(element instanceof StubSyndEntryImpl)) {
			return feedColor;
		}
		return null;
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	private String removeHtmlEntities(String s) {
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
		return sb.toString();
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void setFeedsMap(Map<SyndEntry, SyndFeed> feedsMap, FeedsReader feedReader) {
		this.feedsMap = feedsMap;
		this.feedReader = feedReader;
	}

}
