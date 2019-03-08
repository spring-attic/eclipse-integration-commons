/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.ui.actions;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;


public class ShowDashboardCommand extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException  {
		ShowDashboardAction action = new ShowDashboardAction();
		action.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		action.run(null);
		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		if (FeedMonitor.getInstance() != null && FeedMonitor.getInstance().isNewFeedItems()) {
			element.setIcon(StsUiImages.SPRING_LOGO_NOTIFY);
		} else {
			element.setIcon(StsUiImages.SPRING_LOGO);
		}
	}

}
