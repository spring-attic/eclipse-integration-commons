/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;


/**
 * General purpose 'field' model.
 * 
 * For now this assumes the value of the field is a String. As need be in the future
 * we may need to generalize to support values of other types. That will require
 * abstracting out t
 * 
 * @author Kris De Volder
 */
public class StringFieldModel {
	
	private String label; // Label to display in forms
	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}

	private String name; // used to submit value to some service that handles the form
	private LiveVariable<String> variable;
	private LiveExpression<ValidationResult> validator;
	
	public StringFieldModel(String name, String defaultValue) {
		this.name  = name;
		this.label = name;
		this.variable = new LiveVariable<String>(defaultValue==null?"":defaultValue);
		this.validator = Validator.OK;
	}
	
	/**
	 * Specify label (used in forms). By default the name is used.
	 * @return The receiver for easy chaining.
	 */
	public StringFieldModel label(String l) {
		this.label = l;
		return this;
	}

	/**
	 * Specify different validator. The default validator is one that 
	 * accepts any input as valid.
	 * @return The receiver for easy chaining.
	 */
	public StringFieldModel validator(LiveExpression<ValidationResult> v) {
		this.validator = v;
		return this;
	}

	public void setValue(String v) {
		this.variable.setValue(v);
	}
	
	public String getValue() {
		return variable.getValue();
	}
	
	public LiveVariable<String> getVariable() {
		return variable;
	}
	
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}
	
}
