/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * Copyright (c) 2014 Pivotal Software, Inc.
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
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.browser.javafx;

//Most of this code copied from org.eclipse.ui.internal.browser.BrowserViewer
// modified to reuse as a browser for STS dashboard.

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.browser.BusyIndicator;
import org.eclipse.ui.internal.browser.IBrowserViewerContainer;
import org.eclipse.ui.internal.browser.ImageResource;
import org.eclipse.ui.internal.browser.Messages;
import org.eclipse.ui.internal.browser.ToolbarLayout;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.springsource.ide.eclipse.commons.gettingstarted.Images;

/**
 * A Web browser widget. It provides a JavaFx WebView and adds an optional
 * toolbar complete with a URL combo box, history, back & forward, and refresh
 * buttons.
 * <p>
 * Use the style bits to choose which toolbars are available within the browser
 * composite. You can access the embedded SWT Browser directly using the
 * getBrowser() method.
 * </p>
 * <p>
 * Additional capabilities are available when used as the internal Web browser,
 * including status text and progress on the Eclipse window's status line, or
 * moving the toolbar capabilities up into the main toolbar.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LOCATION_BAR, BUTTON_BAR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>None</dd>
 * </dl>
 *
 * @author Kris De Volder
 * @author Miles Parker
 *
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class JavaFxBrowserViewer extends Composite {
	/**
	 * Style parameter (value 1) indicating that the URL and Go button will be
	 * on the local toolbar.
	 */
	public static final int LOCATION_BAR = 1 << 1;

	/**
	 * Style parameter (value 2) indicating that the toolbar will be available
	 * on the web browser. This style parameter cannot be used without the
	 * LOCATION_BAR style.
	 */
	public static final int BUTTON_BAR = 1 << 2;

	protected static final String PROPERTY_TITLE = "title"; //$NON-NLS-1$

	private static final int MAX_HISTORY = 50;

	public Clipboard clipboard;

	public Combo combo;

	protected boolean showToolbar;

	protected boolean showURLbar;

	protected ToolItem back;

	protected ToolItem forward;

	protected BusyIndicator busy;

	protected boolean loading;

	protected static java.util.List<String> history;

	protected WebView browser;

	protected boolean newWindow;

	protected IBrowserViewerContainer container;

	protected String title;

	protected List<PropertyChangeListener> propertyListeners;

	private String initialUrl;

	/**
	 * Under development - do not use
	 */
	public static interface IBackNextListener {
		public void updateBackNextBusy();
	}

	public IBackNextListener backNextListener;

	private String homeUrl;

	/**
	 * Creates a new Web browser given its parent and a style value describing
	 * its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in the class
	 * header or class <code>SWT</code> which is applicable to instances of this
	 * class, or must be built by <em>bitwise OR</em>'ing together (that is,
	 * using the <code>int</code> "|" operator) two or more of those
	 * <code>SWT</code> style constants. The class description lists the style
	 * constants that are applicable to the class. Style bits are also inherited
	 * from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new
	 * instance (cannot be null)
	 * @param style the style of control to construct
	 */
	public JavaFxBrowserViewer(Composite parent, int style) {
		super(parent, SWT.NONE);

		if ((style & LOCATION_BAR) != 0) {
			showURLbar = true;
		}

		if ((style & BUTTON_BAR) != 0) {
			showToolbar = true;
		}

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.numColumns = 1;
		setLayout(layout);
		clipboard = new Clipboard(parent.getDisplay());

		if (showToolbar || showURLbar) {
			Composite toolbarComp = new Composite(this, SWT.NONE);
			toolbarComp.setLayout(new ToolbarLayout());
			toolbarComp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

			if (showToolbar) {
				createToolbar(toolbarComp);
			}

			if (showURLbar) {
				createLocationBar(toolbarComp);
			}

			if (showToolbar | showURLbar) {
				busy = new BusyIndicator(toolbarComp, SWT.NONE);
				busy.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				busy.addMouseListener(new MouseListener() {
					public void mouseDoubleClick(MouseEvent e) {
						// ignore
					}

					public void mouseDown(MouseEvent e) {
						goHome();
					}

					public void mouseUp(MouseEvent e) {
						// ignore
					}
				});
			}
		}

		// Without this, the JavaFx app will dispose itself the first time the
		// dashboard closes. See
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422258#c3
		Platform.setImplicitExit(false);
		final FXCanvas fxCanvas = new FXCanvas(this, SWT.NONE);
		fxCanvas.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).create());
		fxCanvas.setLayout(GridLayoutFactory.fillDefaults().create());
		browser = new WebView();
		browser.setVisible(false);
		BorderPane border = new BorderPane();
		Scene scene = new Scene(border);
		border.setCenter(browser);
		fxCanvas.setScene(scene);
		if (showURLbar) {
			updateHistory();
		}
		if (showToolbar) {
			updateBackNextBusy();
		}
		addBrowserListeners();
		if (initialUrl != null) {
			browser.getEngine().load(initialUrl);
		}
	}

	/**
	 * Returns the underlying SWT browser widget.
	 *
	 * @return the underlying browser
	 */
	public WebView getBrowser() {
		return browser;
	}

	public String getHomeUrl() {
		return homeUrl;
	}

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}

	/**
	 * Navigate to the home URL.
	 */
	public void goHome() {
		if (homeUrl != null) {
			browser.getEngine().load(homeUrl);
		}
		else {
			browser.getEngine().load("");
		}
	}

	/**
	 * Loads a URL.
	 *
	 * @param url the URL to be loaded
	 * @return true if the operation was successful and false otherwise.
	 * @exception IllegalArgumentException <ul>
	 * <li>ERROR_NULL_ARGUMENT - if the url is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 * <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
	 * <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 * </ul>
	 * @see #getURL()
	 */
	public void setURL(String url) {
		setURL(url, true);
	}

	protected void updateBackNextBusy() {
		if (!back.isDisposed()) {
			back.setEnabled(isBackEnabled());
		}
		if (!forward.isDisposed()) {
			forward.setEnabled(isForwardEnabled());
		}
		if (!busy.isDisposed()) {
			busy.setBusy(loading);
		}

		if (backNextListener != null) {
			backNextListener.updateBackNextBusy();
		}
	}

	/**
     *
     */
	private void addBrowserListeners() {
		if (browser == null) {
			return;
		}

		// Add listener for new window creation so that we can instead of
		// opening a separate
		// new window in which the session is lost, we can instead open a new
		// window in a new
		// shell within the browser area thereby maintaining the session.
		browser.getEngine().setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
			@Override
			public WebEngine call(PopupFeatures config) {
				Shell shell2 = new Shell(getShell(), SWT.SHELL_TRIM);
				shell2.setLayout(new FillLayout());
				shell2.setText(Messages.viewWebBrowserTitle);
				shell2.setImage(getShell().getImage());
				int style = 0;
				if (showURLbar) {
					style += LOCATION_BAR;
				}
				if (showToolbar) {
					style += BUTTON_BAR;
				}
				JavaFxBrowserViewer browser2 = new JavaFxBrowserViewer(shell2, style);
				browser2.setVisible(true);
				browser2.newWindow = true;
				return browser2.getBrowser().getEngine();
			}
		});

		browser.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			@Override
			public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
				int progressWorked = (int) (browser.getEngine().getLoadWorker().getProgress() * 100.0);
				boolean done = newState == State.SUCCEEDED || newState == State.FAILED;
				if (container != null) {
					IProgressMonitor monitor = container.getActionBars().getStatusLineManager().getProgressMonitor();
					if (done) {
						monitor.done();
					}
					else if (progressWorked == 0) {
						monitor.beginTask("", 100); //$NON-NLS-1$
					}
					else {
						monitor.worked(progressWorked);
					}
				}

				if (showToolbar) {
					if (!busy.isBusy() && !done) {
						loading = true;
					}
					else if (busy.isBusy() && done) {
						loading = false;
					}
					updateBackNextBusy();
					updateHistory();
				}

			}
		});

		if (showURLbar) {
			browser.getEngine().locationProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (combo != null) {
						if (!"about:blank".equals(newValue)) { //$NON-NLS-1$
							combo.setText(newValue);
							addToHistory(newValue);
							updateHistory();
						}
					}
				}
			});

			browser.getEngine().titleProperty().addListener(new ChangeListener<String>() {
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					firePropertyChangeEvent(PROPERTY_TITLE, oldValue, newValue);
				}
			});
		}
	}

	/**
	 * Add a property change listener to this instance.
	 *
	 * @param listener java.beans.PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (propertyListeners == null) {
			propertyListeners = new ArrayList<PropertyChangeListener>();
		}
		propertyListeners.add(listener);
	}

	/**
	 * Remove a property change listener from this instance.
	 *
	 * @param listener java.beans.PropertyChangeListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (propertyListeners != null) {
			propertyListeners.remove(listener);
		}
	}

	/**
	 * Fire a property change event.
	 */
	protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
		if (propertyListeners == null) {
			return;
		}

		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		try {
			int size = propertyListeners.size();
			PropertyChangeListener[] pcl = new PropertyChangeListener[size];
			propertyListeners.toArray(pcl);

			for (int i = 0; i < size; i++) {
				try {
					pcl[i].propertyChange(event);
				}
				catch (Exception e) {
					// ignore
				}
			}
		}
		catch (Exception e) {
			// ignore
		}
	}

	/**
	 * Navigate to the next session history item. Convenience method that calls
	 * the underlying SWT browser.
	 *
	 * @return <code>true</code> if the operation was successful and
	 * <code>false</code> otherwise
	 * @exception SWTException <ul>
	 * <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
	 * <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 * </ul>
	 * @see #back
	 */
	public boolean forward() {
		if (isForwardEnabled()) {
			browser.getEngine().getHistory().go(1);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Navigate to the previous session history item. Convenience method that
	 * calls the underlying SWT browser.
	 *
	 * @return <code>true</code> if the operation was successful and
	 * <code>false</code> otherwise
	 * @exception SWTException <ul>
	 * <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
	 * <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 * </ul>
	 * @see #forward
	 */
	public boolean back() {
		if (isBackEnabled()) {
			browser.getEngine().getHistory().go(-1);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns <code>true</code> if the receiver can navigate to the previous
	 * session history item, and <code>false</code> otherwise. Convenience
	 * method that calls the underlying SWT browser.
	 *
	 * @return the receiver's back command enabled state
	 * @exception SWTException <ul>
	 * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
	 * created the receiver</li>
	 * </ul>
	 * @see #back
	 */
	public boolean isBackEnabled() {
		return browser.getEngine().getHistory().getCurrentIndex() > 0;
	}

	/**
	 * Returns <code>true</code> if the receiver can navigate to the next
	 * session history item, and <code>false</code> otherwise. Convenience
	 * method that calls the underlying SWT browser.
	 *
	 * @return the receiver's forward command enabled state
	 * @exception SWTException <ul>
	 * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
	 * created the receiver</li>
	 * </ul>
	 * @see #forward
	 */
	public boolean isForwardEnabled() {
		return browser.getEngine().getHistory().getCurrentIndex() < browser.getEngine().getHistory().getEntries()
				.size() - 1;
	}

	/**
	 * Stop any loading and rendering activity. Convenience method that calls
	 * the underlying SWT browser.
	 *
	 * @exception SWTException <ul>
	 * <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
	 * <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 * </ul>
	 */
	public void stop() {
		if (browser != null) {
			browser.getEngine().getLoadWorker().cancel();
		}
	}

	/**
	 * Refresh the current page. Convenience method that calls the underlying
	 * SWT browser.
	 *
	 * @exception SWTException <ul>
	 * <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
	 * <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 * </ul>
	 */
	public void refresh() {
		if (browser != null) {
			browser.getEngine().reload();
			try {
				Thread.sleep(50);
			}
			catch (Exception e) {
				// ignore
			}
		}
	}

	private void setURL(String url, boolean browse) {
		if (initialUrl == null) {
			this.initialUrl = url;
		}
		if (url == null) {
			goHome();
			return;
		}

		if ("eclipse".equalsIgnoreCase(url)) {
			url = "http://www.eclipse.org"; //$NON-NLS-1$
		}
		else if ("wtp".equalsIgnoreCase(url)) {
			url = "http://www.eclipse.org/webtools/"; //$NON-NLS-1$
		}

		if (browse) {
			if (url != null && url.equals(getURL())) {
				refresh();
			}
			else {
				if (browser != null) {
					browser.getEngine().load(url);
					addToHistory(url);
					updateHistory();
				}
			}
		}
	}

	protected void addToHistory(String url) {
		if (history == null) {
			history = WebBrowserPreference.getInternalWebBrowserHistory();
		}
		int found = -1;
		int size = history.size();
		for (int i = 0; i < size; i++) {
			String s = history.get(i);
			if (s.equals(url)) {
				found = i;
				break;
			}
		}

		if (found == -1) {
			if (size >= MAX_HISTORY) {
				history.remove(size - 1);
			}
			history.add(0, url);
			WebBrowserPreference.setInternalWebBrowserHistory(history);
		}
		else if (found != 0) {
			history.remove(found);
			history.add(0, url);
			WebBrowserPreference.setInternalWebBrowserHistory(history);
		}
	}

	/**
     *
     */
	@Override
	public void dispose() {
		super.dispose();

		showToolbar = false;

		if (busy != null) {
			busy.dispose();
		}
		busy = null;

		if (clipboard != null) {
			clipboard.dispose();
		}
		clipboard = null;
	}

	protected ToolBar createLocationBar(Composite parent) {
		combo = new Combo(parent, SWT.DROP_DOWN);

		updateHistory();

		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent we) {
				try {
					if (combo.getSelectionIndex() != -1 && !combo.getListVisible()) {
						setURL(combo.getItem(combo.getSelectionIndex()));
					}
				}
				catch (Exception e) {
					// ignore
				}
			}
		});
		combo.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				setURL(combo.getText());
			}
		});

		ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

		ToolItem go = new ToolItem(toolbar, SWT.NONE);
		go.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_GO));
		go.setHotImage(ImageResource.getImage(ImageResource.IMG_CLCL_NAV_GO));
		go.setDisabledImage(ImageResource.getImage(ImageResource.IMG_DLCL_NAV_GO));
		go.setToolTipText(Messages.actionWebBrowserGo);
		go.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setURL(combo.getText());
			}
		});

		return toolbar;
	}

	protected ToolBar createToolbar(Composite parent) {
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

		// create 'home' action
		ToolItem home = new ToolItem(toolbar, SWT.NONE);
		home.setImage(Images.getImage(Images.IMG_NAV_HOME));
		home.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goHome();
			}
		});

		// create back and forward actions
		back = new ToolItem(toolbar, SWT.NONE);
		back.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_BACKWARD));
		back.setHotImage(ImageResource.getImage(ImageResource.IMG_CLCL_NAV_BACKWARD));
		back.setDisabledImage(ImageResource.getImage(ImageResource.IMG_DLCL_NAV_BACKWARD));
		back.setToolTipText(Messages.actionWebBrowserBack);
		back.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				back();
			}
		});

		forward = new ToolItem(toolbar, SWT.NONE);
		forward.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_FORWARD));
		forward.setHotImage(ImageResource.getImage(ImageResource.IMG_CLCL_NAV_FORWARD));
		forward.setDisabledImage(ImageResource.getImage(ImageResource.IMG_DLCL_NAV_FORWARD));
		forward.setToolTipText(Messages.actionWebBrowserForward);
		forward.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				forward();
			}
		});

		// create refresh, stop, and print actions
		ToolItem stop = new ToolItem(toolbar, SWT.NONE);
		stop.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_STOP));
		stop.setHotImage(ImageResource.getImage(ImageResource.IMG_CLCL_NAV_STOP));
		stop.setDisabledImage(ImageResource.getImage(ImageResource.IMG_DLCL_NAV_STOP));
		stop.setToolTipText(Messages.actionWebBrowserStop);
		stop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				stop();
			}
		});

		ToolItem refresh = new ToolItem(toolbar, SWT.NONE);
		refresh.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_REFRESH));
		refresh.setHotImage(ImageResource.getImage(ImageResource.IMG_CLCL_NAV_REFRESH));
		refresh.setDisabledImage(ImageResource.getImage(ImageResource.IMG_DLCL_NAV_REFRESH));
		refresh.setToolTipText(Messages.actionWebBrowserRefresh);
		refresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				refresh();
			}
		});

		return toolbar;
	}

	/**
	 * Returns the current URL. Convenience method that calls the underlying SWT
	 * browser.
	 *
	 * @return the current URL or an empty <code>String</code> if there is no
	 * current URL
	 * @exception SWTException <ul>
	 * <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
	 * <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 * </ul>
	 * @see #setURL(String)
	 */
	public String getURL() {
		if (browser != null) {
			return browser.getEngine().locationProperty().get();
		}
		return initialUrl;
	}

	@Override
	public boolean setFocus() {
		if (browser != null) {
			browser.requestFocus();
			updateHistory();
			return true;
		}
		return super.setFocus();
	}

	/**
	 * Update the history list to the global/shared copy.
	 */
	protected void updateHistory() {
		if (combo == null || combo.isDisposed()) {
			return;
		}

		String temp = combo.getText();
		if (history == null) {
			history = WebBrowserPreference.getInternalWebBrowserHistory();
		}

		String[] historyList = new String[history.size()];
		history.toArray(historyList);
		combo.setItems(historyList);

		combo.setText(temp);
	}

	public IBrowserViewerContainer getContainer() {
		return container;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		browser.setVisible(true);
	}

	public void setContainer(IBrowserViewerContainer container) {
		if (container == null && this.container != null) {
			IStatusLineManager manager = this.container.getActionBars().getStatusLineManager();
			if (manager != null) {
				manager.getProgressMonitor().done();
			}
		}
		this.container = container;
	}
}
