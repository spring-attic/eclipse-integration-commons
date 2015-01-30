/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.ui;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.quicksearch.core.preferences.QuickSearchPreferences;
import org.springsource.ide.eclipse.commons.quicksearch.util.LineReader;

public class MaxLineLengthSection extends PrefsPageSection {

	private StringFieldSection input;
	private StringFieldModel model;

	private QuickSearchPreferences prefs = QuickSearchActivator.getDefault().getPreferences();

	public MaxLineLengthSection(IPageWithSections owner) {
		super(owner);
		model = new StringFieldModel("Max Line Length", ""+prefs.getMaxLineLen());
		model.validator(new Validator() {
			{
				dependsOn(model.getVariable());
			}
			protected ValidationResult compute() {
				String str = model.getValue();
				if (str!=null) {
					try {
						int val = Integer.parseInt(str);
						if (val<=0) {
							return ValidationResult.error("Max Line Length must be a positive integer");
						} else if (val <= 400) {
							return ValidationResult.warning("Low Max Line Length is likely "
									+ "to cause desirable search results to go missing");
						}
					} catch (NumberFormatException e) {
						return ValidationResult.error("Max Line Length can't be parsed as an integer");
					}
				}
				return ValidationResult.OK;
			}
		});
		input = new StringFieldSection(owner, model);
		input.tooltip("When QuickSearch encounters a line of text longer than 'Max Line Length' it stops"
				+ "searching the current file. This is meant to avoid searching in machine generated text "
				+ "files, such as, minified javascript."
		);
	}

	@Override
	public boolean performOK() {
		try {
			IEclipsePreferences store = prefs.getStore();
			store.putInt(QuickSearchPreferences.MAX_LINE_LEN, Integer.parseInt(model.getValue()));
			store.flush();
			return true;
		} catch (Exception e) {
			//bad data do not put.
		}
		return false;
	}

	@Override
	public void performDefaults() {
		model.setValue(""+LineReader.DEFAULT_MAX_LINE_LENGTH);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return input.getValidator();
	}

	@Override
	public void createContents(Composite page) {
		input.createContents(page);
	}

}
