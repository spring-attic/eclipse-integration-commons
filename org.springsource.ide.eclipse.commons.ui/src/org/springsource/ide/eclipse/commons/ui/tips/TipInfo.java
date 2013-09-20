/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.tips;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.keys.IBindingService;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

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
			UiUtil.openUrl(text);
		}
	}

}
