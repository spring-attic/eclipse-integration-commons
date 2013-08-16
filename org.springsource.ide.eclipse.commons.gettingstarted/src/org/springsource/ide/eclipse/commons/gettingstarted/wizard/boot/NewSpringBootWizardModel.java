/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.wizard.boot;

import static org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection.getDefaultProjectLocation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.commons.gettingstarted.content.BuildType;
import org.springsource.ide.eclipse.commons.gettingstarted.content.CodeSet;
import org.springsource.ide.eclipse.commons.gettingstarted.importing.ImportUtils;
import org.springsource.ide.eclipse.commons.gettingstarted.util.DownloadManager;
import org.springsource.ide.eclipse.commons.gettingstarted.util.DownloadableItem;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectLocationValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectNameValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.UrlValidator;

/**
 * A ZipUrlImportWizard is a simple wizard in which one can paste a url
 * pointing to a zip file. The zip file is supposed to contain a maven (or gradle)
 * project in the root of the zip.
 */
public class NewSpringBootWizardModel {
	
	private static final String DEFAULT_URL = "http://initializr.cfapps.io/starter.zip";
	
	//todo: maybe project name can be derived from the zip file contents (pom.xml <name> tag).
	
	public final StringFieldModel projectName = new StringFieldModel("name", "initializer-demo");
	{
		projectName
			.label("Project Name")
			.validator(new NewProjectNameValidator(projectName.getVariable()));
	}
	
	@SuppressWarnings("unchecked")
	public final List<FieldModel<String>> stringInputs = Arrays.asList(
			new StringFieldModel("groupId", "org.demo").label("Group Id"),
			new StringFieldModel("artifactId", "demo").label("Artifact Id"),
			new StringFieldModel("description", "Demo project").label("Description"),
			new StringFieldModel("packageName", "demo").label("Package")
	);
	
	public final MultiSelectionFieldModel<String> style = new MultiSelectionFieldModel<String>(String.class, "style")
		.label("Style")
		.choice("Standard", "")
		.choice("Web", "web")
		.choice("Actuator", "actuator")
		.choice("Batch", "batch")
		.choice("JPS", "jpa");
	
	public final LiveVariable<String> location = new LiveVariable<String>(getDefaultProjectLocation(projectName.getValue()));
	public final NewProjectLocationValidator locationValidator = new NewProjectLocationValidator("Location", location, projectName.getVariable());
	
	private boolean allowUIThread = false;

	public final LiveVariable<String> baseUrl = new LiveVariable<String>(DEFAULT_URL);
	public final LiveExpression<ValidationResult> baseUrlValidator = new UrlValidator("Base Url", baseUrl);
	
	public final LiveVariable<String> downloadUrl = new LiveVariable<String>();
	{ 
		UrlMaker computedUrl = new UrlMaker(baseUrl).addField(projectName);
		for (FieldModel<String> param : stringInputs) {
			computedUrl.addField(param);
		}
		computedUrl.addField(style);
		computedUrl.addListener(new ValueListener<String>() {
			public void gotValue(LiveExpression<String> exp, String value) {
				downloadUrl.setValue(value);
			}
		});
	}
	
	public void performFinish(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
		mon.beginTask("Importing "+baseUrl, 1);
		DownloadManager downloader = null;
		try {
			downloader = new DownloadManager().allowUIThread(allowUIThread);
			
			DownloadableItem zip = new DownloadableItem(newURL(downloadUrl .getValue()), downloader);
			CodeSet cs = CodeSet.fromZip(projectName.getValue(), zip, new Path("/"));
			
			IRunnableWithProgress oper = BuildType.MAVEN.getImportStrategy().createOperation(ImportUtils.importConfig(
					new Path(location.getValue()),
					projectName.getValue(),
					cs
			));
			oper.run(new SubProgressMonitor(mon, 1));
			
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (downloader!=null) {
				downloader.dispose();
			}
			mon.done();
		}
	}

	private URL newURL(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			//This should be impossible because the URL syntax is validated beforehand.
			GettingStartedActivator.log(e);
			return null;
		}
	}

	/**
	 * This is mostly for testing purposes where it is just easier to run stuff in the UIThread (test do so
	 * by default). But in production we shouldn't allow downloading stuff in the UIThread.
	 */
	public void allowUIThread(boolean allow) {
		this.allowUIThread = allow;
	}

}
