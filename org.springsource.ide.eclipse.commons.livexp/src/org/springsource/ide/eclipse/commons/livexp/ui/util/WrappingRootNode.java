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

public class WrappingRootNode extends LazyFilteredTreeNode {

	protected final ITreeContentProvider contentProvider;

	public WrappingRootNode(Object wrappedValue, TreeNodeFilter filter, TreeViewer treeViewer,
			ITreeContentProvider contentProvider) {
		super(wrappedValue, null /* no parent */, filter, treeViewer);
		this.contentProvider = contentProvider;
	}

	@Override
	protected synchronized LazyFilteredTreeNode[] createBaseChildren() {
		Object[] baseElements = this.contentProvider.getElements(getWrappedValue());
		if (baseElements != null && baseElements.length > 0) {
			LazyFilteredTreeNode[] rootChildren = new LazyFilteredTreeNode[baseElements.length];
			for (int i = 0; i < baseElements.length; i++) {
				rootChildren[i] = new WrappingTreeNode(baseElements[i], this, filter, treeViewer, contentProvider);
			}
			return rootChildren;
		}
		else {
			// If elements are not yet available, return null to ensure that another request for create children is
			// made by the content provider
			return null;
		}	
	}

}
