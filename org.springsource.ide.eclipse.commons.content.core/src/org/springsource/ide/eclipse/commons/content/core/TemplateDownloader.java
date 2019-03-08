/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.content.core;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.springsource.ide.eclipse.commons.core.HttpUtil;

public class TemplateDownloader {

	protected final ContentItem rootItem;

	private CountDownLatch resultLatch;

	private IProgressMonitor downloadMonitor;

	protected TemplateDownloader(ContentItem rootItem) {
		this.rootItem = rootItem;
		this.timerStatus = Status.OK_STATUS;
	}

	private IStatus timerStatus;

	protected IStatus getTimerStatus() {
		return timerStatus;
	}

	public void setTimerStatus(IStatus status) {
		this.timerStatus = status;
	}

	public void startCountdownTimer(IProgressMonitor monitor) {
		downloadMonitor = monitor;
		Job job = new Job("Downloading template") {
			@Override
			protected IStatus run(IProgressMonitor aMonitor) {
				timerStatus = Status.OK_STATUS;
				resultLatch = new CountDownLatch(1);
				try {
					if (!resultLatch.await(60, TimeUnit.SECONDS)) {
						final String message = NLS.bind("Download of {0} timed out, perhaps the network went down.",
								rootItem.getRemoteDescriptor().getUrl());
						setTimerStatus(new Status(Status.ERROR, ContentPlugin.PLUGIN_ID, message));
					}
				}
				// This is an interrupt of the timer job, not of the
				// download. It probably is not possible to interrupt it.
				catch (InterruptedException e) {
					// just let it pass
					System.err.println("Caught interrupted exception @@@ " + e);
				}
				finally {
					stopCountdownTimer();
				}
				return Status.OK_STATUS; // Handle status with a callback
			}
		};
		job.schedule();

	}

	protected void stopCountdownTimer() {
		resultLatch.countDown();
		if (downloadMonitor != null) {
			downloadMonitor.setCanceled(true);
		}
	}

	public IStatus downloadTemplate(IProgressMonitor monitor) {
		MultiStatus result = new MultiStatus(ContentPlugin.PLUGIN_ID, 0, NLS.bind(
				"Download of ''{0}'' (''{1}'') failed", rootItem.getName(), rootItem.getId()), null);
		SubMonitor progress = SubMonitor.convert(monitor, 20);

		ContentManager manager = ContentPlugin.getDefault().getManager();

		try {
			List<ContentItem> dependencies = manager.getDependencies(rootItem);
			for (ContentItem item : dependencies) {
				String url = item.getRemoteDescriptor().getUrl();
				File baseDirectory = manager.getInstallDirectory();
				File archiveFile = new File(baseDirectory, item.getPathFromRemoteDescriptor()
						+ ContentManager.ARCHIVE_EXTENSION);
				File directory = new File(baseDirectory, item.getPathFromRemoteDescriptor());
				startCountdownTimer(monitor);

				IStatus status = HttpUtil.download(url, archiveFile, directory, progress);
				stopCountdownTimer();
				result.add(status);
			}

			// walk the file system to see if the file did get downloaded
			manager.refresh(progress, false);
		}
		catch (OperationCanceledException e) {
			if (getTimerStatus().isOK()) {
				throw e;
			}
			else {
				return new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, 0, NLS.bind(
						"Download ''{0}'' (''{1}'') timed out", rootItem.getName(), rootItem.getId()), e);
			}
		}
		catch (CoreException e) {
			return new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, 0, NLS.bind(
					"Failed to determine dependencies of ''{0}'' (''{1}'')", rootItem.getName(), rootItem.getId()), e);
		}
		finally {
			progress.done();
			result.add(getTimerStatus());
		}
		return result;
	}
}
