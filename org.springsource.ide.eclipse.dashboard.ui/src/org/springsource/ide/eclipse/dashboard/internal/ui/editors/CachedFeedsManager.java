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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;

import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

/**
 * This class manages reading and storing of cached RSS feeds
 * @author Terry Denney
 * @author Steffen Pingel
 */
public class CachedFeedsManager {

	private final Map<String, String> hashedFeedsToIconsMap;

	private final String feedName;

	private static final String DIRECTORY_FEED = "feeds";

	private final FeedsReader reader;

	public CachedFeedsManager(String feedName, Map<String, String> feedsToIconsMap, FeedsReader reader) {
		this.feedName = feedName;
		this.reader = reader;

		hashedFeedsToIconsMap = new HashMap<String, String>();
		for (String feed : feedsToIconsMap.keySet()) {
			hashedFeedsToIconsMap.put(Integer.toString(feed.hashCode()), feedsToIconsMap.get(feed));
		}
	}

	public void cacheFeeds(Map<String, String> feedToContent) throws IOException {
		File folder = new File(getCacheFolderPath().toOSString());
		if (folder.exists()) {
			for (File file : folder.listFiles()) {
				file.delete();
			}
		}
		else {
			folder.mkdirs();
		}

		// cache newly read feeds
		for (String url : feedToContent.keySet()) {
			String content = feedToContent.get(url);
			if (content.length() > 0) {
				File file = getCachedFeed(url);
				FileWriter writer = new FileWriter(file);
				writer.write(content.toString().toCharArray());
				writer.close();
			}
		}
	}

	private File getCachedFeed(String feedUrl) {
		File file = new File(getCacheFolderPath().append(feedUrl.hashCode() + ".xml").toOSString());
		return file;
	}

	private File[] getCachedFeeds() {
		File folder = new File(getCacheFolderPath().toOSString());
		if (folder.exists() && folder.isDirectory()) {
			return folder.listFiles();
		}
		return new File[0];
	}

	private IPath getCacheFolderPath() {
		IPath folderPath = Platform.getStateLocation(IdeUiPlugin.getDefault().getBundle()).append(DIRECTORY_FEED)
				.append(feedName);
		return folderPath;
	}

	public void readCachedFeeds(IProgressMonitor monitor) throws FeedException {
		SyndFeedInput input = new SyndFeedInput();
		File[] cachedFiles = getCachedFeeds();
		for (File cachedFile : cachedFiles) {
			String fileName = cachedFile.getName().replaceAll(".xml", "");
			String iconPath = hashedFeedsToIconsMap.get(fileName);
			try {
				reader.readFeeds(new FileReader(cachedFile), input, iconPath);
			}
			catch (FileNotFoundException e) {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
						"An unexpected error occurred while retrieving feed content from cache.", e));
			}
		}
	}

}
