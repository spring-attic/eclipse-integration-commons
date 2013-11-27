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
package org.springsource.ide.eclipse.commons.frameworks.core.util;

import org.w3c.dom.Node;

/**
 * Helper methods to access pieces of XMLData in a parsed Document.
 */
public class XmlUtils {

	public static String getTagName(Node labelNode) {
		if (labelNode.getNodeType()==Node.ELEMENT_NODE) {
			return labelNode.getNodeName();
		}
		return null;
	}

}
