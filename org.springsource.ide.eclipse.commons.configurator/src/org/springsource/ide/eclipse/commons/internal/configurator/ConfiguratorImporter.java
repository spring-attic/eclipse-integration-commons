/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.configurator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.osgi.framework.Version;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.IConfigurationContext;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceConfiguratorParticipant;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.IIdeUiStartup;

/**
 * Automatically adds server runtimes and sample projects to the workspace by
 * scanning the local disk.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class ConfiguratorImporter implements IIdeUiStartup, IConfigurationContext, IConfigurator {

	public static class ParticipantExtensionPointReader {

		private static final String ELEMENT_INSTALLABLEITEM = "installableItem";

		private static final String ELEMENT_PARTICIPANT = "participant";

		private static final String EXTENSION_ID_PARTICIPANT = "com.springsource.sts.ide.configurator.participant";

		public static Set<InstallableItem> getInstallableItems() {
			Set<InstallableItem> items = new HashSet<InstallableItem>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_PARTICIPANT);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(ELEMENT_INSTALLABLEITEM)) {
						InstallableItem ext = new InstallableItem(element);
						items.add(ext);
					}
				}
			}
			return items;
		}

		public static Set<WorkspaceConfiguratorParticipant> getParticipants() {
			Set<WorkspaceConfiguratorParticipant> configurators = new HashSet<WorkspaceConfiguratorParticipant>();
			Set<ParticipantDescriptor> extensions = getPartipantExtensions();
			for (ParticipantDescriptor extension : extensions) {
				WorkspaceConfiguratorParticipant configurator = extension.createConfigurator();
				if (configurator != null) {
					configurators.add(configurator);
				}
			}
			return configurators;
		}

		public static Set<ParticipantDescriptor> getPartipantExtensions() {
			Set<ParticipantDescriptor> configurators = new HashSet<ParticipantDescriptor>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_PARTICIPANT);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(ELEMENT_PARTICIPANT)) {
						ParticipantDescriptor ext = new ParticipantDescriptor(element);
						configurators.add(ext);
					}
				}
			}
			return configurators;
		}

	}

	private static final CountDownLatch lazyStartupJobLatch = new CountDownLatch(1);

	private static final String SAMPLES_PATH = "samples";

	public static CountDownLatch getLazyStartupJobLatch() {
		return lazyStartupJobLatch;
	}

	/**
	 * Returns true if <code>name</code> matches the expected constraints expressed
	 * by the other parameters.
	 *
	 * @param name the name of the directory on disk
	 * @param path the expected path prefix
	 * @param versionRange the expected version range
	 * @return
	 */
	public static boolean matches(String name, String path, VersionRange versionRange) {
		if (path == null || !name.startsWith(path)) {
			return false;
		}
		Version version = getVersion(name);
		if (version != null && versionRange != null) {
			return versionRange.isIncluded(version);
		}
		return true;
	}

	private static File getFileFromLocation(Location userLocation) {
		if (userLocation != null) {
			return new File(userLocation.getURL().getFile());
		}
		return null;
	}

	private static Version getVersion(String name) {
		int i = name.lastIndexOf("-");
		if (i != -1) {
			try {
				return new Version(name.substring(i + 1));
			}
			catch (IllegalArgumentException e) {
				// ignore
			}
		}
		return Version.emptyVersion;
	}

	private boolean firstMatchOnly;

	private boolean recurse;

	private List<File> searchLocations;

	private boolean scanInstallPath;

	public ConfiguratorImporter() {
		setRecurse(false);
		setFirstMatchOnly(true);
		setScanInstallPath(true);
	}

	public boolean isScanInstallPath() {
		return scanInstallPath;
	}

	public void setScanInstallPath(boolean scanInstallPath) {
		this.scanInstallPath = scanInstallPath;
	}

	public List<ConfigurableExtension> detectExtensions(final IProgressMonitor monitor) {
		final List<ConfigurableExtension> result = new ArrayList<ConfigurableExtension>();
		Set<WorkspaceConfiguratorParticipant> participants = ParticipantExtensionPointReader.getParticipants();
		for (final WorkspaceConfiguratorParticipant participant : participants) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// logged by super class
				}

				public void run() throws Exception {
					List<ConfigurableExtension> extensions = participant.detectExtensions(ConfiguratorImporter.this,
							monitor);
					result.addAll(extensions);
				}
			});
		}
		Set<InstallableItem> installableItems = ParticipantExtensionPointReader.getInstallableItems();
		for (final InstallableItem item : installableItems) {
			boolean found = false;
			for (ConfigurableExtension extension : result) {
				if (extension.getId().equals(item.getId())) {
					extension.setInstallableItem(item);
					found = true;
				}
			}
			if (!found) {
				for (final WorkspaceConfiguratorParticipant participant : participants) {
					if (participant.getId().equals(item.getConfiguratorId())) {
						final AtomicBoolean added = new AtomicBoolean(false);
						SafeRunner.run(new ISafeRunnable() {
							public void handleException(Throwable exception) {
								// logged by super class
							}

							public void run() throws Exception {
								ConfigurableExtension extension = participant.createExtension(item, monitor);
								if (extension != null) {
									result.add(extension);
									added.set(true);
								}
							}
						});
						if (added.get()) {
							break;
						}
					}
				}
			}
		}

		return result;
	}

	public File getDefaultInstallLocation() {
		List<File> locations = getSearchLocations();
		for (File location : locations) {
			if (location.exists() && location.canWrite()) {
				return location;
			}
		}
		return null;
	}

	public Set<String> getInstalledBundles() {
		final Set<String> installedBundles = new HashSet<String>();
		Set<WorkspaceConfiguratorParticipant> participants = ParticipantExtensionPointReader.getParticipants();
		for (final WorkspaceConfiguratorParticipant configurator : participants) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// logged by super class
				}

				public void run() throws Exception {
					List<ConfigurableExtension> extensions = configurator.detectExtensions(ConfiguratorImporter.this,
							new NullProgressMonitor());
					for (ConfigurableExtension extension : extensions) {
						if (extension.getBundleId() != null) {
							installedBundles.add(extension.getBundleId() + ".feature.group");
						}
					}
				}
			});
		}
		return installedBundles;
	}

	public File getInstallLocation() {
		String path = Activator.getDefault().getPreferenceStore().getString(Activator.PROPERTY_USER_INSTALL_PATH);
		return (path != null) ? new File(path) : null;
	}

	/**
	 * Returns true, if directories are scanned recursively going upwards to the
	 * root of the file-system.
	 */
	public boolean getRecurse() {
		return recurse;
	}

	/**
	 * Returns a list of directories to scan.
	 */
	public List<File> getSearchLocations() {
		if (searchLocations == null) {
			List<File> locations = new ArrayList<File>();

			File file = getSystemLocation();
			if (file != null) {
				locations.add(file);
			}

			file = getFileFromLocation(Platform.getUserLocation());
			if (file != null) {
				locations.add(file);
			}

			String home = System.getProperty("user.home");
			if (home != null) {
				locations.add(new File(home));
			}
			searchLocations = locations;
		}
		return searchLocations;
	}

	public File getSystemLocation() {
		File file = getFileFromLocation(Platform.getInstallLocation());

		if (file != null && file.getParentFile() != null) {

			File systemLocation = file.getParentFile();

			// check new OSX app layout and select the parent folder outside of
			// the .app directory instead of a folder inside of the .app folder
			if (Platform.OS_MACOSX.equals(Platform.getOS())) {
				Pattern pattern = Pattern.compile("(.+)/.*\\.app/Contents");
				Matcher m = pattern.matcher(systemLocation.getAbsolutePath());
				if (m.find()) {
					File auxFile = new File(m.group(1));
					if (auxFile.exists()) {
						systemLocation = auxFile;
					}
				}
			}

			return systemLocation;
		}
		return null;
	}

	public boolean isFirstMatchOnly() {
		return firstMatchOnly;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public boolean isInstalled(String bundleId) {
		return getInstalledBundles().contains(bundleId);
	}

	public void lazyStartup() {
		List<String> commandLineArgs = Arrays.asList(Platform.getCommandLineArgs());
		if (commandLineArgs.contains("-no-autoconfiguration")) {
			return;
		}

		/*
		 * p2 has a bug (ah, more then one actually): it can't make its mind if it wants
		 * sts.ini or STS.ini so on case sensitive file systems we copy it file again.
		 */
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			File upperCaseFile = new File(".", "STS.ini");
			File lowerCaseFile = new File(".", "sts.ini");
			// Check if STS.ini exists. This will fail if we don't run in the
			// STS distribution; also check if sts.ini already exists. This
			// covers the case where STS.ini exists and we run on a
			// case-insensitive file system where sts.ini would also exist
			// already. Otherwise copy file over.
			if (upperCaseFile.exists() && !lowerCaseFile.exists()) {
				try {
					FileUtil.copyFile(upperCaseFile, lowerCaseFile, new NullProgressMonitor());
				}
				catch (CoreException e) {
					StatusHandler.log(
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not copy STS.ini to sts.ini", e));
				}
			}
		}

		// Check if we should run at all
		final boolean configured = Activator.getDefault().getPreferenceStore()
				.getBoolean(Activator.PROPERTY_CONFIGURATOR_PROCESSED);
		final boolean pendingRequests = StringUtils
				.hasLength(Activator.getDefault().getPreferenceStore().getString(Activator.PROPERTY_CONFIGURE_TARGETS));

		if (configured && !pendingRequests) {

			return;
		}

		Job importJob = new Job("Workspace Configuration") {

			@Override
			public IStatus run(IProgressMonitor monitor) {

				// Rerun workspace participants
				if (pendingRequests) {
					Configurator action = new Configurator();
					action.executePendingRequests();
				}

				// Check if import already ran on the workspace
				if (configured) {
					return Status.OK_STATUS;
				}

				// Mark workspace as being processed before running to avoid
				// re-running on failure
				Activator.getDefault().getPreferenceStore().setValue(Activator.PROPERTY_CONFIGURATOR_PROCESSED, true);

				// Save servers view state for later reset
				boolean isShowOnActivity = ServerUIPlugin.getPreferences().getShowOnActivity();

				// Prevent the servers view from showing up
				ServerUIPlugin.getPreferences().setShowOnActivity(false);

				// Import the sample projects
				List<File> samplesPath = scan(SAMPLES_PATH, null);
				if (samplesPath.size() > 0) {
					for (File sample : samplesPath.get(0).listFiles()) {
						createProject(monitor, sample);
					}
				}

				// Run external contributed configurators
				List<ConfigurableExtension> extensions = detectExtensions(monitor);
				for (ConfigurableExtension extension : extensions) {
					// Only configure extensions marked as auto configurable to
					// avoid adding old runtime/server versions
					if (extension.isAutoConfigurable()) {
						extension.configure(monitor);
					}
				}

				// Reset the servers view to original state
				ServerUIPlugin.getPreferences().setShowOnActivity(isShowOnActivity);

				lazyStartupJobLatch.countDown();

				return Status.OK_STATUS;
			}

		};

		importJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		importJob.schedule();
	}

	public List<File> scan(String path, VersionRange versionRange) {
		List<File> matches = new ArrayList<File>();
		List<File> locations = getSearchLocations();
		if (isScanInstallPath()) {
			if (!locations.contains(getInstallLocation())) {
				locations.add(0, getInstallLocation());
			}
		}
		String[] pathSegments = path.split("/");
		outerLoop: for (File location : locations) {
			// Iterate up till dm Server, tc Server or samples dir is found
			IPath p = new Path(location.toString());
			for (int i = 0; i < pathSegments.length - 1; i++) {
				p = p.append(pathSegments[i]);
			}
			File locationContainerFolder = p.toFile();
			if (locationContainerFolder.exists() && locationContainerFolder.isDirectory()) {
				while (locationContainerFolder != null) {
					File[] files = locationContainerFolder.listFiles();
					if (files != null) {
						for (File file : files) {
							if (file.isDirectory()
									&& matches(file.getName(), pathSegments[pathSegments.length - 1], versionRange)) {
								matches.add(file);
							}
						}
					}
					if (!matches.isEmpty() && firstMatchOnly) {
						break outerLoop;
					}
					if (!recurse) {
						break;
					}
					locationContainerFolder = locationContainerFolder.getParentFile();
				}
			}
		}
		if (!matches.isEmpty()) {
			Collections.sort(matches, new Comparator<File>() {
				/**
				 * Sorts high versions first and path names with invalid versions last.
				 */
				public int compare(File o1, File o2) {
					Version v1 = getVersion(o1.getName());
					Version v2 = getVersion(o2.getName());
					if (v1 == null) {
						return (v2 != null) ? 1 : 0;
					}
					else if (v2 == null) {
						return -1;
					}
					return -v1.compareTo(v2);
				}
			});
			// If installFolder is given by a few segments the install location is the top
			// segment.
			// The versions would be attached to the bottom segment hence sorting should be
			// done before "normalization"
			if (pathSegments.length > 1) {
				List<File> normalizedMatches = new ArrayList<>(matches.size());
				for (File match : matches) {
					while (match != null && !match.getName().equals(pathSegments[0])) {
						match = match.getParentFile();
					}
					if (match != null) {
						normalizedMatches.add(match);
					}
				}
				matches = normalizedMatches;
			}
			return matches;
		}
		return Collections.emptyList();
	}

	public void setFirstMatchOnly(boolean firstMatchOnly) {
		this.firstMatchOnly = firstMatchOnly;
	}

	public void setInstallLocation(File location) {
		Activator.getDefault().getPreferenceStore().setValue(Activator.PROPERTY_USER_INSTALL_PATH,
				location.getAbsolutePath());
	}

	/**
	 * @see #getRecurse()
	 */
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	public void setSearchLocations(List<File> searchLocations) {
		this.searchLocations = new ArrayList<File>(searchLocations);
	}

	private void createProject(IProgressMonitor monitor, File sample) {
		if (sample.isDirectory()) {
			try {
				IProjectDescription desc = ResourcesPlugin.getWorkspace()
						.loadProjectDescription(new Path(sample.getAbsolutePath()).append(".project"));
				if (desc != null) {
					String projectName = desc.getName();

					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IProject project = workspace.getRoot().getProject(projectName);

					project.create(desc, new SubProgressMonitor(monitor, 30));
					project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 70));
				}

			}
			catch (CoreException e) {
				StatusHandler
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "An error occurred creating project", e));
			}
		}
	}
}
