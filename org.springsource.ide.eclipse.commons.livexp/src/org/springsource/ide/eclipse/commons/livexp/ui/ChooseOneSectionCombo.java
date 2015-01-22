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

/**
 * Wizard section to choose one element from list of elements. Uses a pulldown Combo box to allow selecting
 * an element.
 */
public class ChooseOneSectionCombo<T> extends AbstractChooseOneSection<T> {

	private final SelectionModel<T> selection;
	private final String label; //Descriptive Label for this section
	private T[] options; //The elements to choose from

	/**
	 * For a combo that allows text edits, a textInputParser must be provided to convert
	 * the input text into a selection value.
	 */
	private Parser<T> inputParser = null;
	//TODO: actually edit support isn't implemented yet.

	/**
	 * Create a ChooseOneSectionCombo with lazy options. You must either override the 'computeOptions' method
	 * if you use this constructor.
	 */
	public ChooseOneSectionCombo(IPageWithSections owner, String label, SelectionModel<T> selection) {
		super(owner);
		this.label = label;
		this.selection = selection;
	}

	public ChooseOneSectionCombo(IPageWithSections owner, String label, SelectionModel<T> selection, T[] options) {
		this(owner, label, selection);
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

		final Combo combo = new Combo(field, SWT.READ_ONLY);

		combo.setItems(getLabels());
		GridDataFactory.fillDefaults().applyTo(combo);

		combo.addModifyListener(new ModifyListener() {
			//@Override
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
		if (selected>=0) {
			selection.selection.setValue(getOptions()[selected]);
		} else {
			selection.selection.setValue(null);
		}
	}

	/**
	 * Clients that can't provide the available options in the constructor can instead
	 * override this method which will be called the first time the
	 * options are required (i.e. when UI widgets are being created).
	 */
	protected T[] computeOptions() {
		return null;
	}

	private String[] getLabels() {
		String[] labels = new String[getOptions().length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = labelProvider.getText(getOptions()[i]);
		}
		return labels;
	}

	private T[] getOptions() {
		if (options==null) {
			options = computeOptions();
		}
		return options;
	}

}
