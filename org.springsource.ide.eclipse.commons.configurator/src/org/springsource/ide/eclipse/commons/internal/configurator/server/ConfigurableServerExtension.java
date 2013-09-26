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
package org.springsource.ide.eclipse.commons.internal.configurator.server;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.internal.configurator.Activator;


/**
 * @author Steffen Pingel
 */
public class ConfigurableServerExtension extends ConfigurableExtension {

	private static final String ID_SERVERS_VIEW = "org.eclipse.wst.server.ui.ServersView";

	private final ServerDescriptor descriptor;

	private final File runtimeLocation;

	public ConfigurableServerExtension(ServerDescriptor descriptor, File runtimeLocation) {
		this(descriptor, runtimeLocation, runtimeLocation.getName());
	}

	/**
	 * Constructs a new extension to configure a webtools server.
	 * 
	 * @param descriptor the descriptor for the server configuration
	 * @param runtimeLocation the location of the server runtime; null, if the
	 * runtime does not require installation
	 * @param id identifies the runtime
	 */
	public ConfigurableServerExtension(ServerDescriptor descriptor, File runtimeLocation, String id) {
		super(id);
		Assert.isNotNull(descriptor);
		this.descriptor = descriptor;
		this.runtimeLocation = runtimeLocation;
		setLabel(descriptor.getServerName());
		if (runtimeLocation != null) {
			setLocation(runtimeLocation.getAbsolutePath());
		}
		setConfigured(getServer() != null);
	}

	@Override
	public IStatus configure(IProgressMonitor monitor) {
		ServerHandler handler = createServerHandler();
		try {
			handler.createServer(monitor, ServerHandler.ALWAYS_OVERWRITE, descriptor.getCallback());
		}
		catch (CoreException e) {
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The server could not be created.", e);
			StatusHandler.log(status);
			return status;
		}
		return new Status(IStatus.OK, Activator.PLUGIN_ID, NLS.bind(
				"A server with the name {0} has been added to the Servers view.", descriptor.getServerName()));
	}

	@Override
	public String getBundleId() {
		return descriptor.getBundleId();
	}

	public ServerDescriptor getDescriptor() {
		return descriptor;
	}

	public IServer getServer() {
		return createServerHandler().getExistingServer();
	}

	@Override
	public void postConfiguration(IStatus result) {
		if (result.isOK()) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				try {
					window.getActivePage().showView(ID_SERVERS_VIEW);
				}
				catch (PartInitException e) {
					// ignore
				}
			}
		}
	}

	@Override
	public IStatus unConfigure(IProgressMonitor monitor) {
		ServerHandler handler = createServerHandler();
		try {
			handler.deleteServerAndRuntime(monitor);
		}
		catch (CoreException e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, "The server could not be removed.", e);
			StatusHandler.log(status);
			return status;
		}
		return Status.OK_STATUS;
	}

	private ServerHandler createServerHandler() {
		return new ServerHandler(descriptor, runtimeLocation);
	}

}
