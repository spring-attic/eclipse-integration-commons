/*******************************************************************************
 * Copyright (c) 2014,2017 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0
 * (https://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution
 * License v1.0 (https://www.eclipse.org/org/documents/edl-v10.html).
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.browser.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Miles Parker
 * @author Kris De Volder
 */
public class StsBrowserView extends ViewPart {

	public static final String EDITOR_ID = "org.springsource.ide.eclipse.commons.javafx.browser.View";

	private StsBrowserViewer browserViewer;

	private StsBrowserManager browserManager;

	private final boolean hasToolbar;

	private String initialUrl;

	public StsBrowserView() {
		hasToolbar = true;
	}

	public StsBrowserView(String initialUrl, boolean hasToolbar) {
		this.initialUrl = initialUrl;
		this.hasToolbar = hasToolbar;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		browserViewer = new StsBrowserViewer(parent, hasToolbar() ? StsBrowserViewer.BUTTON_BAR
				| StsBrowserViewer.LOCATION_BAR : SWT.NONE);
		if (initialUrl != null) {
			setUrl(initialUrl);
		}
		Browser browser = getBrowserViewer().getBrowser();
		if (browser!=null) {
			browser.addProgressListener(new ProgressAdapter() {

				@Override
				public void completed(ProgressEvent event) {
					if (browserManager == null) {
						browserManager = new StsBrowserManager();
					}
					browserManager.setClient(getBrowserViewer().getBrowser());
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	/**
	 * @return the browserViewer
	 */
	public StsBrowserViewer getBrowserViewer() {
		return browserViewer;
	}

	public void setUrl(String url) {
		if (browserViewer != null && !browserViewer.isDisposed()) {
			browserViewer.setHomeUrl(url);
			browserViewer.setURL(url);
		}
		else {
			this.initialUrl = url;
		}
	}

	public String getUrl() {
		if (browserViewer != null && !browserViewer.isDisposed()) {
			return browserViewer.getURL();
		}
		return browserViewer.getURL();
	}

	public boolean hasToolbar() {
		return hasToolbar;
	}
}
