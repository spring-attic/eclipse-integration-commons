/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springsource.ide.eclipse.commons.core.util.NameGenerator;

/**
 * Tests for a basic name generator
 * 
 * @author Alex Boyko
 *
 */
public class NameGeneratorTest {
	
	@Test
	public void testDefault() {
		NameGenerator ng = new NameGenerator("name");
		assertEquals("name-1", ng.generateNext());
		assertEquals("name-2", ng.generateNext());
		assertEquals("name-3", ng.generateNext());
	}

	@Test
	public void testNameWithDash() {
		NameGenerator ng = new NameGenerator("name-blah");
		assertEquals("name-blah-1", ng.generateNext());
		assertEquals("name-blah-2", ng.generateNext());
		assertEquals("name-blah-3", ng.generateNext());
	}

	@Test
	public void testNumberIncrement() {
		NameGenerator ng = new NameGenerator("name-1");
		assertEquals("name-2", ng.generateNext());
		assertEquals("name-3", ng.generateNext());
		assertEquals("name-4", ng.generateNext());
	}

	@Test
	public void testNumberIncrement2() {
		NameGenerator ng = new NameGenerator("name-22");
		assertEquals("name-23", ng.generateNext());
		assertEquals("name-24", ng.generateNext());
		assertEquals("name-25", ng.generateNext());
	}

	@Test
	public void testNumberIncrementWithDash() {
		NameGenerator ng = new NameGenerator("foo-bar-1");
		assertEquals("foo-bar-2", ng.generateNext());
		assertEquals("foo-bar-3", ng.generateNext());
		assertEquals("foo-bar-4", ng.generateNext());
	}
}
