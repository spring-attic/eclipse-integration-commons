/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui.util;

import org.eclipse.jface.viewers.LabelProvider;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class TreeNodeFilter {

	private Filter<String> baseFilter;
	private LabelProvider labels;
	private Cache<Object, Boolean> nodeCache = CacheBuilder.newBuilder().build();
	private UpdateExpansionStates updateExpansionStates;


	public TreeNodeFilter(Filter<String> baseFilter, 
			LabelProvider labels, 
			UpdateExpansionStates updateExpansionStates) {
		this.labels = labels;
		this.updateExpansionStates = updateExpansionStates;
		setFilter(baseFilter);
	}

	private synchronized boolean accept(LazyFilteredTreeNode node) {
		// Useful special case, avoid lots of work if the filter just accepts everything
		// anyways.
		if (baseFilter.isTrivial()) {
			// Note: in this case we deliberately skip auto collapse / expand logic.
			// Unless something is entered in the search box,only the user is in control
			// of expanding/collapsing elements
			return true;
		}

		// Be sure to compute BOTH the node and the children. Even if the node
		// accepts, still compute the children to add them to the cache
		boolean acceptNode = acceptNode(node);
		boolean acceptChildren = acceptChildren(node);


		boolean accept = acceptNode || acceptChildren;
		
		try {
			this.nodeCache.get(node, () -> {
				return accept;
			});
		} catch (Exception er) {
			Log.log(er);
		}
		
		updateExpansionStates.setExpanded(node, accept);
		
		return accept;
	}


	private boolean acceptChildren(LazyFilteredTreeNode node) {
		boolean accept = false;

		if (node != null) {
			LazyFilteredTreeNode[] children = node.getAllChildren();
			if (children != null) {
				for (LazyFilteredTreeNode c : children) {
					// iterate through all children even if an acceptance is already found
					if (accept(c)) {
						accept = true;
					}
				}
			}
		}

		return accept;
	}

	private boolean acceptNode(LazyFilteredTreeNode e) {
		String label = labels.getText(e.getWrappedValue());
		if (label == null) {
			label = "";
		}
		return baseFilter.accept(label);
	}

	public synchronized void setFilter(Filter<String> baseFilter) {
		this.baseFilter = baseFilter == null ? Filters.acceptAll() : baseFilter;
	}

	public synchronized void select(LazyFilteredTreeNode root) {
		if (root != null) {
			this.nodeCache.invalidateAll();
			accept(root);
		}
	}

	public boolean isFiltered(LazyFilteredTreeNode wrappedValue) {
		if (nodeCache.size() == 0) {
			return true;
		}
		Boolean filter = nodeCache.getIfPresent(wrappedValue);
		return filter != null && filter;
	}
}
