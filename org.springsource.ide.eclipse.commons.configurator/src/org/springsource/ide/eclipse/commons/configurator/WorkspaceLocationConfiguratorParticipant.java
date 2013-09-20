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
package org.springsource.ide.eclipse.commons.configurator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter;
import org.springsource.ide.eclipse.commons.internal.configurator.InstallableItem;


/**
 * A configuration participant that corresponds to a location in the file
 * system.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @since 2.2.1
 */
public abstract class WorkspaceLocationConfiguratorParticipant extends WorkspaceConfiguratorParticipant {

	@Override
	public ConfigurableExtension createExtension(File location, IProgressMonitor monitor) {
		if (matches(location)) {
			return doCreateExtension(location, monitor);
		}
		return null;
	}

	@Override
	public List<ConfigurableExtension> detectExtensions(IConfigurationContext context, IProgressMonitor monitor) {
		VersionRange versionRange = getVersionRangeInternal();
		List<ConfigurableExtension> extensions = new ArrayList<ConfigurableExtension>();
		for (String path : getPaths()) {
			if (path != null) {
				List<File> locations = context.scan(path, versionRange);
				for (File location : locations) {
					ConfigurableExtension extension = doCreateExtension(location, monitor);
					if (extension != null) {
						extensions.add(extension);
					}
				}
			}
		}
		if (extensions.size() > 0) {
			// auto configure first extension which has the highest version
			extensions.get(0).setAutoConfigurable(true);
		}
		return extensions;
	}

	public String getPath() {
		return null;
	}

	public String[] getPaths() {
		return new String[] { getPath() };
	}

	public abstract String getVersionRange();

	public boolean matches(File file) {
		for (String path : getPaths()) {
			if (matches(file, path, getVersionRange())) {
				return true;
			}
		}
		return false;
	}

	private VersionRange getVersionRangeInternal() {
		VersionRange versionRange;
		String versionRangeString = getVersionRange();
		if (versionRangeString != null) {
			versionRange = new VersionRange(versionRangeString);
		}
		else {
			versionRange = VersionRange.emptyRange;
		}
		return versionRange;
	}

	protected abstract ConfigurableExtension doCreateExtension(File location, IProgressMonitor monitor);

	protected boolean matches(File location, String path, String versionRange) {
		Assert.isNotNull(location);
		Assert.isNotNull(path);
		return ConfiguratorImporter.matches(location.getName(), path, (versionRange != null ? new VersionRange(
				versionRange) : VersionRange.emptyRange));
	}

	protected Version getVersion(String name) {
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

	@Override
	public ConfigurableExtension createExtension(InstallableItem item, IProgressMonitor monitor) {
		return null;
	}

}
