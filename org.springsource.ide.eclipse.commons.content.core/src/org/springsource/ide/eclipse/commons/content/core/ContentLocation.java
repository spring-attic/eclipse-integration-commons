/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.content.core;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * A non-persistent content location that is meant to be loaded only during a
 * runtime session. This is suppose to complement the list of content locations
 * that are pesisted in a state file by the content manager. A content location
 * should point to an actual, existing file that contains a list of descriptors.
 * For example, a content location would be the location of descriptor files
 * inside an Eclipse bundle. E.g. /template/descriptors.xml would be a content
 * location.
 * 
 */
public interface ContentLocation {

	/**
	 * 
	 * @param relativeURI URI relative to the content location
	 * @return input stream if its a valid URI pointing to a resource in the
	 * content location
	 * @throws CoreException
	 */
	public InputStream streamFromContentLocation(String relativeURI) throws CoreException;

	/**
	 * 
	 * @return location of the content xml. Can be relative as another API
	 * retrieves an actual existing File for the content location
	 */
	public String getContentLocation();

	/**
	 * 
	 * @return an existing File pointing to the content location file.
	 */
	public File getContentLocationFile();

}
