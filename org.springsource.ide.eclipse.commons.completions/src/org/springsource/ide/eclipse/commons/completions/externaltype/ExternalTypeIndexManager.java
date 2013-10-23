/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.externaltype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.springsource.ide.eclipse.commons.completions.CompletionsActivator;
import org.springsource.ide.eclipse.commons.completions.externaltype.indexing.ExternalTypeIndexer;
import org.springsource.ide.eclipse.commons.completions.externaltype.indexing.SimpleExternalTypeIndexer;

/**
 * Only a single instance of this class typically exists. Its purpose is to maintain a mapping between
 * {@link IJavaProject} instances and their respective ExternalType indexes.
 * <p>
 * External types are project specific. 
 * 
 * @author Kris De Volder
 */
public class ExternalTypeIndexManager {
		
	private static final String TYPE_SOURCES_EXTENSION_POINT = "org.spring.ide.eclipse.completions.externaltypes";
	
	private static final ExternalTypeIndexManager INSTANCE = new ExternalTypeIndexManager();
	
	public static synchronized ExternalTypeIndexer indexFor(IJavaProject project) {
		return INSTANCE.getIndexFor(project);
	}

	/**
	 * For now it is impossible to differentiate projects. So there's only one indexer.
	 * In the future indexers will assigned to projects based on the set of ExternalTypeSources
	 * that is associated with each project. This implementati will then be replaced.
	 */
	private ExternalTypeIndexer indexer = null;
	
	public synchronized ExternalTypeIndexer getIndexFor(IJavaProject project) {
		if (indexer==null) {
			indexer = new SimpleExternalTypeIndexer();
		}
		return indexer;
	}

	/**
	 * Add type sources from the extensions point registry.
	 */
	private static void addSources(ExternalTypeIndexer indexer) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] confEls = registry.getConfigurationElementsFor(TYPE_SOURCES_EXTENSION_POINT);
		for (IConfigurationElement confEl : confEls) {
			String name = confEl.getName();
			if ("typeSource".equals(name)) {
				try {
					ExternalTypeDiscovery ets = (ExternalTypeDiscovery) confEl.createExecutableExtension("class");
					indexer.addFrom(ets);
				} catch (CoreException e) {
					CompletionsActivator.log(e);
				}
			}
		}
	}
//
//	/**
//	 * Add a source of external types. All the types in this source will become discoverable
//	 * via this manager. However they may not be available immediately. This is to allow
//	 * for asynchronous processing so that parsing a whole bunch of jars (for example)
//	 * doesn't block everything.
//	 */
//	public abstract void addSource(ExternalTypeDiscovery source);
//
//	public void getByPrefix(final String prefix, final Requestor<ExternalType> requestor) {
//		//default implementation is stupid and not indexed or cached in any way.
//		getAll(new Requestor<ExternalType>() {
//			public boolean receive(ExternalType element) {
//				if (element.getName().startsWith(prefix)) {
//					return requestor.receive(element);
//				}
//				return true;
//			}
//		});
//	}
//	
//	public abstract void getAll(Requestor<ExternalType> requestor);
//
//	public abstract ExternalTypeSource getSource(ExternalType type);

}
