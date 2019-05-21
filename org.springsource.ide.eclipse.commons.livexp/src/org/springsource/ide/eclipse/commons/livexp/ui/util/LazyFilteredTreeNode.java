/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * This is a tree node around a wrapped node that lazily populates its children from a list of filtered children. This node has two layers:
 * <p/>
 * 1. a base child layer that is of fixed length via {@link #createBaseChildren()}
 * <p/>
 * 2. a filtered subset of children computed from this base which may change over time based on filtering, and whose count is obtained from {@link #getChildCount()}. 
 * <p/>
 * This lazy tree node provides a LAZY list of children based on the FILTERED list (so the second layer).
 * <p/>
 * The lazy behaviour is accomplished when a JFace tree viewer requests the node's child count. This child count varies over time as
 * the count is the count of FILTERED children based on the current filter pattern. Once the node informs the lazy tree viewer of it's current child count, 
 * the tree viewer will LATER on, request the actual child at a particular index. It's a this time that the node will lazy provide that child. 
 * 
 */
public abstract class LazyFilteredTreeNode {
	protected final Object wrappedValue;
	protected final LazyFilteredTreeNode parent;
	protected final TreeViewer treeViewer;
	protected final TreeNodeFilter filter;
	
	private List<LazyFilteredTreeNode> filteredNodes = new ArrayList<>();
	

	private LazyFilteredTreeNode[] allChildren = null;
	
	
	public LazyFilteredTreeNode(Object wrappedValue, 
			LazyFilteredTreeNode parent,
			TreeNodeFilter filter,
			TreeViewer treeViewer) {
		this.parent = parent;
		this.filter = filter;
		this.wrappedValue = wrappedValue;	
		this.treeViewer = treeViewer;
	}
	
	/**
	 * 
	 * @return base children that this tree node wraps around, or null if children are not yet available. Return empty list if
	 * node has no children.
	 */
	protected abstract LazyFilteredTreeNode[] createBaseChildren();
	
	@Override
	public String toString() {
		return wrappedValue.toString();
	}

	public LazyFilteredTreeNode getParent() {
		return parent;
	}

	public Object getWrappedValue() {
		return wrappedValue;
	}

	public boolean hasChildren() {
		// This is computed from the base fixed list of children, not from the filtered one.
		return getAllChildren().length > 0;
	}
	
	public LazyFilteredTreeNode[] getAllChildren() {
		if (this.allChildren == null) {
			this.allChildren = createBaseChildren();
		}
		return this.allChildren;
	}

	public int getChildCount() {

		// The lazy tree viewer is requesting a child count for this node as to later
		// lazily request the actual children based on this count.
		
		// It is here where we filter the base nodes to get that child count, but we do NOT
		// yet
		this.filteredNodes.clear();

		int visibleCount = 0;
		LazyFilteredTreeNode[] childNodes = getAllChildren();
		if (childNodes != null) {
			for (LazyFilteredTreeNode node : childNodes) {
				if (filter.isFiltered(node)) {
					this.filteredNodes.add(node);
				}
			}
		}
		visibleCount = this.filteredNodes.size();
		return visibleCount;
	}
	
	public LazyFilteredTreeNode getChild(int index) {
		if (index >= 0 && index < this.filteredNodes.size()) {
			LazyFilteredTreeNode child = this.filteredNodes.get(index);
			treeViewer.update(child.getParent(), null);
			return child;
		} else {
			return null;
		}
	}
}