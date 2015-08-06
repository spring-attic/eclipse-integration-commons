/*******************************************************************************
 * Copyright (c) 2012 - 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.feeds;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.AggregateFeedJob;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.UpdateNotification;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class FeedMonitor {

	public static final String RESOURCE_DASHBOARD_FEEDS_BLOGS = "dashboard.feeds.blogs";

	public static final String RESOURCE_DASHBOARD_FEEDS_UPDATE = "dashboard.feeds.update";

	private final static int FEED_POLLING_RATE = 60 * 60 * 1000; // 60 Minutes

	private static final long FEED_STARTUP_DELAY = 15 * 1000; // Allow STS to startup before pulling feeds from the internet

	private Date lastUpdated;

	private static FeedMonitor instance;

	private boolean newFeedItems;

	private Set<SyndEntry> feedEntries;

	private List<UpdateNotification> updates;

	private List<IFeedListener> listeners = new ArrayList<IFeedListener>();
	
	private AggregateFeedJob blogFeedJob = null;
	
	private AggregateFeedJob newsFeedJob = null;

	private FeedMonitor() {
		IPreferenceStore prefStore = IdeUiPlugin.getDefault().getPreferenceStore();
		long lastUpdateLong = prefStore.getLong(IIdeUiConstants.PREF_FEED_ENTRY_LAST_UPDATE_DISPLAYED);
		lastUpdated = new Date(lastUpdateLong);
		
		prefStore.addPropertyChangeListener(new IPropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (IIdeUiConstants.PREF_UPDATE_DASHBOARD_NEWS_FEED.equals(event.getProperty())) {
					initNewsFeedUpdates();
				}				
			}
		});
		
		ResourceProvider.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				if (RESOURCE_DASHBOARD_FEEDS_BLOGS.equals(evt.getPropertyName())) {
					initBlogFeedUpdates();
				}
			}
		});

		addListener(new IFeedListener() {

			@Override
			public void updated(String id) {
				updateDashboardButtons();
			}
		});

		initBlogFeedUpdates();
		initNewsFeedUpdates();
	}


	private void initBlogFeedUpdates() {
		final Map<String, String> springMap = new HashMap<String, String>();
		String[] urls = ResourceProvider.getUrls(RESOURCE_DASHBOARD_FEEDS_BLOGS);
		for (String url : urls) {
			springMap.put(url, null);
		}
		if (blogFeedJob != null) {
			blogFeedJob.cancel();
		}
		if (!springMap.isEmpty()) {
			blogFeedJob = new AggregateFeedJob(springMap, RESOURCE_DASHBOARD_FEEDS_BLOGS);
			blogFeedJob.setSystem(true);
			blogFeedJob.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void done(IJobChangeEvent event) {
					Map<SyndEntry, SyndFeed> entryToFeed = blogFeedJob.getFeedReader().getFeedsWithEntries();
					Set<SyndEntry> retrieveFeedEntries = entryToFeed.keySet();
					newFeedItems = false;
					feedEntries = new HashSet<SyndEntry>(retrieveFeedEntries);
					checkFeedsUpToDate();
					update(blogFeedJob.getFeedName());
					if (event.getResult().getSeverity() != IStatus.CANCEL) {
						blogFeedJob.schedule(FEED_POLLING_RATE);
					}
				}
			});
			//Add a delay for the initial feed fetch. The job, when run early during startup causes trouble:
			// See https://issuetracker.springsource.com/browse/STS-4188
			blogFeedJob.schedule(FEED_STARTUP_DELAY);
		}
	}
	
	private void initNewsFeedUpdates() {
		if (newsFeedJob != null) {
			newsFeedJob.cancel();
		}
		if (IdeUiPlugin.getDefault().getPreferenceStore().getBoolean(IIdeUiConstants.PREF_UPDATE_DASHBOARD_NEWS_FEED)) {
			Map<String, String> updateMap = new HashMap<String, String>();
			updateMap.put(ResourceProvider.getUrl(RESOURCE_DASHBOARD_FEEDS_UPDATE), null);
			newsFeedJob = new AggregateFeedJob(updateMap, RESOURCE_DASHBOARD_FEEDS_UPDATE);
			newsFeedJob.setSystem(true);
			newsFeedJob.addJobChangeListener(new JobChangeAdapter() {
	
				@Override
				public void done(IJobChangeEvent event) {
					updates = new ArrayList<UpdateNotification>(newsFeedJob
							.getNotifications());
					update(newsFeedJob.getFeedName());
					if (event.getResult().getSeverity() != IStatus.CANCEL) {
						newsFeedJob.schedule(FEED_POLLING_RATE);
					}
				}
			});
			newsFeedJob.schedule(FEED_STARTUP_DELAY);
		}
	}

	public static synchronized FeedMonitor getInstance() {
		if (instance==null) {
			instance = new FeedMonitor();
		}
		return instance;
	}

	public boolean isNewFeedItems() {
		return newFeedItems;
	}

	public Set<SyndEntry> getFeedEntries() {
		return feedEntries;
	}

	public List<UpdateNotification> getUpdates() {
		return updates;
	}

	private void update(String id) {
		if (PlatformUI.isWorkbenchRunning()) {
			for (IFeedListener listener : listeners) {
				listener.updated(id);
			}
		}
	}

	public void updateDashboardButtons() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					ICommandService commandService = (ICommandService) activeWorkbenchWindow
							.getService(ICommandService.class);
					if (commandService != null) {
						commandService.refreshElements("org.springsource.ide.eclipse.dashboard.ui.showDashboard", null);
					}
				}
			}
		});
	}

	private void checkFeedsUpToDate() {
		newFeedItems = false;
		for (SyndEntry entry : feedEntries) {
			if ((entry.getUpdatedDate() != null && entry.getUpdatedDate().after(lastUpdated))
					|| (entry.getPublishedDate() != null && entry.getPublishedDate().after(lastUpdated))) {
				newFeedItems = true;
				break;
			}
		}
	}
	
	public void markRead() {
		lastUpdated = new Date();
		IPreferenceStore prefStore = IdeUiPlugin.getDefault().getPreferenceStore();
		prefStore.setValue(IIdeUiConstants.PREF_FEED_ENTRY_LAST_UPDATE_DISPLAYED, lastUpdated.getTime());
		if (PlatformUI.isWorkbenchRunning()) {
			checkFeedsUpToDate();
			updateDashboardButtons();
		}
	}

	public void addListener(IFeedListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IFeedListener listener) {
		listeners.remove(listener);
	}

}
