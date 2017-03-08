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
		NameGenerator ng = NameGenerator.create("name", "");
		assertEquals("name", ng.generateNext());
		assertEquals("name1", ng.generateNext());
		assertEquals("name2", ng.generateNext());
	}

	@Test
	public void testSimpleDelimiter() {
		NameGenerator ng = NameGenerator.create("name", "-");
		assertEquals("name", ng.generateNext());
		assertEquals("name-1", ng.generateNext());
		assertEquals("name-2", ng.generateNext());
	}

	@Test
	public void testMultiCharDelimiter() {
		NameGenerator ng = NameGenerator.create("name", "---@");
		assertEquals("name", ng.generateNext());
		assertEquals("name---@1", ng.generateNext());
		assertEquals("name---@2", ng.generateNext());
	}

	@Test
	public void testFromSimplePrevious() {
		NameGenerator ng = NameGenerator.createFromPrevious("name");
		assertEquals("name1", ng.generateNext());
		assertEquals("name2", ng.generateNext());
		assertEquals("name3", ng.generateNext());
	}

	@Test
	public void testFromComplexPrevious_1() {
		NameGenerator ng = NameGenerator.createFromPrevious("name___123___");
		assertEquals("name___123___1", ng.generateNext());
		assertEquals("name___123___2", ng.generateNext());
		assertEquals("name___123___3", ng.generateNext());
	}

	@Test
	public void testFromComplexPrevious_2() {
		NameGenerator ng = NameGenerator.createFromPrevious("name___123___-45");
		assertEquals("name___123___-46", ng.generateNext());
		assertEquals("name___123___-47", ng.generateNext());
		assertEquals("name___123___-48", ng.generateNext());
	}
}
