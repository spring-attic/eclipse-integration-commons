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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Stores meta information about a downloadable item such as a tutorial or
 * sample project. Provides static helper methods for serialization.
 * @author Terry Denney
 * @author Steffen Pingel
 * @author Kaitlin Duck Sherwood
 */
public class Descriptor {

	/**
	 * Defines a dependency on a another downloadable item.
	 */
	public static class Dependency {

		private final String id;

		private final String version;

		public Dependency(String id) {
			this(id, null);
		}

		public Dependency(String id, String version) {
			this.id = id;
			this.version = version;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Dependency other = (Dependency) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			}
			else if (!id.equals(other.id)) {
				return false;
			}
			if (version == null) {
				if (other.version != null) {
					return false;
				}
			}
			else if (!version.equals(other.version)) {
				return false;
			}
			return true;
		}

		public String getId() {
			return id;
		}

		public String getVersion() {
			return version;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}

	}

	private static final String ATTRIBUTE_NAME = "name";

	private static final String ATTRIBUTE_ID = "id";

	private static final String ATTRIBUTE_VERSION = "version";

	private static final String ATTRIBUTE_CATEGORY = "category";

	private static final String ATTRIBUTE_SIZE = "size";

	private static final String ATTRIBUTE_URL = "url";

	private static final String ATTRIBUTE_KIND = "kind";

	private static final String ATTRIBUTE_LOCAL = "local";

	private static final String ATTRIBUTE_REQUIRES = "requires";

	private static final String ATTRIBUTE_REQUIRES_BUNDLE = "requiresbundle";

	private static final String ATTRIBUTE_FILTER = "filter";

	private static final String NODE_DESCRIPTION = "description";

	private static final String NODE_DEPENDENCY = "dependency";

	public static Descriptor read(Node node) throws SAXException {
		String id = ContentUtil.getAttributeValue(node, ATTRIBUTE_ID);
		if (id == null) {
			throw new SAXException("Missing id attribute for info");
		}
		String name = ContentUtil.getAttributeValue(node, ATTRIBUTE_NAME);
		if (name == null) {
			throw new SAXException("Missing summary attribute for name");
		}
		String version = ContentUtil.getAttributeValue(node, ATTRIBUTE_VERSION);
		if (version == null) {
			throw new SAXException("Missing summary attribute for version");
		}

		Descriptor info = new Descriptor();
		info.setId(id);
		info.setName(name);
		info.setVersion(version);

		String description = null;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);
			if (NODE_DESCRIPTION.equals(childNode.getNodeName())) {
				description = ContentUtil.getTextValue(childNode);
			}
			else if (NODE_DEPENDENCY.equals(childNode.getNodeName())) {
				String dependencyId = ContentUtil.getAttributeValue(childNode, ATTRIBUTE_ID);
				if (dependencyId == null) {
					// ignore
					continue;
				}
				Dependency dependency = new Dependency(dependencyId, ContentUtil.getAttributeValue(childNode,
						ATTRIBUTE_VERSION));
				info.addDependency(dependency);
			}
		}
		info.setDescription(description);

		info.setCategory(ContentUtil.getAttributeValue(node, ATTRIBUTE_CATEGORY));
		info.setUrl(ContentUtil.getAttributeValue(node, ATTRIBUTE_URL));
		info.setKind(ContentUtil.getAttributeValue(node, ATTRIBUTE_KIND));
		info.setFilter(ContentUtil.getAttributeValue(node, ATTRIBUTE_FILTER));
		info.setRequires(ContentUtil.getAttributeValue(node, ATTRIBUTE_REQUIRES));
		info.setRequiresBundle(ContentUtil.getAttributeValue(node, ATTRIBUTE_REQUIRES_BUNDLE));
		try {
			info.setSize(Long.parseLong(ContentUtil.getAttributeValue(node, ATTRIBUTE_SIZE)));
		}
		catch (NumberFormatException e) {
			info.setSize(0);
		}
		info.setLocal(Boolean.parseBoolean(ContentUtil.getAttributeValue(node, ATTRIBUTE_LOCAL)));
		return info;
	}

	public static void write(Descriptor descriptor, Element node) {
		node.setAttribute(ATTRIBUTE_ID, descriptor.getId());
		node.setAttribute(ATTRIBUTE_NAME, descriptor.getName());
		node.setAttribute(ATTRIBUTE_VERSION, descriptor.getVersion());
		if (descriptor.getCategory() != null) {
			node.setAttribute(ATTRIBUTE_CATEGORY, descriptor.getCategory());
		}
		node.setAttribute(ATTRIBUTE_SIZE, descriptor.getSize() + "");
		if (descriptor.getUrl() != null) {
			node.setAttribute(ATTRIBUTE_URL, descriptor.getUrl());
		}
		if (descriptor.getKind() != null) {
			node.setAttribute(ATTRIBUTE_KIND, descriptor.getKind());
		}
		if (descriptor.getFilter() != null) {
			node.setAttribute(ATTRIBUTE_FILTER, descriptor.getFilter());
		}
		if (descriptor.getRequires() != null) {
			node.setAttribute(ATTRIBUTE_REQUIRES, descriptor.getRequires());
		}
		if (descriptor.getRequiresBundle() != null) {
			node.setAttribute(ATTRIBUTE_REQUIRES_BUNDLE, descriptor.getRequiresBundle());
		}
		node.setAttribute(ATTRIBUTE_LOCAL, Boolean.toString(descriptor.isLocal()));

		Element descriptionNode = node.getOwnerDocument().createElement(NODE_DESCRIPTION);
		descriptionNode.setTextContent(descriptor.getDescription());
		node.appendChild(descriptionNode);

		for (Dependency dependency : descriptor.getDependencies()) {
			Element dependencyNode = node.getOwnerDocument().createElement(NODE_DEPENDENCY);
			dependencyNode.setAttribute(ATTRIBUTE_ID, dependency.getId());
			if (dependency.getVersion() != null) {
				dependencyNode.setAttribute(ATTRIBUTE_VERSION, dependency.getVersion());
			}
			node.appendChild(dependencyNode);
		}
	}

	private String name;

	private String version;

	private String description;

	private String category;

	private String url;

	private String id;

	private long size;

	private String md5Hash;

	private boolean local;

	private String filter;

	private String requires;

	private String kind;

	private Set<Dependency> dependencies;

	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	private String requiresBundle;

	public Descriptor() {
	}

	public void addDependency(Dependency dependency) {
		if (dependencies == null) {
			dependencies = new LinkedHashSet<Dependency>();
		}
		dependencies.add(dependency);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public String getCategory() {
		return category;
	}

	public List<Dependency> getDependencies() {
		if (dependencies == null) {
			return Collections.emptyList();
		}
		else {
			return new ArrayList<Dependency>(dependencies);
		}
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getRequires() {
		return requires;
	}

	public String getRequiresBundle() {
		return requiresBundle;
	}

	public String getFilter() {
		return filter;
	}

	public String getKind() {
		return kind;
	}

	public String getMd5Hash() {
		return md5Hash;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public String getUrl() {
		return url;
	}

	public String getVersion() {
		return version;
	}

	public boolean isLocal() {
		return local;
	}

	public boolean isValid() {
		return id != null && version != null && name != null;
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void setCategory(String category) {
		String oldValue = this.category;
		this.category = category;
		propertyChangeSupport.firePropertyChange("category", oldValue, this.category);
	}

	public void setDescription(String description) {
		String oldValue = this.description;
		this.description = description;
		propertyChangeSupport.firePropertyChange("description", oldValue, description);
	}

	public void setId(String id) {
		String oldValue = this.id;
		this.id = id;
		propertyChangeSupport.firePropertyChange("id", oldValue, id);
	}

	public void setKind(String kind) {
		String oldValue = kind;
		this.kind = kind;
		propertyChangeSupport.firePropertyChange("kind", oldValue, kind);
	}

	public void setFilter(String filter) {
		String oldValue = filter;
		this.filter = filter;
		propertyChangeSupport.firePropertyChange("filter", oldValue, filter);
	}

	public void setRequires(String requires) {
		String oldValue = requires;
		this.requires = requires;
		propertyChangeSupport.firePropertyChange("requires", oldValue, requires);
	}

	public void setRequiresBundle(String requiresBundle) {
		String oldValue = this.requiresBundle;
		this.requiresBundle = requiresBundle;
		propertyChangeSupport.firePropertyChange(ATTRIBUTE_REQUIRES_BUNDLE, oldValue, requiresBundle);
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public void setMd5Hash(String md5Hash) {
		this.md5Hash = md5Hash;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		propertyChangeSupport.firePropertyChange("name", oldValue, name);
	}

	public void setSize(long size) {
		long oldValue = this.size;
		this.size = size;
		propertyChangeSupport.firePropertyChange("size", oldValue, size);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setVersion(String version) {
		String oldValue = this.version;
		this.version = version;
		propertyChangeSupport.firePropertyChange("version", oldValue, version);
	}

	@Override
	public String toString() {
		return this.name;
	}

}