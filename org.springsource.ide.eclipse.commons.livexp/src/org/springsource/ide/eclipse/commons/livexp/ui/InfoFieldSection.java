/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import static org.springsource.ide.eclipse.commons.livexp.ui.UIConstants.FIELD_LABEL_WIDTH_HINT;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Section contain short info field. This similar to a comment section except that
 * it provides a label and a text to display. This is so it can be inserted into
 * a page/dialog with a number of other fields and aling nicely rather than look
 * out of place.
 * 
 * @author Kris De Volder
 */
public class InfoFieldSection extends WizardPageSection {

	private final String labelText;
	private final String infoText;

	public InfoFieldSection(IPageWithSections owner, String label, String info) {
		super(owner);
		this.labelText = label;
		this.infoText = info;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}

	@Override
	public void createContents(Composite page) {
		Composite composite =  new Composite(page, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelText);
        GridDataFactory.fillDefaults()
        	.hint(UIConstants.fieldLabelWidthHint(label), SWT.DEFAULT)
        	.align(SWT.BEGINNING, SWT.CENTER)
        	.applyTo(label);

        Label info = new Label(composite, SWT.NONE);
        info.setText(infoText);
        GridDataFactory.fillDefaults()
        	.grab(true, false)
        	.align(SWT.BEGINNING, SWT.BEGINNING)
        	.applyTo(info);
        
	}

}
