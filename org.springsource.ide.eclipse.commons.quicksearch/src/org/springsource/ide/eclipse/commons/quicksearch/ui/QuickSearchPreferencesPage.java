/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.ui;

import java.util.ArrayList;
import java.util.List;

import org.springsource.ide.eclipse.commons.livexp.ui.PreferencePageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsPageSection;

public class QuickSearchPreferencesPage extends PreferencePageWithSections {

	@Override
	protected List<PrefsPageSection> createSections() {
		List<PrefsPageSection> sections = new ArrayList<PrefsPageSection>();
		sections.add(new QuickSearchIgnoreSection(this));
		sections.add(new MaxLineLengthSection(this));
		return sections;
	}

}
