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
package org.springsource.ide.eclipse.dashboard.ui;

/**
 * Implementators of this interface can decide if the Dashboard contribution
 * should be displayed.
 * @author Christian Dupuis
 * @since 2.3.1
 */
public interface IEnabledDashboardPart {

	/**
	 * Check if the part should be displayed on the Dashboard.
	 * @return <code>false</code> if part should not be included on the
	 * Dashboard
	 */
	boolean shouldAdd();

}
