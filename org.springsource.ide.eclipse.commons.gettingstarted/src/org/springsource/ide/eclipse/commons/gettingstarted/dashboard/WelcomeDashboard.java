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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.javafx.browser.JavaFxBrowser;

public class WelcomeDashboard extends JavaFxBrowser {

	private static final String WELCOME_PAGE_URI = "platform:/plugin/org.springsource.ide.eclipse.commons.gettingstarted/resources/welcome";

	private DashboardWebViewManager dashboardManager;

	public WelcomeDashboard() throws URISyntaxException,
			IOException {
		setName("Welcome");
		URL fileURL = FileLocator.toFileURL(new URL(WELCOME_PAGE_URI));
		File contentInstance = DashboardCopier.getCopy(new File(fileURL.toURI()),
				new NullProgressMonitor());
		File welcomeHtml = new File(contentInstance, "index.html");
		setHomeUrl(welcomeHtml.toURI().toString());
	}

	@Override
	protected boolean hasToolbar() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springsource.ide.eclipse.commons.gettingstarted.browser.JavaFxBrowser#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getBrowserViewer().getBrowser().getEngine().getLoadWorker().stateProperty()
				.addListener(new ChangeListener<Worker.State>() {

					@Override
					public void changed(ObservableValue<? extends State> ov,
							State oldState, State newState) {
						if (newState == Worker.State.SUCCEEDED
								&& getBrowserViewer() != null) {
							if (dashboardManager == null) {
								dashboardManager = new DashboardWebViewManager();
							}
							dashboardManager.setClient(getBrowserViewer().getBrowser());
						}
					}
				});
	}

	@Override
	public void dispose() {
		super.dispose();
		if (dashboardManager != null) {
			dashboardManager.dispose();
		}
	}
}
