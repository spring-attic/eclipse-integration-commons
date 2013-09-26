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
package org.springsource.ide.eclipse.commons.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.springsource.ide.eclipse.commons.ui.tips.TipOfTheDayPopup;
import org.springsource.ide.eclipse.commons.ui.tips.TipProvider;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class UiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springsource.ide.eclipse.commons.ui";

	public static final String SHOW_TIP_O_DAY = "show.tip";

	private static UiPlugin plugin;

	private TipProvider provider;

	public UiPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		final IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setDefault(SHOW_TIP_O_DAY, true);
		if (preferenceStore.getBoolean(SHOW_TIP_O_DAY)) {

			UIJob tipJob = new UIJob("Spring Tool Tips") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					Shell shell = getActiveWorkbenchShell();
					if (shell != null) {
						// don't show this unless we have a parent shell
						// avoid popups dangling without a window
						new TipOfTheDayPopup(shell, preferenceStore, getTipProvider()).open();
					}
					return Status.OK_STATUS;
				}
			};
			tipJob.schedule();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static UiPlugin getDefault() {
		return plugin;
	}

	public TipProvider getTipProvider() {
		if (provider == null) {
			provider = new TipProvider();
		}
		return provider;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		final IWorkbenchWindow[] activeWindow = new IWorkbenchWindow[1];
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				activeWindow[0] = getDefault().getWorkbench().getActiveWorkbenchWindow();
			}
		});
		return activeWindow[0];
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static IWorkbenchPage getActiveWorkbenchPage() {
		return getActiveWorkbenchWindow().getActivePage();
	}

}
