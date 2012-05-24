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

import java.io.InputStream;
import java.lang.reflect.Field;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.springsource.sts.ide.internal.configurator.touchpoint.ConfiguratorAction_e_3_6;

/**
 * The activator class controls the plug-in life cycle
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.commons.configurator";

	public static final String LEGACY_ID = "com.springsource.sts.ide.configurator";

	public static final String PROPERTY_CONFIGURATOR_PROCESSED = LEGACY_ID + ".processed";

	public static final String PROPERTY_CONFIGURE_TARGETS = LEGACY_ID + ".configuretargets";

	public static final String PROPERTY_USER_INSTALL_PATH = LEGACY_ID + ".installPath";

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

		// register configure action based on Eclipse version
		try {
			Field field = ExtensionRegistry.class.getDeclaredField("userToken");
			field.setAccessible(true);
			Object token = field.get(Platform.getExtensionRegistry());

			String resource = "plugin-e3.6.xml";
			try {
				// try creating an instance of 3.6 compatible version
				new ConfiguratorAction_e_3_6();
			}
			catch (Throwable t) {
				// fall-back to provisional P2 APIs available in 3.5
				resource = "plugin-e3.5.xml";
			}

			Bundle bundle = getBundle();
			InputStream inputStream = bundle.getEntry(resource).openStream();
			IContributor contributor = ContributorFactoryOSGi.createContributor(bundle);
			configurationActionRegistered = Platform.getExtensionRegistry().addContribution(inputStream, contributor,
					false, bundle.getSymbolicName(), null, token);
			if (!configurationActionRegistered) {
				getLog().log(
						new Status(
								IStatus.ERROR,
								PLUGIN_ID,
								NLS.bind(
										"Registeration of configure action from ''{0}'' failed. This may cause extension install to fail.",
										resource)));
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
