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
package org.springsource.ide.eclipse.commons.browser.swt;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.springsource.ide.eclipse.commons.browser.BrowserExtensions;
import org.springsource.ide.eclipse.commons.browser.BrowserPlugin;
import org.springsource.ide.eclipse.commons.browser.IBrowserToEclipseFunction;
import org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunction;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 *
 * @author Miles Parker
 */
public class StsBrowserManager {

	private boolean disposed;

	private static final boolean DEBUG = false;

	private Collection<IEclipseToBrowserFunction> onLoadFunctions;

	private String currentUrl;

	private Browser browser;

	private final ObjectMapper mapper = new ObjectMapper();

	private BrowserFunction browser_function;

	public void setClient(Browser browser) {
		this.browser = browser;
		browser.execute(
				"window.ide = {\n" +
				"	call: function () {\n" +
				"		return ide_call.apply(this, arguments);\n" +
				"	}\n" +
				"}"
		);
		if (browser_function==null) {
			browser_function = new BrowserFunction(browser, "ide_call") {
				@Override
				public Object function(Object[] arguments) {
					call((String)arguments[0], (String)arguments[1]);
					return false;
				}
			};
		}
		onLoadFunctions = new ArrayList<IEclipseToBrowserFunction>();
		String oldUrl = currentUrl;
		currentUrl = browser.getUrl();
//		System.out.println("oldUrl: "+oldUrl);
//		System.out.println("newUrl: "+currentUrl);
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
	 * Handle calls <i>from</i> Javascript functions on the browser.
	 *
	 * @param functionId
	 * @param argument
	 */
	private void call(String functionId, String argument) {
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
		Display.getDefault().asyncExec(() -> {
			IEclipseToBrowserFunction provider = function;
			if (provider!=null) {
				String fname = provider.getFunctionName();
				try {
					Object[] fargs = provider.getArguments();
					//System.out.println("doCall "+fname+" "+Arrays.asList(fargs));
					String code = "window."+fname+serializeArguments(fargs);
					boolean success = browser.execute(code);
					//System.out.println("doCall("+fname+") => "+success);
					if (!success) {
						//Can't seem to get rid of this error with 'window.addHtml' (we tried quite a bit but nothing seems to work).
						//The error seems to do no real harm... so let's just hide it...
						if (!code.contains("window.addHtml")) {
							throw new ExecutionException("Problems executing script: '"+code+"'");
						}
					}
				} catch (Exception e) {
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID,
									"Could not call browser function extension: " + fname, e
							)
					);
				}
			}
			if (DEBUG) {
				printPageHtml();
			}
		});
	}

	private String serializeArguments(Object[] fargs) throws JsonGenerationException, JsonMappingException, IOException {
		StringWriter serialized = new StringWriter();
		boolean first = true;
		for (Object arg : fargs) {
			serialized.write(first ? "(" : ", ");
			mapper.writeValue(serialized, arg);
			first = false;
		}
		serialized.write(")");
		return serialized.toString();
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
		Display.getDefault().asyncExec(() -> {
			System.out.println(browser.getText());
		});
	}

	public void dispose() {
		disposed = true;
		for (IEclipseToBrowserFunction function : onLoadFunctions) {
			function.dispose();
		}
		if (browser_function!=null) {
			browser_function.dispose();
			browser_function = null;
		}
	}

}
