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
package org.springsource.ide.eclipse.commons.internal.configurator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceConfiguratorParticipant;


/**
 * Describes a configurator participant that is loaded from an extension.
 * @author Steffen Pingel
 */
public class ParticipantDescriptor {

	private String label;

	private String location;

	private final String id;

	private static final String ATTR_CLASS = "class";

	private static final String ATTR_ID = "id";

	private static final String ATTR_LABEL = "label";

	private final IConfigurationElement element;

	public ParticipantDescriptor(IConfigurationElement element) {
		this.element = element;
		String id = element.getAttribute(ATTR_ID);
		if (id == null) {
			id = element.getAttribute(ATTR_CLASS);
		}
		this.id = id;
		String label = element.getAttribute(ATTR_LABEL);
		if (label == null) {
			label = id;
		}
		this.label = label;
	}

	public ParticipantDescriptor(String id) {
		this.id = id;
		this.element = null;
	}

	public WorkspaceConfiguratorParticipant createConfigurator() {
		if (element != null) {
			try {
				Object object = WorkbenchPlugin.createExtension(element, ATTR_CLASS);
				if (object instanceof WorkspaceConfiguratorParticipant) {
					WorkspaceConfiguratorParticipant participant = (WorkspaceConfiguratorParticipant) object;
					participant.setId(getId());
					return participant;
				}
			}
			catch (CoreException e) {
				// ignore extension
			}
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getLocation() {
		return location;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
