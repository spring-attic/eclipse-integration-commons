/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.core.HttpUtil;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;
import org.springsource.ide.eclipse.commons.internal.core.net.HttpClientTransportService;

/**
 * An instance of this class provides a mechanism to retrieve String properties.
 * This is similar to a Java system properties. However it has support for reading these
 * properties from a fixed url. This allows us to change the properties after release.
 * <p>
 * Properties in this class can come from 3 different sources, listed here in
 * decreasing order of priority:
 *
 *  1) Java System properties (set via -Dmy.prop.name=value) in STS.ini
 *     (properties set this way override anything else).
 *  2) loaded from fixed url
 *  3) default values hard-coded in this class.
 *     (used only if property was not set via either 1 or 2).
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


	private static Boolean isHangingBug = null;

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

	/**
	 * Determinese whether this instance is affected by the 'hanging on startup' bug:
	 * Bug on Eclipse 3.7: https://issuetracker.springsource.com/browse/STS-3581
	 */
	private boolean isHangingBug() {
		if (isHangingBug==null) {
			boolean affected = false;
			try {
				Bundle platformBundle = Platform.getBundle("org.eclipse.platform");
//				System.err.println("org.eclipse.platform bundle: " + platformBundle);
				Version version = platformBundle.getVersion();
				affected = version.getMajor()==3; //Both eclipse 3.7 and 3.8 are affected but not Eclipse 4.2 and 4.3
			} catch (Throwable e) {
				CorePlugin.log(e);
			} finally {
				isHangingBug = affected;
			}
		}
		return isHangingBug;
	}

	private void readProperties(String url, IProgressMonitor mon) {
		if (url!=null) {
			try {
				InputStream content = uriStream(new URI(url), mon);
				if (content != null) {
					try {
						props.load(content);
					} finally {
						content.close();
					}
				}
			} catch (Throwable e) {
				CorePlugin.log(e);
				//Catch and log all exceptions. This should never fail to initialise *something* usable.
				CorePlugin.warn("Couldn't read sts properties from '"+url+"' internal default values will be used");
			}
		}
	}

	private InputStream uriStream(URI uri, IProgressMonitor mon) throws CoreException, MalformedURLException, IOException {
		if (isHangingBug()) {
			return new HttpClientTransportService().stream(uri, mon);
//			//Bug: using HttpUtil causes a hang. So instead we use a simple URLConnection here. This doesn't
//			// provide the same level of completeness (i.e. proxy authentication is not supported yet)
//			//But at least it doesn't hang.
//			URLConnectionFactory cf = URLConnectionFactory.getDefault();
//			URLConnection conn = cf.createConnection(uri.toURL());
//			return conn.getInputStream();

		} else {
			return HttpUtil.stream(uri, mon);
		}
	}

	protected Properties createProperties() {
		Properties props = new Properties();

		// Default properties (guarantees certain properties have a value no
		// matter what).

		props.put("spring.site.url", "http://spring.io");
		props.put("spring.initializr.form.url", "http://start.spring.io/");
		props.put("spring.initializr.download.url", "http://start.spring.io/starter.zip");
		//note: 'spring.initializr.download.url' is no longer used since STS 3.6.0. Instead
		// the download url is obtained by parsing the form at spring.initializr.form.url

		//Urls used in the dashboard. For each XXX.url=... property, if
		//  - XXX.url.label is defined that label will be used for the corresponding
		//     dashboard tab instead of the html page title (title tends to be too long).
		//  - XXX.url.external is defined that url will always be openened in an external browser.

		//Switch to enable new dash
		props.put("sts.new.dashboard.enabled", "true");

		//Forum:
		props.put("sts.forum.url", "http://forum.springsource.org/forumdisplay.php?32-SpringSource-Tool-Suite");
		props.put("sts.forum.url.label", "Forum");
		props.put("sts.forum.url.external", "true");

		//Tracker:
		props.put("sts.tracker.url", "https://issuetracker.springsource.com/browse/STS");
		props.put("sts.tracker.url.label", "Issues");
		props.put("sts.tracker.url.external", "true");

		//Docs
		props.put("spring.docs.url", "https://spring.io/docs");
		props.put("spring.docs.url.label", "Spring Docs");
		props.put("spring.docs.url.external", "true");

		//Blog
		props.put("spring.blog.url", "https://spring.io/blog");
		props.put("spring.blog.url.label", "Blog");
		props.put("spring.blog.url.external", "true");

		//Guides
		props.put("spring.guides.url", "https://spring.io/guides");
		props.put("spring.guides.url.label", "Guides");
		props.put("spring.guides.url.external", "true");
			//future value: "${spring.site.url}/guides"

		//New and Noteworthy
		props.put("sts.nan.url", "http://docs.spring.io/sts/nan/latest/NewAndNoteworthy.html");
		//props.put("sts.nan.url.external", "true");

		//Spring boot runtime
		props.put("spring.boot.install.url", "http://repo.spring.io/release/org/springframework/boot/spring-boot-cli/1.0.0.RELEASE/spring-boot-cli-1.0.0.RELEASE-bin.zip");

		//Discovery url for spring reference app
		props.put("spring.reference.app.discovery.url", "https://raw.github.com/kdvolder/spring-reference-apps-meta/master/reference-apps.json");

		//Url for webservice that generates typegraph for spring boot jar type content assist
		props.put("spring.boot.typegraph.url", "http://aetherial.cfapps.io/boot/typegraph");

		//Default version of spring boot, assumed when we need a version but can't determine it from the classpath of the project.
		//Typically this should point to the latest version of spring-boot (the one used by spring-initialzr app).
		props.put("spring.boot.default.version", "1.0.0.RELEASE");

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

	public static StsProperties getInstance() {
		return getInstance(new NullProgressMonitor());
	}

}
