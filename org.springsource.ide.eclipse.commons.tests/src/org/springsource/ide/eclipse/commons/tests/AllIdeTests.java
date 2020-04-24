/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.tests;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.core.CommandHistoryTest;
import org.springsource.ide.eclipse.commons.core.NameGeneratorTest;
import org.springsource.ide.eclipse.commons.core.ResourceProviderTest;
import org.springsource.ide.eclipse.commons.internal.content.core.DescriptorMatcherTest;
import org.springsource.ide.eclipse.commons.internal.help.HelpPluginTest;
import org.springsource.ide.eclipse.commons.internal.ui.editors.UpdateNotificationTest;

/**
 * Runs all automated tests for STS IDE.
 *
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
@RunWith(Suite.class)
@SuiteClasses({ HelpPluginTest.class, //
		DescriptorMatcherTest.class, //
		ResourceProviderTest.class, //
		CommandHistoryTest.class, //
		NameGeneratorTest.class,
		UpdateNotificationTest.class, //
		DownloadManagerTests.class, //
		StringUtilTests.class
})
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

	private static Version getP2EngineVersion() {
		Bundle bundle = Platform.getBundle("org.eclipse.equinox.p2.engine"); //$NON-NLS-1$
		Assert.isNotNull(bundle);
		return bundle.getVersion();
	}

}
