/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.content.core.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.w3c.dom.Node;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class ContentUtil {

	private static boolean SPRING_IDE_AVAILABLE;

	static {
		try {
			ContentUtil.class.getClassLoader().loadClass("org.springframework.ide.eclipse.core.SpringCoreUtils");
			SPRING_IDE_AVAILABLE = true;
		}
		catch (Throwable e) {
			SPRING_IDE_AVAILABLE = false;
		}
	}

	public static DocumentBuilder createDocumentBuilder() throws CoreException {
		if (SPRING_IDE_AVAILABLE) {
			return SpringCoreUtils.getDocumentBuilder();
		}
		else {
			DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
			xmlFact.setExpandEntityReferences(false);
			try {
				return xmlFact.newDocumentBuilder();
			}
			catch (ParserConfigurationException e) {
				throw new CoreException(null);
			}
		}
	}

	public static Transformer createTransformer() throws CoreException {
		TransformerFactory factory = TransformerFactory.newInstance();
		try {
			return factory.newTransformer();
		}
		catch (TransformerConfigurationException e) {
			throw new CoreException(
					new Status(Status.ERROR, ContentPlugin.PLUGIN_ID, "Could not create transformer", e));
		}

	}

	public static String getAttributeValue(Node node, String attribute) {
		Node item = node.getAttributes().getNamedItem(attribute);
		return (item != null) ? item.getNodeValue() : null;
	}

	public static String getTextValue(Node node) {
		String text = node.getTextContent().trim();
		text = text.replaceAll("\\n", "");
		text = text.replaceAll("\\s+", " ");
		return text;
	}

}
