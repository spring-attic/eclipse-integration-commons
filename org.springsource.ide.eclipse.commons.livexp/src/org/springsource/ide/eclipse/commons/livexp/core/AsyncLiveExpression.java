/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Like a LiveExpression but has an option to ensures that its refresh
 * method is always called in a background job.
 * <p>
 * With a regular LiveExp the refresh will be called on the same thread
 * as the change event it reacts to. So in general you have little control
 * over what thread that might be.
 * <p>
 * {@link AsyncLiveExpression} is when the 'compute' method called during refresh
 * does something that you might not want to just execute, for example, on the UI thread.
 * <p>
 * It is also useful in that when refreshes might be 'lengthy' operations, bursty events
 * triggering refreshes will only causes limited refreshes as only a single Job is
 * being scheduled and rescheduled.
 *
 * @author Kris De Volder
 */
public abstract class AsyncLiveExpression<T> extends LiveExpression<T> {

	public enum AsyncMode {
		SYNC, ASYNC
	}

	private Job refreshJob;
	private long refreshDelay = 0;

	public AsyncLiveExpression(T initialValue) {
		this(initialValue, AsyncMode.ASYNC);
	}

	public AsyncLiveExpression(T initialValue, AsyncMode mode) {
		this(initialValue, mode==AsyncMode.ASYNC ? "AsyncLiveExpression refresh": null);
	}

	/**
	 * Create AsyncLiveExpression. If Job name is passed in then
	 * this expression will refresh itself asynchronously.
	 * <p>
	 * If the jobName is null then it behaves like a plain (i.e.
	 * synchronous) LiveExpression.
	 */
	public AsyncLiveExpression(T initialValue, String jobName) {
		super(initialValue);
		if (jobName!=null) {
			refreshJob = new Job(jobName) {
				protected IStatus run(IProgressMonitor monitor) {
					syncRefresh();
					return Status.OK_STATUS;
				};
			};
		}
	}

	/**
	 * This method is final, if you are overriding this, then you probably should
	 * be overriding 'syncRefresh' instead. Otherwise you probably are breaking
	 * async refresh support.
	 */
	@Override
	public final void refresh() {
		if (refreshJob!=null) {
			refreshJob.schedule(refreshDelay);
		} else {
			syncRefresh();
		}
	}

	protected void syncRefresh() {
		super.refresh();
	}

	public long getRefreshDelay() {
		return refreshDelay;
	}

	public void setRefreshDelay(long refreshDelay) {
		this.refreshDelay = refreshDelay;
	}

}
