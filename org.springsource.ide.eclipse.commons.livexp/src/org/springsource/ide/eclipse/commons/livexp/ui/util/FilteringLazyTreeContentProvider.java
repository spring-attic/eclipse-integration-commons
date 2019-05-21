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

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

/**
 * Wraps another ITreeContentProvider to ensure that all nodes have proper 'getParent'
 * result, even when the wrapped {@link ITreeContentProvider} does not implement
 * a proper 'getParent' and may have nodes that occur multiple times within the same
 * tree with a different parent.
 * <p>
 * This is accomplished by wrapping each element of the original provider into
 * a 'TreeNode' element that properly keeps track of the parent.
 */
public class FilteringLazyTreeContentProvider implements ILazyTreeContentProvider {
	
	private WrappingRootNode root = null;

	private final TreeNodeFilter filter;
	
	private final TreeViewer treeViewer;

	private final ITreeContentProvider baseContentProvider;
	
	
	public FilteringLazyTreeContentProvider(TreeViewer treeViewer, TreeNodeFilter filter, ITreeContentProvider baseContentProvider) {
		this.treeViewer = treeViewer;
		this.filter = filter;
		this.baseContentProvider = baseContentProvider;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof LazyFilteredTreeNode) {
			return ((LazyFilteredTreeNode) element).getParent();
		} else {
			return null;
		}
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		LazyFilteredTreeNode node = (LazyFilteredTreeNode) element;
		int childCount = node.getChildCount();
		treeViewer.setChildCount(node, childCount);
	}

	@Override
	public void updateElement(Object parent, int index) {
		LazyFilteredTreeNode element = ((LazyFilteredTreeNode) parent).getChild(index);
		if (element != null) {
			treeViewer.replace(parent, index, element);
			updateChildCount(element, -1);
		} 
	}
		
	public synchronized void setInput(Object input) {
		// Tree content is computed lazily, so we only set the 
		// root node. The children of the root node are computed later on
		root = new WrappingRootNode(input, filter, treeViewer, baseContentProvider);
		treeViewer.setInput(root);
		treeViewer.expandToLevel(2);
	}
		
	public synchronized void filter(Filter<String> value) {
		filter.setFilter(value);
		if (root != null) {
			filter.select(root);
		}
	}
}