/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.content.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class ContentItem {

	private Descriptor localDescriptor;

	private Descriptor remoteDescriptor;

	private Descriptor defaultDescriptor;

	private final String id;

	private final IProject runtimeProject;

	public ContentItem(String id) {
		this(id, null);
	}

	public ContentItem(String id, IProject runtimeProject) {
		this.id = id;
		this.runtimeProject = runtimeProject;
	}

	public String getDescription() {
		return (defaultDescriptor != null) ? defaultDescriptor.getDescription() : null;
	}

	public long getDownloadSize() {
		return (remoteDescriptor != null) ? remoteDescriptor.getSize() : 0;
	}

	public String getId() {
		return id;
	}

	public Descriptor getLocalDescriptor() {
		return localDescriptor;
	}

	public String getName() {
		return (defaultDescriptor != null) ? defaultDescriptor.getName() : null;
	}

	public String getPath() {
		return defaultDescriptor.getId() + "-" + defaultDescriptor.getVersion();
	}

	public String getPathFromRemoteDescriptor() {
		return remoteDescriptor.getId() + "-" + remoteDescriptor.getVersion();
	}

	public Descriptor getRemoteDescriptor() {
		return remoteDescriptor;
	}

	public boolean isLocal() {
		return localDescriptor != null;
	}

	public boolean isRuntimeDefined() {
		return runtimeProject != null;
	}

	public IProject getRuntimeProject() {
		return runtimeProject;
	}

	public boolean isNewerVersionAvailable() {
		if (localDescriptor != null && remoteDescriptor != null) {
			return (new Version(localDescriptor.getVersion()).compareTo(new Version(remoteDescriptor.getVersion())) < 0);
		}
		return false;
	}

	public boolean needsDownload() {
		return !isLocal() || isNewerVersionAvailable();
	}

	public void setLocalDescriptor(Descriptor localDescriptor) {
		Assert.isNotNull(localDescriptor);
		if (shouldReplace(this.localDescriptor, localDescriptor)) {
			this.localDescriptor = localDescriptor;
			this.defaultDescriptor = localDescriptor;
		}
	}

	public void setRemoteDescriptor(Descriptor remoteDescriptor) {
		Assert.isNotNull(remoteDescriptor);
		if (shouldReplace(this.remoteDescriptor, remoteDescriptor)) {
			this.remoteDescriptor = remoteDescriptor;
			if (this.defaultDescriptor == null) {
				this.defaultDescriptor = remoteDescriptor;
			}
		}
	}

	private boolean shouldReplace(Descriptor oldDescriptor, Descriptor newDescriptor) {
		Assert.isTrue(newDescriptor.isValid());
		if (oldDescriptor != null) {
			if (new Version(oldDescriptor.getVersion()).compareTo(new Version(newDescriptor.getVersion())) >= 0) {
				// keep more recent version
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return id;
	}

}
