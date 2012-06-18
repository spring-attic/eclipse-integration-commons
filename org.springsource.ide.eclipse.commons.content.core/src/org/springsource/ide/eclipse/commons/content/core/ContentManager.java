/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.content.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor.Dependency;
import org.springsource.ide.eclipse.commons.content.core.util.DescriptorReader;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.springsource.ide.eclipse.commons.core.HttpUtil;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.internal.content.core.DescriptorMatcher;

/**
 * Manages the list of available tutorials and sample projects.
 * @author Terry Denney
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Kris De Volder
 * @author Kaitlin Duck Sherwood
 */
public class ContentManager {

	private static final boolean DEBUG = ("" + Platform.getLocation()).contains("bamboo");

	// lightweight lock
	private boolean isRefreshing = false;

	public boolean isRefreshing() {
		return isRefreshing;
	}

	private static void debug(String msg) {
		if (DEBUG) {
			System.out.println(msg);
		}
	}

	private void debug(Exception e) {
		if (DEBUG) {
			e.printStackTrace(System.out);
		}
	}

	public class DownloadJob extends Job {

		private final ContentItem rootItem;

		final CountDownLatch resultLatch = new CountDownLatch(1);

		private DownloadJob(String name, ContentItem rootItem) {
			super(name);
			this.rootItem = rootItem;
		}

