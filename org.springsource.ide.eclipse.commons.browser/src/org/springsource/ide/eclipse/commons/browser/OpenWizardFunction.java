/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0
 * (https://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution
 * License v1.0 (https://www.eclipse.org/org/documents/edl-v10.html).
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.browser;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

public class OpenWizardFunction implements IBrowserToEclipseFunction {

	@Override
	public void call(String wizardId) {
		Object object;
		try {
			IConfigurationElement element = BrowserExtensions.getExtension(BrowserExtensions.EXTENSION_ID_NEW_WIZARD,
					wizardId);
			object = WorkbenchPlugin.createExtension(element, BrowserExtensions.ELEMENT_CLASS);
		}
		catch (CoreException ex) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID, "Could not read dashboard extension", ex));
			return;
		}
		if (!(object instanceof INewWizard)) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID, "Could not load "
					+ object.getClass().getCanonicalName() + " must implement " + INewWizard.class.getCanonicalName()));
			return;
		}

		INewWizard wizard = (INewWizard) object;
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}

}