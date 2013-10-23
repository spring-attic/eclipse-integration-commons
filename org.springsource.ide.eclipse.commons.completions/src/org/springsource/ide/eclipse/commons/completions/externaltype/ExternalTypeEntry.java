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
package org.springsource.ide.eclipse.commons.completions.externaltype;

/**
 * Data object containing an ExternalType and a ExternalTypeSource.
 * 
 * @author Kris De Volder
 */
public class ExternalTypeEntry {

	private final ExternalType type;
	private final ExternalTypeSource source;
	
	public ExternalTypeEntry(ExternalType type, ExternalTypeSource source) {
		super();
		this.type = type;
		this.source = source==null?ExternalTypeSource.UNKNOWN:source;
	}

	public ExternalTypeEntry(String fqName, ExternalTypeSource typeSource) {
		this(new ExternalType(fqName), typeSource);
	}

	@Override
	public String toString() {
		return "ExtType["+type.getFullyQualifiedName()+ " <=  "+source+"]";
	}

	public ExternalTypeSource getSource() {
		return source;
	}

	public ExternalType getType() {
		return type;
	}
	
}
