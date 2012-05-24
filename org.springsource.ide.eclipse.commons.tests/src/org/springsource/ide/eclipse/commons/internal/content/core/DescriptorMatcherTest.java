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
package org.springsource.ide.eclipse.commons.internal.content.core;

import junit.framework.TestCase;

import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;
import org.springsource.ide.eclipse.commons.internal.content.core.DescriptorMatcher;


/**
 * @author Steffen Pingel
 */
public class DescriptorMatcherTest extends TestCase {

	private Descriptor descriptor;

	private DescriptorMatcher matcher;

	@Override
	protected void setUp() throws Exception {
		descriptor = new Descriptor();
		System.setProperty("com.springsource.sts", "abc");
		matcher = new DescriptorMatcher(new ContentManager());
		matcher.setVersion(null);
	}

	public void testMatchFilter() {
		descriptor.setRequires("1.0.0");
		assertTrue(matcher.match(descriptor));
		descriptor.setFilter("(com.springsource.sts=abc)");
		assertTrue(matcher.match(descriptor));
		descriptor.setFilter("(com.springsource.sts=def)");
		assertFalse(matcher.match(descriptor));
	}

	public void testMatchRequiresNullVersion() {
		assertTrue(matcher.match(descriptor));
		descriptor.setRequires("1.0.0");
		assertTrue(matcher.match(descriptor));
		descriptor.setRequires("1.0.0");
		assertTrue(matcher.match(descriptor));
	}

	public void testMatchRequiresVersion() {
		matcher.setVersion(new Version("2.0.0"));

		assertTrue(matcher.match(descriptor));
		descriptor.setRequires("[1.0.0,2.0.0)");
		assertFalse(matcher.match(descriptor));
		descriptor.setRequires("[1.0.0,2.0.0]");
		assertTrue(matcher.match(descriptor));
	}

}
