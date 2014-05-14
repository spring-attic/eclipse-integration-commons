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
package org.springsource.ide.eclipse.commons.browser.javafx;

import java.net.MalformedURLException;
import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.springsource.ide.eclipse.commons.frameworks.core.util.Gtk3Check;

/**
 * An editor that displays the contents of a webpage using JavaFx WebView.
 *
 * @author Kris De Volder
 * @author Miles Parker
 */
public class JavaFxBrowserEditor extends EditorPart {

	static {
		if (Gtk3Check.isGTK3) {
			throw new UnsupportedOperationException("JavaFX doesn't work with GTK3");
		}
	}

	public static final String EDITOR_ID = "org.springsource.ide.eclipse.commons.browser.Editor";

	/**
	 * The URL that will be displayed in this Dashboard webpage.
	 */
	private String homeUrl;

	private String name;

	private JavaFxBrowserViewer browserViewer;

	private JavaFxBrowserManager browserManager;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		browserViewer = new JavaFxBrowserViewer(parent, hasToolbar() ? JavaFxBrowserViewer.BUTTON_BAR
				| JavaFxBrowserViewer.LOCATION_BAR : SWT.NONE);
		final WebView browser = browserViewer.getBrowser();
		if (getEditorInput() instanceof WebBrowserEditorInput) {
			homeUrl = ((WebBrowserEditorInput) getEditorInput()).getURL().toString();
			browserViewer.setVisible(true);
		}
		if (homeUrl != null) {
			browserViewer.setHomeUrl(homeUrl);
			browserViewer.setURL(homeUrl);
		}
		else {
			browser.getEngine().loadContent(
					"<h1>URL not set</h1>" + "<p>Url should be provided via the setInitializationData method</p>");
		}
		if (getName() == null) {
			browser.getEngine().titleProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					setName(newValue);
					setPartName(newValue);
				}
			});
		}
		getBrowserViewer().getBrowser().getEngine().getLoadWorker().stateProperty()
				.addListener(new ChangeListener<Worker.State>() {

					@Override
					public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
						if (newState == Worker.State.SUCCEEDED && getBrowserViewer() != null) {
							if (browserManager == null) {
								browserManager = new JavaFxBrowserManager();
							}
							browserManager.setClient(getBrowserViewer().getBrowser());
							if (getEditorInput() instanceof WebBrowserEditorInput) {
								String url = getBrowserViewer().getURL();
								try {
									setInput(new WebBrowserEditorInput(new URL(url), SWT.NONE, url));
								} catch (MalformedURLException e) {
									throw new RuntimeException(e);
								}
							}
						}
					}
				});
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
	 * The url of the landing page this dashboard page will show when it is
	 * opened.
	 */
	public String getHomeUrl() {
		return homeUrl;
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
	}

	protected JavaFxBrowserViewer getBrowserViewer() {
		return browserViewer;
	}

	@Override
	public void dispose() {
		if (browserViewer != null) {
			browserViewer.dispose();
			browserViewer = null;
		}
		if (browserManager != null) {
			browserManager.dispose();
			browserManager = null;
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}
}
