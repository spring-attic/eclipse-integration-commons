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
package org.springsource.ide.eclipse.commons.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.IIdeUiStartup;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class UiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.commons.ui";

	private static UiPlugin plugin;

	public UiPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		StartupExtensionPointReader.runStartupExtensions();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static UiPlugin getDefault() {
		return plugin;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static IWorkbenchPage getActiveWorkbenchPage() {
		return getActiveWorkbenchWindow().getActivePage();
	}

	static class StartupExtensionPointReader {

		private static final String EXTENSION_ID_STARTUP = "com.springsource.sts.ide.ui.startup";

		private static final String ELEMENT_STARTUP = "startup";

		private static final String ELEMENT_CLASS = "class";

		public static void runStartupExtensions() {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_STARTUP);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().compareTo(ELEMENT_STARTUP) == 0) {
						runStartupExtension(element);
					}
				}
			}
		}

		private static void runStartupExtension(IConfigurationElement configurationElement) {
			try {
				Object object = WorkbenchPlugin.createExtension(configurationElement, ELEMENT_CLASS);
				if (!(object instanceof IIdeUiStartup)) {
					StatusHandler.log(new Status(IStatus.ERROR, UiPlugin.PLUGIN_ID, "Could not load "
							+ object.getClass().getCanonicalName() + " must implement "
							+ IIdeUiStartup.class.getCanonicalName()));
					return;
				}

				IIdeUiStartup startup = (IIdeUiStartup) object;
				startup.lazyStartup();
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, UiPlugin.PLUGIN_ID, "Could not load startup extension", e));
			}

		}

	}

}
