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
package org.springsource.ide.eclipse.commons.core.util;

/**
 * @author Kris De Volder
 * 
 * @since 2.8
 */
public class OsUtils {

	public static boolean isWindows() {
		return System.getProperty("os.name").contains("Windows");
	}

}
