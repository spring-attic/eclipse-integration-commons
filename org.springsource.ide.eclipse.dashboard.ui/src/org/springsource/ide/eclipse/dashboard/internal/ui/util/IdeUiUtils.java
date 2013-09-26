/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.util;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.ICoreRunnable;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;
import org.springsource.ide.eclipse.commons.ui.UiUtil;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;

/**
 * @author Steffen Pingel
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Tomasz Zarna
 */
public class IdeUiUtils {

	public static final String ID_PAGE_FACTORY_CONTEXT = "org.eclipse.mylyn.context.ui.pageFactory.Context";

	public static final String ID_PAGE_FACTORY_JIRA = "org.eclipse.mylyn.jira.ui.pageFactory";

	public static final String JIRA_CONNECTOR_KIND = "jira";

	public static final String JIRA_ATTRIBUTE_COMPONENTS = "attribute.jira.components";

	public static final String JIRA_ATTRIBUTE_AFFECTS_VERSION = "attribute.jira.affectsversions";

	public static final String JIRA_ATTRIBUTE_TYPE = "attribute.jira.type";

	public static final String JIRA_ATTRIBUTE_DESCRIPTION = TaskAttribute.DESCRIPTION;

	public static final String JIRA_ATTRIBUTE_PRIORITY = TaskAttribute.PRIORITY;

	public static final String JIRA_ATTRIBUTE_SUMMARY = TaskAttribute.SUMMARY;

	// private static final String ID_PLATFORM_FEATURE = "org.eclipse.platform";

	private static final String ID_PLATFORM_BUNDLE = "org.eclipse.platform";

	public static void closeEditor(final FormPage page) {
		if (page.getSite() != null && page.getSite().getPage() != null && !page.getManagedForm().getForm().isDisposed()) {
			if (page.getEditor() != null) {
				page.getSite().getPage().closeEditor(page.getEditor(), false);
			}
			else {
				page.getSite().getPage().closeEditor(page, false);
			}
		}
	}

