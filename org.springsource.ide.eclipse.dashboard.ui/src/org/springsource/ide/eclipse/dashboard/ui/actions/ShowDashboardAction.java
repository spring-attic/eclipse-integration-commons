/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.ui.actions;

import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardMainPage;

/**
 * Displays the dashboard.
 * <p>
 * Note: Must not have any dependencies on org.eclipse.mylyn.tasks.ui to avoid
 * class loader warnings.
 * @author Steffen Pingel
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Terry Denney
 */
public class ShowDashboardAction extends ShowDashboardPageAction {

	public ShowDashboardAction() {
		super(DashboardMainPage.PAGE_ID);
	}

}
