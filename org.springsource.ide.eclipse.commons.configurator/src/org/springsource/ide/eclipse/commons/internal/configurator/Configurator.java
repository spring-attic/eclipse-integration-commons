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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceConfiguratorParticipant;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter.ConfiguratorExtensionPointReader;


/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class Configurator {

	private static final String PARAM_TARGET = "targetDir";

	private static final String PARAM_EXTENSION = "extension";

	private final List<WorkspaceConfiguratorParticipant> participants = new ArrayList<WorkspaceConfiguratorParticipant>();

	public IStatus execute(Map<?, ?> parameters) {
		return execute(parameters, false);
	}

	public IStatus execute(Map<?, ?> parameters, boolean errorIfNoParticipant) {
		participants.clear();

		String targetPath = (String) parameters.get(PARAM_TARGET);
		if (targetPath != null) {
			return execute(targetPath, errorIfNoParticipant);
		}
		else {
			String extensionId = (String) parameters.get(PARAM_EXTENSION);
			if (extensionId != null) {
				for (InstallableItem item : ConfiguratorExtensionPointReader.getInstallableItems()) {
					if (extensionId.equals(item.getId())) {
						ConfiguratorImporter importer = new ConfiguratorImporter();
						File installLocation = importer.getInstallLocation();
						if (installLocation != null) {
							IStatus result = item.install(installLocation, new NullProgressMonitor());
							if (result.getSeverity() != IStatus.ERROR) {
								return execute(item.getTargetLocation(installLocation).getAbsolutePath(),
										errorIfNoParticipant);
							}
						}
						break;
					}
				}
				if (errorIfNoParticipant) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "No install item found to configure request");
				}
				// try again later
				persistRequest("extension=" + extensionId);
				return Status.OK_STATUS;
			}
			else {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Missing parameter 'targetDir' or 'extension'");
			}
		}
	}

	private IStatus execute(String targetPath, boolean errorIfNoParticipant) {
		File file = new File(targetPath);
		if (file.exists() && file.isDirectory()) {
			// Run external contributed configurators
			for (WorkspaceConfiguratorParticipant configurator : ConfiguratorExtensionPointReader.getParticipants()) {
				ConfigurableExtension extension = configurator.createExtension(file, new NullProgressMonitor());
				if (extension != null) {
					participants.add(configurator);
					try {
						extension.configure(new NullProgressMonitor());
					}
					catch (Exception e) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1, "Error occured configuring workspace",
								e);
					}
				}
			}
		}
		else {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Parameter 'targetDir' does not denote a valid path");
		}

		if (participants.isEmpty()) {
			if (errorIfNoParticipant) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "No participant found to configure request");
			}
			else {
				// If install wasn't configured by any participant persist the
				// request for later
				persistRequest(targetPath);
			}
		}

		return Status.OK_STATUS;
	}

	public void executePendingRequests() {
		String configureTargets = Activator.getDefault().getPreferenceStore()
				.getString(Activator.PROPERTY_CONFIGURE_TARGETS);
		List<String> newConfigureTargets = new ArrayList<String>();
		if (StringUtils.hasLength(configureTargets)) {
			StringTokenizer targets = new StringTokenizer(configureTargets, File.pathSeparator);
			while (targets.hasMoreTokens()) {
				String target = targets.nextToken();
				Map<String, String> parameters = new HashMap<String, String>();
				if (target.startsWith("extension=")) {
					parameters.put(PARAM_EXTENSION, target.substring("extension=".length()));
				}
				else {
					parameters.put(PARAM_TARGET, target);
				}
				IStatus status = execute(parameters, true);
				if (!status.isOK()) {
					newConfigureTargets.add(target);
				}
			}
		}
		Activator
				.getDefault()
				.getPreferenceStore()
				.setValue(Activator.PROPERTY_CONFIGURE_TARGETS,
						StringUtils.collectionToDelimitedString(newConfigureTargets, File.pathSeparator));
	}

	private void persistRequest(String target) {
		String configureTargets = Activator.getDefault().getPreferenceStore()
				.getString(Activator.PROPERTY_CONFIGURE_TARGETS);
		if (StringUtils.hasLength(configureTargets)) {
			configureTargets += File.pathSeparatorChar + target;
		}
		else {
			configureTargets = target;
		}
		Activator.getDefault().getPreferenceStore().setValue(Activator.PROPERTY_CONFIGURE_TARGETS, configureTargets);
	}

	public IStatus undo(Map<?, ?> parameters) {
		String targetPath = (String) parameters.get(PARAM_TARGET);

		if (targetPath != null) {
			File file = new File(targetPath);
			if (file.exists() && file.isDirectory()) {
				for (WorkspaceConfiguratorParticipant configurator : participants) {
					ConfigurableExtension extension = configurator.createExtension(file, new NullProgressMonitor());
					if (extension != null) {
						extension.unConfigure(new NullProgressMonitor());
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

}
