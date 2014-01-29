/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html).
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.browser.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Miles Parker
 *
 */
public class JavaFxBrowserView extends ViewPart {

	public static final String EDITOR_ID = "org.springsource.ide.eclipse.commons.javafx.browser.View";

	private JavaFxBrowserViewer browserViewer;

	private JavaFxBrowserManager browserManager;

	private final boolean hasToolbar;

	private String initialUrl;

	public JavaFxBrowserView() {
		hasToolbar = true;
	}

	public JavaFxBrowserView(String initialUrl, boolean hasToolbar) {
		this.initialUrl = initialUrl;
		this.hasToolbar = hasToolbar;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		browserViewer = new JavaFxBrowserViewer(parent, hasToolbar() ? JavaFxBrowserViewer.BUTTON_BAR
				| JavaFxBrowserViewer.LOCATION_BAR : SWT.NONE);
		if (initialUrl != null) {
			setUrl(initialUrl);
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
						}
					}
				});
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
	public JavaFxBrowserViewer getBrowserViewer() {
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
