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
public class StringFieldModel extends FieldModel<String> {
	
	
	public StringFieldModel(String name, String defaultValue) {
		super(String.class, name, defaultValue);
	}
	
	
}
