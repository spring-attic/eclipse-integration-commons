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
package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardEditorInput;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.MultiPageDashboardEditor;
import org.springsource.ide.eclipse.dashboard.ui.actions.IDashboardWithPages;

public class ShowDashboardCommand extends AbstractHandler {

	//TODO: this class is not used? Remove? (using action sets in old dashboard.ui plugin)
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchWindow workbench = HandlerUtil.getActiveWorkbenchWindow(event);
			if (workbench!=null) {
				IWorkbenchPage page = workbench.getActivePage();
				if (page!=null) {
					IEditorPart editor = page.openEditor(DashboardEditorInput.INSTANCE, MultiPageDashboardEditor.NEW_EDITOR_ID);
					if (editor instanceof IDashboardWithPages) {
						IDashboardWithPages dashboard = (IDashboardWithPages) editor;
						//dashboard.setActivePage(pageId);
					}
				}
			}
		} catch (Exception e) {
			throw new ExecutionException("Couldn't open dashboard", e);
		}
		return null;
	}

}
