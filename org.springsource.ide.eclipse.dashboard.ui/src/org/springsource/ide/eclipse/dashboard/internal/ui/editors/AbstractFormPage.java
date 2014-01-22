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

import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPage;


/**
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class AbstractFormPage extends AbstractDashboardPage {

	protected FormToolkit toolkit;

	protected ScrolledForm form;

	protected EditorToolkit editorToolkit;

	public AbstractFormPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	public AbstractFormPage(String id, String title) {
		super(id, title);
	}

	protected boolean canPerformAction(String actionId) {
		return EditorUtil.canPerformAction(actionId, EditorUtil.getFocusControl(this));
	}

	protected void doAction(String actionId) {
		EditorUtil.doAction(actionId, EditorUtil.getFocusControl(this));
	}

	protected void init(IManagedForm managedForm, String title) {
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		if (title != null) {
			form.setText(title);
			toolkit.decorateFormHeading(form.getForm());
		}
		toolkit.setBorderStyle(SWT.NULL);
		editorToolkit = new EditorToolkit(toolkit, getEditorSite());
	}

	protected void setEnabledState(Composite composite, boolean enabled) {
		if (!composite.isDisposed()) {
			composite.setEnabled(enabled);
			for (Control control : composite.getChildren()) {
				control.setEnabled(enabled);
				if (control instanceof Composite) {
					setEnabledState(((Composite) control), enabled);
				}
			}
		}
	}

	@Override
	public void showBusy(boolean busy) {
		if (getManagedForm() != null && !getManagedForm().getForm().isDisposed()) {
			setEnabledState(getManagedForm().getForm().getBody(), !busy);
		}
	}
}
