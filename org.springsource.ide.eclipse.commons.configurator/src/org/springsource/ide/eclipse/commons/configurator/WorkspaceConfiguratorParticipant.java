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
package org.springsource.ide.eclipse.commons.configurator;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springsource.ide.eclipse.commons.internal.configurator.InstallableItem;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.2.1
 */
public abstract class WorkspaceConfiguratorParticipant {

	private String id;

	/**
	 * Returns a configurable extension for <code>location</code> if the
	 * participant can configure the location.
	 * 
	 * @return the extension, if supported; null, otherwise
	 * @deprecated
	 */
	@Deprecated
	public abstract ConfigurableExtension createExtension(File location, IProgressMonitor monitor);

	/**
	 * Returns a configurable extension for <code>item</code> if the participant
	 * can configure item.
	 * 
	 * @return the extension, if supported; null, otherwise
	 */
	public abstract ConfigurableExtension createExtension(InstallableItem item, IProgressMonitor monitor);

	/**
	 * Returns a list of configurable extensions.
	 * 
	 * @param context provides information where to search for extensions
	 * @param monitor progress monitor
	 */
	public abstract List<ConfigurableExtension> detectExtensions(IConfigurationContext context, IProgressMonitor monitor);

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
