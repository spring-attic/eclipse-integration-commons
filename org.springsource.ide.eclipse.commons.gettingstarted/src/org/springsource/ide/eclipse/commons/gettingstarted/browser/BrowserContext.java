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
package org.springsource.ide.eclipse.commons.gettingstarted.browser;

import javafx.scene.web.WebView;
import org.eclipse.swt.widgets.Shell;

/**
 * Covenient class mostly meant to be subclassed to implement something that 
 * is created in the context of a Browser. 
 * 
 * @author Kris De Volder
 */
public class BrowserContext {

	private WebView browser;
	
	public BrowserContext(WebView browser) {
		this.browser = browser;
		
	}
	
	public WebView getBrowser() {
		return browser;
	}

}
