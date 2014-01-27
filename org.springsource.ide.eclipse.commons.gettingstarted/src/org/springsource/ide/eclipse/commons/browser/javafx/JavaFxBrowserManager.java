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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import netscape.javascript.JSObject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.springsource.ide.eclipse.commons.browser.BrowserExtensions;
import org.springsource.ide.eclipse.commons.browser.IBrowserToEclipseFunction;
import org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunction;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;

/**
 * 
 * @author Miles Parker
 */
public class JavaFxBrowserManager {

	private WebEngine engine;

	private WebView view;

	private boolean disposed;

	private static final boolean DEBUG = false;

	public void setClient(WebView view) {
		this.view = view;
		this.engine = view.getEngine();
		JSObject window = (JSObject) engine.executeScript("window");
		window.setMember("ide", this);
		Collection<IEclipseToBrowserFunction> onLoadFunctions = new ArrayList<IEclipseToBrowserFunction>();
		IConfigurationElement[] extensions = BrowserExtensions.getExtensions(
				BrowserExtensions.EXTENSION_ID_ECLIPSE_TO_BROWSER, null, view.getEngine().locationProperty().get());
		for (IConfigurationElement element : extensions) {
			try {
				String onLoad = element.getAttribute(BrowserExtensions.ELEMENT_ONLOAD);
				if (onLoad != null && onLoad.equals("true")) {
					onLoadFunctions.add((IEclipseToBrowserFunction) WorkbenchPlugin.createExtension(element,
							BrowserExtensions.ELEMENT_CLASS));
				}
			}
			catch (CoreException ex) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
								"Could not instantiate browser element provider extension.", ex));
				return;
			}
		}
		callOnBrowser(onLoadFunctions);
	}

	/**
	 * Handle calls <i>from</i> Javascript functions on the browser. (This is
	 * called by reflection by JavaFx so there won't be any apparent usages for
	 * this method.)
	 * @param functionId
	 * @param argument
	 */
	public void call(String functionId, String argument) {
		try {
			IConfigurationElement element = BrowserExtensions.getExtension(
					BrowserExtensions.EXTENSION_ID_BROWSER_TO_ECLIPSE, functionId, view.getEngine().locationProperty()
							.get());
			if (element != null) {
				IBrowserToEclipseFunction function = (IBrowserToEclipseFunction) WorkbenchPlugin.createExtension(
						element, BrowserExtensions.ELEMENT_CLASS);
				function.call(argument);
			}
			else {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
								"Could not instantiate browser function extension: " + functionId));
			}
		}
		catch (CoreException ex) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not find dashboard extension", ex));
			return;
		}
	}

	/**
	 * Calls Javascript functions <i>to</i> the browser, refreshing the browser
	 * when all calls have completed.
	 * @param functions
	 */
	public void callOnBrowser(final Collection<IEclipseToBrowserFunction> functions) {
		final Collection<IEclipseToBrowserFunction> waitingFunctions = new CopyOnWriteArrayList<IEclipseToBrowserFunction>();
		IEclipseToBrowserFunction.Callback callback = new IEclipseToBrowserFunction.Callback() {
			@Override
			public void ready(IEclipseToBrowserFunction function) {
				waitingFunctions.remove(function);
				if (waitingFunctions.isEmpty() && !disposed) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							JSObject js = (JSObject) getEngine().executeScript("window");
							for (IEclipseToBrowserFunction provider : functions) {
								js.call(provider.getFunctionName(), (Object[]) provider.getArguments());
							}
							getView().requestLayout();
							getView().setVisible(true);
							if (DEBUG) {
								printPageHtml();
							}
						}
					});
				}
			}
		};
		for (IEclipseToBrowserFunction function : functions) {
			if (!function.isReady()) {
				waitingFunctions.add(function);
				function.setCallback(callback);
			}
		}
		callback.ready(null);
	}

	/**
	 * For debugging..
	 */
	private void printPageHtml() {
		StringWriter sw = new StringWriter();
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(getView().getEngine().getDocument()), new StreamResult(sw));
			System.out.println(sw.toString());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void dispose() {
		disposed = true;
	}

	/**
	 * @return the view
	 */
	public WebView getView() {
		return view;
	}

	/**
	 * @return the engine
	 */
	public WebEngine getEngine() {
		return engine;
	}
}
