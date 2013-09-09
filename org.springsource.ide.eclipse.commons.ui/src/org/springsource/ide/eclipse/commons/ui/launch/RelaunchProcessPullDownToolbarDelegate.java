/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.launch;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;

public class RelaunchProcessPullDownToolbarDelegate extends AbstractLaunchToolbarPulldown {

	@Override
	protected void performOperation(ILaunch launch) throws DebugException {
		LaunchUtils.terminateAndRelaunch(launch);
	}

	@Override
	protected String getOperationName() {
		return "Relaunch";
	}
	

}
