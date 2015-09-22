/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.springsource.ide.eclipse.commons.browser.BrowserExtensions;
import org.springsource.ide.eclipse.commons.browser.BrowserPlugin;
import org.springsource.ide.eclipse.commons.browser.IBrowserToEclipseFunction;
import org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunction;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 *
 * @author Miles Parker
 */
public class JavaFxBrowserManager {

	private WebEngine engine;

	private WebView view;

	private boolean disposed;

	private static final boolean DEBUG = false;

	private Collection<IEclipseToBrowserFunction> onLoadFunctions;

	private String currentUrl;

	public void setClient(WebView view) {
		this.view = view;
		this.engine = view.getEngine();
		JSObject window = (JSObject) engine.executeScript("window");
		window.setMember("ide", this);
		onLoadFunctions = new ArrayList<IEclipseToBrowserFunction>();
		currentUrl = view.getEngine().locationProperty().get();
		//Need to remove any query parameters that might break pattern matching for extensions
		currentUrl = StringUtils.substringBeforeLast(currentUrl, "?");
		currentUrl = StringUtils.substringBeforeLast(currentUrl, "&");
		loadInitialFunctions();
	}

	private void loadInitialFunctions() {
		IConfigurationElement[] extensions = BrowserExtensions.getExtensions(
				BrowserExtensions.EXTENSION_ID_ECLIPSE_TO_BROWSER, null, currentUrl);
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
						new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID,
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
					BrowserExtensions.EXTENSION_ID_BROWSER_TO_ECLIPSE, functionId, currentUrl);
			if (element != null) {
				IBrowserToEclipseFunction function = (IBrowserToEclipseFunction) WorkbenchPlugin.createExtension(
						element, BrowserExtensions.ELEMENT_CLASS);
				function.call(argument);
			}
			else {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID,
								"Could not instantiate browser function extension: " + functionId));
			}
		}
		catch (CoreException ex) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID, "Could not find dashboard extension", ex));
			return;
		}
	}

	private void doCall(final IEclipseToBrowserFunction function) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				JSObject js = (JSObject) getEngine().executeScript("window");
				IEclipseToBrowserFunction provider = function;
				if (provider!=null) {
					String fname = provider.getFunctionName();
					Object[] fargs = provider.getArguments();
					js.call(fname, fargs);
				}
				getView().requestLayout();
				getView().setVisible(true);
				if (DEBUG) {
					printPageHtml();
				}
			}
		});
	}

	/**
	 * Calls Javascript functions <i>to</i> the browser, refreshing the browser
	 * after each call
	 */
	public void callOnBrowser(final Collection<IEclipseToBrowserFunction> functions) {
		final Collection<IEclipseToBrowserFunction> waitingFunctions = new CopyOnWriteArrayList<IEclipseToBrowserFunction>();
		IEclipseToBrowserFunction.Callback callback = new IEclipseToBrowserFunction.Callback() {
			@Override
			public void ready(final IEclipseToBrowserFunction function) {
				if (waitingFunctions.remove(function)) {
					if (!disposed) {
						doCall(function);
					}
				}
			}
		};
		for (IEclipseToBrowserFunction function : functions) {
			if (!function.isReady()) {
				waitingFunctions.add(function);
				function.setCallback(callback);
			} else {
				doCall(function);
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
		for (IEclipseToBrowserFunction function : onLoadFunctions) {
			function.dispose();
		}
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
