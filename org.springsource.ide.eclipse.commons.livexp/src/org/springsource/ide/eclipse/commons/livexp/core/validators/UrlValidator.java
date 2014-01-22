/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core.validators;

import java.net.URI;
import java.net.URISyntaxException;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * Basic validator for URLs. It merely verifies that the String can be parsed into a URI instance.
 * Some tolerance for leading and trailing whitespace is built-in (i.e. leading / trailing white space
 * is stripped off before validating).
 * 
 * A null value is treated the same as the empty String.
 * 
 * @author Kris De Volder
 */
public class UrlValidator extends LiveExpression<ValidationResult> {

	private String fieldName;
	private LiveVariable<String> url;
	private boolean nullable;

	public UrlValidator(String fieldName, LiveVariable<String> url) {
		this(fieldName, url, false);
	}
	
	public UrlValidator(String fieldName, LiveVariable<String> url, boolean nullable) {
		this.fieldName = fieldName;
		this.url = url;
		this.nullable = nullable;
	}

	@Override
	protected ValidationResult compute() {
		String str = url.getValue();
		if (str==null) {
			str = "";
		} else {
			str = str.trim();
		}
		if (nullable && str.equals("") ) {
			return ValidationResult.OK;
		} else {
			try {
				new URI(str);
			} catch (URISyntaxException e) {
				return ValidationResult.error(fieldName+" is not a valid URL: URISyntaxException "+e.getMessage());
			}
		}
		return ValidationResult.OK;
	}

}
