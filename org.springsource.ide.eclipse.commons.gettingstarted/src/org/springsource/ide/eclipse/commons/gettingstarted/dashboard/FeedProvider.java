/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (https://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (https://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunction;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.IFeedListener;

/**
 * @author Miles Parker
 * 
 * Code refactored from org.springsource.ide.eclipse.dashboard.internal.ui
 * .editors.DashboardMainPage:
 * 
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public abstract class FeedProvider extends IEclipseToBrowserFunction.Extension {

	private static final long TIMEOUT = 20000;
	private boolean isTimeout;

	public FeedProvider(final String feedId) {
		FeedMonitor.getInstance().addListener(new IFeedListener() {

			@Override
			public void updated(String id) {
				if (id.equals(feedId)) {
					notifyIfReady();
				}
			}
		});
		Job timeouter = new Job("Timeouter") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				isTimeout = true;
				notifyIfReady();
				return Status.OK_STATUS;
			}
		};
		timeouter.setSystem(true);
		timeouter.schedule(TIMEOUT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springsource.ide.eclipse.commons.browser.IBrowserElementProvider#
	 * dispose()
	 */
	@Override
	public void dispose() {
		FeedMonitor.getInstance().markRead();
	}
	
	@Override
	public final String getDynamicArgumentValue(String id) {
		if ("html".equals(id)) {
			String html = getFeedHtml();
			if (html!=null) {
				return html;
			} else if (isTimeout()) {
				return getTimeoutMessage();
			}
		}
		return null;
	}
	
	public String getTimeoutMessage() {
		return "<p>No entries. Check internet connection?</p>";
	}

	private boolean isTimeout() {
		return isTimeout;
	}

	public abstract String getFeedHtml();

	public abstract boolean isFeedReady();
	
	public final boolean isReady() {
		boolean ready = isTimeout || isFeedReady();
		return ready;
	}
}
