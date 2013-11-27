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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.compiler.apt.model.Factory;
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
	 * For now we only keep one index. If more than is need accross the projects in the workspace
	 * this may be very bad becayse the index is thrown away and rebuilt each time we have to switch.
	 * <p>
	 * With the current implementers of this extension point however it is ok because there
	 * is only one and it always provides discovery that is shareable between all projects it applies to.
	 */
	private ExternalTypeIndexer index = null;
	
	/**
	 * This 'key' should identify the content of an indexer so that if the key changes (not equals) then 
	 * the indexer is no longer valid and the index should be rebuilt.
	 * <p>
	 * For now the assumption is that mostly all projects have a non-empty index will be sharing it.
	 * This is true for now, but maybe not always be true in the future if different implementations
	 * of the extension point provide disovery algorithms that vary depending on project type,
	 * library versions etc.
	 */
	private Set<ExternalTypeDiscovery> contentKey = null;
	
	public synchronized ExternalTypeIndexer getIndexFor(IJavaProject project) {
		HashSet<ExternalTypeDiscovery> newContentKey = new HashSet<ExternalTypeDiscovery>();
		for (ExternalTypeDiscoveryFactory factory : getFactories()) {
			ExternalTypeDiscovery discovery = factory.discoveryFor(project);
			if (discovery!=null) {
				newContentKey.add(discovery);
			}
		}
		if (newContentKey.isEmpty()) {
			//A useful special case: if none of the factories applies to our current project...
			// the empty index is pretty cheap so don't throw away a more valuable index because of it.
			return ExternalTypeIndexer.EMPTY;
		}
		if (index!=null && contentKey.equals(newContentKey)) {
			return index;
		}
		//Must (re)build the index
		//TODO: consider building index asynchronously if it takes too much time.
		contentKey = newContentKey;
		index = new SimpleExternalTypeIndexer();
		for (ExternalTypeDiscovery discovery : contentKey) {
			index.addFrom(discovery);
		}
		return index;
	}

	private static List<ExternalTypeDiscoveryFactory> factories = null;	
	
	/**
	 * Read the extension point.
	 */
	private static synchronized List<ExternalTypeDiscoveryFactory> getFactories() {
		//TODO: natures info not utilized at the moment. Use it to 
		// avoid instantiation of factories we know are not applicable to project of interest.
		if (factories==null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] confEls = registry.getConfigurationElementsFor(TYPE_SOURCES_EXTENSION_POINT);
			for (IConfigurationElement confEl : confEls) {
				String name = confEl.getName();
				if ("typeSource".equals(name)) {
					try {
						ExternalTypeDiscoveryFactory factory = (ExternalTypeDiscoveryFactory) confEl.createExecutableExtension("class");
						if (factories==null) {
							// We may loose some confEls from errors but confEls.length is still a good guess 
							//  for the size of the arraylist
							factories = new ArrayList<ExternalTypeDiscoveryFactory>(confEls.length);
						}
						factories.add(factory);
					} catch (CoreException e) {
						CompletionsActivator.log(e);
					}
				}
			}
		}
		return factories;
	}

}
