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
package org.springsource.ide.eclipse.commons.internal.configurator.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.IConfigurationContext;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceConfiguratorParticipant;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter;
import org.springsource.ide.eclipse.commons.internal.configurator.InstallableItem;


/**
 * @author Steffen Pingel
 */
public class ServerWorkspaceConfigurator extends WorkspaceConfiguratorParticipant {

	private final ServerConfigurator configurator;

	public ServerWorkspaceConfigurator(ServerConfigurator configurator) {
		this.configurator = configurator;
	}

	public ServerWorkspaceConfigurator() {
		this(new ServerConfigurator());
	}

	@Override
	public ConfigurableExtension createExtension(File location, IProgressMonitor monitor) {
		ServerDescriptor descriptor = getServerDescriptor(location);
		if (descriptor != null) {
			return new ConfigurableServerExtension(descriptor, location);
		}
		return null;
	}

	@Override
	public ConfigurableExtension createExtension(InstallableItem item, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public List<ConfigurableExtension> detectExtensions(IConfigurationContext context, IProgressMonitor monitor) {
		List<ConfigurableExtension> extensions = new ArrayList<ConfigurableExtension>();
		for (ServerDescriptor descriptor : configurator.getDescriptors()) {
			if (descriptor.getInstallPath() != null) {
				List<File> locations = context.scan(descriptor.getInstallPath(),
						new VersionRange(descriptor.getVersionRange()));
				boolean autoConfigurable = descriptor.isAutoConfigurable();
				for (File location : locations) {
					ConfigurableServerExtension extension = new ConfigurableServerExtension(descriptor, location);
					extensions.add(extension);

					// auto configure first extension which has the highest
					// version
					extension.setAutoConfigurable(autoConfigurable);
					autoConfigurable = false;
				}
			}
			else {
				ConfigurableServerExtension extension = new ConfigurableServerExtension(descriptor, null,
						descriptor.getRuntimeTypeId());
				extension.setAutoConfigurable(descriptor.isAutoConfigurable());
				extensions.add(extension);
			}
		}
		return extensions;
	}

	public ServerDescriptor getServerDescriptor(File location) {
		for (ServerDescriptor descriptor : configurator.getDescriptors()) {
			if (descriptor.getInstallPath() != null
					&& matches(location, descriptor.getInstallPath(), descriptor.getVersionRange())) {
				return descriptor;
			}
		}
		return null;
	}

	protected boolean matches(File location, String path, String versionRange) {
		Assert.isNotNull(location);
		Assert.isNotNull(path);
		return ConfiguratorImporter.matches(location.getName(), path, (versionRange != null ? new VersionRange(
				versionRange) : VersionRange.emptyRange));
	}

}
