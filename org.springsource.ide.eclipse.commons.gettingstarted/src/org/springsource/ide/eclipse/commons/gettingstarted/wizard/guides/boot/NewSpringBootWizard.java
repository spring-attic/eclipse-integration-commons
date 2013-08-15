/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.wizard.guides.boot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.springframework.ide.eclipse.wizard.WizardImages;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public class NewSpringBootWizard extends Wizard implements INewWizard {
	
	private static final ImageDescriptor IMAGE = WizardImages.TEMPLATE_WIZARD_ICON;
	   //TODO: Get our own icon for GSG wizard
	
	private NewSpringBootWizardModel model = new NewSpringBootWizardModel();
	
	public NewSpringBootWizard() {
		setDefaultPageImageDescriptor(IMAGE);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	public void addPages() {
		super.addPages();
		addPage(new PageOne());
//		addPage(pageTwo);
	}
	
	public class PageOne extends WizardPageWithSections {

		protected PageOne() {
			super("page1", "Import Spring Initializer Project", null);
		}

		@Override
		protected List<WizardPageSection> createSections() {
			List<WizardPageSection> sections = new ArrayList<WizardPageSection>();
			sections.add(new StringFieldSection(this, "Project Name", model.projectName, model.projectNameValidator));
			sections.add(new ProjectLocationSection(this, model.location, model.projectName, model.locationValidator));
			sections.add(new StringFieldSection(this, "Zip Url", model.url, model.urlValidator)
				.tooltip("Paste a generated project Zip URL from http://initializr.cfapps.io/")
			);
			
			return sections;
		}
	}

	@Override
	public boolean performFinish() {
		Job job = new Job("Import Getting Started Content") {
			@Override
			protected IStatus run(IProgressMonitor mon) {
				try {
					model.performFinish(mon);
					return Status.OK_STATUS;
				} catch (Throwable e) {
					return ExceptionUtil.status(e);
				}
			}
		};
		//job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setPriority(Job.BUILD);
		job.setUser(true); //shows progress in default eclipse config
		job.schedule();
		return true;
	}


}
