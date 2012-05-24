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
package org.springsource.ide.eclipse.commons;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class IdeTestPlugin extends AbstractUIPlugin {

	private static IdeTestPlugin INSTANCE;

	public static IdeTestPlugin getDefault() {
		return INSTANCE;
	}

	public IdeTestPlugin() {
		INSTANCE = this;
	}

}
