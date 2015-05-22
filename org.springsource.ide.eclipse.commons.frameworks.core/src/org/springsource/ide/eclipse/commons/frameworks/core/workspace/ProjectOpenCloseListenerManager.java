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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author Kris De Volder
 */
public class ProjectOpenCloseListenerManager implements IResourceChangeListener {

	public interface ProjectOpenCloseListener {
		void projectOpened(IProject project);
		void projectClosed(IProject project);
	}

	private IWorkspace workspace;
	private ProjectOpenCloseListener listener;

	public ProjectOpenCloseListenerManager(IWorkspace workspace, ProjectOpenCloseListener listener) {
		this.workspace = workspace;
		this.listener = listener;
		this.workspace.addResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
				IResource res = event.getResource();
				IProject project;
				switch (res.getType()) {
				case IResource.FOLDER:
					project = ((IFolder) res).getProject();
					break;
				case IResource.FILE:
					project = ((IFile) res).getProject();
					break;
				case IResource.PROJECT:
					project = (IProject) res;
					break;
				default:
					return;
				}
				projectClosed(project);
			} else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				// Find out if a project was opened.
				IResourceDelta delta = event.getDelta();
				if (delta == null) return;

				IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
				for (int i = 0; i < projDeltas.length; ++i) {
					IResourceDelta projDelta = projDeltas[i];
					if ((projDelta.getFlags() & IResourceDelta.OPEN) == 0)
						continue;
					IResource resource = projDeltas[i].getResource();
					if (!(resource instanceof IProject))
						continue;

					IProject project = (IProject) resource;
					projectOpened(project);
				}
			}
		} catch (OperationCanceledException oce) {
			// do nothing
		}
	}

	private void projectClosed(final IProject project) {
		listener.projectClosed(project);
	}

	private void projectOpened(final IProject project) {
		listener.projectOpened(project);
	}

	public void dispose() {
		if (listener!=null) {
			workspace.removeResourceChangeListener(this);
			listener = null;
		}
	}

}
