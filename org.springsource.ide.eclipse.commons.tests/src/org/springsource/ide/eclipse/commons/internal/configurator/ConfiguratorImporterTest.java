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
package org.springsource.ide.eclipse.commons.internal.configurator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.ServerCore;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;

/**
 * @author Steffen Pingel
 * @author Martin Lippert
 * @author Tomasz Zarna
 */
@SuppressWarnings("restriction")
public class ConfiguratorImporterTest extends TestCase {

	public void testDetectExtensions() throws Exception {
		ConfiguratorImporter importer = new ConfiguratorImporter();
		List<ConfigurableExtension> extensions = importer.detectExtensions(new NullProgressMonitor());
		// assertContains("grails-", extensions);
		assertContains("apache-maven-", extensions);
		assertContains("spring-roo-", extensions);
		assertContains("vfabric-tc-server-developer-", extensions);
		// assertContains("com.springsource.cloudfoundryserver.runtime.10",
		// extensions);
	}

	public void testGetSearchLocations() throws IOException {
		ConfiguratorImporter importer = new ConfiguratorImporter();
		List<File> locations = importer.getSearchLocations();
		assertEquals(3, locations.size());
	}

	public void testStartupJob() throws Exception {
		CountDownLatch latch = ConfiguratorImporter.getLazyStartupJobLatch();
		assertTrue("Configurator did not complete before timeout", latch.await(120, TimeUnit.SECONDS));
		// FIXME re-enable test
		ConfiguratorImporter importer = new ConfiguratorImporter();
		assertServer(importer, "VMware vFabric tc Server Developer Edition v2.7");
	}

	private void assertContains(String id, List<ConfigurableExtension> extensions) {
		for (ConfigurableExtension extension : extensions) {
			if (extension.getId().startsWith(id)) {
				assertTrue("Expected auto configuration flag for extension " + extension,
						extension.isAutoConfigurable());
				return;
			}
		}
		fail("Expected extension with id prefix '" + id + "' in " + StringUtils.join(extensions, ", "));
	}

	private void assertServer(ConfiguratorImporter importer, String id) {
		try {
			assertNotNull("Expected auto configuration of server with id " + id, ServerCore.findServer(id));
		}
		catch (AssertionError e) {
			System.err.println("Extensions: "
					+ StringUtils.join(importer.detectExtensions(new NullProgressMonitor()), ", "));
			throw e;
		}
	}

}
