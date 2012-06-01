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
package org.springsource.ide.eclipse.commons.internal.configurator;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.commons.configurator";

	public static final String PROPERTY_CONFIGURATOR_PROCESSED = PLUGIN_ID + ".processed";

	public static final String PROPERTY_CONFIGURE_TARGETS = PLUGIN_ID + ".configuretargets";

	public static final String PROPERTY_USER_INSTALL_PATH = PLUGIN_ID + ".installPath";

	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	private boolean configurationActionRegistered;

	public boolean isConfigurationActionRegistered() {
		return configurationActionRegistered;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		new DefaultScope().getNode(PLUGIN_ID).putBoolean(PROPERTY_CONFIGURATOR_PROCESSED, false);

		try {
			IExtension extension = Platform.getExtensionRegistry().getExtension(
					"org.eclipse.equinox.p2.engine.actions", "org.springsource.ide.eclipse.commons.configure");
			configurationActionRegistered = extension != null;
			if (!configurationActionRegistered) {
				getLog().log(
						new Status(IStatus.ERROR, PLUGIN_ID,
								"Registeration of configure action failed. This may cause extension install to fail."));
			}
		}
		catch (Throwable t) {
			getLog().log(
					new Status(IStatus.ERROR, PLUGIN_ID,
							"Registeration of configure action failed. This may cause extension install to fail.", t));
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
