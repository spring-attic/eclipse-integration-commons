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
package org.springsource.ide.eclipse.commons.gettingstarted.wizard.url;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.commons.gettingstarted.content.BuildType;
import org.springsource.ide.eclipse.commons.gettingstarted.content.CodeSet;
import org.springsource.ide.eclipse.commons.gettingstarted.content.GSContent;
import org.springsource.ide.eclipse.commons.gettingstarted.content.ZipFileCodeSet;
import org.springsource.ide.eclipse.commons.gettingstarted.importing.ImportUtils;
import org.springsource.ide.eclipse.commons.gettingstarted.util.DownloadManager;
import org.springsource.ide.eclipse.commons.gettingstarted.util.DownloadableItem;
import org.springsource.ide.eclipse.commons.gettingstarted.util.UIThreadDownloadDisallowed;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectLocationValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectNameValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.UrlValidator;

import static org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection.getDefaultProjectLocation;
import static org.springsource.ide.eclipse.commons.ui.UiUtil.openUrl;

/**
 * A ZipUrlImportWizard is a simple wizard in which one can paste a url
 * pointing to a zip file. The zip file is supposed to contain a maven (or gradle)
 * project in the root of the zip.
 */
public class ZipUrlImportWizardModel {
	
	public final LiveVariable<String> url = new LiveVariable<String>("http://initializr.cfapps.io/starter.zip");
	public final LiveExpression<ValidationResult> urlValidator = new UrlValidator("Zip Url", url);
	
	//todo: maybe project name can be derived from the zip file contents (pom.xml <name> tag).
	
	public final LiveVariable<String> projectName = new LiveVariable<String>("initializer-demo");
	public final NewProjectNameValidator projectNameValidator = new NewProjectNameValidator(projectName); 
	
	public final LiveVariable<String> location = new LiveVariable<String>(getDefaultProjectLocation(projectName.getValue()));
	public final NewProjectLocationValidator locationValidator = new NewProjectLocationValidator("Location", location, projectName);
	private boolean allowUIThread = false;
	
	public void performFinish(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
		mon.beginTask("Importing "+url, 1);
		DownloadManager downloader = null;
		try {
			downloader = new DownloadManager().allowUIThread(allowUIThread);
			
			DownloadableItem zip = new DownloadableItem(newURL(url.getValue()), downloader);
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
