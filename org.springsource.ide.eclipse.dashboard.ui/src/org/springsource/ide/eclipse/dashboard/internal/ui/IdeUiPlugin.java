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
package org.springsource.ide.eclipse.dashboard.internal.ui;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardEditorInputFactory;
import org.springsource.ide.eclipse.dashboard.ui.IConfigurator;
import org.springsource.ide.eclipse.dashboard.ui.IIdeUiStartup;
import org.springsource.ide.eclipse.dashboard.ui.actions.ShowDashboardAction;


/**
 * Note: Bundle activation is triggered by Mylyn's tasks ui startup due to
 * implemented task editor factory extensions.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Wesley Coelho
 * @author Leo Dos Santos
 */
public class IdeUiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.dashboard.ui";

	private static IdeUiPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		StartupExtensionPointReader.runStartupExtensions();

		// avoid cyclic startup dependency on org.eclipse.mylyn.tasks.ui
		Job startupJob = new UIJob("SpringSource Tool Suite Initialization") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if (window != null) {
							// prevent loading if already opened by workspace
							// restore
							IEditorReference[] references = window.getActivePage().getEditorReferences();
							for (IEditorReference reference : references) {
								if (DashboardEditorInputFactory.FACTORY_ID.equals(reference.getFactoryId())) {
									return;
								}
							}

							if (getPreferenceStore().getBoolean(IIdeUiConstants.PREF_OPEN_DASHBOARD_STARTUP)) {
								// don't show if welcome page is visible
								if (window.getWorkbench().getIntroManager().getIntro() != null) {
									// scheduleUpdateJob();
									return;
								}

								ShowDashboardAction showDashboard = new ShowDashboardAction();
								showDashboard.init(window);
								showDashboard.run(null);
								return;
							}
						}
						// scheduleUpdateJob();
					}
				});
				return Status.OK_STATUS;
			}
		};
		startupJob.setSystem(true);
		startupJob.schedule();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(IIdeUiConstants.PREF_OPEN_DASHBOARD_STARTUP, IIdeUiConstants.DEFAULT_OPEN_DASHBOARD_STARTUP);
	}

	public static IConfigurator getConfigurator() {
		return ConfiguratorExtensionPointReader.getConfigurator();
	}

	public static IdeUiPlugin getDefault() {
		return plugin;
	}

	public static Image getImage(String path) {
		ImageRegistry imageRegistry = getDefault().getImageRegistry();
		Image image = imageRegistry.get(path);
		if (image == null) {
			// Add support for loading and creating images from remote URLs
			if (path.startsWith("http")) {
				try {
					ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(new URL(path));
					if (imageDescriptor == null) {
						imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
					}
					image = imageDescriptor.createImage(true);
					imageRegistry.put(path, image);
				}
				catch (Exception e) {
					// make sure to ignore all here
					ImageDescriptor imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
					image = imageDescriptor.createImage(true);
					imageRegistry.put(path, image);
				}
			}
			else {
				ImageDescriptor imageDescriptor = getImageDescriptor(path);
				if (imageDescriptor == null) {
					imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
				}
				image = imageDescriptor.createImage(true);
				imageRegistry.put(path, image);
			}
		}
		return image;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, "icons/" + path);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(IStatus status, boolean informUser) {
		getDefault().getLog().log(status);
		if (informUser) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", status.getMessage());
		}
	}

	public static void log(Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Unexpected exception", e));
	}

	static class ConfiguratorExtensionPointReader {

		private static final String EXTENSION_ID_CONFIGURATOR = "com.springsource.sts.ide.ui.configurator";

		private static final String ELEMENT_CONFIGURATOR = "configurator";

		private static final String ELEMENT_CLASS = "class";

		public static IConfigurator getConfigurator() {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_CONFIGURATOR);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().compareTo(ELEMENT_CONFIGURATOR) == 0) {
						IConfigurator configurator = readConfiguratorExtension(element);
						if (configurator != null) {
							return configurator;
						}
					}
				}
			}
			return null;
		}

		private static IConfigurator readConfiguratorExtension(IConfigurationElement configurationElement) {
			try {
				Object object = WorkbenchPlugin.createExtension(configurationElement, ELEMENT_CLASS);
				if (!(object instanceof IConfigurator)) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not load "
							+ object.getClass().getCanonicalName() + " must implement "
							+ IConfigurator.class.getCanonicalName()));
				}
				return (IConfigurator) object;
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
						"Could not load configurator extension", e));
			}
			return null;
		}

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
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not load "
							+ object.getClass().getCanonicalName() + " must implement "
							+ IIdeUiStartup.class.getCanonicalName()));
					return;
				}

				IIdeUiStartup startup = (IIdeUiStartup) object;
				startup.lazyStartup();
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not load startup extension",
						e));
			}

		}

	}

}
