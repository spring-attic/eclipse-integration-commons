/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.ui.progress.IProgressConstants;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.core.HttpUtil;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.util.IdeUiUtils;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Miles Parker
 */
public class AggregateFeedJob extends Job {

	public static final Object CONTENT_FAMILY = new Object();

	private final Map<String, String> feedsToIconsMap;

	private final CachedFeedsManager feedManager;

	private final FeedsReader feedReader;
	
	private String feedName;

	private List<UpdateNotification> notifications = new ArrayList<UpdateNotification>();

	public AggregateFeedJob(Map<String, String> feedsToIconsMap, String feedName) {
		this("Downloading RSS feeds", feedsToIconsMap, feedName);
	}

	public AggregateFeedJob(String name, Map<String, String> feedsToIconsMap, String feedName) {
		super(name);
		this.feedsToIconsMap = feedsToIconsMap;
		this.feedName = feedName;
		feedReader = new FeedsReader();
		feedManager = new CachedFeedsManager(feedName, feedsToIconsMap, feedReader);
		setProperty(IProgressConstants.ICON_PROPERTY, StsUiImages.RSS);
	}

	@Override
	public boolean belongsTo(Object family) {
		return CONTENT_FAMILY == family;
	}

	public FeedsReader getFeedReader() {
		return feedReader;
	}

	public boolean isCoveredBy(AggregateFeedJob other) {
		if (other.feedsToIconsMap != null && this.feedsToIconsMap != null) {
			return other.feedsToIconsMap.equals(this.feedsToIconsMap);
		}
		return false;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		synchronized (getClass()) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			Job[] buildJobs = Job.getJobManager().find(CONTENT_FAMILY);
			for (Job curr : buildJobs) {
				if (curr != this && curr instanceof AggregateFeedJob) {
					AggregateFeedJob job = (AggregateFeedJob) curr;
					if (job.isCoveredBy(this)) {
						curr.cancel();
					}
				}
			}
		}

		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		final CountDownLatch resultLatch = new CountDownLatch(1);

		Runnable downloadRunnable = new Runnable() {

			public void run() {
				SyndFeedInput input = new SyndFeedInput();
				Map<String, String> feedToContent = new HashMap<String, String>();

				try {
					Set<Entry<String, String>> entrySet = feedsToIconsMap.entrySet();
					SubMonitor progress = SubMonitor.convert(monitor, entrySet.size());
					if (!entrySet.isEmpty()) {
						Iterator<Entry<String, String>> iter = entrySet.iterator();

						while (iter.hasNext()) {
							Entry<String, String> entry = iter.next();
							String feedUrlStr = entry.getKey();
							String iconPath = entry.getValue();

							XmlReader reader;
							if (feedUrlStr.startsWith("http")) {
								reader = new XmlReader(HttpUtil.stream(new URI(feedUrlStr), progress.newChild(1)));
							}
							else {
								InputStream stream = FileLocator.openStream(IdeUiPlugin.getDefault().getBundle(),
										new Path(feedUrlStr), false);
								reader = new XmlReader(stream);
							}

							StringBuilder cachedFeed = new StringBuilder();
							char[] buffer = new char[256];
							int length = 0;
							while ((length = reader.read(buffer)) > 0) {
								cachedFeed.append(buffer, 0, length);
							}
							reader.close();

							feedReader.readFeeds(new StringReader(cachedFeed.toString()), input, iconPath);

							feedToContent.put(feedUrlStr, cachedFeed.toString());
						}

					}
					feedManager.cacheFeeds(feedToContent);
				}
				catch (Exception e) {
					// do nothing, feeds read from cache
				}
				resultLatch.countDown();
			}
		};

		try {
			new Thread(downloadRunnable).start();
			if (resultLatch.await(30, TimeUnit.SECONDS)) {
				updateNotifications(monitor);
				return Status.OK_STATUS;
			}
			else {
				// if this code is reached, feeds were not read properly. Try
				// reading from cache if it exists
				try {
					feedManager.readCachedFeeds(monitor);
				}
				catch (Exception e) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
							"An unexpected error occurred while retrieving feed content from cache.", e));
				}
			}
		}
		catch (InterruptedException e) {
		}
		updateNotifications(monitor);
		return Status.CANCEL_STATUS;
	}

	private void updateNotifications(IProgressMonitor monitor) {
		Map<SyndEntry, SyndFeed> entryToFeed = getFeedReader().getFeedsWithEntries();
		Set<SyndEntry> entries = entryToFeed.keySet();

		Set<String> installedFeatures = null;
		try {
			installedFeatures = DiscoveryUi.createInstallJob().getInstalledFeatures(monitor);
		}
		catch (NullPointerException e) {
			// profile is not available
		}
		Dictionary<Object, Object> environment = new Hashtable<Object, Object>(System.getProperties());

		// make sure the entries are sorted correctly
		List<SyndEntry> sortedEntries = new ArrayList<SyndEntry>(entries);
		Collections.sort(sortedEntries, new Comparator<SyndEntry>() {
			public int compare(SyndEntry o1, SyndEntry o2) {
				Date o1Date = o1.getPublishedDate() != null ? o1.getPublishedDate() : o1.getUpdatedDate();
				Date o2Date = o2.getPublishedDate() != null ? o2.getPublishedDate() : o2.getUpdatedDate();
				if (o1Date == null && o2Date == null) {
					return 0;
				} else if (o1Date == null) {
					return -1;
				} else if (o2Date == null) {
					return 1;
				} else {
					return o2Date.compareTo(o1Date);
				}
			}
		});

		Version ideVersion = IdeUiUtils.getVersion();
		Set<UpdateNotification> notificationsSet = new HashSet<UpdateNotification>(notifications);
		for (SyndEntry entry : sortedEntries) {
			UpdateNotification notification = new UpdateNotification(entry);
			if (notification.matches(ideVersion, installedFeatures, environment)) {
				notificationsSet.add(notification);
			}
		}
		notifications = new ArrayList<UpdateNotification>(notificationsSet);
	}

	public List<UpdateNotification> getNotifications() {
		return notifications;
	}

	public String getFeedName() {
		return feedName;
	}
}
