/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.externaltype.examples;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeEntry;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeSource;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;

/**
 * Sample implementation of {@link ExternalTypeDiscovery}. It simply provides types
 * from a list of fully qualified names provided via the constructor.
 * <p>
 * The types are purley fictional and don't come from a real source so the source
 * doesn't implement the bit of adding itself to the project's classpath.
 * 
 * @author Kris De Volder
 */
public class DemoTypeDiscovery implements ExternalTypeDiscovery, IExecutableExtension {

	/**
	 * Type source for a 'fictional' type. There's nothing to add to a project classpath since the
	 * types in this DemoTypeS doesn't actually exist
	 */
	private static final ExternalTypeSource DUMMY_SOURCE = new ExternalTypeSource() {
		
		@Override
		public void addToClassPath(IJavaProject project, IProgressMonitor mon) {
			//nothing to do
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	private String[] fqNames;

	public DemoTypeDiscovery(String... fqTypeNames) {
		this.fqNames = fqTypeNames;
	}

	/**
	 * When using via a extension point we need a 0 arg consutructor. The fq strings have to
	 * be provided in a different way (i.e. via {@link IExecutableExtension}.setInitializationData
	 */
	public DemoTypeDiscovery() {
	}

	@Override
	public void getTypes(Requestor<ExternalTypeEntry> requestor) {
		for (String fqName : fqNames) {
			int split = fqName.lastIndexOf('.');
			if (split>0) {
				requestor.receive(entry(
						fqName.substring(split+1), 
						fqName.substring(0,split)
				));
			}
		}
	}

	private ExternalTypeEntry entry(String name, String pkg) {
		ExternalType type = new ExternalType(name, pkg);
		return new ExternalTypeEntry(type, DUMMY_SOURCE);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (data instanceof String) {
			this.fqNames = ((String) data).split(":");
		}
	}

}
