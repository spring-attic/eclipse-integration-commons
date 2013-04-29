package org.springsource.ide.eclipse.commons.quicksearch.test;

import static org.springsource.ide.eclipse.commons.quicksearch.core.priority.PriorityFunction.PRIORITY_DEFAULT;
import static org.springsource.ide.eclipse.commons.quicksearch.core.priority.PriorityFunction.PRIORITY_IGNORE;
import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.springsource.ide.eclipse.commons.quicksearch.core.priority.PrioriTree;

public class PrioriTreeTest extends TestCase {

	PrioriTree tree;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tree = new PrioriTree();
	}

	public void testWithEmptyTree() {
		//In the empty tree most paths are assigned 'DEFAULT' priority.
		checkPriority(PRIORITY_DEFAULT, "/");
		checkPriority(PRIORITY_DEFAULT, "/foo/bar/zor");
		
		//Paths with ignored extensions should be 'ignored'.
		checkPriority(PRIORITY_IGNORE, "/foo/muck.zip");
		checkPriority(PRIORITY_IGNORE, "/muck.jar");
		checkPriority(PRIORITY_IGNORE, "/images/muck.jpg");
		
		//Names starting with ignored prefix should be ignored
		checkPriority(PRIORITY_IGNORE, "/project/.git");
		
		//Some specific names are also to be ignored
		checkPriority(PRIORITY_IGNORE, "/project/target");
		checkPriority(PRIORITY_IGNORE, "/project/build");
		
	}
	
	public void testSinglePathSet() {
		setPriority("/foo/bar/zor", 100.0);
		
		//Path itself should have the set priority
		checkPriority(100.0, "/foo/bar/zor");
		
		//Also the parent paths should have been set automatically
		checkPriority(100.0, "/foo/bar");
		checkPriority(100.0, "/foo");
		checkPriority(100.0, "/");
		
		//Things not on the paths should still be 'default'
		checkPriority(PRIORITY_DEFAULT, "/other/bar");
		checkPriority(PRIORITY_DEFAULT, "/other");
		
		//The things nested underneath the set path should also still have default priority (in the current algorithm at least).
		checkPriority(PRIORITY_DEFAULT, "/foo/bar/zor/nested");
		checkPriority(PRIORITY_DEFAULT, "/foo/bar/zor/nested/deeper");
	}
	
	public void testSetOverlappingPaths() {
		setPriority("/shared/foo", 50.0);
		setPriority("/shared/bar", 100.0);
		
		tree.dump();
		
		checkPriority(50.0,  "/shared/foo");
		checkPriority(100.0, "/shared/bar");
		
		//Shared section of path should get highest priority of both
		checkPriority(100.0,  "/");
		checkPriority(100.0, "/shared");
		
		//Disjoint paths remain default
		checkPriority(PRIORITY_DEFAULT, "/other");
	}
	
	/**
	 * Similar to testSetOverlappingPaths but order of
	 * priority set operations is reversed. The result should
	 * be the same.
	 */
	public void testSetOverlappingPaths2() {
		setPriority("/shared/bar", 100.0);
		setPriority("/shared/foo", 50.0);
		
		checkPriority(50.0,  "/shared/foo");
		checkPriority(100.0, "/shared/bar");
		
		//Shared section of path should get highest priority of both
		checkPriority(100.0,  "/");
		checkPriority(100.0, "/shared");
		
		//Disjoint paths remain default
		checkPriority(PRIORITY_DEFAULT, "/other");
	}
	
	/**
	 * Need support for setting priority of an entire subtree.
	 */
	public void testSetTreePriority() {
		setTreePriority("/promoted", 100.0);
		
		//Stuff not in the raised subtree should be unchanged
		checkPriority(PRIORITY_DEFAULT, "/unrelated");
		
		//Stuff in the raised subtree should be affected.
		checkPriority(100.0,            "/promoted");
		checkPriority(100.0,            "/promoted/sub");
		checkPriority(100.0,            "/promoted/sub/sub");
		
		//But... ignored stuff should never be made searchable even in a raised subtree.
		checkPriority(PRIORITY_IGNORE,  "/promoted/big.zip");
	}
	
	private void setTreePriority(String pathStr, double pri) {
		tree.setTreePriority(new Path(pathStr), pri);
	}

	private void setPriority(String pathStr, double pri) {
		tree.setPriority(new Path(pathStr), pri);
	}

	private void checkPriority(double expected, String pathStr) {
		assertEquals(pathStr,
				expected, tree.priority(new MockResource(pathStr)));
	}
	
}
