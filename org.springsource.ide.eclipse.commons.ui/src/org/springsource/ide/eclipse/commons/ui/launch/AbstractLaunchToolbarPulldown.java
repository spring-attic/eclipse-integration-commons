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
package org.springsource.ide.eclipse.commons.ui.launch;

import java.util.ArrayList;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * Abstract superclass for an launch toolbar button with pulldown
 * that applies some operation to launchConfiguration from some historic list of
 * launch configs.
 *
 * @author Kris De Volder
 */
public abstract class AbstractLaunchToolbarPulldown implements IWorkbenchWindowPulldownDelegate {

	private IWorkbenchWindow window;
	private Menu menu;
	private LaunchList.Listener launchListener;
	private LaunchList launches = createList().addListener(launchListener = new LaunchList.Listener() {
		public void changed() {
			if (action!=null && window!=null) {
				Shell shell = window.getShell();
				if (shell!=null) {
					//We may not be in the UIThread here. So take care before futzing with the widgets!
					Display display = shell.getDisplay();
					if (display!=null) {
						display.asyncExec(new Runnable() {
							public void run() {
								update();
							}
						});
					}
				}
			}
		}
	});
	private IAction action;

	/**
	 * FActory method to create or obtain an instance that keeps track of the launches to be
	 * shown in the pulldown menu.
	 */
	protected abstract LaunchList createList();

	@Override
	public void run(IAction action) {
		this.action = action;
		ILaunchConfiguration l = launches.getLast();
		if (l!=null) {
			try {
				performOperation(l);
			} catch (DebugException e) {
				CorePlugin.log(e);
				MessageDialog.openError(window.getShell(), "Error relaunching",
						ExceptionUtil.getMessage(e)
				);
			}
		} else {
			MessageDialog.openError(window.getShell(), "No Processes Found",
					"Couldn't "+getOperationName()+": no active processes"
			);
		}
	}

	protected abstract String getOperationName();

	protected abstract void performOperation(ILaunchConfiguration l) throws DebugException;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		update();
	}

	private void update() {
		if (action!=null) {
			ILaunchConfiguration launch = launches.getLast();
			String label = getOperationName();
			if (launch!=null) {
				label = label + " " + launch.getName();
			}
			action.setText(label);
			action.setToolTipText(label);
			action.setEnabled(launch!=null);
		}
	}

	@Override
	public void dispose() {
		if (menu!=null) {
			menu.dispose();
			menu = null;
		}
		if (launches!=null && launchListener!=null) {
			launches.removeListener(launchListener);
			launches = null;
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
		update();
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

	private void fillMenu() {
		new SubMenuProvider(launches).fill(menu, -1);
	}

	private static final IContributionItem EMPTY_ITEM = new ActionContributionItem(new EmptyAction("No active processes"));

	/**
	 * An action that is disabled and does nothing. Its only purpose is to
	 * show text when there is nothing else to show in an otherwise empty menu.
	 */
	private static class EmptyAction extends Action {
		public EmptyAction(String label) {
			super(label);
		}
		@Override
		public boolean isEnabled() {
			return false;
		}
	}

	/**
	 * Dynamically creates menus to relaunch currently active launches.
	 */
	private class SubMenuProvider extends CompoundContributionItem {

		private final LaunchList launches;

		public SubMenuProvider(LaunchList launches) {
			this.launches = launches;
		}

		private class RelaunchAction extends Action {
			private final ILaunchConfiguration launch;

			public RelaunchAction(ILaunchConfiguration launch) {
				this.launch = launch;
				this.setText(launch.getName());
			}

			@Override
			public void run() {
				try {
					performOperation(launch);
				} catch (DebugException e) {
					CorePlugin.log(e);
				}
			}

		}

		@Override
		protected IContributionItem[] getContributionItems() {
			return createContributionItems();
		}

		private IContributionItem[] createContributionItems() {
			ArrayList<IContributionItem> items = new ArrayList<IContributionItem>();
			for (ILaunchConfiguration launch : launches.getLaunches()) {
				items.add(new ActionContributionItem(new RelaunchAction(launch)));
			}
			if (items.isEmpty()) {
				items.add(EMPTY_ITEM);
			}
			// Return item in reverse order (so older item at the bottom of the menu).
			IContributionItem[] array = new IContributionItem[items.size()];
			for (int i = 0; i < array.length; i++) {
				array[array.length-i-1] = items.get(i);
			}
			return array;
		}

	}


}
