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
package org.springsource.ide.eclipse.dashboard.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;
import org.springsource.ide.eclipse.commons.core.ResourceProvider.Property;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;


/**
 * @author Steffen Pingel
 */
public class MainPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private class PropertyEditor {

		private Text control;

		private final Property property;

		public PropertyEditor(Property property) {
			this.property = property;

		}

		public Control createControl(Composite parent) {
			int style = SWT.BORDER;
			if (property.isMultiValue()) {
				style |= SWT.MULTI | SWT.V_SCROLL;
			}
			control = new Text(parent, style);
			return control;
		}

		public Label createLabel(Composite composite) {
			Label label = new Label(composite, SWT.NONE);
			if (property.getLabel() != null) {
				label.setText(property.getLabel() + ":");
			}
			return label;
		}

		public boolean isControlOnSeparateRow() {
			return property.isMultiValue();
		}

		public void performDefaults() {
			setValue(property.getDefaultValue());
		}

		public void performOk() {
			property.setValue(control.getText());
		}

		public void performReset() {
			setValue(property.getValue());
		}

		private void setValue(String value) {
			control.setText((value) != null ? value : "");
		}

	}

	private static final int HEIGHT_MULTI_LINE_TEXT = 60;

	private List<PropertyEditor> editors;

	private Button showOnStartupButton;

	private Button useNewDashboardButton;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		createShowOnStartupButton(composite);
		
		createUseOldDasboardButton(composite);

		for (PropertyEditor editor : editors) {
			Label label = editor.createLabel(composite);
			GridDataFactory.fillDefaults().applyTo(label);
			if (editor.isControlOnSeparateRow()) {
				((GridData) label.getLayoutData()).horizontalSpan = 2;
			}

			Control control = editor.createControl(composite);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
			if (editor.isControlOnSeparateRow()) {
				((GridData) control.getLayoutData()).horizontalSpan = 2;
				((GridData) control.getLayoutData()).heightHint = HEIGHT_MULTI_LINE_TEXT;
			}

			editor.performReset();
		}

		return composite;
	}
	
	private void createUseOldDasboardButton(Composite composite) {
		useNewDashboardButton = new Button(composite, SWT.CHECK);
		useNewDashboardButton.setText("Use Old Dashboard");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(useNewDashboardButton);
		useNewDashboardButton.setSelection(getUseOldDashboard());
	}

	private boolean getUseOldDashboard() {
		String value = getPreferenceStore().getString(IIdeUiConstants.PREF_USE_OLD_DASHOARD);
		if (value==null) {
			return getDefaultUseOldDashboard();
		}
		return Boolean.valueOf(value);
	}
	
	private boolean getDefaultUseOldDashboard() {
		boolean useNew = StsProperties.getInstance().get("sts.new.dashboard.enabled", false);
		return !useNew;
	}

	private void createShowOnStartupButton(Composite composite) {
		showOnStartupButton = new Button(composite, SWT.CHECK);
		showOnStartupButton.setText("Show Dashboard On Startup");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(showOnStartupButton);
		showOnStartupButton.setSelection(getPreferenceStore().getBoolean(IIdeUiConstants.PREF_OPEN_DASHBOARD_STARTUP));
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return IdeUiPlugin.getDefault().getPreferenceStore();
	}

	public void init(IWorkbench workbench) {
		editors = new ArrayList<PropertyEditor>();
		for (Property property : ResourceProvider.getInstance().getProperties()) {
			if (property.isUserConfigurable()) {
				PropertyEditor editor = new PropertyEditor(property);
				editors.add(editor);
			}
		}
	}

	@Override
	protected void performDefaults() {
		showOnStartupButton.setSelection(getPreferenceStore().getDefaultBoolean(
				IIdeUiConstants.PREF_OPEN_DASHBOARD_STARTUP));
		for (PropertyEditor editor : editors) {
			editor.performDefaults();
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(IIdeUiConstants.PREF_OPEN_DASHBOARD_STARTUP, showOnStartupButton.getSelection());
		for (PropertyEditor editor : editors) {
			editor.performOk();
		}
		setBoolean(getPreferenceStore(), IIdeUiConstants.PREF_USE_OLD_DASHOARD, useNewDashboardButton.getSelection());
		return super.performOk();
	}

	/**
	 * Set boolean preference, taking care to only actually set it if it differs from the default value (so that if
	 * default value changes at a later time...)
	 */
	private void setBoolean(IPreferenceStore preferenceStore, String propName, boolean value) {
		boolean defaultValue = preferenceStore.getDefaultBoolean(propName);
		if (value==defaultValue) {
			preferenceStore.setToDefault(propName);	
		} else {
			preferenceStore.setValue(propName, value);
		}
	}

}
