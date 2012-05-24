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
package org.springsource.ide.eclipse.commons.tests.util;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

/**
 * Derived from AbstractBeansCoreTestCase
 * @author Steffen Pingel
 * @author Terry Denney
 */
public abstract class StsTestCase extends TestCase {

	protected IProject createPredefinedProject(final String projectName) throws CoreException, IOException {
		return StsTestUtil.createPredefinedProject(projectName, getBundleName());
	}

	protected IResource createPredefinedProjectAndGetResource(String projectName, String resourcePath)
			throws CoreException, IOException {
		IProject project = createPredefinedProject(projectName);
		// XXX do a second full build to ensure markers are up-to-date
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		IResource resource = project.findMember(resourcePath);
		StsTestUtil.waitForResource(resource);
		return resource;
	}

	protected abstract String getBundleName();

	protected String getSourceWorkspacePath() {
		return StsTestUtil.getSourceWorkspacePath(getBundleName());
	}

	@Override
	protected void tearDown() throws Exception {
		StsTestUtil.cleanUpProjects();
		super.tearDown();
	}

}
