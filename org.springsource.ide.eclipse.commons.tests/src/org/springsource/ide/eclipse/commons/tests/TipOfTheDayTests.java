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
package org.springsource.ide.eclipse.commons.tests;

import junit.framework.TestCase;

import org.springsource.ide.eclipse.commons.ui.tips.TipProvider;

/**
 * tests for the Tip of the day support
 * @author Andrew Eisenberg
 * @since 3.3.0
 */
public class TipOfTheDayTests extends TestCase {
	// test that tips file has correct syntax
	public void testTipProvider() throws Exception {
		TipProvider provider = new TipProvider();
		provider.refresh();
		if (provider.getError() != null) {
			throw provider.getError();
		}
	}
}
