/*******************************************************************************
 * Copyright (c) 2012 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.core;

import java.util.PriorityQueue;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springsource.ide.eclipse.commons.quicksearch.ui.QuickSearchActivator;

/**
 * A Helper class that allows traversing all the resources in the workspace, assigning priorities
 * to the resources to decide the ordering and completely ignore some resources.
 * <p>
 * The walker can also be paused and resumed.
 * 
 * @author Kris De Volder
 */
public abstract class ResourceWalker extends Job {
	
	/**
	 * The highest priority. Any elements in the queue with this priority will be visited before
	 * any others in the queue. Be warned that assigning this priority to a deeply nested
	 * element in the tree alone doesn't guarantee it will be visited early on because in
	 * order to reach the element the parents have to be visited first. If the parent
	 * has a low priority...
	 */
	public static final double PRIORITY_VISIT_FIRST = Double.POSITIVE_INFINITY;
	
	/**
	 * A special priority that causes elements (and their children) to be completely ignored.
	 */
	public static final double PRIORITY_IGNORE = Double.NEGATIVE_INFINITY;
	
	/**
	 * A default priority value. Meant to be used for elements that are neither particularly
	 * interesting or particularly non-interesting. Use larger numbers to emphasize elements
	 * and lower numbers to de-emphasise them. Note that in order to emphasise an element
	 * globally it is probably also necessary to ensure the priority of their parents
	 * are raised as well.
	 */
	private static final double PRIORITY_DEFAULT = 0;

	/**
	 * The default priority function causes any resources that end with these strings to
	 * be ignored.
	 */
	public String[] ignoredExtensions = {
		".png", ".jpg", ".zip", ".jar", "~"
	};
	
	/**
	 * The default priority function causes any resource who's name (i.e last path segment)
	 * starts with any of these Strings to be ignored.
	 */
	public String[] ignoredPrefixes = {
		"."
	};
	
	/**
	 * The default priority function causes any resources who's name equals any of these
	 * Strings to be ignored.
	 */
	public static final String[] ignoredNames = {
		"bin", "target", "build"
	};
	
	private class QItem implements Comparable<QItem> {
		public final double priority;
		public final IResource resource;
		
		public QItem(double p, IResource r) {
			this.priority = p;
			this.resource = r;
		}
		
		@Override
		public int compareTo(QItem other) {
			return Double.compare(this.priority, other.priority);
		}
	}

	public ResourceWalker() {
		super("QuickSearch");
		queue.add(new QItem(0, ResourcesPlugin.getWorkspace().getRoot()));
	}

	/**
	 * Queue of work to do. When all work is done this will be set to null. So it
	 * can also be used to determine 'done' status. 
	 */
	private PriorityQueue<QItem> queue = new PriorityQueue<QItem>();
	
	/**
	 * Setting this to true will cause the ResourceWalker to stop walking. If the walker is running
	 * as a scheduled job, then this Job will terminate. However it is possible to 'resume' the
	 * later since pending list of workitems will be retained. 
	 */
	private boolean suspend = false;
	
	public boolean isDone() {
		return queue==null;
	}
	
	/**
	 * Request that the walker stops walking at the next reasonable opportunity.
	 */
	public void suspend() {
		this.suspend = true;
	}

	protected boolean ignore(IResource r) {
		String name = r.getName();
		if (name.startsWith(".")) {
			return true;
		}
		if (name.endsWith(".jar") || name.endsWith(".zip")) {
			return true;
		}
		if (name.equals("bin")) {
			return true;
		}
		return false;
	}
	
	public IStatus run(IProgressMonitor monitor) {
		while (!suspend && queue!=null) {
			IResource r = getWork();
			if (r!=null) {
				if (!ignore(r)) {
					if (r instanceof IFile) {
						IFile f = (IFile) r;
						visit(f);
					} else if (r instanceof IContainer) {
						IContainer f = (IContainer) r;
						try {
							for (IResource child : f.members()) {
								enqueue(child);
							}
						} catch (CoreException e) {
							QuickSearchActivator.log(e);
						}
					}
				}
			} else {
				queue = null;
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Add a resource to the work queue taking account the priority of the resource.
	 */
	private void enqueue(IResource child) {
		double p = priority(child);
		if (p==PRIORITY_IGNORE) {
			return;
		}
		queue.add(new QItem(p, child));
	}

	protected abstract void visit(IFile r);
	
	/**
	 * Assigns a priority to a given resource. This priority will affect the order in which 
	 * resources get visited. Resources to be visited are tracked in a priority queue and
	 * at any time the resource with highest priority number is visited first.
	 * <p>
	 * Note that if a resource is a folder then lowering its priority implicitly reduces
	 * the priority of anything nested inside that folder because to visit the children
	 * one has to first visit the parent to reach them.
	 * <p>
	 * If the priority returned is PRIORITY_IGNORE then the resource will be ignored 
	 * completely and not visited at all. 
	 * 
	 * @param r
	 * @return
	 */
	protected double priority(IResource r) {
		if (r!=null) {
			String name = r.getName();
			for (String ext : ignoredExtensions) {
				if (name.endsWith(ext)) {
					return PRIORITY_IGNORE;
				}
			}
			for (String pre : ignoredPrefixes) {
				if (name.startsWith(pre)) {
					return PRIORITY_IGNORE;
				}
			}
			for (String n : ignoredNames) {
				if (name.equals(n)) {
					return PRIORITY_IGNORE;
				}
			}
			return PRIORITY_DEFAULT;
		}
		return PRIORITY_IGNORE;
	}

	private IResource getWork() {
		if (queue!=null) {
			synchronized (queue) {
				if (!queue.isEmpty()) {
					return queue.remove().resource;
				}
			}
		}
		return null;
	}

}
