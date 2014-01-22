/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.util.IdeUiUtils;


/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class StsTaskPageFactory extends AbstractTaskEditorPageFactory {

	@Override
	public boolean canCreatePageFor(TaskEditorInput input) {
		ITask task = input.getTask();
		if (TasksUiUtil.isOutgoingNewTask(task, IdeUiUtils.JIRA_CONNECTOR_KIND)
				&& IIdeUiConstants.SPRINGSOURCE_BUGS_URL.equals(task
						.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL))) {
			TaskData taskData;
			try {
				taskData = TasksUi.getTaskDataManager().getTaskData(task);
				if (taskData != null) {
					TaskAttribute attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.PRODUCT);
					if (attribute != null
							&& IIdeUiConstants.SPRINGSOURCE_BUGS_PROJECT_NAME.equals(taskData.getAttributeMapper()
									.getValueLabel(attribute))) {
						return true;
					}
				}
			}
			catch (CoreException e) {
				// ignore
			}
		}
		return false;
	}

	@Override
	public IFormPage createPage(TaskEditor parentEditor) {
		return new StsTaskPage(parentEditor);
	}

	@Override
	public String[] getConflictingIds(TaskEditorInput input) {
		return new String[] { ITasksUiConstants.ID_PAGE_PLANNING, IdeUiUtils.ID_PAGE_FACTORY_CONTEXT,
				IdeUiUtils.ID_PAGE_FACTORY_JIRA };
	}

	@Override
	public Image getPageImage() {
		return null;
	}

	@Override
	public String getPageText() {
		return "Details";
	}

	@Override
	public int getPriority() {
		return PRIORITY_TASK;
	}

}
