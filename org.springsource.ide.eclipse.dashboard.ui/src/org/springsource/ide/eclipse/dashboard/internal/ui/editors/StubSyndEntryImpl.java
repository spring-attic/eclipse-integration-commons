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
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import com.sun.syndication.feed.synd.SyndEntryImpl;

/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class StubSyndEntryImpl extends SyndEntryImpl {

	private static final long serialVersionUID = 1L;

	private final String text;

	public StubSyndEntryImpl(String description) {
		this.text = description;
	}

	public String getText() {
		return text;
	}
}
