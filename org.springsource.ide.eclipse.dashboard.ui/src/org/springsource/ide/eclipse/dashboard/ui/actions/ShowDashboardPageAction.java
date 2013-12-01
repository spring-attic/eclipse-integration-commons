/*******************************************************************************
 * Copyright (c) 2012 - 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.ui.actions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardEditorInput;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardMainPage;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.MultiPageDashboardEditor;

/**
 * Displays the dashboard.
 * <p>
 * Note: Must not have any dependencies on org.eclipse.mylyn.tasks.ui to avoid
 * class loader warnings.
 * @author Terry Denney
 */
public class ShowDashboardPageAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	private String pageId;

	public ShowDashboardPageAction(String pageId) {
		this.pageId = pageId;
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		run(this.window, pageId);
	}

	public static void run(final IWorkbenchWindow window, final String _pageId) {
		final String pageId = _pageId==null ? DashboardMainPage.PAGE_ID : _pageId;
		UIJob job = new UIJob("Show Dashboard") {
			@Override
			public IStatus runInUIThread(IProgressMonitor mon) {
				mon.beginTask("Show Dashboard", 2);
				try {
					IIntroManager introMgr = window.getWorkbench().getIntroManager();
					IIntroPart intro = introMgr.getIntro();
					if (intro != null) {
						introMgr.closeIntro(intro);
					}
					IWorkbenchPage page = window.getActivePage();
					try {
						try {
							if (IdeUiPlugin.getDefault().useNewDashboard(new SubProgressMonitor(mon, 1))) {
								IEditorPart editor = page.openEditor(DashboardEditorInput.INSTANCE, MultiPageDashboardEditor.NEW_EDITOR_ID);
								if (editor instanceof IDashboardWithPages) {
									IDashboardWithPages dashboard = (IDashboardWithPages) editor;
									dashboard.setActivePage(pageId);
								}
								return Status.OK_STATUS;
							}
						}
						catch (Throwable e) {
							IdeUiPlugin.log(e);
						}
						// Using new dashboard is disabled or failed... use the old one
						FormEditor editor = (FormEditor) page.openEditor(DashboardEditorInput.INSTANCE,
								MultiPageDashboardEditor.EDITOR_ID);
						editor.setActivePage(pageId);
					}
					catch (PartInitException e) {
						IdeUiPlugin.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not open dashboard", e));
					}
				} finally {
					mon.done();
				}
				
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Optionally set the page of the dashboard to be opened.
	 * @param pageId a unique constant defined in the page class
	 */
	public void setEditorPageId(String pageId) {
		this.pageId = pageId;
	}

}
