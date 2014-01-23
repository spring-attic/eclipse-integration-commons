/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.browser;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

/**
 * @author Miles Parker
 *
 */
public class BrowserUtils {

	public static final String ELEMENT_ID = "id";

	public static final String ELEMENT_CLASS = "class";

	public static final String ELEMENT_ICON = "icon";

	public static final String ELEMENT_NAME = "name";

	public static IConfigurationElement getExtension(String extensionId, String id) {
		IExtensionRegistry registry = org.eclipse.core.runtime.Platform
				.getExtensionRegistry();
		IConfigurationElement[] configurations = registry
				.getConfigurationElementsFor(extensionId);
		for (IConfigurationElement element : configurations) {
			String elementId = element.getAttribute(ELEMENT_ID);
			if (elementId.equals(id)) {
				return element;
			}
		}
		return null;
	}

}
