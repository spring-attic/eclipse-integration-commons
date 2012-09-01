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
		final Group group = new Group(page, SWT.NONE);
		group.setText(groupTitle);
		group.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
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
	
	@Override
	public void dispose() {
		for (WizardPageSection s : sections) {
			s.dispose();
		}
		super.dispose();
	}
	
}
