/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.boot;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;
import org.eclipse.jface.operation.IRunnableContext;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;

public class BootLaunchShortcut extends JavaApplicationLaunchShortcut {
	
	private static final String MAIN_CLASS_PROP = "start-class";

	@Override
	protected IType[] findTypes(Object[] elements, IRunnableContext context)
			throws InterruptedException, CoreException {
		//For spring boot app, instead of searching for a main type in the entire project and all its
		// libraries... try to look inside the project's pom for the corresponding property.
		for (Object e : elements) {
			if (e instanceof IJavaElement) {
				IJavaProject p = ((IJavaElement)e).getJavaProject();
				IFile pomFile = p.getProject().getFile("pom.xml");
				if (pomFile.exists()) {
					PomParser pomParser = new PomParser(pomFile);
					String starterClassName = pomParser.getProperty(MAIN_CLASS_PROP);
					if (starterClassName!=null) {
						IType mainType = p.findType(starterClassName);
						if (mainType!=null) {
							return new IType[] { mainType };
						}
						throw ExceptionUtil.coreException("'pom.xml' defines '"+MAIN_CLASS_PROP+"' as '"+starterClassName+"' but it could not be found");
					}
				}
			}
		}
		//This isn't the best thing to to do as it searches also in all the library jars for main types. But it is
		// only a fallback option if the above code failed. (Or should we rather signal an error instead?)
		return super.findTypes(elements, context);
	}

}
