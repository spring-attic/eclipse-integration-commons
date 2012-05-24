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
package com.springsource.sts.ide.internal.configurator.touchpoint;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;

import com.springsource.sts.ide.internal.configurator.Configurator;

/**
 * @author Steffen Pingel
 * @since 2.2.1
 */
@SuppressWarnings( { "restriction", "unchecked" })
public class ConfiguratorAction_e_3_5 extends ProvisioningAction {

	private final Configurator configurator;

	public ConfiguratorAction_e_3_5() {
		configurator = new Configurator();
	}

	@Override
	public IStatus execute(Map parameters) {
		return configurator.execute(parameters);
	}

	@Override
	public IStatus undo(Map parameters) {
		return configurator.undo(parameters);
	}

}
