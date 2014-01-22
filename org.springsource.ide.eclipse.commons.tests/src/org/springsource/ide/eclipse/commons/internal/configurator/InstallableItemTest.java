/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.configurator;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.springsource.ide.eclipse.commons.internal.configurator.InstallableItem;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter.ParticipantExtensionPointReader;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Steffen Pingel
 */
public class InstallableItemTest extends TestCase {

	private File tempDir;

	private InstallableItem getExtension(String id) {
		Set<InstallableItem> extensions = ParticipantExtensionPointReader.getInstallableItems();
		for (InstallableItem extension : extensions) {
			if (extension.getId().equals(id)) {
				return extension;
			}
		}
		return null;
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void testInstallOperations() throws Exception {
		InstallableItem extension = getExtension("com.springsource.sts.ide.tests.runtime");
		assertNotNull(extension);
		assertEquals(2, extension.getInstallOperations().size());
		tempDir = StsTestUtil.createTempDirectory();
		IStatus result = extension.install(tempDir, null);
		assertTrue("Unexpected result: " + result, result.isOK());
	}

}
