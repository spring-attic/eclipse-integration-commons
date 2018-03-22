/*******************************************************************************
 * Copyright (c) 2014, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *   Jacek Pospychala - jacek.pospychala@pl.ibm.com - fix for bug 224887
 *   Kris De Volder - Renamed to 'STSBrowserViewer and
 *                    modified to use as browser for STS dashboard.
 *   Miles Parker - Re-purposed Kris' STS wrapper into a JavaFx based implementation.
 *   Kris De Volder - Removed JavaFx dependencies again, swith back to full SWT-b
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.browser.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.browser.BrowserViewer;

@SuppressWarnings("restriction")
public class StsBrowserViewer extends BrowserViewer {

	private String homeUrl;

	public StsBrowserViewer(Composite parent, int style) {
		super(parent, style);
	}

	public String getHomeUrl() {
		return homeUrl;
	}

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}

	@Override
	public void home() {
		if (homeUrl != null) {
			browser.setUrl(homeUrl);
		} else {
			super.home();
		}
	}

}
