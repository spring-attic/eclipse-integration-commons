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
package org.springsource.ide.eclipse.commons.internal.configurator.touchpoint;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.springsource.ide.eclipse.commons.internal.configurator.Configurator;

/**
 * @author Steffen Pingel
 * @since 2.2.1
 */
public class ConfiguratorAction_e_3_6 extends ProvisioningAction {

	private final Configurator configurator;

	public ConfiguratorAction_e_3_6() {
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
