/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.help;

import java.util.Collection;

import junit.framework.TestCase;

import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Terry Denney
 * @author Tomasz Zarna
 */
public class HelpPluginTest extends TestCase {

	public void testGetSampleProjects() {
		ContentManager manager = ContentPlugin.getDefault().getManager();
		if (manager.isDirty()) {
			// refresh to download remote descriptors
			manager.refresh(null, true);
		}

		Collection<ContentItem> projects = manager.getItemsByKind(ContentManager.KIND_SAMPLE);

		assertEquals(3, projects.size());
	}
}
