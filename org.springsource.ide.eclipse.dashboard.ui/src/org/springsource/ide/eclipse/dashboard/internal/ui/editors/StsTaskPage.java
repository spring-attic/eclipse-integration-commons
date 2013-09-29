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
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.springsource.ide.eclipse.dashboard.internal.ui.util.IdeUiUtils;


/**
 * @author Steffen Pingel
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class StsTaskPage extends AbstractTaskEditorPage {

	private class ActionPart extends AbstractTaskEditorPart {

		@Override
		public void createControl(Composite parent, FormToolkit toolkit) {
			Composite composite = toolkit.createComposite(parent);
			composite.setLayout(new GridLayout(1, false));

			if (IdeUiUtils.getBugsQuery() == null) {
				// FIXME
				// final Button addQueryButton = toolkit.createButton(composite,
				// "Add query to Task List", SWT.CHECK);
				// addQueryButton.setSelection(true);
				// addQuery = true;
				// addQueryButton.addSelectionListener(new SelectionAdapter() {
				// @Override
				// public void widgetSelected(SelectionEvent event) {
				// addQuery = addQueryButton.getSelection();
				// }
				// });
			}

			Button submitButton = toolkit.createButton(composite, "Submit", SWT.NONE);
			submitButton.setImage(CommonImages.getImage(TasksUiImages.REPOSITORY_SUBMIT));
			submitButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					doSubmit();
				}
			});
		}
	}

	private class AttributesPart extends AbstractTaskEditorPart {

		private AbstractAttributeEditor summaryEditor;

		private AbstractAttributeEditor addAttribute(Composite composite, FormToolkit toolkit, String attributeId,
				boolean span) {
			AbstractAttributeEditor editor = createAttributeEditor(getTaskData().getRoot().getMappedAttribute(
					attributeId));
			editor.createLabelControl(composite, toolkit);
			editor.createControl(composite, toolkit);
			if (span) {
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).span(2, 1)
						.applyTo(editor.getControl());
			}
			else {
				GridDataFactory.fillDefaults().applyTo(editor.getControl());
				toolkit.createLabel(composite, ""); // place holder
			}
			// limit height
			if (editor.getLayoutHint() != null && editor.getLayoutHint().rowSpan == RowSpan.MULTIPLE) {
				((GridData) editor.getControl().getLayoutData()).heightHint = 50;
			}

			return editor;
		}

		@Override
		public void createControl(Composite parent, FormToolkit toolkit) {
			Composite composite = toolkit.createComposite(parent);
			composite.setLayout(new GridLayout(3, false));

			summaryEditor = addAttribute(composite, toolkit, IdeUiUtils.JIRA_ATTRIBUTE_SUMMARY, true);
			addAttribute(composite, toolkit, IdeUiUtils.JIRA_ATTRIBUTE_PRIORITY, false);
			TaskAttribute attribute = getTaskData().getRoot().getMappedAttribute(
					IdeUiUtils.JIRA_ATTRIBUTE_AFFECTS_VERSION);
			if (attribute != null) {
				if ("".equals(attribute.getValue())) {
					String value = IdeUiUtils.getVersion().toString();
					for (Map.Entry<String, String> entry : attribute.getOptions().entrySet()) {
						if (entry.getValue().equals(value)) {
							attribute.setValue(entry.getKey());
							getModel().attributeChanged(attribute);
							break;
						}
					}
				}
				addAttribute(composite, toolkit, IdeUiUtils.JIRA_ATTRIBUTE_AFFECTS_VERSION, false);
			}
			toolkit.paintBordersFor(composite);
			setControl(composite);
		}

		@Override
		public void setFocus() {
			summaryEditor.getControl().setFocus();
		}

	}

	public static final String PAGE_ID = "com.springsource.sts.ide.ui.editors.page.task";

	protected boolean addQuery;

	public StsTaskPage(TaskEditor editor) {
		super(editor, IdeUiUtils.JIRA_CONNECTOR_KIND);
		// headerForm.getForm().setText("Report SpringSource Bug or Enhancement")
		// ;
		// getToolkit().decorateFormHeading(headerForm.getForm().getForm());
		setNeedsSubmitButton(true);
	}

	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> descriptors = new LinkedHashSet<TaskEditorPartDescriptor>();
		descriptors.add(new TaskEditorPartDescriptor(ID_PART_SUMMARY) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new HeaderPart(
						"Please use this form to provide feedback on the Spring Tool Suite, or report a bug.");
			}
		}.setPath(PATH_HEADER));
		descriptors.add(new TaskEditorPartDescriptor(ID_PART_ATTRIBUTES) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new AttributesPart();
			}
		}.setPath(PATH_ATTRIBUTES));
		descriptors.add(new TaskEditorPartDescriptor(ID_PART_DESCRIPTION) {
			@Override
			public AbstractTaskEditorPart createPart() {
				TaskEditorDescriptionPart part = new TaskEditorDescriptionPart();
				if (getModel().getTaskData().isNew()) {
					part.setExpandVertically(true);
					part.setSectionStyle(ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
				}
				return part;
			}
		}.setPath(PATH_COMMENTS));
		descriptors.add(new TaskEditorPartDescriptor(ID_PART_ACTIONS) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new ActionPart();
			}
		}.setPath(PATH_ACTIONS));
		return descriptors;
	}

	// FIXME
	// @Override
	// protected void handleSuccess(ITask task) {
	// if (addQuery) {
	// IdeUiUtils.createQuery(IIdeUiConstants.SPRINGSOURCE_BUGS_URL,
	// IIdeUiConstants.LABEL_SPRINGSOURCE_BUGS,
	// IIdeUiConstants.QUERY_SPRINGSOURCE_BUGS);
	// }
	//
	// MessageDialog.openInformation(getSite().getShell(), "Submit",
	// "Your task has been submitted to SpringSource.");
	//
	// IdeUiUtils.closeEditor(this);
	//
	// if (task != null) {
	// showTask(task);
	// }
	// }

}
