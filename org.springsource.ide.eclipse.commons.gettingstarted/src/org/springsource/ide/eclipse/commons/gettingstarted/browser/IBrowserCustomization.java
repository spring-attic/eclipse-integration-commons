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

import org.eclipse.swt.browser.Browser;

/**
 * Instances of this interface can be contributed via extension point
 * 'org.springsource.ide.eclipse.commons.dashboard.browser.customization'.
 * Customizations will be automatically applied to any browser widget
 * created in an STS Dashboard page (but not to the Eclipse internal
 * web browser).
 * 
 * @author Kris De Volder
 */
public interface IBrowserCustomization {

	/**
	 * Called right after a browser instance is created to display content in a 
	 * dashboard page.
	 */
	void apply(Browser browser);

}
