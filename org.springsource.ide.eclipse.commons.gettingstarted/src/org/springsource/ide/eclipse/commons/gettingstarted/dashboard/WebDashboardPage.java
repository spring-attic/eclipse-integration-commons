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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.browser.WebBrowserUtil;
import org.springsource.ide.eclipse.commons.gettingstarted.browser.BrowserFactory;
import org.springsource.ide.eclipse.commons.gettingstarted.browser.STSBrowserViewer;

//Note: some complications on Linux systems because of problems satisfying
// the requirements for SWT browser component to work.
//For Ubuntu 12.04 some usefull info here.
//Maybe this can be somehow fixed by us packaging a compatible xulrunner
//and STS.ini file?

// http://askubuntu.com/questions/125980/how-do-i-install-xulrunner-in-12-04

/**
 * A DashBoard page that displays the contents of a webpage.
 * 
 * @author Kris De Volder
 * @author Miles Parker
 */
public class WebDashboardPage extends ADashboardPage /*
													 * implements
													 * IExecutableExtension
													 */{

	// /**
	// * Using this ID ensures we only open one 'slave' browser when opening
	// links from within
	// * a dashboard page.
	// */
	// public static final String DASHBOARD_SLAVE_BROWSER_ID =
	// WebDashboardPage.class.getName()+".SLAVE";

	/**
	 * The URL that will be displayed in this Dashboard webpage.
	 */
	private String homeUrl;

	private String name;

	private Shell shell;

	private STSBrowserViewer browserViewer;

	private ADashboardPage errorPage;

	/**
	 * Constructor for when this class is used as n {@link IExecutableExtension}
	 * . In that case setInitializationData method will be called with infos
	 * from plugin.xml to fill in the fields.
	 */
	public WebDashboardPage() {
	}

	/**
	 * Name may by null, in that case the name will be set later when the page
	 * is loaded in the browser (The title from the first page title event is
	 * used).
	 */
	public WebDashboardPage(String name, String homeUrl) {
		this.name = name;
		this.homeUrl = homeUrl;
	}

	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		if (WebBrowserUtil.canUseInternalWebBrowser()) {
			this.shell = parent.getShell();
			parent.setLayout(new FillLayout());
			browserViewer = BrowserFactory.create(parent, hasToolbar());
			final WebView browser = browserViewer.getBrowser();
			if (homeUrl != null) {
				browserViewer.setHomeUrl(homeUrl);
				browserViewer.setURL(homeUrl);
			} else {
				browser.getEngine()
						.loadContent(
								"<h1>URL not set</h1>"
										+ "<p>Url should be provided via the setInitializationData method</p>");
			}
			if (getName() == null) {
				browser.getEngine().titleProperty()
						.addListener(new ChangeListener<String>() {
							@Override
							public void changed(
									ObservableValue<? extends String> observable,
									String oldValue, String newValue) {
								setName(newValue);
								browser.getEngine().titleProperty()
								.removeListener(this);
							}
						});
			}
			addBrowserHooks();
		} else {
			errorPage = new BrowserErrorPage();
			errorPage.createControl(parent);
		}
	}

	/**
	 * Subclasses may override if they don't want the url and buttons toolbar.
	 * Defailt implementation returns true causing the toolbar to be added when
	 * the browser widget is created.
	 */
	protected boolean hasToolbar() {
		return true;
	}

	/**
	 * Subclasses may override this if they want to customize the browser (e.g.
	 * add listeners to handle certain urls specially.
	 */
	protected void addBrowserHooks() {
	}

	/**
	 * The url of the landing page this dashboard page will show when it is
	 * opened.
	 */
	public String getHomeUrl() {
		return homeUrl;
	}

	public String getPageId() {
		return getHomeUrl();
	}

	/**
	 * Change the url this dashboard page will show when it is first opened, or
	 * when the user clicks on the 'home' icon.
	 */
	public void setHomeUrl(String url) {
		this.homeUrl = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		DashboardPageContainer container = getContainer();
		if (name != null && container != null) {
			CTabItem widget = container.getWidget();
			if (widget != null && !widget.isDisposed()) {
				widget.setText(name);
			}
		}
	}

	@Override
	public boolean canClose() {
		return true;
	}

	public Shell getShell() {
		return shell;
	}

	public void goHome() {
		browserViewer.goHome();
	}
	
	protected STSBrowserViewer getBrowserViewer() {
		return browserViewer;
	}

	@Override
	public void dispose() {
		if (errorPage != null) {
			errorPage.dispose();
		}
		if (this.browserViewer != null) {
			this.browserViewer.dispose();
			this.browserViewer = null;
		}
		super.dispose();
	}
}
