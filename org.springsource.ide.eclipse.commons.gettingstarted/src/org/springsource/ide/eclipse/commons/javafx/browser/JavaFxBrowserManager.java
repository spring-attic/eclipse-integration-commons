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
package org.springsource.ide.eclipse.commons.javafx.browser;

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import netscape.javascript.JSObject;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.springsource.ide.eclipse.commons.browser.BrowserUtils;
import org.springsource.ide.eclipse.commons.browser.IBrowserFunction;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.UpdateNotification;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.IFeedListener;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * 
 * @author Miles Parker
 */
public class JavaFxBrowserManager {

	public static final String EXTENSION_ID_NEW_WIZARD = "org.eclipse.ui.newWizards";

	public static final String EXTENSION_ID_DASHBOARD_FUNCTION = "org.springsource.ide.common.dashboard.browser.function";

	private WebEngine engine;

	private WebView view;

	public void setClient(WebView view) {
		this.view = view;
		this.engine = view.getEngine();
		JSObject window = (JSObject) engine.executeScript("window");
		window.setMember("ide", this);
	}

	public void call(String functionId, String argument) {
		try {
			IConfigurationElement element = BrowserUtils.getExtension(
					EXTENSION_ID_DASHBOARD_FUNCTION, functionId);
			if (element != null) {
				IBrowserFunction function = (IBrowserFunction) WorkbenchPlugin
						.createExtension(element, BrowserUtils.ELEMENT_CLASS);
				function.call(argument);
			} else {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
						"Could not find dashboard extension: " + functionId));
			}
		} catch (CoreException ex) {
			StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not find dashboard extension", ex));
			return;
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
