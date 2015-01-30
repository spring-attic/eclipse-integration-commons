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
package org.springsource.ide.eclipse.commons.quicksearch.ui;

import static org.springsource.ide.eclipse.commons.quicksearch.core.preferences.QuickSearchPreferences.IGNORED_EXTENSIONS;
import static org.springsource.ide.eclipse.commons.quicksearch.core.preferences.QuickSearchPreferences.IGNORED_NAMES;
import static org.springsource.ide.eclipse.commons.quicksearch.core.preferences.QuickSearchPreferences.IGNORED_PREFIXES;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.PreferencePageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsGroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.UIConstants;
import org.springsource.ide.eclipse.commons.quicksearch.core.preferences.QuickSearchPreferences;
import org.springsource.ide.eclipse.commons.quicksearch.core.priority.DefaultPriorityFunction;

/**
 *
 * @author Kris De Volder
 */
public class QuickSearchIgnoreSection extends PrefsPageSection {

	private static final String[] prefsKeys = {
		IGNORED_EXTENSIONS, IGNORED_PREFIXES, IGNORED_NAMES
	};

	private static final String[] fieldNames = {
		"Extensions", "Prefixes", "Names"
	};

	private static final String[] toolTips = {
		"Enter a list of file extensions. Elements in the list can be separated by commas or newlines." +
		"Any file or folder ending with one of the extensions will be ignored.",
		"Enter a list of file prefixes. Elements in the list can be separated by commas or newlines." +
		"Any file or folder who's name begins with one of the extensions will be ignored.",
		"Enter a list of file names. Elements in the list can be separated by commas or newlines." +
		"Any file or folder who's name equals one of the extensions will be ignored."
	};

	private QuickSearchPreferences prefs;
	private IEclipsePreferences prefsStore;

	private PrefsGroupSection group;

	public QuickSearchIgnoreSection(PreferencePageWithSections owner) {
		super(owner);
		prefs = QuickSearchActivator.getDefault().getPreferences();
		prefsStore = prefs.getStore();

		String[] defaultIgnores = {
				withSeparator(", ", defaultPriorityFun.ignoredExtensions),
				withSeparator(", ", defaultPriorityFun.ignoredPrefixes),
				withSeparator(", ", defaultPriorityFun.ignoredNames)
		};

        IgnoreListField[] fields = new IgnoreListField[prefsKeys.length];
        for (int i = 0; i < prefsKeys.length; i++) {
        	fields[i] = new IgnoreListField(owner, fieldNames[i], prefsKeys[i], defaultIgnores[i], toolTips[i]);
		};

		this.group = new PrefsGroupSection(owner, "Ignore", fields);
	}

	private DefaultPriorityFunction defaultPriorityFun = new DefaultPriorityFunction();

	private class IgnoreListField extends PrefsPageSection {
		private static final int HEIGHT_HINT = 60; //TODO: compute based on font size. 3 / 4 lines of text
		private static final int FIELD_INDENT = 30;
		Label label;
		Text text;
		private String prefsKey;
		private String labelText;
		private String defaultValue;
		private String tooltip;

		public IgnoreListField(PreferencePageWithSections owner, String labelText, String prefsKey, String defaultValue, String tooltip) {
			super(owner);
			this.labelText = labelText;
			this.defaultValue = defaultValue;
			this.prefsKey = prefsKey;
			this.defaultValue = defaultValue;
			this.tooltip = tooltip;
		}

		public boolean performOK() {
			prefsStore.put(prefsKey, text.getText());
			return true;
		}

		public void performDefaults() {
			text.setText(defaultValue);
		}

		public LiveExpression<ValidationResult> getValidator() {
			return Validator.constant(ValidationResult.OK);
		}

		@Override
		public void createContents(Composite parent) {
	        GridDataFactory alignLabel = GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING);

			label = new Label(parent, SWT.NONE);
			label.setText(this.labelText);
			alignLabel.applyTo(label);
			label.setToolTipText(tooltip);

	    	text = new Text(parent, SWT.BORDER|SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL|SWT.WRAP);
	    	GridDataFactory.fillDefaults()
	    		.hint(UIConstants.FIELD_TEXT_AREA_WIDTH, HEIGHT_HINT)
	    		.grab(true, false)
	    		.indent(FIELD_INDENT, 0)
	    		.applyTo(text);
	    	text.setText(prefsStore.get(prefsKey, defaultValue));
			text.setToolTipText(tooltip);
		}
	}

	@Override
	public boolean performOK() {
		boolean result = group.performOK();
		if (result) {
			try {
				prefsStore.flush();
			} catch (BackingStoreException e) {
				QuickSearchActivator.log(e);
			}
		}
		return result;
	}

	@Override
	public void performDefaults() {
		group.performDefaults();
	}

	private String withSeparator(String string, String[] strings) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			if (i>0) {
				buf.append(", ");
			}
			buf.append(strings[i]);
		}
		return buf.toString();
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return group.getValidator();
	}

	@Override
	public void createContents(Composite parent) {
		group.createContents(parent);
	}

}
