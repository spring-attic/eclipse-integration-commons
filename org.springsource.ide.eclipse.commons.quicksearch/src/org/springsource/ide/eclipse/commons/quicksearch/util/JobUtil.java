package org.springsource.ide.eclipse.commons.quicksearch.util;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class JobUtil {

	/**
	 * Create a scheduling rule that conflicts only with itself and only contains itself.
	 * GradleRunnables that want to have a 'light' impact on blocking other jobs
	 * but still some guarantee that they won't trample over other things that require
	 * access to some internal shared resource that only they can access should use this 
	 * rule to protect the resource. 
	 */
	public static ISchedulingRule lightRule(final String name) {
		return new ISchedulingRule() {
			public boolean contains(ISchedulingRule rule) {
				return rule == this;
			}

			public boolean isConflicting(ISchedulingRule rule) {
				return rule == this || rule.contains(this);
			}
			public String toString() {
				return name;
			};
		};
	}
	
}
