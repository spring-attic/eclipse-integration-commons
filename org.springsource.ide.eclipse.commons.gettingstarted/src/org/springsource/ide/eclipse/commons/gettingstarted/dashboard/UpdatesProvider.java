/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springsource.ide.eclipse.dashboard.internal.ui.editors.UpdateNotification;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * @author Miles Parker
 * 
 * Code refactored from org.springsource.ide.eclipse.dashboard.internal.ui
 * .editors.DashboardMainPage:
 * 
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class UpdatesProvider extends FeedProvider {

	/**
	 * @param manager
	 */
	public UpdatesProvider() {
		super(FeedMonitor.RESOURCE_DASHBOARD_FEEDS_UPDATE);
	}

	@Override
	public String getDynamicArgumentValue(String id) {
		if (id.equals("html")) {
			List<UpdateNotification> updates = FeedMonitor.getInstance().getUpdates();
			if (updates == null) {
				return null;
			}
			String html = "";
			// make sure the entries are sorted correctly
			Collections.sort(updates, new Comparator<UpdateNotification>() {
				public int compare(UpdateNotification o1, UpdateNotification o2) {
					if (o2.getEntry() != null && o2.getEntry().getPublishedDate() != null && o1.getEntry() != null) {
						return o2.getEntry().getPublishedDate().compareTo(o1.getEntry().getPublishedDate());
					}
					return 0;
				}
			});

			for (UpdateNotification notification : updates) {
				String update = buildUpdate(notification);
				if (!update.isEmpty()) {
					html += update;
				}
			}
			return html;
		}
		return null;
	}

	private String buildUpdate(UpdateNotification notification) {
		String html = "";
		SyndEntry entry = notification.getEntry();
		html += "<div class=\"blog--container blog-preview\">";
		html += "	<div class=\"blog--title\">";
		html += "   <i class=\"fa fa-exclamation new-star\"></i>";
		html += "	<a href=\"\" onclick=\"ide.call('openWebPage', '" + entry.getLink() + "')\"><b>" + entry.getTitle()
				+ "</b></a>";
		html += "	</div>";
		html += "</div>";
		return html;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springsource.ide.eclipse.commons.browser.IBrowserElementProvider#
	 * isReady()
	 */
	@Override
	public boolean isReady() {
		return FeedMonitor.getInstance().getUpdates() != null;
	}
}
