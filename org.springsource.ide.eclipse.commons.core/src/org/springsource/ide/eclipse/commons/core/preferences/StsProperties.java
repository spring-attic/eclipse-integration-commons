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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.springsource.ide.eclipse.commons.core.HttpUtil;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * An instance of this class provides a mechanism to retrieve String properties.
 * This is similar to a Java system properties. However it has support for reading these
 * properties from a fixed url. This allows us to change the properties after release.
 * <p>
 * Properties in this class can come from 3 different sources, listed here in
 * decreasing order of priority:
 *
 *   1) Java System properties (set via -Dmy.prop.name=value) in STS.ini
 *      (properties set this way override anything else).
 *   2) loaded from fixed url
 *   3) default values hard-coded in this class.
 *      (used only if property was not set via either 1 or 2).
 *
 * @since 3.4.M1
 *
 * @author Kris De Volder
 */
public class StsProperties {

	private static final String PROPERTIES_URL_PROPERTY = "sts.properties.url";


	/**
	 * The properties url is normally defined as a property on the active product {@link IProduct}).
	 * See the product plugin i.e. 'org.springsource.sts' and 'org.springsource.ggts' for toolsuite-distribution repo.
	 * <p>
	 * If STS or GGTS is installed from update site then product may not be STS or GGTS. In that case
	 * the 'no_product.properties url will be used.
	 */
	private static final String NO_PRODUCT_PROPERTIES = "http://dist.springsource.com/release/STS/discovery/no_product.properties";


	//Note: there is also a class called 'ResourceProvider'.. which reads various properties
	// from eclipse extension points. This is different because the STSProperties themselves
	// are read from an external url.
	//The ResourceProvider only allows properties to defined by extensions contained in plugins
	// installed into the Ecliple platform.

	/**
	 * This class is a singleton. This holds the instance once created.
	 */
	private static StsProperties instance = null;

	public static StsProperties getInstance(IProgressMonitor mon) {
		if (instance==null) {
			StsProperties newInstance = new StsProperties(determineUrl(), mon);
			instance = newInstance;
		}
		return instance;
	}

	private final Properties props;

	private StsProperties(String url, IProgressMonitor mon) {
		props = createProperties();
		if (url!=null) {
			try {
				InputStream content = HttpUtil.stream(new URI(url), mon);
				if (content != null) {
					try {
						props.load(content);
					} finally {
						content.close();
					}
				}
			} catch (Throwable e) {
				//Catch and log all exceptions. This should never fail to initialise *something* usable.
				CorePlugin.warn("Couldn't read sts properties from '"+url+"' internal default values will be used");
			}
		}
	}

	/**
	 * Determines the URL from where the properties file shall be read.
	 */
	private static String determineUrl() {
		//Allow easy overriding by setting a system property:
		String url = System.getProperty(PROPERTIES_URL_PROPERTY);
		if (url==null) {
			IProduct product = Platform.getProduct();
			if (product!=null) {
				url = product.getProperty(PROPERTIES_URL_PROPERTY);
			}
		}
		if (url==null) {
			url = NO_PRODUCT_PROPERTIES;
		}
		return url;
	}

	protected Properties createProperties() {
		Properties props = new Properties();

		// Default properties (guarantees certain properties have a value no
		// matter what).

		//Sites from which STS wizards generate some other urls.
		props.put("spring.site.url", "http://bogus.springsource.com");


		props.put("spring.initializr.form.url", "http://start.springframework.io/");
		props.put("spring.initializr.download.url", "http://start.springframework.io/starter.zip");

		//Urls used in the dashboard. For each XXX.url=... property, if
		//  - XXX.url.label is defined that label will be used for the corresponding
		//     dashboard tab instead of the html page title (title tends to be too long).
		//  - XXX.url.external is defined that url will always be openened in an external browser.

		//Switch to enable new dash
		props.put("sts.new.dashboard.enabled", "false");

		//Forum:
		props.put("sts.forum.url", "http://forum.springsource.org/forumdisplay.php?32-SpringSource-Tool-Suite");
		props.put("sts.forum.url.label", "Forum");
		props.put("sts.forum.url.external", "true");

		//Tracker:
		props.put("sts.tracker.url", "https://issuetracker.springsource.com/browse/STS");
		props.put("sts.tracker.url.label", "Issues");
		props.put("sts.tracker.url.external", "true");

		//Docs
		props.put("spring.docs.url", "http://www.springsource.org/documentation");
		props.put("spring.docs.url.label", "Spring Docs");
		props.put("spring.docs.url.external", "true");

		//Blog
		props.put("spring.blog.url", "http://blog.springsource.org");
		props.put("spring.blog.url.label", "Blog");
		props.put("spring.blog.url.external", "true");

		//Guides
		props.put("spring.guides.url", "http://www.springsource.org/get-started");
		props.put("spring.guides.url.label", "Guides");
		props.put("spring.guides.url.external", "true");
			//future value: "${spring.site.url}/guides"

		//New and Noteworthy
		props.put("sts.nan.url", "http://static.springsource.org/sts/nan/latest/NewAndNoteworthy.html");
		return props;
	}

	/**
	 * Procudes names of properties that have explicitly been set, either from properties file
	 * or by the explicitly provided defaults.  More precisely this does not return
	 * properties simply inherited from Java system properties.
	 */
	public Collection<String> getExplicitProperties() {
		ArrayList<String> keys = new ArrayList<String>();
		for (Object string : props.keySet()) {
			if (string instanceof String) {
				keys.add((String) string);
			}
		}
		return keys;
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
