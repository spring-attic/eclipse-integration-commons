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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;

public class OpenEditorFunction implements IBrowserToEclipseFunction {

	public final static NullEditorInput NULL_EDITOR = new NullEditorInput();

	@Override
	public void call(String editorId) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(NULL_EDITOR, editorId);
		}
		catch (PartInitException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID,
					"Could not find editor extension " + editorId, e));
		}
	}
}