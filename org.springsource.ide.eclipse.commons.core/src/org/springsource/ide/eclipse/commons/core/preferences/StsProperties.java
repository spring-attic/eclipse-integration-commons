/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core.preferences;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springsource.ide.eclipse.commons.core.HttpUtil;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * An instance of this class provides a mechanism to retrieve String properties.
 * This is similar to a Java system properties. However it has support for reading these
 * properties from a fixed url. This allows us to change the properties
 * across all released versions of STS (from 3.4 onward) without requiring a new
 * release of STS.
 * <p>
 * Properties in this class can come from 3 different sources, listed here in
 * decreasing order of priority:
 *
 *   1) Java System properties (set via -Dmy.prop.name=value) in STS.ini
 *      (properties set this way override anything else).
 *   2) via the fixed url
 *   3) default values hard-coded in this class.
 *      (used only if property was not set via either 1 or 2).
 *
 * @since 3.4.M1
 *
 * @author Kris De Volder
 */
public class StsProperties {

	/**
	 * External url where properties are read from.
	 */
	public static final String PROPERTIES_URL = System.getProperty("sts.url.properties.url",
			"http://dist.springsource.com/release/STS/discovery/sts.properties");

	/**
	 * This class is a singleton. This holds the instance once created.
	 */
	private static StsProperties instance = null;

	public static StsProperties getInstance(IProgressMonitor mon) {
		if (instance==null) {
			StsProperties newInstance = new StsProperties(mon);
			instance = newInstance;
		}
		return instance;
	}

	private final Properties props;

	private StsProperties(IProgressMonitor mon) {
		props = createProperties();
		try {
			InputStream content = HttpUtil.stream(new URI(PROPERTIES_URL), mon);
			if (content != null) {
				try {
					props.load(content);
				} finally {
					content.close();
				}
			}
		}
		catch (Throwable e) {
			//Catch and log all exceptions. This should never fail to initialize *something* useable.
			CorePlugin.log(e);
		}
	}

	protected Properties createProperties() {
		Properties props = new Properties();

		// Default properties (guarantees certain properties have a value no
		// matter what).
		props.put("spring.site.url", "http://bogus.springsource.com");
		props.put("spring.initialzr.site.url", "http://initializr.cfapps.io");

		return props;
	}

	public String get(String key) {
		String value = System.getProperty(key);
		if (value == null) {
			value = props.getProperty(key);
		}
		return value;
	}

	public boolean get(String key, boolean deflt) {
		String value = get(key);
		if (value!=null) {
			return Boolean.valueOf(value);
		}
		return deflt;
	}

}
