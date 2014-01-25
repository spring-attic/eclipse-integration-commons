/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunction;
import org.springsource.ide.eclipse.commons.javafx.browser.JavaFxBrowserManager;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.IFeedListener;

/**
 * @author Miles Parker
 * 
 *         Code refactored from
 *         org.springsource.ide.eclipse.dashboard.internal.ui
 *         .editors.DashboardMainPage:
 * 
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public abstract class FeedProvider extends IEclipseToBrowserFunction.Extension {

	public FeedProvider(final String feedId) {
		FeedMonitor.getInstance().addListener(new IFeedListener() {

			@Override
			public void updated(String id) {
				if (id.equals(feedId)) {
					notifyIfReady();
				}
			}
		});
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
}
