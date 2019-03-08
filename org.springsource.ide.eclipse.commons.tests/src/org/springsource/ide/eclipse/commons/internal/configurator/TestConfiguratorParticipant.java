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
package org.springsource.ide.eclipse.commons.internal.configurator;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.IConfigurationContext;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceConfiguratorParticipant;
import org.springsource.ide.eclipse.commons.internal.configurator.InstallableItem;


/**
 * @author Steffen Pingel
 */
public class TestConfiguratorParticipant extends WorkspaceConfiguratorParticipant {

	private class TestExtension extends ConfigurableExtension {

		public TestExtension() {
			super("com.springsource.sts.ide.tests.runtime");
			setLabel("Test: " + getId());
			setConfigured(true);
		}

		@Override
		public IStatus unConfigure(IProgressMonitor monitor) {
			return Status.OK_STATUS;
		}

		@Override
		public IStatus configure(IProgressMonitor monitor) {
			return Status.OK_STATUS;
		}
	};

	public TestConfiguratorParticipant() {
	}

	@Override
	public List<ConfigurableExtension> detectExtensions(IConfigurationContext context, IProgressMonitor monitor) {
		return Arrays.asList((ConfigurableExtension) new TestExtension());
	}

	@Override
	public ConfigurableExtension createExtension(File location, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public ConfigurableExtension createExtension(InstallableItem item, IProgressMonitor monitor) {
		TestExtension extension = new TestExtension();
		extension.setInstallableItem(item);
		extension.setLabel("Test: " + item.getId());
		extension.setConfigured(false);
		return extension;
	}

}
