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
package org.springsource.ide.eclipse.commons.internal.ui.editors;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.UpdateNotification;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.UpdateNotification.Artifact;


/**
 * @author Steffen Pingel
 */
public class UpdateNotificationTest extends TestCase {

	private final Dictionary<Object, Object> env = new Properties();

	private final Set<String> installedFeatures = new HashSet<String>();

	private final Version version = new Version("1.0.0");

	public void testGetConflictingBundles() {
		UpdateNotification notification = new UpdateNotification(Arrays.asList(new String[] { "bundle=!com.vmware" }));
		HashSet<Artifact> expected = new HashSet<Artifact>();
		expected.add(new Artifact("com.vmware", null));
		assertEquals(expected, notification.getConflictingBundles());
	}

	public void testGetConflictingBundlesVersion() {
		UpdateNotification notification = new UpdateNotification(
				Arrays.asList(new String[] { "bundle=!com.vmware;version=1.2.0;ignore=foo" }));
		HashSet<Artifact> expected = new HashSet<Artifact>();
		expected.add(new Artifact("com.vmware", new VersionRange("1.2.0")));
		assertEquals(expected, notification.getConflictingBundles());
	}

	public void testGetMultipleProperties() {
		UpdateNotification notification = new UpdateNotification(Arrays.asList(new String[] { "version=[1.2.0,1.3.0)",
				"platform.filter=(foo=bar)", "severity=high", "key=value", "invalid" }));
		assertEquals("[1.2.0,1.3.0)", notification.getVersionRange());
		assertEquals("(foo=bar)", notification.getPlatformFilter());
	}

	public void testGetMultipleSame() {
		UpdateNotification notification = new UpdateNotification(Arrays.asList(new String[] { "version=[1.2.0,1.3.0)",
				"version=8.0.0" }));
		assertEquals("8.0.0", notification.getVersionRange());
		assertEquals(null, notification.getPlatformFilter());
	}

	public void testGetPlatformFilter() {
		UpdateNotification notification = new UpdateNotification(
				Arrays.asList(new String[] { "platform.filter=(os.arch=myos)" }));
		assertEquals("(os.arch=myos)", notification.getPlatformFilter());

		notification = new UpdateNotification(Arrays.asList(new String[] { "platform=value" }));
		assertEquals(null, notification.getPlatformFilter());
	}

	public void testGetRequiredBundles() {
		UpdateNotification notification = new UpdateNotification(Arrays.asList(new String[] { "bundle=com.vmware" }));
		HashSet<Artifact> expected = new HashSet<Artifact>();
		expected.add(new Artifact("com.vmware", null));
		assertEquals(expected, notification.getRequiredBundles());
	}

	public void testGetRequiredBundlesVersion() {
		UpdateNotification notification = new UpdateNotification(
				Arrays.asList(new String[] { "bundle=com.vmware;ignore=foo;version=[1.0.0,2.0.0)" }));
		HashSet<Artifact> expected = new HashSet<Artifact>();
		expected.add(new Artifact("com.vmware", new VersionRange("[1.0.0,2.0.0)")));
		assertEquals(expected, notification.getRequiredBundles());
	}

	public void testGetSeverity() {
		UpdateNotification notification = new UpdateNotification(Arrays.asList(new String[] { "severity=high" }));
		assertEquals("high", notification.getSeverity());

		notification = new UpdateNotification(Arrays.asList(new String[] { "severity" }));
		assertEquals(null, notification.getSeverity());
	}

	public void testGetVersion() {
		UpdateNotification notification = new UpdateNotification(Arrays.asList(new String[] { "version=1.2.3" }));
		assertEquals("1.2.3", notification.getVersionRange());

		notification = new UpdateNotification(Arrays.asList(new String[] { "version=[1.2.0,1.3.0)" }));
		assertEquals("[1.2.0,1.3.0)", notification.getVersionRange());

		notification = new UpdateNotification(Arrays.asList(new String[] { "platform=value" }));
		assertEquals(null, notification.getVersionRange());
	}

	public void testMatchesBundleConflicting() {
		List<String> properties = Arrays.asList(new String[] { "bundle=!com.vmware" });
		UpdateNotification notification = new UpdateNotification(properties);

		installedFeatures.add("com.vmware");
		assertFalse(notification.matches(version, installedFeatures, env));

		installedFeatures.remove("com.vmware");
		assertTrue(notification.matches(version, installedFeatures, env));
	}

	public void testMatchesBundleRequired() {
		List<String> properties = Arrays.asList(new String[] { "bundle=com.vmware" });
		UpdateNotification notification = new UpdateNotification(properties);

		installedFeatures.add("abc");
		assertFalse(notification.matches(version, installedFeatures, env));

		installedFeatures.add("com.vmware");
		assertTrue(notification.matches(version, installedFeatures, env));
	}

	public void testMatchesBundleRequiredNullInstalled() {
		List<String> properties = Arrays.asList(new String[] { "bundle=com.vmware" });
		UpdateNotification notification = new UpdateNotification(properties);
		assertTrue(notification.matches(version, null, env));
	}

	public void testMatchesBundleConflictingNullInstalled() {
		List<String> properties = Arrays.asList(new String[] { "bundle=!com.vmware" });
		UpdateNotification notification = new UpdateNotification(properties);
		assertTrue(notification.matches(version, null, env));
	}

	public void testMatchesPlatformFilter() {
		List<String> properties = Arrays.asList(new String[] { "platform.filter=(os.arch=myos)" });
		UpdateNotification notification = new UpdateNotification(properties);

		assertFalse(notification.matches(version, installedFeatures, env));

		env.put("os.arch", "myos");
		assertTrue(notification.matches(version, installedFeatures, env));
	}

	public void testMatchesMultiple() {
		List<String> properties = Arrays.asList(new String[] { "platform.filter=(os.arch=myos)", "bundle=com.vmware",
				"bundle=!com.springsource" });
		UpdateNotification notification = new UpdateNotification(properties);

		assertFalse(notification.matches(version, installedFeatures, env));

		env.put("os.arch", "myos");
		assertFalse(notification.matches(version, installedFeatures, env));

		installedFeatures.add("com.vmware");
		assertTrue(notification.matches(version, installedFeatures, env));

		installedFeatures.add("com.springsource");
		assertFalse(notification.matches(version, installedFeatures, env));
	}

}
