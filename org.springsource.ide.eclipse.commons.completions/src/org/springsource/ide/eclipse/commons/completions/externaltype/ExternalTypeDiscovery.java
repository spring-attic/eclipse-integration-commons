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
package org.springsource.ide.eclipse.commons.completions.externaltype;

import org.springsource.ide.eclipse.commons.completions.util.Requestor;

/**
 * An instance of this interface represents an algorithm for discovering 'external types'.
 * 
 * @author Kris De Volder
 */
public interface ExternalTypeDiscovery {

	/**
	 * Request all available types from the type source. The types are returned to the
	 * requestor by calling its 'receive' method. It is assumbed by clients that the 
	 * call to getTypes method blocks until all elements are delivered to the 
	 * requestor (i.e. element delivery should *not* be asynchronous).
	 */
	public void getTypes(Requestor<ExternalTypeEntry> requestor);

}
