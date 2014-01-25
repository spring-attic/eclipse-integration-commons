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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

/**
 * @author Miles Parker
 * 
 */
public class BrowserExtensions {

	public static final String EXTENSION_ID_NEW_WIZARD = "org.eclipse.ui.newWizards";

	public static final String EXTENSION_ID_BROWSER_TO_ECLIPSE = "org.springsource.ide.browser.function.browserToEclipse";

	public static final String EXTENSION_ID_ECLIPSE_TO_BROWSER = "org.springsource.ide.browser.function.eclipseToBrowser";

	public static final String ELEMENT_ID = "id";

	public static final String ELEMENT_URL = "urlExpression";

	public static final String ELEMENT_CLASS = "class";

	public static final String ELEMENT_ICON = "icon";

	public static final String ELEMENT_NAME = "name";

	public static final String ELEMENT_ARGUMENT = "argument";

	public static final String ELEMENT_DYNAMIC = "dynamic";

	public static final String ELEMENT_FUNCTION_NAME = "functionName";

	public static final String ELEMENT_LITERAL = "literal";

	public static final String ELEMENT_ONLOAD = "invokeOnLoad";

	public static IConfigurationElement[] getExtensions(String extensionId, String id, String url) {
		List<IConfigurationElement> elements = new ArrayList<IConfigurationElement>();
		IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
		IConfigurationElement[] configurations = registry.getConfigurationElementsFor(extensionId);
		for (IConfigurationElement element : configurations) {
			String elementId = element.getAttribute(ELEMENT_ID);
			String elementUrl = element.getAttribute(ELEMENT_URL);
			if ((elementId == null || elementId.equals(id))
					&& (url == null || (elementUrl != null && url.matches(elementUrl)))) {
				elements.add(element);
			}
		}
		return elements.toArray(new IConfigurationElement[] {});
	}

	public static IConfigurationElement getExtension(String extensionId, String id, String url) {
		IConfigurationElement[] extensionsForUrl = getExtensions(extensionId, id, url);
		if (extensionsForUrl.length > 0) {
			return extensionsForUrl[0];
		}
		return null;
	}

	public static IConfigurationElement getExtension(String extensionId, String id) {
		return getExtension(extensionId, id, null);
	}
}
