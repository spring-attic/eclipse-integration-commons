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
package org.springsource.ide.eclipse.commons.frameworks.core.maintype;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * Provides access to heuristic algorithms to 'guess' the main type for a project.
 * 
 * Note: these algorithms may depend on project type and require access to 
 * various optional dependencies. E.g. gradle tooling for gradle projects, 
 * m2e for pom based algorithms etc. 
 * <p>
 * Ideally in the future additional algorithms should be contributable via extension
 * point so that we don't need to add a gazilion dependencies in this plugin,
 * but clients can depend on this plugin and implicitly gain access if the
 * required stuff is installed.
 * 
 * @author Kris De Volder
 */
public class MainTypeFinder {
	
	private static MainTypeFinder instance;

	/**
	 * Find the most likely main type that should be run when a user select this
	 * project with 'run as'. 
	 * 
	 * @return A type with main method or null if the 'most likely' main type can't be determined.
	 * @throws CoreException if something unexpected happened..
	 */
	public static IType findMainType(IJavaProject project, IProgressMonitor mon) throws CoreException {
		return getInstance().findMain(project, mon);
	}

	private static synchronized MainTypeFinder getInstance() {
		if (instance==null) {
			instance = new MainTypeFinder();
		}
		return instance;
	}

	private IMainTypeFinder[] algos;

	/**
	 * Singelton, use getInstance or call one of the static public methods.
	 */
	private MainTypeFinder() {
		//TODO: support contributing additional algos with priorities via extension point
		this.algos = new IMainTypeFinder[]{
				//add more here if they should be considered more authorative
				new FindInPom()
				//add more here if they should be considered less authorative
		};
	}
	
	private IType findMain(IJavaProject project, IProgressMonitor mon) throws CoreException {
		IType found = null;
		Throwable error = null;
		mon.beginTask("Search main type", algos.length);
		try {
			for (IMainTypeFinder algo : algos) {
				try {
					found = algo.findMain(project, new SubProgressMonitor(mon, 1));
				} catch (Throwable e) {
					if (error==null) {
						//if multiple errors keep only the first one.
						error = e;
					}
				}
			}
			mon.worked(1);
		} finally {
			mon.done();
		}
		if (found!=null) {
			return found;
		} else {
			//If there was an error throw it as an explanation of the failure.
			if (error!=null) {
				throw ExceptionUtil.coreException(error);
			}
		}
		return null;
	}
	
	private static final String MAIN_CLASS_PROP = "start-class";
	


	public static class FindInPom implements IMainTypeFinder {

		public IType findMain(IJavaProject jp, IProgressMonitor mon)
				throws Exception {
			mon.beginTask("Search main in pom", 1);
			try {
				IProject p = jp.getProject();
				IFile pomFile = p.getFile("pom.xml");
				if (pomFile.exists()) {
					PomParser pomParser = new PomParser(pomFile);
					String starterClassName = pomParser.getProperty(MAIN_CLASS_PROP);
					if (starterClassName!=null) {
						IType mainType = jp.findType(starterClassName);
						if (mainType!=null) {
							return mainType;
						}
						throw ExceptionUtil.coreException("'pom.xml' defines '"+MAIN_CLASS_PROP+"' as '"+starterClassName+"' but it could not be found");
					}
				}
			} finally {
				mon.done();
			}
			return null;
		}

	}
}
