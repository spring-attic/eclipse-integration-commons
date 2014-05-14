/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui;

import java.net.URL;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;
import org.springsource.ide.eclipse.commons.core.ResourceProvider.Property;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardEditorInputFactory;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardMainPage;
import org.springsource.ide.eclipse.dashboard.ui.actions.ShowDashboardAction;
import org.springsource.ide.eclipse.commons.frameworks.core.util.Gtk3Check;

/**
 * Note: Bundle activation is triggered by Mylyn's tasks ui startup due to
 * implemented task editor factory extensions.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Miles Parker
 */
public class IdeUiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.dashboard.ui";

	private static IdeUiPlugin plugin;

	public static final Version JAVAFX_MINIMUM_ECLIPSE_VERSION = new Version("4.3");
	
	public static final Version JAVAFX_MINIMUM_JRE_VERSION = new Version("1.7");
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		migrateBlogFeeds();
		
		// avoid cyclic startup dependency on org.eclipse.mylyn.tasks.ui
		Job startupJob = new UIJob("Spring Tool Suite Initialization") {
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
		store.setDefault(IIdeUiConstants.PREF_USE_OLD_DASHOARD, IIdeUiConstants.DEFAULT_PREF_USE_OLD_DASHOARD);
		store.setDefault(IIdeUiConstants.PREF_IO_BLOGFEED_MIGRATION, false);
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
	
	public boolean supportsNewDashboard(IProgressMonitor mon) {
		Version eclipseVersion = new Version(Platform.getBundle("org.eclipse.platform").getHeaders().get("Bundle-Version"));
		boolean eclipseCompatible = eclipseVersion.compareTo(JAVAFX_MINIMUM_ECLIPSE_VERSION) >= 0;
		String javaVersionString = System.getProperty("java.version");
		String[] majorMinorQualifier = StringUtils.split(javaVersionString, ".");
		Version jreVersion = new Version(Integer.parseInt(majorMinorQualifier[0]), Integer.parseInt(majorMinorQualifier[1]), 0);
		boolean jreCompatible = jreVersion.compareTo(JAVAFX_MINIMUM_JRE_VERSION) >= 0;
		return eclipseCompatible && jreCompatible;
	}

	public boolean useNewDashboard(IProgressMonitor mon) {
		return supportsNewDashboard(mon) && !IdeUiPlugin.getDefault().getPreferenceStore().getBoolean(IIdeUiConstants.PREF_USE_OLD_DASHOARD)
				&& !Gtk3Check.isGTK3;
	}
	
	private void migrateBlogFeeds() {
		IPreferenceStore prefStore = getPreferenceStore();
		if (!prefStore.getBoolean(IIdeUiConstants.PREF_IO_BLOGFEED_MIGRATION)) {
			ResourceProvider provider = ResourceProvider.getInstance();
			Property feedsProp = provider.getProperty(DashboardMainPage.RESOURCE_DASHBOARD_FEEDS_BLOGS);
			if (feedsProp != null) {
				String value = feedsProp.getValue();
				if (value.contains("http://www.springframework.org/node/feed/")) {
					value = value.replace("http://www.springframework.org/node/feed/", " ");
				}
				if (value.contains("http://blog.springsource.com/main/feed/")) {
					value = value.replace("http://blog.springsource.com/main/feed/", " ");
				}
				if (!value.contains("https://spring.io/blog.atom")) {
					value = value.concat("\nhttps://spring.io/blog.atom");
				}
				value = value.trim();
				feedsProp.setValue(value);
			}
			prefStore.setValue(IIdeUiConstants.PREF_IO_BLOGFEED_MIGRATION, true);
		}
	}

}
