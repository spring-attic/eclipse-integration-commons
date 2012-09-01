package org.springsource.ide.eclipse.commons.livexp.ui;

import static org.springsource.ide.eclipse.commons.livexp.core.LiveExpression.constant;
import static org.springsource.ide.eclipse.commons.livexp.core.ValidationResult.OK;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

import org.eclipse.swt.widgets.Label;

public class CommentSection extends WizardPageSection {

	private String text;

	public CommentSection(WizardPageWithSections page, String text) {
		super(page);
		this.text = text;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return constant(OK); //Nothing much to validate this just displays a comment.
	}

	@Override
	public void createContents(Composite page) {
		Label l = new Label(page, SWT.WRAP);
		l.setText(text);
		GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(l);
	}

}
