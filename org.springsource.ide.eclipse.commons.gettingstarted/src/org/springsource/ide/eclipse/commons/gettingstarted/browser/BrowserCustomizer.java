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
package org.springsource.ide.eclipse.commons.gettingstarted.browser;

import javafx.scene.web.WebView;

public class BrowserCustomizer extends BrowserContext {

	private IBrowserCustomization[] customizations = BrowserCustomizationReader.get();

	public BrowserCustomizer(WebView browser) {
		super(browser);
		addBrowserHooks(browser);
	}
	
	protected void addBrowserHooks(final WebView browser) {
		if (customizations!=null) {
			for (IBrowserCustomization customization : customizations) {
				customization.apply(browser);
			}
		}
	}
	
}
