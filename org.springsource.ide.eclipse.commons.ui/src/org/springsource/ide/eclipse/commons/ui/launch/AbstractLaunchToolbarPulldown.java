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
import java.util.Collection;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;
import org.springsource.ide.eclipse.commons.ui.launch.ProcessTracker.Listener;

/**
 * Abstract superclass for an launch toolbar button with pulldown
 * that applies some operation to a an active launch.
 *
 * @author Kris De Volder
 */
public abstract class AbstractLaunchToolbarPulldown implements IWorkbenchWindowPulldownDelegate {

	private IWorkbenchWindow window;
	private Menu menu;
	private Listener processListener;
	private ProcessTracker processes = ProcessTracker.getInstance().addListener(processListener = new ProcessTracker.Listener() {
			public void changed() {
				if (action!=null) {
					//We may not be in the UIThread here. So take care before futzing with the widgets!
					window.getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							update();
						}
					});
				}
			}
		});
	private IAction action;

	@Override
	public void run(IAction action) {
		this.action = action;
		IProcess p = processes.getLast();
		if (p!=null) {
			try {
				performOperation(p.getLaunch());
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

	protected abstract void performOperation(ILaunch launch) throws DebugException;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		update();
	}

	private void update() {
		if (action!=null) {
			IProcess p = processes.getLast();
			String label = getOperationName();
			if (p!=null) {
				ILaunch launch = p.getLaunch();
				if (launch!=null) {
					ILaunchConfiguration conf = launch.getLaunchConfiguration();
					if (conf!=null) {
						label = label + " " + conf.getName();
					}
				}
			}
			action.setText(label);
			action.setToolTipText(label);
			action.setEnabled(p!=null);
		}
	}

	@Override
	public void dispose() {
		if (menu!=null) {
			menu.dispose();
			menu = null;
		}
		if (processes!=null && processListener!=null) {
			processes.removeListener(processListener);
			processes = null;
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
		new SubMenuProvider(processes).fill(menu, -1);
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

		private final ProcessTracker processes;

		public SubMenuProvider(ProcessTracker processes) {
			this.processes = processes;
		}

		private class RelaunchAction extends Action {
			private final ILaunch launch;

			public RelaunchAction(ILaunch launch) {
				this.launch = launch;
				this.setText(launch.getLaunchConfiguration().getName());
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
			Collection<ILaunch> launches = processes.getLaunches();
			for (ILaunch launch : launches) {
				items.add(new ActionContributionItem(new RelaunchAction(launch)));
			}
			if (items.isEmpty()) {
				items.add(EMPTY_ITEM);
			}
			return items.toArray(new IContributionItem[items.size()]);
		}

	}


}
