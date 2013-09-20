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
package org.springsource.ide.eclipse.dashboard.ui;

import org.eclipse.swt.widgets.Shell;

/**
 * To contribute a new project wizard link to the STS dashboard, extends this
 * class to define what action should be taken when the link is activated
 * @author Terry Denney
 * @author Christian Dupuis
 */
public abstract class AbstractNewProjectAction {

	public abstract void run(Shell shell);

}
