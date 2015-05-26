/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.util;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class JobUtil {

	/**
	 * Create a scheduling rule that conflicts only with itself and only contains itself.
	 * Jobs that want to have a 'light' impact on blocking other jobs
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
