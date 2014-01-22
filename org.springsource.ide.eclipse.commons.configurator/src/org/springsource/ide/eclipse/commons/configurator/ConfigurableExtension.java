/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.configurator;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.springsource.ide.eclipse.commons.internal.configurator.InstallableItem;


/**
 * @author Steffen Pingel
 */
public abstract class ConfigurableExtension {

	private boolean autoConfigurable;

	private boolean configured;

	private final String id;

	private InstallableItem installableItem;

	private String label;

	private String location;

	public ConfigurableExtension(String id) {
		Assert.isNotNull(id);
		this.id = id;
	}

	public abstract IStatus configure(IProgressMonitor monitor);

	public String getBundleId() {
		return null;
	}

	public String getId() {
		return id;
	}

	public InstallableItem getInstallableItem() {
		return installableItem;
	}

	public String getLabel() {
		return label;
	}

	public String getLocation() {
		return location;
	}

	public IStatus install(File targetDir, IProgressMonitor monitor) {
		return getInstallableItem().install(targetDir, monitor);
	}

	public boolean isAutoConfigurable() {
		return autoConfigurable;
	}

	public boolean isConfigured() {
		return configured;
	}

	public boolean isInstallable() {
		return installableItem != null;
	}

	/**
	 * Invoked on UI thread after manual configuration. Does nothing by default.
	 * Subclasses my override.
	 * 
	 * @param result status returned by {@link #configure(IProgressMonitor)}
	 */
	public void postConfiguration(IStatus result) {
	}

	public void setAutoConfigurable(boolean autoConfigurable) {
		this.autoConfigurable = autoConfigurable;
	}

	public void setConfigured(boolean configured) {
		this.configured = configured;
	}

	public void setInstallableItem(InstallableItem installableItem) {
		this.installableItem = installableItem;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "ConfigurableExtension [id=" + id + ", autoConfigurable=" + autoConfigurable + ", configured="
				+ configured + ", location=" + location + "]";
	}

	public abstract IStatus unConfigure(IProgressMonitor monitor);

}
