/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.ui.actions;

import org.springsource.ide.eclipse.dashboard.internal.ui.discovery.DashboardExtensionsPage;

/**
 * Displays the install extension page.
 * <p>
 * Note: Must not have any dependencies on org.eclipse.mylyn.tasks.ui to avoid
 * class loader warnings.
 * @author Terry Denney
 */
public class ShowInstallExtensionAction extends ShowDashboardPageAction {

	public ShowInstallExtensionAction() {
		super(DashboardExtensionsPage.ID);
	}

}
