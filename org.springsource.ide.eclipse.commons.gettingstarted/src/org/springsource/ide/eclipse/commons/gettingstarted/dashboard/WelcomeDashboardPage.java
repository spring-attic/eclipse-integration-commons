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
package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

public class WelcomeDashboardPage extends WebDashboardPage {

	private File welcomeHtml;
	private DashboardEditor dashboard;
	private DashboardWebView webView;

	public WelcomeDashboardPage(DashboardEditor dashboard) throws URISyntaxException,
			IOException {
		this.dashboard = dashboard;
		StsProperties props = StsProperties.getInstance(new NullProgressMonitor());
		String contentUrl = props.get("dashboard.welcome.url");
		setName("Welcome"); // Although this is the title in the html page,
							// windows browser doesn't seem to reliably give a
							// title event for it. So we must
							// provide a name ourselves.
		if (contentUrl == null) {
			// shouldn't happen, but do something with this anyhow, better than
			// a blank page or an error.
			setHomeUrl("http://spring.io");
		} else if (contentUrl.startsWith("platform:")) {
			// platform url assumed to point to a bundled directory of
			// 'templated' content that needs StsProperties replaced.
			URL fileURL = FileLocator.toFileURL(new URL(contentUrl));
			File contentInstance = DashboardCopier.getCopy(urlToFile(fileURL),
					new NullProgressMonitor());
			welcomeHtml = new File(contentInstance, "index.html");
			setHomeUrl(welcomeHtml.toURI().toString());
		} else {
			setHomeUrl(contentUrl);
		}
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
	protected void addBrowserHooks(final WebView browser) {
		super.addBrowserHooks(browser);
		getBrowserViewer().getBrowser().getEngine().getLoadWorker().stateProperty()
				.addListener(new ChangeListener<Worker.State>() {

					@Override
					public void changed(ObservableValue<? extends State> ov,
							State oldState, State newState) {
						if (newState == Worker.State.SUCCEEDED
								&& getBrowserViewer() != null) {
							webView = new DashboardWebView(getBrowserViewer()
									.getBrowser(), dashboard);
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
