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
package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springsource.ide.eclipse.commons.browser.javafx.JavaFxBrowser;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardReopener;

public class WelcomeDashboard extends JavaFxBrowser {

	private static final String WELCOME_PAGE_URI = "platform:/plugin/org.springsource.ide.eclipse.commons.gettingstarted/resources/welcome";

	public WelcomeDashboard() throws URISyntaxException, IOException {
		DashboardReopener.ensure();
		setName("Welcome");
		URL fileURL = FileLocator.toFileURL(new URL(WELCOME_PAGE_URI));
		File contentInstance = DashboardCopier.getCopy(new File(fileURL.toURI()), new NullProgressMonitor());
		File welcomeHtml = new File(contentInstance, "index.html");
		setHomeUrl(welcomeHtml.toURI().toString());
	}

	@Override
	protected boolean hasToolbar() {
		return false;
	}
}
