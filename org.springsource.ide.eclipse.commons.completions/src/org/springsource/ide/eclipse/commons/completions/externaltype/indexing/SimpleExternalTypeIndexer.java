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
package org.springsource.ide.eclipse.commons.completions.externaltype.indexing;

import java.util.TreeMap;

import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeEntry;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeIndexManager;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeSource;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;

/**
 * Extremely simplistic implementation of ExternalTypeIndexer based on in memory TreeMap.
 * While fast, this implementation probably doesn't scale well in terms of memory
 * performance. 
 * <p>
 * The plan is to use something like BrekelyDB eventually to 
 *  a) persist the index accross Eclipse sessions.
 *  b) reduce memory footprint.
 * 
 * @author Kris De Volder
 */
public class SimpleExternalTypeIndexer extends ExternalTypeIndexer {

	//TODO: implement getByIndex to use TreeMap slicing to avoid iterating the entire tree.
	
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