	public static TaskData createAnalysisTaskData(TaskRepository taskRepository, String description)
			throws CoreException {
		TaskData taskData = createTaskData(taskRepository,
				IIdeUiConstants.SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_PROJECT_NAME);
		if (taskData == null) {
			throw new CoreException(new Status(Status.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not create new repository item"));
		}
		setAttributeValue(taskData, JIRA_ATTRIBUTE_COMPONENTS,
				IIdeUiConstants.SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_COMPONENT);
		setAttributeValue(taskData, JIRA_ATTRIBUTE_AFFECTS_VERSION,
				IIdeUiConstants.SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_VERSION);
		setAttributeValue(taskData, JIRA_ATTRIBUTE_TYPE, IIdeUiConstants.SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_ISSUE_TYPE);
		setAttributeValue(taskData, JIRA_ATTRIBUTE_DESCRIPTION, description);
		return taskData;
	}

	public static TaskData createBugTaskData(TaskRepository taskRepository, String description) throws CoreException {
		TaskData taskData = createTaskData(taskRepository, IIdeUiConstants.SPRINGSOURCE_BUGS_PROJECT_NAME);
		if (taskData == null) {
			throw new CoreException(new Status(Status.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not create new repository item"));
		}
		setAttributeValue(taskData, JIRA_ATTRIBUTE_COMPONENTS, IIdeUiConstants.SPRINGSOURCE_BUGS_COMPONENT);
		setAttributeValue(taskData, JIRA_ATTRIBUTE_AFFECTS_VERSION, getVersion().toString());
		setAttributeValue(taskData, JIRA_ATTRIBUTE_TYPE, IIdeUiConstants.SPRINGSOURCE_BUGS_ISSUE_TYPE);
		setAttributeValue(taskData, JIRA_ATTRIBUTE_DESCRIPTION, description);
		return taskData;
	}

	public static IRepositoryQuery createQuery(String repositoryUrl, String queryLabel, String queryUrl) {
		TaskRepository taskRepository = TasksUiPlugin.getRepositoryManager().getRepository(repositoryUrl);
		if (taskRepository == null) {
			StatusHandler.log(new Status(IStatus.WARNING, IdeUiPlugin.PLUGIN_ID,
					"Query initialization failed, repository configuration not found: " + repositoryUrl));
			return null;
		}

		IRepositoryQuery query = getQuery(queryLabel);
		if (query != null) {
			return query;
		}

		query = TasksUi.getRepositoryModel().createRepositoryQuery(taskRepository);
		query.setUrl(queryUrl);
		query.setSummary(queryLabel);
		TasksUiPlugin.getTaskList().addQuery((RepositoryQuery) query);
		TasksUiInternal.synchronizeRepository(taskRepository, false);
		return query;
	}

	/**
	 * Creates a regular expression from the given string by escaping
	 * illegal/control characters.
	 * @return the valid regular expression
	 */
	public static String createRegularExpression(String expression) {
		expression = expression.replaceAll("\\\\", "\\\\\\\\");
		expression = expression.replaceAll("\\(", "\\\\(");
		expression = expression.replaceAll("\\)", "\\\\)");
		expression = expression.replaceAll("\\[", "\\\\[");
		expression = expression.replaceAll("\\]", "\\\\]");
		expression = expression.replaceAll("\\$", "\\\\\\$");
		return expression;
	}

	private static TaskData createTaskData(final TaskRepository taskRepository, final String projectKey)
			throws CoreException {
		AbstractRepositoryConnector connector = TasksUi.getRepositoryConnector(JIRA_CONNECTOR_KIND);
		final AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		final TaskData taskData = new TaskData(taskDataHandler.getAttributeMapper(taskRepository), JIRA_CONNECTOR_KIND,
				taskRepository.getRepositoryUrl(), "");
		ICoreRunnable runner = new ICoreRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				TaskMapping mapping = new TaskMapping() {
					@Override
					public String getProduct() {
						return projectKey;
					}
				};
				if (!taskDataHandler.initializeTaskData(taskRepository, taskData, mapping, monitor)) {
					throw new CoreException(new Status(Status.ERROR, IdeUiPlugin.PLUGIN_ID,
							"The repository does not have a project with key: " + projectKey));
				}
			}
		};
		UiUtil.busyCursorWhile(runner);
		return taskData;
	}

	public static IRepositoryQuery getBugsQuery() {
		return getQuery(IIdeUiConstants.LABEL_SPRINGSOURCE_BUGS);
	}

	public static Version getPlatformVersion() {
		// IBundleGroupProvider[] providers =
		// Platform.getBundleGroupProviders();
		// if (providers != null) {
		// for (IBundleGroupProvider provider : providers) {
		// for (IBundleGroup group : provider.getBundleGroups()) {
		// if (ID_PLATFORM_FEATURE.equals(group.getIdentifier())) {
		// try {
		// return new Version(group.getVersion());
		// }
		// catch (IllegalArgumentException e) {
		// // should never happen
		// }
		// }
		// }
		// }
		// }

		Bundle bundle = Platform.getBundle(ID_PLATFORM_BUNDLE);
		if (bundle == null) {
			bundle = Platform.getBundle(Platform.PI_RUNTIME);
		}
		if (bundle != null) {
			String versionString = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
			try {
				return new Version(versionString);
			}
			catch (IllegalArgumentException e) {
				// should never happen
			}
		}
		return Version.emptyVersion;
	}

	private static IRepositoryQuery getQuery(String queryLabel) {
		TaskList taskList = TasksUiPlugin.getTaskList();
		Set<RepositoryQuery> queries = taskList.getQueries();
		for (RepositoryQuery query : queries) {
			if (queryLabel.equals(query.getSummary())) {
				// query already exists
				return query;
			}
		}
		return null;
	}

	public static IRepositoryQuery getRuntimeErrorAnalysisQuery() {
		return getQuery(IIdeUiConstants.LABEL_SPRINGSOURCE_ANALYSES);
	}

	public static String getShortVersion() {
		Version version = getVersion();
		return version.getMajor() + "." + version.getMinor() + "." + version.getMicro();
	}

	private static TaskRepository getTaskRepository(String repositoryUrl) {
		return getTaskRepository(JIRA_CONNECTOR_KIND, repositoryUrl);
	}

	public static TaskRepository getTaskRepository(String connectorKind, String repositoryUrl) {
		Assert.isNotNull(repositoryUrl);
		TaskRepository taskRepository = TasksUi.getRepositoryManager().getRepository(connectorKind, repositoryUrl);
		if (taskRepository == null) {
			taskRepository = new TaskRepository(connectorKind, repositoryUrl);
			Set<RepositoryTemplate> templates = TasksUiPlugin.getRepositoryTemplateManager()
					.getTemplates(connectorKind);
			for (RepositoryTemplate template : templates) {
				if (repositoryUrl.equals(template.repositoryUrl)) {
					taskRepository.setRepositoryLabel(template.label);
				}
			}
			TasksUi.getRepositoryManager().addRepository(taskRepository);
		}
		return taskRepository;
	}

	public static Version getVersion() {
		return IdeUiPlugin.getDefault().getBundle().getVersion();
	}

	public static void openNewRuntimeErrorAnalysisEditor(String runtimeErrorMessage) {
		try {
			TaskRepository taskRepository = getTaskRepository(IIdeUiConstants.SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_URL);
			if (taskRepository == null) {
				UiStatusHandler.logAndDisplay(new Status(Status.ERROR, IdeUiPlugin.PLUGIN_ID,
						"Please create a Jira repository with the following URL in the Task Repositories view: "
								+ IIdeUiConstants.SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_URL));
				return;
			}
			TaskData taskData = createAnalysisTaskData(taskRepository, runtimeErrorMessage);
			if (taskData != null) {
				TasksUiInternal.createAndOpenNewTask(taskData);
			}
		}
		catch (OperationCanceledException e) {
			// user cancelled, nothing to do
		}
		catch (PartInitException e) {
			IdeUiPlugin.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not open runtime error analysis editor", e));
		}
		catch (CoreException e) {
			UiStatusHandler.logAndDisplay(new Status(Status.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not open runtime error analysis editor: " + e.getMessage()));
		}
	}

	public static void reportBug(String description) {
		try {
			TaskRepository taskRepository = getTaskRepository(IIdeUiConstants.SPRINGSOURCE_BUGS_URL);
			TaskData taskData = createBugTaskData(taskRepository, description);
			if (taskData != null) {
				TasksUiInternal.createAndOpenNewTask(taskData);
			}
		}
		catch (OperationCanceledException e) {
			// user cancelled, nothing to do
		}
		catch (PartInitException e) {
			IdeUiPlugin.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not open editor", e));
		}
		catch (CoreException e) {
			UiStatusHandler.logAndDisplay(new Status(Status.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not open editor: "
					+ e.getMessage()));
		}

	}

	private static void setAttributeValue(TaskData taskData, String attributeId, String value) {
		TaskAttribute attribute = taskData.getRoot().getAttribute(attributeId);
		if (attribute != null && !attribute.getMetaData().isReadOnly()) {
			if (attribute.getOptions().isEmpty()) {
				attribute.setValue(value);
			}
			else {
				for (Map.Entry<String, String> entry : attribute.getOptions().entrySet()) {
					if (entry.getValue().equals(value)) {
						attribute.setValue(entry.getKey());
						break;
					}
				}
			}
		}
	}
}
