/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * @author Kris De Volder
 */
public abstract class WizardPageWithSections extends WizardPage implements IPageWithSections, ValueListener<ValidationResult> {

	/**
	 * A delay used for posting status messages to the dialog area after a status update happens.
	 * This is to get rid of spurious message that only appear for a fraction of a second as
	 * internal some auto updating states in models are inconsistent. E.g. in new boot project wizard 
	 * when project name is entered it is temporarily inconsistent with default project location until
	 * that project location itself is update in response to the change event from the project name.
	 * If the project location validator runs before the location update, a spurious validation error
	 * temporarily results.
	 * 
	 * Note: this is a hacky solution. It would be better if the LiveExp framework solved this by
	 * tracking and scheduling refreshes based on the depedency graph. Thus it might guarantee
	 * that the validator never sees the inconsistent state because it is refreshed last.
	 */
	private static final long MESSAGE_DELAY = 250;

	protected WizardPageWithSections(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	private List<WizardPageSection> sections = null;
	private CompositeValidator validator;
	private UIJob updateJob;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		GridDataFactory.fillDefaults().grab(true,true).applyTo(parent);
		
		ScrolledComposite scroller = new PackedScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		Display display = Display.getCurrent();
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		scroller.setBackground(blue);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		Composite page = new Composite(scroller, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 12;
        layout.marginWidth = 12;
        page.setLayout(layout);
        validator = new CompositeValidator();
        for (PageSection section : getSections()) {
			section.createContents(page);
			validator.addChild(section.getValidator());
		}
        validator.addListener(this);
        Dialog.applyDialogFont(page);
        page.pack(true);
        scroller.setMinSize(page.getSize());
//        scroller.setSize(page.getSize());
		scroller.setContent(page);
        setControl(scroller);
        if (getContainer().getCurrentPage()!=null) { // Otherwise an NPE will ensue when updating buttons. Buttons depend on current page so that is logical.
	        getContainer().updateButtons();
	        getContainer().updateMessage();
        }
	}
	
	protected synchronized List<WizardPageSection> getSections() {
		if (sections==null) {
			sections = createSections();
		}
		return sections;
	}
	
	/**
	 * This method should be implemented to generate the contents of the page.
	 */
	protected abstract List<WizardPageSection> createSections();
	
	public void gotValue(LiveExpression<ValidationResult> exp, final ValidationResult status) {
//		setPageComplete(status.isOk()); //Don't delay this, never allow clicking finish button if state not consistent.
		scheduleUpdateJob();
	}
	
	private synchronized void scheduleUpdateJob() {
		Shell shell = getShell();
		if (shell!=null) {
			if (this.updateJob==null) {
				this.updateJob = new UIJob("Update Wizard message") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						ValidationResult status = validator.getValue();
						setErrorMessage(null);
						setMessage(null);
						if (status.isOk()) {
						} else if (status.status == IStatus.ERROR) {
							setErrorMessage(status.msg);
						} else if (status.status == IStatus.WARNING) {
							setMessage(status.msg, IMessageProvider.WARNING);
						} else if (status.status == IStatus.INFO) {
							setMessage(status.msg, IMessageProvider.INFORMATION);
						} else {
							setMessage(status.msg, IMessageProvider.NONE);
						}
						setPageComplete(status.isOk());
						return Status.OK_STATUS;
					}
				};
				updateJob.setSystem(true);
			}
			updateJob.schedule(MESSAGE_DELAY);
		}
	}

	public void dispose() {
		for (WizardPageSection s : sections) {
			s.dispose();
		}
	}

}
