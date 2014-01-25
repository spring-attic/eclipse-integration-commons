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

package org.springsource.ide.eclipse.commons.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;

public class OpenJavaFxBrowserFunction implements IBrowserToEclipseFunction {

	@Override
	public void call(String url) {
		try {
			WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(url));
			PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.openEditor(input,
							GettingStartedActivator.JAVAFX_BROWSER_EDITOR_ID);
		} catch (MalformedURLException e) {
			StatusHandler.log(new Status(IStatus.ERROR, GettingStartedActivator.PLUGIN_ID,
					"Bad page url: " + url, e));
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, GettingStartedActivator.PLUGIN_ID,
					"Could not find brwoser editor extension", e));
		}
	}
}