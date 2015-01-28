/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class StringFieldSection extends WizardPageSection {

	/// options
	private String labelText; //what text to use for the label of the field
	private String tooltip = null; // what text to use for tooltip

	/// model elements
	private final LiveVariable<String> variable;
	private final LiveExpression<ValidationResult> validator;

	/// UI elements
	private Text text;


	//////////////////////////////

	public StringFieldSection(IPageWithSections owner,
			String labelText,
			LiveVariable<String> variable,
			LiveExpression<ValidationResult> validator) {
		super(owner);
		this.labelText = labelText;
		this.variable = variable;
		this.validator = validator;
	}

	public StringFieldSection(IPageWithSections owner, FieldModel<String> f) {
		this(owner, f.getLabel(), f.getVariable(), f.getValidator());
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

	@Override
	public void createContents(Composite page) {
        // project specification group
        Composite projectGroup = new Composite(page, SWT.NONE);
        GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(2).margins(0,2).create();
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(projectGroup, SWT.NONE);
        label.setText(labelText);
        GridDataFactory.fillDefaults()
        	.hint(UIConstants.fieldLabelWidthHint(label), SWT.DEFAULT)
        	.align(SWT.BEGINNING, SWT.CENTER)
        	.applyTo(label);

        text = new Text(projectGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = UIConstants.FIELD_TEXT_AREA_WIDTH;
        text.setLayoutData(data);
        text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				variable.setValue(text.getText());
			}
		});
        variable.addListener(new ValueListener<String>() {
			public void gotValue(LiveExpression<String> exp, String value) {
				if (text!=null && !text.isDisposed()) {
					if (value==null) {
						value = "";
					}
					String oldText = text.getText();
					if (!oldText.equals(value)) {
						text.setText(value);
					}
				}
			}
		});

        if (tooltip!=null) {
        	label.setToolTipText(tooltip);
        	text.setToolTipText(tooltip);
        }
	}

	public StringFieldSection tooltip(String string) {
		this.tooltip = string;
		return this;
	}

}
