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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jst.server.tomcat.core.internal.TomcatConfiguration;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServer;
import org.eclipse.jst.server.tomcat.core.internal.WebModule;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.Context;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.ServerInstance;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.InstallableRuntime2;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.internal.wizard.fragment.LicenseWizardFragment;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;
import org.springsource.ide.eclipse.commons.configurator.ServerHandlerCallback;
import org.springsource.ide.eclipse.commons.core.HttpUtil;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.internal.configurator.Activator;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter;
import org.springsource.ide.eclipse.commons.ui.UiUtil;


/**
 * Automatically adds server runtimes and sample projects to the workspace by
 * scanning the local disk.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class ServerConfigurator {

	public static class ServerDescriptorExtensionPointReader {

		private static final String ELEMENT_RUNTIME = "runtime";

		private static final String EXTENSION_ID = "com.springsource.sts.ide.configurator.serverConfigurations";

		public static Set<ServerDescriptor> getDescriptors() {
			Set<ServerDescriptor> items = new HashSet<ServerDescriptor>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(ELEMENT_RUNTIME)) {
						ServerDescriptor item = new ServerDescriptor(element);
						if (item.isValid()) {
							items.add(item);
						}
					}
				}
			}
			return items;
		}
	}

	private class ServerDescriptorInstaller extends InstallableRuntime2 {

		private final ServerDescriptor descriptor;

		public ServerDescriptorInstaller(ServerDescriptor descriptor) {
			super(null);
			this.descriptor = descriptor;
		}

		@Override
		public String getArchivePath() {
			return descriptor.getArchivePath();
		}

		@Override
		public String getArchiveUrl() {
			return descriptor.getArchiveUrl();
		}

		@Override
		public String getId() {
			return descriptor.getRuntimeTypeId();
		}

		@Override
		public String getLicenseURL() {
			return descriptor.getLicenseUrl();
		}

		@Override
		public String getName() {
			return descriptor.getRuntimeName();
		}
	}

	// /** Boolean property that determines if the ASF layout should be used. */
	// public static final String KEY_ASF_LAYOUT =
	// "com.springsource.tcserver.asf";
	//
	// /**
	// * String property for the name of the server instance for the
	// SpringSource
	// * layout.
	// */
	// public static final String KEY_SERVER_NAME =
	// "com.springsource.tcserver.name";

	public static final String RESOURCE_DOWNLOAD_DM_SERVER_1 = "download.dm.server.1";

	public static final String RESOURCE_DOWNLOAD_DM_SERVER_2 = "download.dm.server.2";

	public static final String RESOURCE_DOWNLOAD_TOMCAT_6 = "download.tomcat.6";

	public static String ID_DM_SERVER_1 = "com.springsource.sts.ide.configurator.server.DmServer1";

	public static String ID_DM_SERVER_2 = "com.springsource.sts.ide.configurator.server.DmServer2";

	public static String ID_CF_SERVER = "com.springsource.sts.ide.configurator.server.CfServer";

	public static String ID_TOMCAT = "com.springsource.sts.ide.configurator.server.Tomcat";

	public List<ServerDescriptor> descriptors;

	private ConfiguratorImporter configurator;

	public ServerConfigurator() {
		descriptors = new ArrayList<ServerDescriptor>();

		ServerDescriptor dmServer1 = new ServerDescriptor(ID_DM_SERVER_1);
		// dmServer1.setArchiveUrl(ResourceProvider.getUrl(RESOURCE_DOWNLOAD_DM_SERVER_1));
		dmServer1.setArchivePath("springsource-dm-server-1.0.2.SR02");
		dmServer1.setRuntimeTypeId("com.springsource.server.runtime.10");
		dmServer1.setServerTypeId("com.springsource.server.10");
		dmServer1.setRuntimeName("SpringSource dm Server (Runtime) v1.0");
		dmServer1.setServerName("SpringSource dm Server v1.0");
		dmServer1.setName("SpringSource dm Server");
		dmServer1
				.setDescription("SpringSource dm Server is an open source, completely modular, OSGi-based Java server designed to run enterprise Java applications and Spring-powered application.");
		dmServer1.setInstallPath("dm-server-1");
		dmServer1.setVersionRange("[1.0.0,2.0.0)");
		dmServer1.setBundleId("com.springsource.server.dm.bundle-v1");
		addDescriptor(dmServer1);

		ServerDescriptor dmServer2 = new ServerDescriptor(ID_DM_SERVER_2);
		// dmServer2.setArchiveUrl(ResourceProvider.getUrl(RESOURCE_DOWNLOAD_DM_SERVER_2));
		dmServer2.setArchivePath("springsource-dm-server-2.0.0.RELEASE");
		dmServer2.setRuntimeTypeId("com.springsource.server.runtime.20");
		dmServer2.setServerTypeId("com.springsource.server.20");
		dmServer2.setRuntimeName("SpringSource dm Server (Runtime) v2.0");
		dmServer2.setServerName("SpringSource dm Server v2.0");
		dmServer2.setName("SpringSource dm Server");
		dmServer2
				.setDescription("SpringSource dm Server is an open source, completely modular, OSGi-based Java server designed to run enterprise Java applications and Spring-powered application.");
		dmServer2.setInstallPath("dm-server-2");
		dmServer2.setVersionRange("[2.0.0,3.0.0)");
		dmServer2.setBundleId("com.springsource.server.dm.bundle-v2");
		addDescriptor(dmServer2);

		// ServerDescriptor tcServer2 = new ServerDescriptor(ID_TC_SERVER_2_0);
		// //
		// tcServer2.setDownloadUrl("http://dist.springsource.com/release/DMS/springsource-dm-server-1.0.2.RELEASE.zip");
		// tcServer2.setArchivePath("tcServer-6.0");
		// tcServer2.setRuntimeTypeId("com.springsource.tcserver.runtime.60");
		// tcServer2.setServerTypeId("com.springsource.tcserver.60");
		// tcServer2.setRuntimeName("SpringSource tc Server Developer Edition (Runtime) v2.0");
		// tcServer2.setServerName("SpringSource tc Server Developer Edition v2.0");
		// tcServer2.setName("SpringSource tc Server Developer Edition");
		// tcServer2
		// .setDescription("SpringSource tc Server is an enterprise version of Apache Tomcat that provides developers with the lightweight server they want paired with the operational management, advanced diagnostics, and mission-critical support capabilities businesses need.");
		// tcServer2.setVersionRange("[2.0.0,2.1.0)");
		// tcServer2.setInstallPath("tc-server-developer");
		// tcServer2.setBundleId("com.springsource.server.tc.bundle-v2");
		// // configure non-ASL layout to enable Spring Insight
		// tcServer2.setCallback(new ServerHandlerCallback() {
		// @Override
		// public void configureServer(IServerWorkingCopy server) throws
		// CoreException {
		// // TODO e3.6 remove casts for setAttribute()
		// ((ServerWorkingCopy)
		// server).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String)
		// null);
		// ((ServerWorkingCopy)
		// server).setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);
		// ((ServerWorkingCopy) server).setAttribute(KEY_ASF_LAYOUT, false);
		// ((ServerWorkingCopy) server).setAttribute(KEY_SERVER_NAME,
		// "spring-insight-instance");
		// ((ServerWorkingCopy)
		// server).importRuntimeConfiguration(server.getRuntime(), null);
		// }
		// });
		// addDescriptor(tcServer2);

		// ServerDescriptor tcServer21 = new ServerDescriptor(ID_TC_SERVER_2_1);
		// //
		// tcServer2.setDownloadUrl("http://dist.springsource.com/release/DMS/springsource-dm-server-1.0.2.RELEASE.zip");
		// tcServer21.setArchivePath("springsource-tc-server-developer");
		// tcServer21.setRuntimeTypeId("com.springsource.tcserver.runtime.70");
		// tcServer21.setServerTypeId("com.springsource.tcserver.70");
		// tcServer21.setRuntimeName("SpringSource tc Server Developer Edition (Runtime) v2.1");
		// tcServer21.setServerName("SpringSource tc Server Developer Edition v2.1");
		// tcServer21.setName("SpringSource tc Server Developer Edition");
		// tcServer21
		// .setDescription("SpringSource tc Server is an enterprise version of Apache Tomcat that provides developers with the lightweight server they want paired with the operational management, advanced diagnostics, and mission-critical support capabilities businesses need.");
		// tcServer21.setVersionRange("[2.1.0,3.0.0)");
		// tcServer21.setInstallPath("tc-server-developer");
		// tcServer21.setBundleId("com.springsource.server.tc.bundle-v21");
		// // configure non-ASL layout to enable Spring Insight
		// tcServer21.setCallback(new ServerHandlerCallback() {
		// public void configureServer(IServerWorkingCopy server) throws
		// CoreException {
		//
		// // Create Spring Insight instance in case it is missing
		// IPath installLocation = server.getRuntime().getLocation();
		// if
		// (!installLocation.append("spring-insight-instance").toFile().exists())
		// {
		// try {
		// ServerInstanceCommand command = new
		// ServerInstanceCommand(installLocation.toFile());
		// command.execute("create", "spring-insight-instance", "-i "
		// + installLocation.toFile().getCanonicalPath(), "-t", "insight",
		// "--force");
		// }
		// catch (Exception e) {
		// Activator
		// .getDefault()
		// .getLog()
		// .log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
		// "Error creating spring-insight-instance", e));
		// }
		// }
		//
		// // TODO e3.6 remove casts for setAttribute()
		// ((ServerWorkingCopy)
		// server).setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String)
		// null);
		// ((ServerWorkingCopy)
		// server).setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);
		// ((ServerWorkingCopy) server).setAttribute(KEY_ASF_LAYOUT, false);
		// ((ServerWorkingCopy) server).setAttribute(KEY_SERVER_NAME,
		// "spring-insight-instance");
		// ((ServerWorkingCopy)
		// server).importRuntimeConfiguration(server.getRuntime(), null);
		// }
		// });
		// addDescriptor(tcServer21);

		// ServerDescriptor tcServer = new ServerDescriptor(ID_TC_SERVER_6);
		// //
		// tcServer.setDownloadUrl("http://dist.springsource.com/release/DMS/springsource-dm-server-1.0.2.RELEASE.zip");
		// tcServer.setArchivePath("tcServer-6.0");
		// tcServer.setRuntimeTypeId("com.springsource.tcserver.runtime.60");
		// tcServer.setServerTypeId("com.springsource.tcserver.60");
		// tcServer.setRuntimeName("SpringSource tc Server (Runtime) v6.0");
		// tcServer.setServerName("SpringSource tc Server v6.0");
		// tcServer.setName("SpringSource tc Server");
		// tcServer.setDescription("SpringSource tc Server is an enterprise version of Apache Tomcat that provides developers with the lightweight server they want paired with the operational management, advanced diagnostics, and mission-critical support capabilities businesses need.");
		// tcServer.setVersionRange("[6.0.0,7.0.0)");
		// tcServer.setInstallPath("tc-server");
		// tcServer.setBundleId("com.springsource.server.tc.bundle");
		// addDescriptor(tcServer);

		ServerDescriptor cfServer = new ServerDescriptor(ID_CF_SERVER);
		cfServer.setRuntimeTypeId("com.springsource.cloudfoundryserver.runtime.10");
		cfServer.setServerTypeId("com.springsource.cloudfoundryserver.10");
		cfServer.setRuntimeName("SpringSource Cloud Foundry (Runtime) v1.0");
		cfServer.setServerName("SpringSource Cloud Foundry v1.0");
		cfServer.setName("SpringSource Cloud Foundry Server");
		cfServer.setDescription("Publishes and runs J2EE Web projects to Cloud Foundry.");
		cfServer.setForceCreateRuntime(true);
		cfServer.setAutoConfigurable(false);
		addDescriptor(cfServer);

		ServerDescriptor tomcatServer = new ServerDescriptor(ID_TOMCAT);
		// tomcatServer.setArchiveUrl(ResourceProvider.getUrl(RESOURCE_DOWNLOAD_TOMCAT_6));
		// tomcatServer.setArchivePath("apache-tomcat-6.0.26");
		tomcatServer.setRuntimeTypeId("org.eclipse.jst.server.tomcat.runtime.60");
		tomcatServer.setServerTypeId("org.eclipse.jst.server.tomcat.60");
		tomcatServer.setRuntimeName("Apache Tomcat (Runtime) v6.0");
		tomcatServer.setServerName("Apache Tomcat v6.0");
		tomcatServer.setName("Apache Tomcat");
		tomcatServer.setDescription("Apache Tomcat supports J2EE 1.2, 1.3, 1.4, and Java EE 5.");
		tomcatServer.setVersionRange("[6.0.0,7.0.0)");
		tomcatServer.setInstallPath("apache-tomcat-6.0");
		tomcatServer.setBundleId("org.apache.tomcat.bundle");
		// configure manager application
		tomcatServer.setCallback(new ServerHandlerCallback() {
			@Override
			public void configureServer(IServerWorkingCopy server) {
				try {
					TomcatServer ts = (TomcatServer) server.loadAdapter(TomcatServer.class, null);
					TomcatConfiguration configuration = ts.getTomcatConfiguration();
					String docBase = server.getRuntime().getLocation().append("webapps").append("manager").toOSString();
					WebModule managerModule = new WebModule("/manager", docBase, null, false);
					configuration.addWebModule(-1, managerModule);

					Field field = configuration.getClass().getDeclaredField("serverInstance");
					field.setAccessible(true);
					ServerInstance serverInstance = (ServerInstance) field.get(configuration);
					Context context = serverInstance.getContext("/manager");
					if (context != null) {
						context.setAttributeValue("privileged", "true");
					}
				}
				catch (LinkageError e) {
					// ignore
				}
				catch (Exception e) {
					// ignore
				}
			}
		});
		addDescriptor(tomcatServer);

		Set<ServerDescriptor> items = ServerDescriptorExtensionPointReader.getDescriptors();
		for (ServerDescriptor item : items) {
			addDescriptor(item);
		}
	}

	private void addDescriptor(ServerDescriptor descriptor) {
		if (ServerCore.findRuntimeType(descriptor.getRuntimeTypeId()) == null) {
			// runtime support is not installed
			return;
		}

		descriptors.add(descriptor);
	}

	public ServerDescriptor getDescriptor(String id) {
		for (ServerDescriptor descriptor : descriptors) {
			if (descriptor.getId().equals(id)) {
				return descriptor;
			}
		}
		return null;
	}

	public ServerDescriptor getDescriptorByBundleId(String id) {
		Assert.isNotNull(id);
		for (ServerDescriptor descriptor : descriptors) {
			if (id.equals(descriptor.getBundleId())) {
				return descriptor;
			}
		}
		return null;
	}

	public List<ServerDescriptor> getDescriptors() {
		return Collections.unmodifiableList(descriptors);
	}

	public File getLocation(ServerDescriptor descriptor) {
		List<File> locations = getConfigurator().scan(descriptor.getInstallPath(),
				new VersionRange(descriptor.getVersionRange()));
		return (locations.size() > 0) ? locations.iterator().next() : null;
	}

	public Set<String> getInstalledBundles() {
		Set<String> installedBundles = new HashSet<String>();
		for (ServerDescriptor descriptor : descriptors) {
			if (descriptor.getBundleId() != null && getLocation(descriptor) != null) {
				installedBundles.add(descriptor.getBundleId());
			}
		}
		return installedBundles;
	}

	public ServerHandler installServer(final ServerDescriptor descriptor, File installLocation, IOverwriteQuery query,
			IProgressMonitor monitor) throws CoreException {
		try {
			SubMonitor progress = SubMonitor.convert(monitor);
			progress.beginTask(NLS.bind("Installing Runtime {0}", descriptor.getRuntimeName()), 100);

			File serverLocation = getLocation(descriptor);
			if (serverLocation == null) {
				final boolean[] response = new boolean[1];
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						response[0] = MessageDialog.openQuestion(
								UiUtil.getShell(),
								"Install Runtime",
								NLS.bind("No local installation of {0} found. Proceed with download?",
										descriptor.getServerName()));
					}
				});
				if (!response[0]) {
					throw new OperationCanceledException();
				}

				InstallableRuntime2 ir = new ServerDescriptorInstaller(descriptor);

				// prompt license if necessary
				if (ir.getLicenseURL() != null) {
					progress.subTask("Downloading license");
					try {
						final boolean[] result = new boolean[1];
						final String license = ir.getLicense(progress.newChild(20));
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								TaskModel taskModel = new TaskModel();
								taskModel.putObject(LicenseWizardFragment.LICENSE, license);
								TaskWizard wizard2 = new TaskWizard("License", new WizardFragment() {
									@SuppressWarnings({ "unchecked", "rawtypes" })
									@Override
									protected void createChildFragments(List list) {
										list.add(new LicenseWizardFragment());
									}
								}, taskModel);

								WizardDialog dialog2 = new WizardDialog(UiUtil.getShell(), wizard2);
								result[0] = (dialog2.open() == Window.OK);
							}

						});
						if (!result[0]) {
							// user did not agree to license
							throw new OperationCanceledException();
						}
					}
					catch (CoreException e) {
						StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error getting license", e));
					}
				}

				progress.setWorkRemaining(80);

				// schedule download job
				serverLocation = new File(installLocation, descriptor.getInstallPath());
				monitor.subTask(NLS.bind("Downloading runtime to {0}", serverLocation.getAbsolutePath()));
				File archiveFile = File.createTempFile("runtime", null);
				archiveFile.deleteOnExit();
				HttpUtil.download(descriptor.getArchiveUrl(), archiveFile, serverLocation, descriptor.getArchivePath(),
						progress.newChild(70));
				// Path path = new Path(location.getAbsolutePath());
				// ir.install(path, new SubProgressMonitor(monitor, 70));
			}

			// create wtp runtime
			progress.setWorkRemaining(10);
			monitor.subTask(NLS.bind("Creating server {0}", descriptor.getServerName()));
			ServerHandler serverHandler = new ServerHandler(descriptor, serverLocation);
			serverHandler.createServer(progress.newChild(10), query, descriptor.getCallback());
			return serverHandler;
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Installing runtime failed", e));
		}
		finally {
			monitor.done();
		}
	}

	public ConfiguratorImporter getConfigurator() {
		if (configurator == null) {
			configurator = new ConfiguratorImporter();
		}
		return configurator;
	}

	public ServerHandler installServer(ServerDescriptor descriptor, IOverwriteQuery query, IProgressMonitor monitor)
			throws CoreException {
		File installLocation = getConfigurator().getInstallLocation();
		if (installLocation == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Unable to determine writeable location for installing runtime."));
		}
		return installServer(descriptor, installLocation, query, monitor);
	}

	public boolean isInstalled(String bundleId) {
		ServerDescriptor descriptor = getDescriptorByBundleId(bundleId);
		if (descriptor != null) {
			return getLocation(descriptor) != null;
		}
		return false;
	}

	public void setDescriptors(List<ServerDescriptor> descriptors) {
		this.descriptors = new ArrayList<ServerDescriptor>(descriptors);
	}

}
