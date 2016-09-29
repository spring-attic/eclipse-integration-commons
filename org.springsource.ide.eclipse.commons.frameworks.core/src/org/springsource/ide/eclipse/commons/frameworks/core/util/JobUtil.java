/*******************************************************************************
 * Copyright (c) 2012, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;

public class JobUtil {

	/**
	 * Create a scheduling rule that conflicts only with itself and only
	 * contains itself. Jobs that want to have a 'light' impact on blocking
	 * other jobs but still some guarantee that they won't trample over other
	 * things that require access to some internal shared resource that only
	 * they can access should use this rule to protect the resource.
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

	/**
	 * Runs a job in the background through a progress service that provides UI
	 * progress. Because the progress service provides UI progress, the initial
	 * launching of the services is done from the UI thread BEFORE the
	 * background job is started . A progress service is required and cannot be
	 * null.
	 * 
	 */
	public static void runBackgroundJobWithUIProgress(final IRunnableWithProgress runnableWithProgress,
			final IRunnableContext progressService, final String jobLabel) throws Exception {
		// This outer runnable launches the background job
		final IRunnableWithProgress outerRunnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(jobLabel, IProgressMonitor.UNKNOWN);
				// Fork outside UI thread
				Job job = new Job(jobLabel) {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						SubMonitor subMonitor = SubMonitor.convert(monitor);
						subMonitor.setTaskName(jobLabel);
						try {
							runnableWithProgress.run(subMonitor);
						} catch (Throwable e) {
							FrameworkCoreActivator.log(e);
						} finally {
							subMonitor.done();
						}
						return Status.OK_STATUS;
					}

				};
				job.schedule();
			}
		};

		// Progress services needs to be launched in UI thread.
		Exception[] error = new Exception[1];
		Display.getDefault().syncExec(() -> {
			try {
				progressService.run(true, true, outerRunnable);
			} catch (InvocationTargetException e) {
				error[0] = e;
			} catch (InterruptedException e) {
				error[0] = e;
			}
		});

		if (error[0] != null) {
			throw error[0];
		}
	}

}
