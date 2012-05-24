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
package org.springsource.ide.eclipse.commons.internal.configurator.server;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter;
import org.springsource.ide.eclipse.commons.internal.configurator.server.ServerConfigurator;
import org.springsource.ide.eclipse.commons.internal.configurator.server.ServerDescriptor;

import junit.framework.TestCase;


/**
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class ServerConfiguratorTest extends TestCase {

	public static String ID_CONFIG_TC_SERVER_6 = "com.springsource.sts.ide.configurator.server.TcServer6";

	private File file;

	private ServerConfigurator importer;

	private File root;

	public void testGetInstallLocationApache6() throws IOException {
		createTempDirectory("tomcat-6.0.20");
		boolean success = false;
		for (ServerDescriptor descriptor : importer.getDescriptors()) {
			File serverLocation = importer.getLocation(descriptor);
			if (serverLocation != null) {
				assertEquals("org.eclipse.jst.server.tomcat.60", descriptor.getServerTypeId());
				success = true;
			}
		}
		assertTrue("No server descriptor matched directory " + file.getAbsolutePath(), success);
	}

	public void testGetInstallLocationDmServer1() throws IOException {
		createTempDirectory("dm-server-1.0.2");
		boolean success = false;
		for (ServerDescriptor descriptor : importer.getDescriptors()) {
			File serverLocation = importer.getLocation(descriptor);
			if (serverLocation != null) {
				assertEquals("com.springsource.server.10", descriptor.getServerTypeId());
				success = true;
			}
		}
		assertTrue("No server descriptor matched directory " + file.getAbsolutePath(), success);
	}

	public void testGetInstallLocationDmServer2() throws IOException {
		createTempDirectory("dm-server-2.0.0");
		boolean success = false;
		for (ServerDescriptor descriptor : importer.getDescriptors()) {
			File serverLocation = importer.getLocation(descriptor);
			if (serverLocation != null) {
				assertEquals("com.springsource.server.20", descriptor.getServerTypeId());
				success = true;
			}
		}
		assertTrue("No server descriptor matched directory " + file.getAbsolutePath(), success);
	}

	public void testGetInstallLocationDmServer3() throws IOException {
		createTempDirectory("dm-server-3.0.0");
		for (ServerDescriptor descriptor : importer.getDescriptors()) {
			File serverLocation = importer.getLocation(descriptor);
			if (serverLocation != null) {
				fail("Unexpected match for directory " + file.getAbsolutePath() + ", server type "
						+ descriptor.getServerTypeId());
			}
		}
	}

	public void testGetInstallLocationMultipleTcServer() throws IOException {
		createTempDirectory("tc-server");
		createTempDirectory("tc-server-6.0.20");
		createTempDirectory("tc-server-6.0.20.A");
		createTempDirectory("tc-serverznoversion");
		File dirB = createTempDirectory("tc-serverz-6.0.20.B");
		File dirC = createTempDirectory("tc-server-6.0.20.C");
		createTempDirectory("tc-server-7.0.0");
		// ensure that configuration checks version, not filename ordering
		assertTrue(dirB.compareTo(dirC) > 0);

		ServerDescriptor descriptor = importer.getDescriptor(ID_CONFIG_TC_SERVER_6);
		File serverLocation = importer.getLocation(descriptor);
		assertEquals(dirC, serverLocation);
	}

	public void testGetInstallLocationTcServer6() throws IOException {
		createTempDirectory("tc-server-6.0.20.A");
		boolean success = false;
		for (ServerDescriptor descriptor : importer.getDescriptors()) {
			File serverLocation = importer.getLocation(descriptor);
			if (serverLocation != null) {
				assertEquals("com.springsource.tcserver.60", descriptor.getServerTypeId());
				success = true;
			}
		}
		assertTrue("No server descriptor matched directory " + file.getAbsolutePath(), success);
	}

	public void testGetLocationServerDescriptor() throws IOException {
		ServerDescriptor descriptor = new ServerDescriptor("id") {
			// create subclass to make protected methods accessible
			{
				setRuntimeTypeId("com.springsource.server.runtime.10");
				setServerTypeId("com.springsource.server.10");
				setRuntimeName("Server Runtime");
				setServerName("Server Name");
				setInstallPath("abc");
				setVersionRange("[1.0.0,2.0.0)");
			}
		};

		// test fall back if directory has no version
		createTempDirectory("abc");
		File location = importer.getLocation(descriptor);
		assertEquals("abc", location.getName());
		file.delete();

		// test unknown prefix
		createTempDirectory("def");
		assertNull(importer.getLocation(descriptor));

		// test too low version
		createTempDirectory("abc-0.1");
		assertNull(importer.getLocation(descriptor));

		// test too high version
		createTempDirectory("abc-2.1");
		assertNull(importer.getLocation(descriptor));

		// test match
		createTempDirectory("abc-1.1");
		location = importer.getLocation(descriptor);
		assertEquals("abc-1.1", location.getName());
		file.delete();

		// test match with invalid version format
		createTempDirectory("abc-a.b");
		location = importer.getLocation(descriptor);
		assertEquals("abc-a.b", location.getName());
		file.delete();
	}

	public void testGetSearchLocations() throws IOException {
		ConfiguratorImporter importer = new ConfiguratorImporter();
		List<File> locations = importer.getSearchLocations();
		assertEquals(3, locations.size());
	}

	private File createTempDirectory(String name) throws IOException {
		file = new File(root, name);
		file.mkdirs();
		file.deleteOnExit();
		return file;
	}

	@Override
	protected void setUp() throws Exception {
		root = File.createTempFile("configurator importer test", null);
		root.delete();
		root.mkdirs();
		root.deleteOnExit();

		// mock lockup of extensions
		final ConfiguratorImporter configurator = new ConfiguratorImporter();
		configurator.setSearchLocations(Collections.singletonList(root));
		configurator.setRecurse(false);

		importer = new ServerConfigurator() {
			@Override
			public ConfiguratorImporter getConfigurator() {
				return configurator;
			};
		};
	}

	@Override
	protected void tearDown() throws Exception {
		if (file != null) {
			file.delete();
		}
		if (root != null) {
			root.delete();
		}
	}

}
