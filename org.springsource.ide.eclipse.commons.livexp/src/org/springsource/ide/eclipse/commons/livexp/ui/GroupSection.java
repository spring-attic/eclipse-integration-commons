/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class GroupSection extends WizardPageSection {

	List<WizardPageSection> sections;
	
	private CompositeValidator validator;
	private String groupTitle;
	
	/**
	 * Setting isVisible to false will make this group disappear.
	 * Setting it to true will make it re-appear.
	 */
	public final LiveVariable<Boolean> isVisible = new LiveVariable<Boolean>(true);

	/**
	 * If title is null then it creates a normal composite without a box around it. Otherwise
	 * it creates a 'group' and uses the title as label for the group.
	 */
	public GroupSection(WizardPageWithSections owner, String title, WizardPageSection... _sections) {
		super(owner);
		this.groupTitle = title;
		this.sections = new ArrayList<WizardPageSection>();
		for (WizardPageSection s : _sections) {
			if (s!=null) {
				sections.add(s);
			}
		}
		
		validator = new CompositeValidator();
		for (WizardPageSection s : sections) {
			validator.addChild(s.getValidator());
		}
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

	@Override
	public void createContents(Composite page) {
		final Composite group = createComposite(page);
		for (WizardPageSection s : sections) {
			s.createContents(group);
		}
		isVisible.addListener(new ValueListener<Boolean>() {
			public void gotValue(LiveExpression<Boolean> exp, Boolean isVisible) {
				group.setVisible(isVisible); 
				GridData layout = (GridData) group.getLayoutData();
				layout.exclude = !isVisible;
				group.setLayoutData(layout);
				owner.getShell().layout(new Control[] {group});
			};
		});
	}

	private Composite createComposite(Composite page) {
		if (groupTitle!=null) {
			//Create a group with box around it and a title
			final Group group = new Group(page, SWT.NONE);
			group.setText(groupTitle);
			group.setLayout(createLayout());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
			return group;
		} else {
			//Create a normal composite. No box
			final Composite composite = new Composite(page, SWT.NONE);
			composite.setLayout(createLayout());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
			return composite;
		}
	}

	/**
	 * Factory method that creates the layout for the group. Subclass may overide to change how
	 * components in the group are layed out. Default just creates a 1 column GridLayout
	 */
	protected GridLayout createLayout() {
		return new GridLayout(1, false);
	}
	
	@Override
	public void dispose() {
		for (WizardPageSection s : sections) {
			s.dispose();
		}
		super.dispose();
	}
	
}
