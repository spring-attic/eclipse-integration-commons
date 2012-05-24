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
package org.springsource.ide.eclipse.commons.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.core.CommandHistoryTest;
import org.springsource.ide.eclipse.commons.core.ResourceProviderTest;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporterTest;
import org.springsource.ide.eclipse.commons.internal.configurator.touchpoint.ConfiguratorActionTest_e_3_6;
import org.springsource.ide.eclipse.commons.internal.content.core.DescriptorMatcherTest;
import org.springsource.ide.eclipse.commons.internal.help.HelpPluginTest;
import org.springsource.ide.eclipse.commons.internal.ui.editors.UpdateNotificationTest;

import com.springsource.sts.internal.ide.configurator.touchpoint.ConfiguratorActionTest_e_3_5;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class AllIdeTests {

	public static boolean isEclipse_3_4() {
		return new VersionRange("[1.0.0,1.0.100)").isIncluded(getP2EngineVersion());
	}

	public static boolean isEclipse_3_5() {
		return new VersionRange("[1.0.100,2.0.0)").isIncluded(getP2EngineVersion());
	}

	public static boolean isEclipse_3_6() {
		return new VersionRange("2.0.0").isIncluded(getP2EngineVersion());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllIdeTests.class.getName());
		suite.addTestSuite(HelpPluginTest.class);
		suite.addTestSuite(ConfiguratorImporterTest.class);
		// suite.addTestSuite(ServerConfiguratorTest.class);
		if (isEclipse_3_5()) {
			suite.addTestSuite(ConfiguratorActionTest_e_3_5.class);
		}
		if (isEclipse_3_6()) {
			suite.addTestSuite(ConfiguratorActionTest_e_3_6.class);
		}
		suite.addTestSuite(DescriptorMatcherTest.class);
		suite.addTestSuite(ResourceProviderTest.class);
		suite.addTestSuite(CommandHistoryTest.class);
		suite.addTestSuite(UpdateNotificationTest.class);
		return suite;
	}

	private static Version getP2EngineVersion() {
		Bundle bundle = Platform.getBundle("org.eclipse.equinox.p2.engine"); //$NON-NLS-1$
		Assert.isNotNull(bundle);
		// TODO e3.5 replace new Version(...) by bundle.getVersion()
		return new Version((String) bundle.getHeaders().get("Bundle-Version"));
	}

}
