/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;
import org.springsource.ide.eclipse.commons.ui.tips.TipOfTheDayPopup;

/**
 * A header page for all preference pages contributed to the
 * org.springsource.ide.eclipse.commons.preferencePage category
 * 
 * @author Leo Dos Santos
 * @author Andrew Eisenberg
 */
public class CommonsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Shell shell;

	public CommonsPreferencePage() {
		super(GRID);
		setPreferenceStore(UiPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(UiPlugin.SHOW_TIP_O_DAY, "Show tip o' the day on startup",
				getFieldEditorParent()));

		Button openTipButton = new Button(getFieldEditorParent(), SWT.PUSH);
		openTipButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		openTipButton.setText("Open tip o' the day now");
		openTipButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new TipOfTheDayPopup(shell == null ? getShell() : shell, getPreferenceStore(), UiPlugin.getDefault()
						.getTipProvider()).open();
			}
		});
	}

	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
		try {
			shell = workbench.getActiveWorkbenchWindow().getShell();
		}
		catch (NullPointerException e) {
			// OK to ignore.. maybe during shutdown or startup
		}
	}

}
