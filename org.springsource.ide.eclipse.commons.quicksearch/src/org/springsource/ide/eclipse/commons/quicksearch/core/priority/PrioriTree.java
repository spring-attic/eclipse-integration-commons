package org.springsource.ide.eclipse.commons.quicksearch.core.priority;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * A PrioriTree is an implementation of PriorityFunction that is based on assigning specific priorities
 * to a finite set of paths. The paths are kept in a tree-like structure internally so that
 * assinging a priority to a given path also implicitly forces all the children leading to that
 * path to have a priority that is at least as high as that of the path itself.
 * <p>
 * If a path is not in the tree than the DefaultPriorityFunction implementation will be used to
 * assign a priority.
 * 
 * @author Kris De Volder
 */
public class PrioriTree extends DefaultPriorityFunction {
	
	private static final boolean DEBUG =  false; //(""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}
	

	/**
	 * Priority assigned to any path lookup that ends here.
	 */
	private double priority = PRIORITY_IGNORE; 
		//Must start out as low as possible because putting stuff into the tree will raise the priorities monotonically.
		//Note that normally we will not create a PrioriTree node unless there is some reason to
	    // assign a priority. So this value is expected to be always overwritten shortly after
	    // a node is created.
	
	/**
	 * Children indexed by first segment in the path. This is only initialised if there's at least one
	 * child.
	 */
	private Map<String, PrioriTree> children = null;

	/**
	 * Set the priority for a given path. Also forces an update of all 'ancestor' nodes in the
	 * tree leading to this path to ensure that a parent node always has a priority at least as high as any
	 * of its children.
	 * <p>
	 * Note: this operation never reduces the priority of any path already in the tree.
	 * Thus if the same path gets assigned a priority more than once, only the highest priority will
	 * be retained in the tree node for that path.
	 */
	public void setPriority(IPath path, double priority) {
		this.priority = Math.max(this.priority, priority); //Use Math.max, never reduce priorities!
		if (path.segmentCount()>0) {
			PrioriTree child = ensureChild(path.segment(0));
			child.setPriority(path.removeFirstSegments(1), priority);
		}
	}

	/**
	 * Ensure that this node has a child for a given segment string. If no node exists yet, create it.
	 * @param segment
	 * @return {@link PrioriTree} the existing or newly created child, never null.
	 */
	private PrioriTree ensureChild(String segment) {
		if (children==null) {
			children = new HashMap<String, PrioriTree>();
		}
		PrioriTree child = children.get(segment);
		if (child==null) {
			child = new PrioriTree();
			children.put(segment, child);
		}
		return child;
	}

	@Override
	public double priority(IResource r) {
		PrioriTree node = this.lookup(r.getFullPath());
		double result;
		if (node==null) {
			result = super.priority(r);
		} else {
			result = node.priority;
		}
		debug("Priority for "+r.getFullPath() + " = " + result);
		return result;
	}


	/**
	 * Locate tree node corresponding to a given path.
	 * @param fullPath
	 * @return The node or null if no corresponding node exists in the tree.
	 */
	private PrioriTree lookup(IPath path) {
		if (path.segmentCount()==0) {
			return this;
		} else {
			PrioriTree child = getChild(path.segment(0));
			if (child!=null) {
				return child.lookup(path.removeFirstSegments(1));
			}
		}
		return null;
	}

	/**
	 * Fetch the child for the corresponding segment String.
	 * @param segment
	 * @return The child or null if there is no such child.
	 */
	private PrioriTree getChild(String segment) {
		if (children!=null) {
			return children.get(segment);
		}
		return null;
	}

}
