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
package org.springsource.ide.eclipse.commons.ui.launch;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;

public class LaunchUtils {

	/**
	 * Terminate a given launch (if it is running) then relaunch it.
	 */
	public static void terminateAndRelaunch(final ILaunch launch) throws DebugException {
		if (!launch.isTerminated()) {
			launch.terminate();
		}
		whenTerminated(launch, new UiRunnable() {
			@Override
			public void uiRun() {
				//must run in UI thread since it may popup dialogs in some cases.
				DebugUITools.launch(launch.getLaunchConfiguration(), launch.getLaunchMode());
			}
		});
	}

	/**
	 * Execute some code as soon as a given launch is terminated. If the launch is already terminated
	 * then the code is executed synchronously, otherwise it is executed asynchronously when
	 * a termination event is received.
	 */
	public static void whenTerminated(ILaunch launch, Runnable runnable) {
		new WhenTerminated(launch, runnable);
	}
	private static class WhenTerminated implements IDebugEventSetListener {

		private final ILaunch launch;
		private Runnable runnable;
		private final DebugPlugin debugPlugin;

		public WhenTerminated(ILaunch launch, Runnable runnable) {
			this.launch = launch;
			this.runnable = runnable;
			this.debugPlugin = DebugPlugin.getDefault();
			debugPlugin.addDebugEventListener(this);

			//Careful... what if the launch has terminated since we last checked it...
			// in that case we might not get a termination event! So start off with
			// an initial check now.
			checkAndRun();
		}

		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent e : events) {
				//Don't ckeck source==launch because we don't get termination events for launches
				// only for processes in a launch.
				if (e.getKind()==DebugEvent.TERMINATE) {
					checkAndRun();
				}
			}
		}

		private void checkAndRun() {
			Runnable runit = check();
			if (runit!=null) {
				debugPlugin.removeDebugEventListener(this);
				runit.run();
			}
		}

		/**
		 * Checks whether condition for firing the runable is satisfied. If so, then the runable
		 * is returned. At the same time the runnable field is nulled to ensure it can not be
		 * executed more than once.
		 * <p>
		 * Executing the runable does not happen in this method because it is 'synchronized'
		 * and we don't want to hang on to the monitor any longer than necessary (especially
		 * not when firing the runnable!)
		 */
		private synchronized Runnable check() {
			if (runnable!=null) {
				if (launch.isTerminated()) {
					//bingo!
					Runnable it = runnable;
					runnable = null;
					return it;
				}
			}
			return null;
		}

	}


}
