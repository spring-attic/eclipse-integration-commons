/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

/**
 * @author Kris De Volder
 */
public abstract class Validator extends LiveExpression<ValidationResult> {

	public static final LiveExpression<ValidationResult> OK = constant(ValidationResult.OK);

	public Validator() {
		super(ValidationResult.OK);
	}

	public static <T> LiveExpression<ValidationResult> notNull(final LiveExpression<T> target, final String errorMessage) {
		Validator v = new Validator() {
			@Override
			protected ValidationResult compute() {
				if (target.getValue()==null) {
					return ValidationResult.error(errorMessage);
				} else {
					return ValidationResult.OK;
				}
			}
		};
		v.dependsOn(target);
		return v;
	}

	/**
	 * Create a trivial validator that always has the same error message.
	 */
	public static LiveExpression<ValidationResult> alwaysError(String msg) {
		return LiveExpression.constant(ValidationResult.error(msg));
	}

}
