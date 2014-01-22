/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.externaltype;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Instances of this interface can contributed via the extension point
 * org.spring.ide.eclipse.completions.externaltypes
 * <p>
 * An implementation defines an algorithm for associating a
 * ExternalTypeDiscovery algorithm with a project.
 * 
 * @author Kris De Volder
 */
public interface ExternalTypeDiscoveryFactory {
	
	/**
	 *Retuns a corresponding ExternalTypeDiscovery instance of the given project. May return null if
	 *the project fails some applicability checks.
	 *<p>
	 *Implementors may and are encouraged to return the same instance for more than one project. This will
	 *allow the suggestion engine to reused the index based on the discovery algorithm across the projects
	 *thereby saving lots of memory.
	 */
	public ExternalTypeDiscovery discoveryFor(IJavaProject project);

}
