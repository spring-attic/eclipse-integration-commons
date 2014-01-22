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
package org.springsource.ide.eclipse.commons.configurator;

import java.io.File;
import java.util.List;

import org.eclipse.osgi.service.resolver.VersionRange;

/**
 * This interface is not intended to be subclassed.
 * @author Steffen Pingel
 * @since 2.2
 */
public interface IConfigurationContext {

	/**
	 * Returns an existing location that matches the path name and version
	 * range.
	 * 
	 * @param path a directory name
	 * @param versionRange a version range that is part of the filename
	 * @return null, if no matching location was found
	 */
	public abstract List<File> scan(String path, VersionRange versionRange);

}
