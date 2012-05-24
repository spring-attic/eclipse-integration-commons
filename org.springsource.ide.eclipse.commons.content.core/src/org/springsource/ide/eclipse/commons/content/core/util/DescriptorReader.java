/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.content.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads tutorial descriptions from a file.
 * @author Terry Denney
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class DescriptorReader {

	private static final String TAG_DESCRIPTORS = "descriptors";

	private static final String TAG_DESCRIPTOR = "descriptor";

	private final List<Descriptor> descriptors;

	public DescriptorReader() {
		descriptors = new ArrayList<Descriptor>();
	}

	public List<Descriptor> getDescriptors() {
		return Collections.unmodifiableList(descriptors);
	}

	private List<Descriptor> read(Document document) throws SAXException {
		Element rootNode = document.getDocumentElement();
		if (rootNode == null) {
			throw new SAXException("No root node");
		}

		List<Descriptor> newDescriptors = new ArrayList<Descriptor>(1);
		NodeList children = rootNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				if (TAG_DESCRIPTOR.equals(childNode.getNodeName())) {
					Descriptor descriptor = Descriptor.read(childNode);
					newDescriptors.add(descriptor);
					descriptors.add(descriptor);
				}
			}
		}
		return newDescriptors;
	}

	public List<Descriptor> read(File file) throws CoreException {
		try {
			return read(new FileInputStream(file));
		}
		catch (IOException e) {
			throw new CoreException(
					new Status(Status.ERROR, ContentPlugin.PLUGIN_ID, "Reading of descriptor failed", e));
		}
	}

	public List<Descriptor> read(InputStream in) throws CoreException {
		DocumentBuilder documentBuilder = ContentUtil.createDocumentBuilder();
		Document document = null;
		try {
			document = documentBuilder.parse(in);
			return read(document);
		}
		catch (SAXException e) {
			throw new CoreException(
					new Status(Status.ERROR, ContentPlugin.PLUGIN_ID, "Reading of descriptor failed", e));
		}
		catch (IOException e) {
			throw new CoreException(
					new Status(Status.ERROR, ContentPlugin.PLUGIN_ID, "Reading of descriptor failed", e));
		}
	}

	public void write(File file) throws CoreException {
		DocumentBuilder documentBuilder = ContentUtil.createDocumentBuilder();
		Transformer serializer = ContentUtil.createTransformer();
		Document document = documentBuilder.newDocument();
		writeDocument(document);
		DOMSource source = new DOMSource(document);
		try {
			StreamResult target = new StreamResult(file);
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.transform(source, target);
		}
		catch (TransformerException e) {
			throw new CoreException(new Status(Status.ERROR, ContentPlugin.PLUGIN_ID,
					"Could not write initialization data for tutorial"));
		}
	}

	private void writeDocument(Document document) {
		Element rootNode = document.createElement(TAG_DESCRIPTORS);
		document.appendChild(rootNode);

		for (Descriptor descriptor : descriptors) {
			Element childNode = document.createElement(TAG_DESCRIPTOR);
			rootNode.appendChild(childNode);
			Descriptor.write(descriptor, childNode);
		}
	}

}
