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
package org.springsource.ide.eclipse.commons.internal.configurator.operations;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.springsource.ide.eclipse.commons.internal.configurator.Activator;


/**
 * @author Steffen Pingel
 */
public class ChmodOperation extends AbstractInstallOperation {

	private static final String ATTR_MODE = "mode";

	private static final String ATTR_FILE = "file";

	private final String mode;

	private final String filename;

	public ChmodOperation(IConfigurationElement element) {
		this.mode = element.getAttribute(ATTR_MODE);
		this.filename = element.getAttribute(ATTR_FILE);
	}

	@Override
	public IStatus install(IProgressMonitor monitor) {
		File file = new File(getTargetBase(), filename);
		if (file.exists()) {
			String[] command = { "chmod", mode, file.getAbsolutePath() };
			try {
				Process process = Runtime.getRuntime().exec(command);
				int result = process.waitFor();
				if (result != 0) {
					return new Status(IStatus.WARNING, Activator.PLUGIN_ID, NLS.bind(
							"Execution of chmod command for file ''{0}'' returned unexpected code: {1}",
							file.getAbsolutePath(), result));
				}
			}
			catch (IOException e) {
				return new Status(IStatus.WARNING, Activator.PLUGIN_ID, NLS.bind(
						"Execution of chmod command for file ''{0}'' failed.", file.getAbsolutePath()), e);
			}
			catch (InterruptedException e) {
				return new Status(IStatus.WARNING, Activator.PLUGIN_ID, NLS.bind(
						"Execution of chmod command for file ''{0}'' failed.", file.getAbsolutePath()), e);
			}
		}
		else {
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, NLS.bind(
					"Execution of chmod command for file ''{0}'' failed. File does not exist.", file.getAbsolutePath()));
		}
		return new Status(IStatus.OK, Activator.PLUGIN_ID, NLS.bind(
				"Execution of chmod {0} for file ''{1}'' succeeded.", mode, file.getAbsolutePath()));
	}

	@Override
	public String toString() {
		return "ChmodOperation [mode=" + mode + ", filename=" + filename + "]";
	}

}
