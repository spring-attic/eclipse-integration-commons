/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;


import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.util.ReflowUtil;

/**
 * Section containing an ExpandableComposite that contains another
 * section which is shown/hidden inside the expandable composite.
 * <p>
 * The contained page section is assumed to be a SelectionSource<T>
 * and so the expandable composite is as well. The 'selection' in
 * the child are propagated as the selection of the composite,
 * but only when the composite is in the 'expanded' state.
 * In non-expanded state, the composite propagates an empty
 * selection instead.
 * <p>
 * Note: This has not been used in many contexts and may not
 * be re-usable as is in contexts where it hasn't been tested.
 * The component is somewhat fiddly w.r.t. how parent composite
 * need to reflow their layout when this element is
 * expanded/collapsed.
 *
 * @author Kris De Volder
 */
public class ExpandableSection extends WizardPageSection implements Disposable {

	private IPageSection child;
	private String title;
	private LiveVariable<Boolean> expansionState = new LiveVariable<>(true);
	private LiveVariable<Boolean> visibleState = new LiveVariable<>(true);

	public ExpandableSection(IPageWithSections owner, String title, IPageSection expandableContent) {
		super(owner);
		this.title = title;
		this.child = expandableContent;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public void createContents(final Composite page) {
		final ExpandableComposite comp = new ExpandableComposite(page, SWT.NONE);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);

		comp.setText(title);
		comp.setLayout(new FillLayout());
		comp.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
			}
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				expansionState.setValue(comp.isExpanded());
				reflow(owner, comp);
			}
		});
		expansionState.addListener(new ValueListener<Boolean>() {
			public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
				if (value!=null && comp!=null && !comp.isDisposed()) {
					boolean newState = value;
					boolean currentState = comp.isExpanded();
					if (currentState!=newState) {
						comp.setExpanded(newState);
						reflow(owner, comp);
					}
				}
			}
		});

		visibleState.addListener(new ValueListener<Boolean>() {
			@Override
			public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
				if (value!=null && comp!=null && !comp.isDisposed()) {
					boolean newState = value;
					comp.setVisible(newState);
					GridData data = (GridData) comp.getLayoutData();
					data.exclude = !newState;
					reflow(owner, comp);
				}
			}
		});

		Composite client = new Composite(comp, SWT.NONE);
		client.setLayout(new GridLayout());

		child.createContents(client);
		comp.setClient(client);
	}

	public LiveVariable<Boolean> getExpansionState() {
		return expansionState;
	}

	@Override
	public void dispose() {
		if (child!=null) {
			if (child instanceof Disposable) {
				((Disposable) child).dispose();
			}
			child = null;
		}
	}

	/**
	 * Called after a expandable section was expanded or collapsed. It should
	 * cause the surrounding parent widgets to 'reflow' to adapt to new size.
	 */
	protected void reflow(IPageWithSections owner, ExpandableComposite comp) {
		ReflowUtil.reflow(owner, comp);
// Old implementation doesn't work as well:
//		boolean reflowed = false;
//		if (owner instanceof Reflowable) {
//			reflowed = ((Reflowable) owner).reflow();
//		}
//		if (!reflowed) {
//			//sortof works in some cases, but may not adjust scrollbars in the page.
//			comp.getParent().layout(true);
//		}
	}

	public void setVisible(boolean reveal) {
		this.visibleState.setValue(reveal);
	}


	@Override
	public String toString() {
		return "ExpandableSection("+title+")";
	}


}
