/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.core.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.springsource.ide.eclipse.commons.quicksearch.util.LineReader;

/**
 * Helper class to access the QuickSearch Preferences.
 *
 * @author Kris De Volder
 */
public class QuickSearchPreferences {

	//Keys used to fetch 'raw' preferences values from the preferences store.
	public static final String IGNORED_EXTENSIONS = "ignored.extensions";
	public static final String IGNORED_NAMES = "ignored.names";
	public static final String IGNORED_PREFIXES = "ignored.prefixes";
	public static final String MAX_LINE_LEN = "LineReader.MAX_LINE_LEN";

	private IEclipsePreferences store;

	public QuickSearchPreferences(IEclipsePreferences store) {
		this.store = store;
	}

	public IEclipsePreferences getStore() {
		return store;
	}

	public String[] getIgnoredExtensions() {
		return getAndParseStringList(IGNORED_EXTENSIONS);
	}

	public String[] getIgnoredPrefixes() {
		return getAndParseStringList(IGNORED_PREFIXES);
	}

	public String[] getIgnoredNames() {
		return getAndParseStringList(IGNORED_NAMES);
	}

	public int getMaxLineLen() {
		return store.getInt(MAX_LINE_LEN, LineReader.DEFAULT_MAX_LINE_LENGTH);
	}

	private String[] getAndParseStringList(String key) {
		String raw = store.get(key, null);
		if (raw!=null) {
			return parseStringList(raw);
		}
		return null;
	}

	/**
	 * Takes a raw string list as entered in the prefs page input field and parses it.
	 * <p>
	 * Commas and newline are treated as 'separators' between elements. Further, any trailing
	 * and leading whitespace is stripped from individual elements and empty strings are silently
	 * dropped.
	 */
	private String[] parseStringList(String raw) {
		String[] elements = raw.split("[,\n]");
		List<String> list = new ArrayList<String>(elements.length);
		for (String e : elements) {
			e = e.trim();
			if (!"".equals(e)) {
				list.add(e);
			}
		}
		return list.toArray(new String[list.size()]);
	}


}
