/*******************************************************************************
 *  Copyright (c) 2013 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.ui.actions.ShowDashboardPageAction;

/**
 * Responsible for closing dashboard and reopening it when the 'use old dashboard' 
 * preference is changed.
 * 
 * @author Kris De Volder
 */
public class DashboardReopener implements IPropertyChangeListener {

	private static DashboardReopener instance;
	
	private static final String[] EDITOR_IDS = {
		MultiPageDashboardEditor.EDITOR_ID,
		MultiPageDashboardEditor.NEW_EDITOR_ID
	};

	public static synchronized void ensure() {
		if (instance==null) {
			instance = new DashboardReopener();
		}
	}

	/**
	 * Singleton created by the 'ensure' method.
	 */
	private DashboardReopener() {
		IdeUiPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	public void propertyChange(final PropertyChangeEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow targetWindow = null;
				IWorkbenchPage targetPage = null;
				if (event.getProperty().equals(IIdeUiConstants.PREF_USE_OLD_DASHOARD)) {
					for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
					    for (IWorkbenchPage page : window.getPages()) {
					        for (IEditorReference editor : page.getEditorReferences()) {
					        	String eid = editor.getId();
					        	for (String interestingId : EDITOR_IDS) {
									if (interestingId.equals(eid)) {
										if (targetPage==null) {
											//if multiple dash open on differnt workbench pages then we only remember one page.
											// so we won't reopen more than one dashboard.
											targetPage = page;
											targetWindow = window;
										}
										page.closeEditors(new IEditorReference[] {editor}, false);
									}
								}
					        }
					    }
					}
				}
				//targetPage is not null if a dahsboard editor was open before so reopen it only in that case.
				if (targetPage!=null) {
					ShowDashboardPageAction.run(targetWindow, null);
				}
			}
		});
	}
	
}
