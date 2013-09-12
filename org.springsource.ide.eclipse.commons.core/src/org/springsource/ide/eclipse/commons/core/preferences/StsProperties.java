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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Properties;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
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

	public static class AscendingPriority implements Comparator<FromUrl> {

		public int compare(FromUrl o1, FromUrl o2) {
			return o1.priority - o2.priority;
		}

	}

	private static class FromUrl {

		public final String url;
		public final int priority;

		public FromUrl(IConfigurationElement element) {
			this.url = element.getAttribute("url");
			this.priority = Integer.parseInt(element.getAttribute("priority"));
		}

		@Override
		public String toString() {
			return "("+priority+", "+url+")";
		}

	}

	private static final String EXTENSION_ID = "org.springsource.ide.commons.core.properties";

	private FromUrl[] readExtensionPoints() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint(EXTENSION_ID);
		IExtension[] extensions = extensionPoint.getExtensions();

		ArrayList<FromUrl> sources = new ArrayList<FromUrl>();

		// read property definitions
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				String name = element.getName();
				if ("fromUrl".equals(name)) {
					sources.add(new FromUrl(element));
				}
			}
		}

		FromUrl[] sourcesArray = sources.toArray(new FromUrl[sources.size()]);
		Arrays.sort(sourcesArray, new AscendingPriority());
		return sourcesArray;
	}

	private static final String PROPERTIES_URL_PROPERTY = "sts.properties.url";


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
			StsProperties newInstance = new StsProperties(mon);
			instance = newInstance;
		}
		return instance;
	}

	private final Properties props;

	private StsProperties(IProgressMonitor mon) {
		props = createProperties();
		FromUrl[] sources = readExtensionPoints();
		mon.beginTask("Read Sts Properties", sources.length+1);
		try {
			for (FromUrl source : sources) {
				readProperties(source.url, new SubProgressMonitor(mon, 1));
			}
			String url = System.getProperty(PROPERTIES_URL_PROPERTY);
			if (url!=null) {
				readProperties(url, new SubProgressMonitor(mon, 1));
			}
		} finally {
			mon.done();
		}
	}

	private void readProperties(String url, IProgressMonitor mon) {
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

	protected Properties createProperties() {
		Properties props = new Properties();

		// Default properties (guarantees certain properties have a value no
		// matter what).

		props.put("spring.site.url", "http://springsource.org");
		props.put("spring.initializr.form.url", "http://start.springframework.io/");
		props.put("spring.initializr.download.url", "http://start.springframework.io/starter.zip");

		//Urls used in the dashboard. For each XXX.url=... property, if
		//  - XXX.url.label is defined that label will be used for the corresponding
		//     dashboard tab instead of the html page title (title tends to be too long).
		//  - XXX.url.external is defined that url will always be openened in an external browser.

		//Switch to enable new dash
		props.put("sts.new.dashboard.enabled", "false");

		//Points to where the content for the dash is. If a platform url it will be interpreted as a directory to be
		// copied and 'instantiated' by substituting StsProperties. If a non-platform url then it will
		// passed directly to the browser without further processing.
		// This default value points to a bundled STS dashboard welcome page.
		props.put("dashboard.welcome.url", "platform:/plugin/org.springsource.ide.eclipse.commons.gettingstarted/resources/welcome");

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
		//props.put("sts.nan.url.external", "true");
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
