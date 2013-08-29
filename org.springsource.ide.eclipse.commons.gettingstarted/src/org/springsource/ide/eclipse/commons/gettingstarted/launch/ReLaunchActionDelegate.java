/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.launch;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.commands.ICommandService;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;


public class ReLaunchActionDelegate implements IWorkbenchWindowPulldownDelegate {

	private IWorkbenchWindow window;
	private Menu menu;
	
	ICommandService comandService = null;

	private ProcessTracker processes = new ProcessTracker() {
		protected void changed() {
			if (action!=null) {
				//We may not be in the UIThread here. So take care before futzing with the widgets!
				window.getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						update();
					}
				});
			}
		}
	};

	private IAction action;
	
	@Override
	public void run(IAction action) {
		this.action = action;
		IProcess p = processes.getLast();
		if (p!=null) {
			try {
				LaunchUtils.terminateAndRelaunch(p.getLaunch());
			} catch (DebugException e) {
				GettingStartedActivator.log(e);
				MessageDialog.openError(window.getShell(), "Error relaunching", 
						ExceptionUtil.getMessage(e)
				);
			}
		} else {
			MessageDialog.openError(window.getShell(), "No Processes Found", 
					"Couldn't relaunch: no active processes"
			);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		update();
	}

	private void update() {
		IProcess p = processes.getLast();
		String label = "Relaunch";
		if (p!=null) {
			label = label + " " + p.getLaunch().getLaunchConfiguration().getName();
		}
		action.setText(label);
		action.setToolTipText(label);
	}

	@Override
	public void dispose() {
		if (menu!=null) {
			menu.dispose();
			menu = null;
		}
		if (processes!=null) {
			processes.dispose();
			processes = null;
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public Menu getMenu(Control parent) {
		if (menu!=null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		fillMenu();
		return menu;
	}
	
//	/**
//	 * Adds the given action to the specified menu.
//	 */
//	protected void addToMenu(Menu menu, IAction action) {
//		StringBuffer label= new StringBuffer();
////		if (accelerator >= 0 && accelerator < 10) {
////			//add the numerical accelerator
////			label.append('&');
////			label.append(accelerator);
////			label.append(' ');
////		}
//		label.append(action.getText());
//		action.setText(label.toString());
//		ActionContributionItem item= new ActionContributionItem(action);
//		item.fill(menu, -1);
//	}

	
	private void fillMenu() {
		new RelaunchMenuProvider(processes).fill(menu, -1);
	}

}
