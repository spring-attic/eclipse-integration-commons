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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.core.SetVariablesOperation;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

public class WelcomeDashboardPage extends WebDashboardPage {

	private static final String WELCOME_PAGE_URI = "platform:/plugin/org.springsource.ide.eclipse.commons.gettingstarted/resources/welcome";

	private File welcomeHtml;
	private DashboardEditor dashboard;
	private DashboardWebViewManager webView;

	public WelcomeDashboardPage(DashboardEditor dashboard) throws URISyntaxException,
			IOException {
		this.dashboard = dashboard;
		setName("Welcome"); // Although this is the title in the html page,
							// windows browser doesn't seem to reliably give a
							// title event for it. So we must
							// provide a name ourselves.
		// platform url assumed to point to a bundled directory of
		// 'templated' content that needs StsProperties replaced.
		URL fileURL = FileLocator.toFileURL(new URL(WELCOME_PAGE_URI));
		File contentInstance = DashboardCopier.getCopy(urlToFile(fileURL),
				new NullProgressMonitor());
		welcomeHtml = new File(contentInstance, "index.html");
		setHomeUrl(welcomeHtml.toURI().toString());
	}

	private File urlToFile(URL fileURL) {
		try {
			// proper way to conver url to file:
			return new File(fileURL.toURI());
		} catch (URISyntaxException e) {
			// Deal with broken file urls (may contain spaces in unescaped form,
			// thanks Eclipse FileLocator!).
			// We will assume that if some chars are not escaped none of them
			// are escaped.
			return new File(fileURL.getFile());
		}
	}

	@Override
	protected boolean hasToolbar() {
		return false;
	}

	@Override
	public boolean canClose() {
		// Shouldn't allow closing the main / welcome page.
		return false;
	}

	@Override
	protected void addBrowserHooks() {
		super.addBrowserHooks();
		getBrowserViewer().getBrowser().getEngine().getLoadWorker().stateProperty()
				.addListener(new ChangeListener<Worker.State>() {

					@Override
					public void changed(ObservableValue<? extends State> ov,
							State oldState, State newState) {
						if (newState == Worker.State.SUCCEEDED
								&& getBrowserViewer() != null) {
							if (webView == null) {
								webView = new DashboardWebViewManager(dashboard);
							}
							webView.setClient(getBrowserViewer().getBrowser());
						}
					}
				});
	}

	@Override
	public void dispose() {
		super.dispose();
		if (webView != null) {
			webView.dispose();
		}
	}
}
