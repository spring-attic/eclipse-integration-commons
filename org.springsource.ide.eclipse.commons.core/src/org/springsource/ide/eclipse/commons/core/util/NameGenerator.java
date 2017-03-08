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
package org.springsource.ide.eclipse.commons.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple name generator implementation
 *
 * @author Alex Boyko
 *
 */
public class NameGenerator {

	private static Pattern EXAMPLE_PATTERN = Pattern.compile("^(.+\\D+)(\\d+)$");

	private final String prefix;
	private final String delimiter;
	private int number = 0;

	private NameGenerator(String prefix, String delimiter, int number) {
		this.prefix = prefix;
		this.delimiter = delimiter;
		this.number = number;
	}

	/**
	 * Generates the next name
	 * @return next name
	 */
	public String generateNext() {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		if (number > 0) {
			sb.append(delimiter);
			sb.append(number);
		}
		number++;
		return sb.toString();
	}

	/**
	 * Creates name generator able to generate names of type <code>|prefix|delimiter|?number</code>. For example <code>create("name", "---")</code> would generate names:
	 * <ul>
	 * <li>name</li>
	 * <li>name---1</li>
	 * <li>name---2</li>
	 * <li>...</li>
	 * </ul>
	 * @param prefix name prefix
	 * @param delimiter delimiter between the prefix and the number part of the name
	 * @return The <code>NameGenerator</code> able to generate names described above
	 */
	public static NameGenerator create(String prefix, String delimiter) {
		return new NameGenerator(prefix, delimiter, 0);
	}

	/**
	 * Creates <code>NameGenerator</code> based on the "previous" name. For example <code>createFromPrevious("name---56")</code> would generate names:
	 * <ul>
	 * <li>name---57</li>
	 * <li>name---58</li>
	 * <li>name---59</li>
	 * <li>...</li>
	 * </ul>
	 * @param previousName Previously generated name
	 * @return The <code>NameGenerator</code> able to generate names described above
	 */
	public static NameGenerator createFromPrevious(String previousName) {
		Matcher matcher = EXAMPLE_PATTERN.matcher(previousName);
		if (matcher.find()) {
			String prefix = matcher.group(1);
			int number = Integer.valueOf(matcher.group(2));
			return new NameGenerator(prefix, "", number + 1);
		} else {
			return new NameGenerator(previousName, "", 1);
		}
	}

}
