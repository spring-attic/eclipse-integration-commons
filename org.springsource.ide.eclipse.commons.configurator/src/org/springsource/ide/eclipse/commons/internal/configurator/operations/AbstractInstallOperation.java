/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.configurator.operations;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * @author Steffen Pingel
 */
public abstract class AbstractInstallOperation {

	private File sourceBase;

	private File targetBase;

	public final File getSourceBase() {
		return sourceBase;
	}

	public final File getTargetBase() {
		return targetBase;
	}

	public abstract IStatus install(IProgressMonitor monitor);

	public final void setSourceBase(File sourceBase) {
		this.sourceBase = sourceBase;
	}

	public final void setTargetBase(File targetBase) {
		this.targetBase = targetBase;
	}

	public void undo() {
		uninstall(null);
	}

	public IStatus uninstall(IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

}
