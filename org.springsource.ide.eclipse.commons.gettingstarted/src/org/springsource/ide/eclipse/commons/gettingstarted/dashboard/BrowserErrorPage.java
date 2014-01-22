/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * Page that is shown to the user instead of normal dashboard welcome page when the browser can't 
 * be instantiated.
 * 
 * @author Kris De Volder
 */
public class BrowserErrorPage extends FormDashboardPage {

	public BrowserErrorPage() {
	}

	@Override
	public String getName() {
		return "Error";
	}

	@Override
	public boolean canClose() {
		return false;
	}

	@Override
	protected void createFormContents(Form form) {
		TableWrapData td;
		FormText formText;
		FormToolkit toolkit = getToolkit();
		form.setText("Internal Browser Unavailable");

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns=1;
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 1;
		form.getBody().setLayout(layout);

//		formText = toolkit.createFormText(form.getBody(), true);
//		formText.setLayoutData(td);
//		String text = "Here is some plain text for the text to render.";
//		formText.setText(text, false, false);
//		
//		
//		formText = toolkit.createFormText(form.getBody(), true);
//		td = new TableWrapData(TableWrapData.FILL);
//		td.colspan = 1;
//		formText.setLayoutData(td);
//		text = "<p>Here is some plain text for the text to render; "+
//				" this text is at http://www.eclipse.org web site.</p>";
//		formText.setText(text, true, true);
		

		//		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		//		 td.colspan = 2;
		//		 formText .setLayoutData(td);
		//		 String text = "Here is some plain text for the text to render; "+
		//		   this text is at http://www.eclipse.org web site.";
		//		 formText .setText(text, false, true);

		formText = toolkit.createFormText(form.getBody(), true);
		td = new TableWrapData(TableWrapData.FILL);
		formText.setLayoutData(td);
		formText.setText("<form>"
				+ "The Browser Widget is required to render the new dashboard content. It "
				+ "appears to be unavailable on your system."
				+ "<p>You can:</p>"
				+ "  <li bindent=\"20\">Try to fix this by installing the required software. More information can be"
				+ "      found in the <a href=\"http://www.eclipse.org/swt/faq.php#browserlinux\">SWT FAQ</a>.</li>"
			    + "  <li bindent=\"20\">Switch to the old Dashboard.</li>" 
			    + "  <li bindent=\"20\">Close the dashboard and stop it from being opened again.</li>" 
			    + "<p><a href=\"dashprefs\">Open the Dashboard Preferences Page</a> to make these changes.</p>"
				+ "</form>", true, true);

		formText.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				Object url = e.getHref();
				if ("dashprefs".equals(url)) {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(),
							"com.springsource.sts.ide.ui.preferencePage", null, null);
					dialog.open();
				} else {
					SpringUIUtils.openUrl((String)url);
				}
			}
		});
		
		//		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.EXPANDED);

	}

}
