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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.progress.UIJob;

import com.google.common.collect.ImmutableList;

public class UpdateExpansionStates extends UIJob {

	private List<LazyFilteredTreeNode> toExpand = new ArrayList<>();
	private List<LazyFilteredTreeNode> toCollapse = new ArrayList<>();
	private  int limit = 15;

	private TreeViewer treeViewer;
	private Set<Object> expandedElements;

	public UpdateExpansionStates(TreeViewer treeViewer) {
		super("Update Tree Viewer Expansions After Search");
		this.treeViewer = treeViewer;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		synchronized(this) {
			if (treeViewer == null || expandedElements == null || expandedElements.isEmpty()) {
				return Status.OK_STATUS;
			}
			
			List<Object> currentExpand = null;

			if (expandedElements.size() > limit) {
				currentExpand  = expandedElements.stream().limit(limit).collect(Collectors.toList());
			} else {
				currentExpand = ImmutableList.copyOf(expandedElements);
			}
			expandedElements.removeAll(currentExpand);

			if (currentExpand != null) {
				// NOTE: This doesn't seem to work well. Instead iterate through the elements and manually set expansion state
//				treeViewer.setExpandedElements(currentExpand.toArray());
				for (Object toExpand : currentExpand) {
					treeViewer.setExpandedState(toExpand, true);
				}
			}
		}

		return Status.OK_STATUS;
	}

	public synchronized void collapseElement(LazyFilteredTreeNode e) {
		toExpand.remove(e);
		if (!toExpand.contains(e)) {
			toExpand.add(e);
		}
	}

	public synchronized void expandElement(LazyFilteredTreeNode e) {
		List<LazyFilteredTreeNode> expand = new ArrayList<>();
		while (e != null) {
			if (!expand.contains(e)) {
				expand.add(e);
			}
			toCollapse.remove(e);
			e = e.getParent();
		}
		for (int i = expand.size() - 1; i >=0 ; i--) {
			LazyFilteredTreeNode node = expand.get(i);
			if (!toExpand.contains(node)) {
				toExpand.add(node);
			}
		}
	}

	public void setExpanded(LazyFilteredTreeNode e, boolean expand) {
		if (expand) {
			expandElement(e);
		} else {
			collapseElement(e);
		}
	}
	
	public void scheduleExpand(int limit) {

		this.expandedElements = new HashSet<>(
				ImmutableList.copyOf((Object[]) treeViewer.getExpandedElements()));
		synchronized (this) {
			if (limit > 0) {
				this.limit = limit;
			}

			expandedElements.addAll(toExpand);
			expandedElements.removeAll(toCollapse);
			clear();
		}
		schedule();
	}
	
	public void scheduleExpandRemaining() {
		schedule();
	}

	public synchronized void clear() {
		toExpand.clear();
		toCollapse.clear();
	}
}