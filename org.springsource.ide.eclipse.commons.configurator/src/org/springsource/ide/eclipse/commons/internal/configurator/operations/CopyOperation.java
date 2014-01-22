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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.springsource.ide.eclipse.commons.internal.configurator.Activator;


/**
 * @author Steffen Pingel
 */
public class CopyOperation extends AbstractInstallOperation {

	private static final String ATTR_SOURCE = "source";

	private static final String ATTR_TARGET = "target";

	/**
	 * Copy file from src (path to the original file) to dest (path to the
	 * destination file).
	 */
	private static void copy(File src, File dest) throws IOException {
		InputStream in = new FileInputStream(src);
		try {
			OutputStream out = new FileOutputStream(dest);
			try {
				byte[] buf = new byte[8 * 1024];
				int len;
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
			}
			finally {
				out.close();
			}
		}
		finally {
			in.close();
		}
	}

	/**
	 * Copy the given source directory (and all its contents) to the given
	 * target directory.
	 */
	private static int copyDirectory(File source, File target) throws IOException {
		int fileCount = 0;
		if (!target.exists()) {
			target.mkdirs();
		}
		File[] files = source.listFiles();
		if (files != null) {
			for (File sourceChild : files) {
				String name = sourceChild.getName();
				File targetChild = new File(target, name);
				if (sourceChild.isDirectory()) {
					fileCount += copyDirectory(sourceChild, targetChild);
				}
				else {
					fileCount++;
					copy(sourceChild, targetChild);
				}
			}
		}
		return fileCount;
	}

	private final String targetName;

	private final String sourceName;

	public CopyOperation(IConfigurationElement element) {
		this.sourceName = element.getAttribute(ATTR_SOURCE);
		this.targetName = element.getAttribute(ATTR_TARGET);
	}

	@Override
	public IStatus install(IProgressMonitor monitor) {
		try {
			File source = getSource();
			File target = getTarget();
			int count = copyDirectory(source, target);
			return new Status(IStatus.OK, Activator.PLUGIN_ID, NLS.bind("Copied {0} files from ''{1}'' to ''{2}''.",
					new Object[] { count, source, target }));
		}
		catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "I/O error while copying files", e);
		}
	}

	private File getTarget() {
		return (targetName != null) ? new File(getTargetBase(), targetName) : getTargetBase();
	}

	private File getSource() {
		return (sourceName != null) ? new File(getSourceBase(), sourceName) : getSourceBase();
	}

	@Override
	public String toString() {
		return "CopyOperation [target=" + targetName + ", source=" + sourceName + "]";
	}

}