		public CountDownLatch getLatch() {
			return resultLatch;

		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(ContentPlugin.PLUGIN_ID, 0, NLS.bind(
					"Download of ''{0}'' (''{1}'') failed", rootItem.getName(), rootItem.getId()), null);
			try {
				List<ContentItem> dependencies = getDependencies(rootItem);
				SubMonitor progress = SubMonitor.convert(monitor, dependencies.size() * 3 + 1);
				for (ContentItem item : dependencies) {
					String url = item.getRemoteDescriptor().getUrl();
					File baseDirectory = getInstallDirectory();
					File archiveFile = new File(baseDirectory, item.getPathFromRemoteDescriptor() + ARCHIVE_EXTENSION);
					File directory = new File(baseDirectory, item.getPathFromRemoteDescriptor());

					IStatus status = HttpUtil.download(url, archiveFile, directory, progress.newChild(3));
					result.add(status);
				}

				refresh(progress.newChild(1));
			}
			catch (CoreException e) {
				return new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, 0, NLS.bind(
						"Failed to determine dependencies of ''{0}'' (''{1}'')", rootItem.getName(), rootItem.getId()),
						e);
			}
			finally {
				resultLatch.countDown();
			}
			return result;
		}
	}

	public static final String EVENT_REFRESH = "refresh";

	public static final String KIND_SAMPLE = "sample";

	public static final String KIND_TEMPLATE = "template";

	public static final String KIND_TUTORIAL = "tutorial";

	private static final String DIRECTORY_METADATA = ".metadata";

	private static final String DIRECTORY_STS = ".sts";

	private static final String DIRECTORY_INSTALL = "content";

	public static final String ARCHIVE_EXTENSION = ".zip";

	public static final String[] DESCRIPTOR_FILENAMES = { IContentConstants.SAMPLE_PROJECT_DATA_FILE_NAME,
			IContentConstants.TUTORIAL_DATA_FILE_NAME, "server.xml", "template.xml" };

	public static final String RESOURCE_CONTENT_DESCRIPTORS = "content.descriptors";

	private final Map<String, ContentItem> itemById;

	private final Map<String, Set<ContentItem>> itemsByKind;

	private final List<PropertyChangeListener> listeners;

	private File stateFile;

	private File defaultStateFile;

	public ContentManager() {
		itemById = new HashMap<String, ContentItem>();
		itemsByKind = new HashMap<String, Set<ContentItem>>();
		listeners = new CopyOnWriteArrayList<PropertyChangeListener>();
	}

	public void addListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	public DownloadJob createDownloadJob(ContentItem item) {
		return new DownloadJob(NLS.bind("Downloading {0}", item.getName()), item);
	}

	private ContentItem createItem(Descriptor descriptor) {
		String id = descriptor.getId();
		ContentItem item = new ContentItem(id);
		itemById.put(id, item);

		String kind = descriptor.getKind();
		Set<ContentItem> items = itemsByKind.get(kind);
		if (items == null) {
			items = new HashSet<ContentItem>();
			itemsByKind.put(kind, items);
		}
		items.add(item);

		return item;
	}

	private void firePropertyChangeEvent(String eventName) {
		PropertyChangeListener[] listeners = this.listeners.toArray(new PropertyChangeListener[0]);
		if (listeners.length > 0) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, eventName, null, null);
			for (PropertyChangeListener listener : listeners) {
				listener.propertyChange(event);
			}
		}
	}

	public File getDataDirectory() {
		return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().append(DIRECTORY_METADATA)
				.append(DIRECTORY_STS).toOSString());
	}

	public File getDefaultStateFile() {
		return defaultStateFile;
	}

	/**
	 * @param item
	 * @return
	 * @throws CoreException
	 */
	public List<ContentItem> getDependencies(ContentItem item) throws CoreException {
		List<ContentItem> results = new ArrayList<ContentItem>();

		Stack<ContentItem> queue = new Stack<ContentItem>();
		queue.add(item);
		while (!queue.isEmpty()) {
			ContentItem next = queue.pop();
			results.add(next);
			List<Dependency> dependencies = next.getRemoteDescriptor().getDependencies();
			for (Dependency dependency : dependencies) {
				ContentItem dependentItem = itemById.get(dependency.getId());
				if (dependentItem == null) {
					String message = NLS.bind(
							"Failed to resolve dependencies: ''{0}'' requires ''{1}'' which is not available",
							next.getId(), dependency.getId());
					throw new CoreException(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message));
				}
				if (dependentItem.needsDownload()) {
					if (!results.contains(dependentItem)) {
						queue.add(dependentItem);
					}
				}
			}
		}
		return results;
	}

	public File getInstallDirectory() {
		return new File(getDataDirectory(), DIRECTORY_INSTALL);
	}

	public File getInstallDirectory(ContentItem item) {
		return new File(getInstallDirectory(), item.getPath());
	}

	public ContentItem getItem(String id) {
		return itemById.get(id);
	}

	public Collection<ContentItem> getItems() {
		return new HashSet<ContentItem>(itemById.values());
	}

	public Collection<ContentItem> getItemsByKind(String kind) {
		Set<ContentItem> items = itemsByKind.get(kind);
		if (items == null) {
			items = new HashSet<ContentItem>();
		}
		return Collections.unmodifiableCollection(items);
	}

	private Set<String> getRemoteDescriptorLocations() {
		return new HashSet<String>(Arrays.asList(ResourceProvider.getUrls(RESOURCE_CONTENT_DESCRIPTORS)));
	}

	public File getStateFile() {
		return stateFile;
	}

	public void init() {
		itemById.clear();
		itemsByKind.clear();

		MultiStatus result = new MultiStatus(ContentPlugin.PLUGIN_ID, 0, NLS.bind("Reading of content failed", null),
				null);

		File file = getStateFile();
		if (file != null && file.exists()) {
			try {
				read(file);
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.WARNING, ContentPlugin.PLUGIN_ID, NLS.bind(
						"Detected error in ''{0}''", file.getAbsoluteFile()), e));
			}
		}

		file = getDefaultStateFile();
		if (file != null) {
			try {
				read(file);
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.WARNING, ContentPlugin.PLUGIN_ID, NLS.bind(
						"Detected error in ''{0}''", file.getAbsoluteFile()), e));
			}
		}

		if (!result.isOK()) {
			StatusHandler.log(result);
		}

		firePropertyChangeEvent(EVENT_REFRESH);
	}

	private void read(File file) throws CoreException {
		DescriptorMatcher matcher = new DescriptorMatcher(this);
		DescriptorReader reader = new DescriptorReader();
		reader.read(file);
		List<Descriptor> descriptors = reader.getDescriptors();
		for (Descriptor descriptor : descriptors) {
			if (!matcher.match(descriptor)) {
				continue;
			}

			ContentItem item = itemById.get(descriptor.getId());
			if (item == null) {
				item = createItem(descriptor);
				if (item != null) {
					itemById.put(item.getId(), item);
				}
			}
			if (item != null) {
				if (descriptor.isLocal()) {
					item.setLocalDescriptor(descriptor);
				}
				else {
					item.setRemoteDescriptor(descriptor);
				}
			}
		}
	}

	private void readFromUrl(DescriptorReader reader, String location, IProgressMonitor monitor) throws CoreException {
		debug("entering readFromURL: " + location);
		try {
			InputStream in = HttpUtil.stream(new URI(location), monitor);
			try {
				reader.read(in);
			}
			catch (Exception e) {
				String message = NLS.bind("Error downloading {0} - Internet connection might be down", location);
				throw new CoreException(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));

			}
			finally {
				debug("exiting readFromURL: " + location);
				try {
					in.close();
				}
				catch (IOException e) {
					String message = NLS.bind("No route to {0} - Internet connection might be down", location);
					throw new CoreException(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));

				}
			}
		}
		catch (URISyntaxException e) {
			debug(e);
			String message = NLS.bind("I/O error while retrieving data: ", e);
			throw new CoreException(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));
		}
		catch (CoreException e) {
			String message = NLS.bind("Error while retrieving {0}", location);
			throw new CoreException(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));

		}
	}

	public IStatus refresh(IProgressMonitor monitor) {
		File targetFile = getStateFile();
		Assert.isNotNull(targetFile, "stateFile not initialized");
		isRefreshing = true;

		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			progress.beginTask("Refreshing", 200);

			MultiStatus result = new MultiStatus(ContentPlugin.PLUGIN_ID, 0, "Results of template project refresh:",
					null);
			DescriptorReader reader = new DescriptorReader();

			// local descriptors
			File dir = getInstallDirectory();
			File[] children = dir.listFiles();
			if (children != null) {
				SubMonitor loopProgress = progress.newChild(30).setWorkRemaining(children.length);
				for (File childDirectory : children) {
					if (childDirectory.isDirectory()) {
						for (String filename : DESCRIPTOR_FILENAMES) {
							File descriptorFile = new File(childDirectory, filename);
							if (descriptorFile.exists()) {
								try {
									List<Descriptor> localDescriptors = reader.read(descriptorFile);
									for (Descriptor descriptor : localDescriptors) {
										descriptor.setLocal(true);
									}
								}
								catch (CoreException e) {
									String message = NLS.bind("Error while parsing ''{0}''",
											descriptorFile.getAbsolutePath());
									result.add(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));
								}
							}
						}
					}
					loopProgress.worked(1);
				}
			}
			else {
				progress.setWorkRemaining(70);
			}

			for (String descriptorLocation : getRemoteDescriptorLocations()) {
				// remote descriptor
				try {
					if (descriptorLocation != null && descriptorLocation.length() > 0) {
						readFromUrl(reader, descriptorLocation, progress.newChild(70));
					}
				}
				catch (CoreException e) {
					String message = NLS.bind("Error while downloading or parsing ''{0}'':\n\n{1}", descriptorLocation,
							e);
					result.add(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));

				}
			}

			// store on disk
			try {
				reader.write(targetFile);
				init();
			}
			catch (CoreException e) {
				String message = NLS.bind("Failed to store updated descriptors to ''{0}''",
						targetFile.getAbsolutePath());
				result.add(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));
			}

			return result;
		}
		finally {
			isRefreshing = false;
			progress.done();
		}
	}

	public void removeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	public void setDefaultStateFile(File defaultStateFile) {
		this.defaultStateFile = defaultStateFile;
	}

	public void setStateFile(File stateFile) {
		this.stateFile = stateFile;
	}

}
