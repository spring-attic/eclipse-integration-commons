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
package org.springsource.ide.eclipse.commons.completions.externaltype;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

/**
 * An instance of this class represents a 'source' from which we can get
 * some external types. The main purpose of an instance is to define
 * how the type can be added to a project's classpath.
 * 
 * @author Kris De Volder
 */
public interface ExternalTypeSource {
	
	/**
	 * Instance representing an unknown type source. Use instead of 'null'.
	 */
	public static final ExternalTypeSource UNKNOWN = new ExternalTypeSource() {
		public void addToClassPath(IJavaProject project, IProgressMonitor mon) {
			//nothing to do
		}
		public String toString() {
			return "UNKNOWN";
		}
	};

	/**
	 * Manipulate the project's classpath in some way to add this type source
	 * to the classpath.
	 * <p>
	 * The implementation of this method may assume that it only gets called 
	 * if the type that triggers this addition is not yet on the project's 
	 * classpath. The caller should ensure this.
	 */
	void addToClassPath(IJavaProject project, IProgressMonitor mon);
	
}
