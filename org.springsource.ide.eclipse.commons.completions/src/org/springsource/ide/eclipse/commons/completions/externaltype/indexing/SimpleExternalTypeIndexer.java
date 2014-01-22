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
package org.springsource.ide.eclipse.commons.completions.externaltype.indexing;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeEntry;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeSource;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;

/**
 * Extremely simplistic implementation of ExternalTypeIndexer based on in memory TreeMap.
 * While fast, this implementation probably doesn't scale well in terms of memory
 * performance.
 * 
 * @author Kris De Volder
 */
public class SimpleExternalTypeIndexer extends ExternalTypeIndexer {

	private TreeMap<ExternalType, ExternalTypeSource> allKnownTypes = new TreeMap<ExternalType, ExternalTypeSource>();
	
	public SimpleExternalTypeIndexer() {
	}
	
	private synchronized void add(ExternalType et, ExternalTypeSource source) {
		allKnownTypes.put(et, source);
	}
	
	@Override
	public synchronized void getAll(Requestor<ExternalType> requestor) {
		requestor.receive(allKnownTypes.keySet());
	}
	
	@Override
	public synchronized void getByPrefix(String prefix, Requestor<ExternalType> requestor) {
		System.out.println(">>> getByPrefix: "+prefix);
		try {
			ExternalType prefixKey = new ExternalType(prefix, "");
			NavigableMap<ExternalType, ExternalTypeSource> tailMap = allKnownTypes.tailMap(prefixKey, true);
			for (ExternalType type : tailMap.keySet()) {
				if (type.getName().startsWith(prefix)) { 
					//still a match
					boolean wantsMore = requestor.receive(type);
					System.out.println(type);
					if (!wantsMore) {
						return;
					}
				} else {
					//no longer a match... done!
					return;
				}
			}
		} finally {
			System.out.println(">>> getByPrefix: "+prefix);
		}
	}

	@Override
	public void addFrom(final ExternalTypeDiscovery source) {
		source.getTypes(new Requestor<ExternalTypeEntry>() {
			public boolean receive(ExternalTypeEntry element) {
				add(element.getType(), element.getSource());
				return true;
			}
		});
	}

	@Override
	public synchronized ExternalTypeSource getSource(ExternalType type) {
		ExternalTypeSource source = allKnownTypes.get(type);
		if (source!=null) {
			return source;
		}
		return ExternalTypeSource.UNKNOWN;
	}

}
