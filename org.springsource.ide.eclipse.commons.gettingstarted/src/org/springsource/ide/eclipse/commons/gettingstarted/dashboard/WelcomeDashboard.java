/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.springsource.ide.eclipse.commons.browser.javafx.JavaFxBrowserEditor;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardReopener;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;

import com.sun.syndication.feed.synd.SyndEntry;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;

public class WelcomeDashboard extends JavaFxBrowserEditor {

	private static final String WELCOME_PAGE_URI = "platform:/plugin/org.springsource.ide.eclipse.commons.gettingstarted/resources/welcome";
	private IEditorSite site;
	private IPartListener partListener = null;
	
	private static final ISchedulingRule RULE = JobUtil.lightRule(WelcomeDashboard.class.getName());
	protected static final long RETRY_DELAY = 3000; // Retry after 3 seconds if feed not ready
	protected static final long MAX_FEED_WAIT = 2 * 60 * 1000; // for max of 2 minutes then give up
	
	private static final boolean DEBUG = false;
	
	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
 	}

	@Override
	public void init(IEditorSite _site, IEditorInput input) throws PartInitException {
		super.init(_site, input);
		this.site = _site;
		
		site.getPage().addPartListener(this.partListener = new IPartListener() {
			
			@Override
			public void partOpened(IWorkbenchPart part) {
			}
			
			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}
			
			@Override
			public void partClosed(IWorkbenchPart part) {
				if (WelcomeDashboard.this==part) {
					IPreferenceStore prefs = IdeUiPlugin.getDefault().getPreferenceStore();
					prefs.setValue(IIdeUiConstants.PREF_OPEN_DASHBOARD_STARTUP, false);
					disposeListeners();
				}
			}
			
			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}
			
			@Override
			public void partActivated(IWorkbenchPart part) {
			}
		});
	}

	@Override
	public void dispose() {
		disposeListeners();
		super.dispose();
	}

	private void disposeListeners() {
		IWorkbenchPage page = site.getPage();
		if (page!=null && partListener!=null) {
			page.removePartListener(partListener);
		}
		partListener = null;
	}
	
	public WelcomeDashboard() throws URISyntaxException, IOException {
		DashboardReopener.ensure();
		setName("Welcome");
		String loadingUrl = FileLocator.toFileURL(new URL(WELCOME_PAGE_URI)).toString()+"index.html";
		setHomeUrl(loadingUrl);
		Job job = new Job("Populate Welcome Dashboard") {
			
			int tries = (int) (MAX_FEED_WAIT / RETRY_DELAY);
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Set<SyndEntry> feedEntries = FeedMonitor.getInstance().getFeedEntries();
					if (feedEntries==null || feedEntries.isEmpty()) {
						throw new Exception("Feed not ready");
					} else {
						debug("# Feed entries: "+feedEntries.size());
					}
					File file = getWelcomeFile();
					File contentInstance = DashboardCopier.getCopy(file, new NullProgressMonitor());
					File welcomeHtml = new File(contentInstance, "index.html");
					setHomeUrl(welcomeHtml.toURI().toString());
					setUrl(welcomeHtml.toURI().toString());
					return Status.OK_STATUS;
				} catch (Exception e) {
					debug("Welcome Dash Setup failed: "+ExceptionUtil.getMessage(e));
					//Nasty excpetions sometime happen because trying to do this too early during startup
					// when eclipse mars services aren't yet all up and running.
					if (tries-->0) {
						debug("Retrying Welcome Dash Setup...");
						this.schedule(RETRY_DELAY);
						return Status.OK_STATUS;
					} else {
						return ExceptionUtil.status(e);
					}
				}
			}

		};
		job.setRule(RULE);
		job.setSystem(true);
		job.schedule();
	}

	private File getWelcomeFile() throws IOException, MalformedURLException, URISyntaxException {
		URL fileURL = FileLocator.toFileURL(new URL(WELCOME_PAGE_URI));
		File file;
		try {
			file = new File(fileURL.toURI());
		} catch (URISyntaxException e) {
			//https://issuetracker.springsource.com/browse/STS-3712
			//Actually this is expected because FileLocator is buggy and returns urls with illegal
			// chars like spaces in them without properly encoding them.
			//So... proceed assuming the 'file' portion of the url is unencoded.
			file = new File(fileURL.getFile());
		}
		return file;
	}

	@Override
	protected boolean hasToolbar() {
		return false;
	}
}
