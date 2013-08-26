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
package org.springsource.ide.eclipse.commons.gettingstarted.boot;

import java.util.ArrayList;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;

/**
 * Dynamically creates menus to relaunch currently active launches.
 */
public class RelaunchMenuProvider extends CompoundContributionItem {
	
	private static final IContributionItem EMPTY_ITEM = new ActionContributionItem(new EmptyAction("No active processes"));
	
	/**
	 * An action that is disabled and does nothing. Its only purpose is to 
	 * show text when there is nothing else to show in an otherwise empty menu.
	 */
	public static class EmptyAction extends Action {
		public EmptyAction(String label) {
			super(label);
		}
		@Override
		public boolean isEnabled() {
			return false;
		}
	}
	
	private static class RelaunchAction extends Action {
		private ILaunch launch;

		public RelaunchAction(ILaunch launch) {
			this.launch = launch;
			this.setText(launch.getLaunchConfiguration().getName());
		}
		
		@Override
		public void run() {
			try {
				LaunchUtils.terminateAndRelaunch(launch);
			} catch (DebugException e) {
				GettingStartedActivator.log(e);
			}
		}
		
	}


	public RelaunchMenuProvider() {
	}

	public RelaunchMenuProvider(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		ArrayList<IContributionItem> items = new ArrayList<IContributionItem>();
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = lm.getLaunches();
		for (ILaunch launch : launches) {
			if (!launch.isTerminated() && launch.canTerminate()) {
				items.add(new ActionContributionItem(new RelaunchAction(launch)));
			}
		}
		if (items.isEmpty()) {
			items.add(EMPTY_ITEM);
		}
		return items.toArray(new IContributionItem[items.size()]);
	}

}
