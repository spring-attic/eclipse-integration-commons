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

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Steffen Pingel
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		ConfiguratorImporter importer = new ConfiguratorImporter();
		File location = importer.getDefaultInstallLocation();
		if (location != null) {
			preferences.setDefault(Activator.PROPERTY_USER_INSTALL_PATH, location.getAbsolutePath());
		}
		else {
			preferences.setDefault(Activator.PROPERTY_USER_INSTALL_PATH, null);
		}
	}

}
