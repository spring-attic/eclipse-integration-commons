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
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.internal.tasks.core.sync.GetTaskHistoryJob;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Represents a notification based on an RSS feed entry.
 * 
 * @author Steffen Pingel
 */
public class UpdateNotification {

	public static class Artifact {

		final String bundleId;

		// currently not supported
		final VersionRange versionRange;

		public Artifact(String bundleId, VersionRange bundleVersion) {
			Assert.isNotNull(bundleId);
			this.bundleId = bundleId;
			this.versionRange = bundleVersion;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Artifact other = (Artifact) obj;
			if (bundleId == null) {
				if (other.bundleId != null) {
					return false;
				}
			}
			else if (!bundleId.equals(other.bundleId)) {
				return false;
			}
			if (versionRange == null) {
				if (other.versionRange != null) {
					return false;
				}
			}
			else if (!versionRange.equals(other.versionRange)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bundleId == null) ? 0 : bundleId.hashCode());
			result = prime * result + ((versionRange == null) ? 0 : versionRange.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "Artifact [bundleId=" + bundleId + ", versionRange=" + versionRange + "]";
		}

	}

	private final Set<Artifact> conflictingBundles = new HashSet<Artifact>();

	private final SyndEntry entry;

	private String platformFilter;

	private final Set<Artifact> requiredBundles = new HashSet<Artifact>();

	private String severity;

	private String versionRange;

	public UpdateNotification(List<String> properties) {
		this.entry = null;
		for (String property : properties) {
			parseProperty(property);
		}
	}

	public UpdateNotification(SyndEntry entry) {
		this.entry = entry;
		List<?> categories = entry.getCategories();
		for (int i = 0; i < categories.size(); i++) {
			Object obj = categories.get(i);
			if (obj instanceof SyndCategory) {
				SyndCategory category = (SyndCategory) obj;
				String text = category.getName();
				parseProperty(text);
			}
		}
	}

	public Set<Artifact> getConflictingBundles() {
		return Collections.unmodifiableSet(conflictingBundles);
	}

	public SyndEntry getEntry() {
		return entry;
	}

	public String getPlatformFilter() {
		return platformFilter;
	}

	public Set<Artifact> getRequiredBundles() {
		return Collections.unmodifiableSet(requiredBundles);
	}

	public String getSeverity() {
		return severity;
	}

	public String getVersionRange() {
		return versionRange;
	}

	/**
	 * Return true, if the notification is valid for the passed parameters.
	 * 
	 * @param ideVersion the version of STS to check against
	 * @param environment
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean matches(Version ideVersion, Set<String> installedFeatureIds, Dictionary<?, ?> environment) {
		if (versionRange != null) {
			VersionRange range = new VersionRange(versionRange);
			if (!range.isIncluded(ideVersion)) {
				return false;
			}
		}
		if (installedFeatureIds != null && !requiredBundles.isEmpty()) {
			for (Artifact artifact : requiredBundles) {
				if (!installedFeatureIds.contains(artifact.bundleId)) {
					return false;
				}
			}
		}
		if (installedFeatureIds != null && !conflictingBundles.isEmpty()) {
			for (Artifact artifact : conflictingBundles) {
				if (installedFeatureIds.contains(artifact.bundleId)) {
					return false;
				}
			}
		}
		if (platformFilter != null) {
			try {
				Filter filter = FrameworkUtil.createFilter(platformFilter);
				// TODO e3.7 remove cast and use expected typesObject
				if (!filter.match((Dictionary) environment)) {
					return false;
				}
			}
			catch (InvalidSyntaxException e) {
				// ignore
			}
		}
		return true;
	}

	private Artifact parseArtifact(String substring) {
		String bundleId = null;
		VersionRange bundleVersion = null;
		String[] tokens = substring.split(";");
		for (String token : tokens) {
			if (token.contains("=")) {
				if (token.startsWith("version=")) {
					try {
						bundleVersion = new VersionRange(token.replaceFirst("version=", ""));
					}
					catch (IllegalArgumentException e) {
						// ignore
						return null;
					}
				}
			}
			else {
				bundleId = token;
			}
		}
		if (bundleId == null) {
			// invalid specification
			return null;
		}
		return new Artifact(bundleId, bundleVersion);
	}

	private void parseProperty(String text) {
		if (text.startsWith("severity=")) {
			severity = text.replaceFirst("severity=", "");
		}
		else if (text.startsWith("version=")) {
			versionRange = text.replaceFirst("version=", "");
		}
		else if (text.startsWith("platform.filter=")) {
			platformFilter = text.replaceFirst("platform.filter=", "");
		}
		else if (text.startsWith("bundle=")) {
			String bundle = text.replaceFirst("bundle=", "");
			if (bundle.startsWith("!")) {
				Artifact artifact = parseArtifact(bundle.substring(1));
				if (artifact != null) {
					conflictingBundles.add(artifact);
				}
			}
			else {
				Artifact artifact = parseArtifact(bundle);
				if (artifact != null) {
					requiredBundles.add(artifact);
				}
			}
		}
	}

	@Override
	public int hashCode() {
		if (entry.getTitle() != null) {
			return entry.getTitle().hashCode();
		}
		else {
			return -1;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof UpdateNotification && ((UpdateNotification) other).getEntry() != null && ((UpdateNotification) other).getEntry().getTitle().equals(entry.getTitle());
	}
}
