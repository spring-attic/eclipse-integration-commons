/*******************************************************************************
 * Copyright (c) 2013, 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.util.Parser;

import static org.springsource.ide.eclipse.commons.livexp.ui.UIConstants.*;

/**
 * Wizard section to choose one element from list of elements. Uses a pulldown Combo box to allow selecting
 * an element.
 */
public class ChooseOneSectionCombo<T> extends AbstractChooseOneSection<T> {

	private final SelectionModel<T> selection;
	private final String label; //Descriptive Label for this section
	private LiveExpression<T[]> options; //The elements to choose from

	/**
	 * For a combo that allows text edits, a textInputParser must be provided to convert
	 * the input text into a selection value.
	 */
	private Parser<T> inputParser = null;

	public ChooseOneSectionCombo(IPageWithSections owner, String label, SelectionModel<T> selection, T[] options) {
		this(owner, label, selection, LiveExpression.constant(options));
	}

	public ChooseOneSectionCombo(IPageWithSections owner, String label, SelectionModel<T> selection, LiveExpression<T[]> options) {
		super(owner);
		this.label = label;
		this.selection = selection;
		this.options = options;
	}

	/**
	 * Enable's support for 'editable' text widget in the Combo. This means user can perform textual edits
	 * in addition to using the combo.
	 * <p>
	 * To support these 'free form' edits. A inputParser must be provided.
	 */
	public void allowTextEdits(Parser<T> inputParser) {
		this.inputParser = inputParser;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return selection.validator;
	}

	public LiveVariable<T> getSelection() {
		return selection.selection;
	}

	@Override
	public void createContents(Composite page) {
		Composite field = new Composite(page, SWT.NONE);
		GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(2).create();
		field.setLayout(layout);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(field);

		Label fieldNameLabel = new Label(field, SWT.NONE);
		fieldNameLabel.setText(label);
        GridDataFactory.fillDefaults()
        	.hint(UIConstants.fieldLabelWidthHint(fieldNameLabel), SWT.DEFAULT)
        	.align(SWT.BEGINNING, SWT.CENTER)
        	.applyTo(fieldNameLabel);

		final Combo combo = new Combo(field, inputParser==null?SWT.READ_ONLY:SWT.NONE);

		options.addListener(new ValueListener<T[]>() {
			public void gotValue(org.springsource.ide.eclipse.commons.livexp.core.LiveExpression<T[]> exp, T[] value) {
				combo.setItems(getLabels());
			};
		});
		if (inputParser==null) {
			GridDataFactory.fillDefaults().applyTo(combo);
		} else {
			GridDataFactory.fillDefaults().hint(FIELD_TEXT_AREA_WIDTH, SWT.DEFAULT).applyTo(combo);
		}

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleModifyText(combo);
			}
		});
		selection.selection.addListener(new ValueListener<T>() {
			public void gotValue(LiveExpression<T> exp, T newSelection) {
				if (newSelection!=null) {
					//Technically, not entirely correct. This might
					// select the wrong element if more than one option
					// has the same label text.
					String newText = labelProvider.getText(newSelection);
					combo.setText(newText);
					if (!combo.getText().equals(newText)) {
						//widget rejected the selection. To avoid widget state
						// and model state getting out-of-sync, refelct current
						// widget state back to the model:
						handleModifyText(combo);
					}
				}
			}
		});
	}

	private void handleModifyText(final Combo combo) {
		int selected = combo.getSelectionIndex();
		T[] options = getOptionsArray();
		if (options!=null && selected>=0 && selected<options.length) {
			selection.selection.setValue(getOptionsArray()[selected]);
		} else {
			selection.selection.setValue(parse(combo.getText()));
		}
	}

	private T parse(String text) {
		try {
			if (inputParser!=null) {
				return inputParser.parse(text);
			}
		} catch (Exception e) {
			//ignore unparsable input
		}
		return null;
	}

	private String[] getLabels() {
		String[] labels = new String[getOptionsArray().length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = labelProvider.getText(getOptionsArray()[i]);
		}
		return labels;
	}

	private T[] getOptionsArray() {
		return options.getValue();
	}

	public LiveExpression<T[]> getOptions() {
		return options;
	}

	/**
	 * Convenience method that returns the options cast to LiveVariable. This method
	 * will throw an {@link ClassCastException} if the options were not provided
	 * via a LiveVariable.
	 */
	public LiveVariable<T[]> getOptionsVar() {
		return (LiveVariable<T[]>) options;
	}
}
