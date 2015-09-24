/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.workspace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;

/**
 * @author Kris De Volder
 */
public class ProjectDeletionListenerManager implements IResourceChangeListener {

	public interface ProjectDeletionListener {
		void projectAboutToBeDeleted(IProject project);
	}

	private IWorkspace workspace;
	private ProjectDeletionListener listener;

	public ProjectDeletionListenerManager(IWorkspace workspace, ProjectDeletionListener listener) {
		this.workspace = workspace;
		this.listener = listener;
		this.workspace.addResourceChangeListener(this, IResourceChangeEvent.PRE_DELETE);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResource rsrc = event.getResource();
		if (rsrc instanceof IProject) {
			listener.projectAboutToBeDeleted((IProject) rsrc);
		}
	}

	public void dispose() {
		if (listener!=null) {
			workspace.removeResourceChangeListener(this);
			listener = null;
		}
	}

}
