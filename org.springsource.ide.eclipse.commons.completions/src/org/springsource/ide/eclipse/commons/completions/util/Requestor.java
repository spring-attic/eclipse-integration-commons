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
package org.springsource.ide.eclipse.commons.completions.util;

/**
 * An entity that can request elements of type T. The elements are delivered
 * one by one to the Requestor as they are being discovered / found.
 */
public abstract class Requestor<T> {
	
	/**
	 * Called when an element is available. The requestor may return 'false' to indicate it
	 * doesn't want any more elements (allowing the producer of the elements to stop 
	 * producing elements needlessly). 
	 */
	public abstract boolean receive(T element);
		
	/**
	 * Recieve a series of elements from a Collection or another Iterable.
	 */
	public final boolean receive(Iterable<T> elements) {
		boolean wantMore = true;
		for (T e : elements) {
			wantMore = receive(e);
			if (!wantMore) {
				return false;
			}
		}
		return wantMore;
	}

}
