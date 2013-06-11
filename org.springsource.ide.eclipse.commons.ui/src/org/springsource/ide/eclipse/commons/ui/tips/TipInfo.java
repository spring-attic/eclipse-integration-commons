/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.tips;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.keys.IBindingService;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;

/**
 * 
 * @author Andrew Eisenberg
 * @since 3.3.0
 */
class TipInfo {
	public static final String COMMAND = "command:";

	public static final String PREF = "pref:";

	public TipInfo(String infoText, String linkText) {
		this.linkText = linkText;
		this.infoText = infoText;
		this.keyBindingId = null;
	}

	public TipInfo(String infoText, String linkText, String keyBindingId) {
		this.linkText = linkText;
		this.infoText = infoText;
		this.keyBindingId = keyBindingId;
	}

	final String linkText;

	final String infoText;

	final String keyBindingId;

	public String getKeyBindingText(IWorkbenchWindow activeWindow) {
		if (this.keyBindingId != null) {
			IBindingService bindingService = (IBindingService) activeWindow.getService(IBindingService.class);
			Binding[] bindings = bindingService.getBindings();
			KeySequence keySequence = null;
			for (Binding binding : bindings) {
				if (binding instanceof KeyBinding && binding.getParameterizedCommand() != null
						&& binding.getParameterizedCommand().getId().equals(this.keyBindingId)) {
					keySequence = ((KeyBinding) binding).getKeySequence();
					break;
				}
			}
			if (keySequence != null) {
				return " Key binding: " + keySequence;
			}
		}
		return null;
	}

	public void invokeAction(String text, IWorkbenchWindow activeWindow) {
		if (text.startsWith(TipInfo.PREF)) {
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(activeWindow.getShell(),
					text.substring(TipInfo.PREF.length()), null, null);
			dialog.open();
		}
		else if (text.startsWith(TipInfo.COMMAND)) {
			String commandIdString = text.substring(TipInfo.COMMAND.length());
			if (activeWindow != null) {
				ICommandService commandService = (ICommandService) activeWindow.getService(ICommandService.class);
				try {
					Command command = commandService.getCommand(commandIdString);
					command.executeWithChecks(new ExecutionEvent());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			openUrl(text);
		}
	}

	public static void openUrl(String location) {
		try {
			URL url = null;

			if (location != null) {
				url = new URL(location);
			}
			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(url);
				}
				catch (Exception e) {
					CorePlugin.log(e);
				}
			}
			else {
				IWebBrowser browser = null;
				int flags = 0;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags |= IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}
				else {
					flags |= IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, UiPlugin.PLUGIN_ID, null, null);
				browser.openURL(url);
			}
		}
		catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Browser initialization error",
					"Browser could not be initiated");
		}
		catch (MalformedURLException e) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Malformed URL", location);
		}
	}

}