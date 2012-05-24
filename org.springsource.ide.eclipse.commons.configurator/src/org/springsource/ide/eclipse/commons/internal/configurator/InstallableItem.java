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
package org.springsource.ide.eclipse.commons.internal.configurator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.springsource.ide.eclipse.commons.core.CoreUtil;
import org.springsource.ide.eclipse.commons.internal.configurator.operations.AbstractInstallOperation;
import org.springsource.ide.eclipse.commons.internal.configurator.operations.ChmodOperation;
import org.springsource.ide.eclipse.commons.internal.configurator.operations.CopyOperation;


/**
 * @author Steffen Pingel
 */
public class InstallableItem {

	private static final String ATTR_CONFIGURATOR = "configurator";

	private static final String ATTR_ID = "id";

	private static final String ATTR_LABEL = "label";

	private static final String ATTR_TARGET = "target";

	private static final String ELEMENT_CHMOD = "chmod";

	private static final String ELEMENT_COPY = "copy";

	private static String readId(IConfigurationElement element) {
		Bundle bundle = Platform.getBundle(element.getContributor().getName());
		String id = element.getAttribute(ATTR_ID);
		if (id == null) {
			id = bundle.getSymbolicName() + "-" + bundle.getVersion();
		}
		else {
			Properties properties = new Properties();
			properties.setProperty("package", bundle.getSymbolicName());
			properties.setProperty("version", bundle.getVersion().toString());
			id = CoreUtil.substitute(id, properties);
		}
		return id;
	}

	private final IConfigurationElement element;

	private final String id;

	private String name;

	public InstallableItem(IConfigurationElement element) {
		this.id = readId(element);
		this.element = element;
		String label = element.getAttribute(ATTR_LABEL);
		if (label != null) {
			setName(label);
		}
		else {
			Bundle bundle = Platform.getBundle(element.getContributor().getName());
			setName((String) bundle.getHeaders().get("Bundle-Name"));
		}
	}

	public String getConfiguratorId() {
		return element.getAttribute(ATTR_CONFIGURATOR);
	}

	public String getTarget() {
		String target = element.getAttribute(ATTR_TARGET);
		if (target == null) {
			return getId();
		}
		return target;
	}

	public String getId() {
		return id;
	}

	public List<AbstractInstallOperation> getInstallOperations() {
		IConfigurationElement[] elements = element.getChildren();
		List<AbstractInstallOperation> operations = new ArrayList<AbstractInstallOperation>(elements.length);
		for (IConfigurationElement element : elements) {
			if (element.getName().equals(ELEMENT_COPY)) {
				operations.add(new CopyOperation(element));
			}
			else if (element.getName().equals(ELEMENT_CHMOD)) {
				operations.add(new ChmodOperation(element));
			}
		}
		return operations;
	}

	public String getName() {
		return name;
	}

	public IStatus install(File base, IProgressMonitor monitor) {
		File targetLocation = getTargetLocation(base);
		targetLocation.mkdirs();

		MultiStatus result = new MultiStatus(Activator.PLUGIN_ID, 0, NLS.bind("Installation of {0} failed", getName()),
				null);
		List<AbstractInstallOperation> operations = getInstallOperations();
		SubMonitor progress = SubMonitor.convert(monitor, NLS.bind("Installing {0}", getName()), operations.size());
		for (AbstractInstallOperation operation : operations) {
			try {
				operation.setSourceBase(getSourceLocation());
			}
			catch (CoreException e) {
				return e.getStatus();
			}
			operation.setTargetBase(targetLocation);
			IStatus status = operation.install(progress.newChild(1));
			result.add(status);
		}
		return result;
	}

	public File getTargetLocation(File base) {
		return new File(base, getTarget());
	}

	public boolean isInstallable() {
		return getInstallOperations().size() > 0;
	}

	public void setName(String name) {
		this.name = name;
	}

	private File getSourceLocation() throws CoreException {
		Bundle bundle = Platform.getBundle(element.getContributor().getName());
		try {
			return FileLocator.getBundleFile(bundle);
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to determine install location", e));
		}
	}

}
