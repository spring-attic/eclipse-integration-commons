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
package org.springsource.ide.eclipse.commons.browser.swt;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.eclipse.ui.part.EditorPart;

/**
 * An editor that displays the contents of a webpage using SWT Embedded Browser Widget.
 *
 * @author Kris De Volder
 * @author Miles Parker
 */
public class StsBrowserEditor extends EditorPart {

	public static final String EDITOR_ID = "org.springsource.ide.eclipse.commons.browser.Editor";

	/**
	 * The URL that will be displayed in this Dashboard webpage.
	 */
	private String homeUrl;

	private String name;

	private StsBrowserViewer browserViewer;

	private StsBrowserManager browserManager;

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
		browserViewer = new StsBrowserViewer(parent, hasToolbar() ? StsBrowserViewer.BUTTON_BAR
				| StsBrowserViewer.LOCATION_BAR : SWT.NONE);
		final Browser browser = browserViewer.getBrowser();
		if (getEditorInput() instanceof WebBrowserEditorInput) {
			homeUrl = ((WebBrowserEditorInput) getEditorInput()).getURL().toString();
			browserViewer.setVisible(true);
		}
		if (homeUrl != null) {
			browserViewer.setHomeUrl(homeUrl);
			browserViewer.setURL(homeUrl);
		}
		else {
			browser.setText(
					"<h1>URL not set</h1>" + "<p>Url should be provided via the setInitializationData method</p>"
			);
		}
		if (getName() == null) {
			browser.addTitleListener(new TitleListener() {

				@Override
				public void changed(TitleEvent event) {
					String newValue = event.title;
					setName(newValue);
					setPartName(newValue);
				}
			});
		}
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				StsBrowserViewer browserViewer = getBrowserViewer();
				if (browserViewer != null) {
					if (browserManager == null) {
						browserManager = new StsBrowserManager();
					}
					browserManager.setClient(browserViewer.getBrowser());
					if (getEditorInput() instanceof WebBrowserEditorInput) {
						String url = browserViewer.getURL();
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

	/**
	 * Sets the url the browser to display immediately. If the browser's controls
	 * have not yet been created then this does nothing.
	 */
	public void setUrl(final String url) {
		if (browserViewer!=null) {
			browserViewer.getDisplay().asyncExec(new Runnable() {
				public void run() {
					browserViewer.setURL(url);
				}
			});
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected StsBrowserViewer getBrowserViewer() {
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
