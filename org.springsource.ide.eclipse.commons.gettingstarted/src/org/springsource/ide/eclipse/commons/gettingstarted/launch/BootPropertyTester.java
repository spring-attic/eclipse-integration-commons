/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.launch;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;

public class BootPropertyTester extends PropertyTester {

	public BootPropertyTester() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean test(Object project, String property, Object[] args, Object expectedValue) {
		if (project instanceof IProject && "isBootProject".equals(property)) {
			return expectedValue.equals(isBootProject((IProject)project));
		}
		return false;
	}

	public static boolean isBootProject(IProject project) {
		if (project==null || ! project.isAccessible()) {
			return false;
		}
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject jp = JavaCore.create(project);
				IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
				//Look for a 'spring-boot' jar entry
				for (IClasspathEntry e : classpath) {
					if (isBootJar(e)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			GettingStartedActivator.log(e);
		}
		return false;
	}

	private static boolean isBootJar(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.endsWith(".jar") && name.startsWith("spring-boot");
		}
		return false;
	}

}
