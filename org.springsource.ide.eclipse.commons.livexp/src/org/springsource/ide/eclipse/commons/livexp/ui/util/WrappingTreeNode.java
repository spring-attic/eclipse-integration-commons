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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

public class WrappingTreeNode extends LazyFilteredTreeNode {

	private final ITreeContentProvider contentProvider;

	public WrappingTreeNode(Object wrappedValue, LazyFilteredTreeNode parent, TreeNodeFilter filter,
			TreeViewer treeViewer, ITreeContentProvider contentProvider) {
		super(wrappedValue, parent, filter, treeViewer);
		this.contentProvider = contentProvider;
	}

	@Override
	protected LazyFilteredTreeNode[] createBaseChildren() {
		LazyFilteredTreeNode[] wrappingNodes = null;
		Object[] baseChildren = contentProvider.getChildren(getWrappedValue());
		if (baseChildren == null) {
			wrappingNodes = new LazyFilteredTreeNode[0];
		} else {
			wrappingNodes = new LazyFilteredTreeNode[baseChildren.length];

			for (int i = 0; i < wrappingNodes.length; i++) {
				wrappingNodes[i] = new WrappingTreeNode(baseChildren[i], this, filter, treeViewer, contentProvider);
			}
		}
		return wrappingNodes;
	}

}