package org.springsource.ide.eclipse.dashboard.internal.ui.feeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
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

public class FeedMonitor implements IStartup {

	public static final String RESOURCE_DASHBOARD_FEEDS_BLOGS = "dashboard.feeds.blogs";

	public static final String RESOURCE_DASHBOARD_FEEDS_UPDATE = "dashboard.feeds.update";

	private final static int FEED_POLLING_RATE = 5 * 60 * 1000; // 5 Minutes

	private Date lastUpdated;

	private static FeedMonitor instance;

	private boolean newFeedItems;

	private Set<SyndEntry> feedEntries;

	private List<UpdateNotification> updates;

	private List<IFeedListener> listeners = new ArrayList<IFeedListener>();

	public void earlyStartup() {
		IPreferenceStore prefStore = IdeUiPlugin.getDefault().getPreferenceStore();
		long lastUpdateLong = prefStore.getLong(IIdeUiConstants.PREF_FEED_ENTRY_LAST_UPDATE_DISPLAYED);
		lastUpdated = new Date(lastUpdateLong);

		addListener(new IFeedListener() {

			@Override
			public void updated(String id) {
				updateDashboardButtons();
			}
		});

		initializeFeeds();

		instance = this;
	}

	private void initializeFeeds() {
		{
			final Map<String, String> springMap = new HashMap<String, String>();
			String[] urls = ResourceProvider.getUrls(RESOURCE_DASHBOARD_FEEDS_BLOGS);
			for (String url : urls) {
				springMap.put(url, null);
			}
			final AggregateFeedJob feedJob = new AggregateFeedJob(springMap, RESOURCE_DASHBOARD_FEEDS_BLOGS);
			feedJob.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void done(IJobChangeEvent event) {
					Map<SyndEntry, SyndFeed> entryToFeed = feedJob.getFeedReader().getFeedsWithEntries();
					Set<SyndEntry> retrieveFeedEntries = entryToFeed.keySet();
					newFeedItems = false;
					feedEntries = new HashSet<SyndEntry>(retrieveFeedEntries);
					checkFeedsUpToDate();
					update(feedJob.getFeedName());
					feedJob.schedule(FEED_POLLING_RATE);
				}
			});
			feedJob.schedule();
		}

		{
			Map<String, String> updateMap = new HashMap<String, String>();
			updateMap.put(ResourceProvider.getUrl(RESOURCE_DASHBOARD_FEEDS_UPDATE), null);
			final AggregateFeedJob updatesJob = new AggregateFeedJob(updateMap, RESOURCE_DASHBOARD_FEEDS_UPDATE);
			updatesJob.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void done(IJobChangeEvent event) {
					updates = new ArrayList<UpdateNotification>(updatesJob
							.getNotifications());
					update(updatesJob.getFeedName());
					updatesJob.schedule(FEED_POLLING_RATE);
				}
			});
			updatesJob.schedule();
		}
	}

	public static FeedMonitor getInstance() {
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
