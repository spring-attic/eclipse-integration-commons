/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/

package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import org.eclipse.core.runtime.IConfigurationElement;
import org.springsource.ide.eclipse.commons.browser.BrowserExtensions;
import org.springsource.ide.eclipse.commons.browser.IEclipseToBrowserFunction;

/**
 * @author Miles Parker
 * 
 */
public class ProjectWizardProvider extends IEclipseToBrowserFunction.Extension {

	private static final String GRAILS_WIZARD_ID = "org.grails.ide.eclipse.ui.wizard.newGrailsProjectWizard";

	private static final String ROO_WIZARD_ID = "com.springsource.sts.roo.ui.wizard.newRooProjectWizard";

	private static final String GROOVY_WIZARD_ID = "org.codehaus.groovy.eclipse.ui.groovyProjectWizard";

	private static final String SPRING_WIZARD_ID = "com.springsource.sts.wizard.template";

	private static final String JAVA_WIZARD_ID = "org.eclipse.jdt.ui.wizards.JavaProjectWizard";

	@Override
	public String getDynamicArgumentValue(String id) {
		if (id.equals("html")) {
			String html = "";

			String[] ids = new String[] { JAVA_WIZARD_ID, SPRING_WIZARD_ID, ROO_WIZARD_ID, GROOVY_WIZARD_ID,
					GRAILS_WIZARD_ID };

			for (int i = 0; i < ids.length; i++) {
				IConfigurationElement element = BrowserExtensions.getExtension(
						BrowserExtensions.EXTENSION_ID_NEW_WIZARD, ids[i]);
				if (element != null && element.getAttribute(BrowserExtensions.ELEMENT_CLASS) != null
						&& element.getAttribute(BrowserExtensions.ELEMENT_NAME) != null
						&& element.getAttribute(BrowserExtensions.ELEMENT_ICON) != null) {
					// We use github_download as that seems to provide the shape
					// we
					// need
					html += "<a class=\"ide_widget btn btn-black uppercase\" href=\"\" onclick=\"ide.call('openWizard', '"
							+ ids[i]
							+ "')\">"
							+ "Create "
							+ element.getAttribute(BrowserExtensions.ELEMENT_NAME)
							+ "</a>";
				}
			}
			return html;
		}
		return null;
	}
}
