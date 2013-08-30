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
package org.springsource.ide.eclipse.commons.gettingstarted.browser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;

/**
 * Retrieves IBrowserCustomization instance ctronibute via extension point.
 *  
 * @author Kris De Volder
 */
public class BrowserCustomizationReader {

	private static final String EXTENSION_POINT_ID = "org.springsource.ide.eclipse.commons.dashboard.browser.customization";
	private static IBrowserCustomization[] _list;

	public static IBrowserCustomization[] get() {
		if (_list==null) {
			List<IBrowserCustomization> list = new ArrayList<IBrowserCustomization>();
			
			IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
			for (IConfigurationElement element : elements) {
				try {
					IBrowserCustomization it = (IBrowserCustomization)element.createExecutableExtension("class");
					list.add(it);
				} catch (CoreException e) {
					GettingStartedActivator.log(e);
				}
			}
			
			_list = list.toArray(new IBrowserCustomization[list.size()]);
		}
		return _list;
	}

}
