/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.externaltype.indexing;

import org.eclipse.core.runtime.Assert;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeSource;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;

/**
 * An ExternalTypeIndexer instance is responsible for building up a searchable index
 * mapping {@link ExternalType}s to a {@link ExternalTypeSource}s.
 * <p>
 * Building the index is a potentially very long operation and should be done asyncronously.
 * Also methods that allow clients to consult the index should be implemented in such a way
 * that they can be used safely on an incomplete index, without blocking for extended periods of
 * time.
 * 
 * @author Kris De Volder
 */
public abstract class ExternalTypeIndexer {

	/**
	 * This provides the means to iterate all external types currently in the index.
	 */
	public abstract void getAll(Requestor<ExternalType> requestor);

	/**
	 * Add a bunch of types to the indexer. This can potentially take a very long time. 
	 * Thus this operation is normally implemented asynchronously. I.e. types will be indexed 
	 * eventually but may not be indexed before the method returns.
	 */
	public abstract void addFrom(ExternalTypeDiscovery ets);

	/**
	 * Subclasses realy *should* override this method. The implementation here serves only
	 * as a 'specification' of the expected behavior but it is highly ineffecient. A proper
	 * indexed implementation should use the index to avoid iterating everything.
	 */
	public void getByPrefix(final String prefix, final Requestor<ExternalType> requestor) {
		getAll(new Requestor<ExternalType>() {
			public boolean receive(ExternalType element) {
				if (element.getName().startsWith(prefix)) {
					return requestor.receive(element);
				}
				return true;
			}
		});
	}

	/**
	 * Find an ExternalTypeSource associated with the given type. If more than one source exists 
	 * an arbitrary source is returned. If no source is found the {@link ExternalTypeSource}.UNKNOWN
	 * instance is returned.
	 */
	public abstract ExternalTypeSource getSource(ExternalType type);
	

	/**
	 * TRivial implementation of an empty index. You can't store anything in it and requesting any
	 * information from it returns nothing.
	 * <p>
	 * Usually it is more appropriate to use this instance instead of 'null'. As it won't cause NPEs
	 * when trying to retrieve data from it.
	 */
	public static final ExternalTypeIndexer EMPTY = new ExternalTypeIndexer() {
		
		@Override
		public ExternalTypeSource getSource(ExternalType type) {
			return null;
		}
		
		@Override
		public void getAll(Requestor<ExternalType> requestor) {
			//Nothing to do.
		}
		
		@Override
		public void addFrom(ExternalTypeDiscovery ets) {
			Assert.isLegal(false, "EMPTY index is immutable");
		}
	};
	
}
