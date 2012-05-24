/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package com.springsource.sts.internal.ide.configurator.touchpoint;

import junit.framework.TestCase;

import org.eclipse.equinox.internal.p2.engine.ActionManager;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;

import com.springsource.sts.ide.internal.configurator.Activator;

/**
 * @author Steffen Pingel
 */
// TODO e3.5 move into source folder
public class ConfiguratorActionTest_e_3_5 extends TestCase {

	public void testActionPresent() {
		ActionManager manager = new ActionManager();
		ProvisioningAction action = manager.getAction("com.springsource.sts.ide.configure", null);
		assertNotNull("Expected com.springsource.sts.ide.configure action, got null", action);
	}

	public void testConfiguratorActionRegistered() {
		assertNotNull(Activator.getDefault());
		assertTrue(Activator.getDefault().isConfigurationActionRegistered());
	}

}
