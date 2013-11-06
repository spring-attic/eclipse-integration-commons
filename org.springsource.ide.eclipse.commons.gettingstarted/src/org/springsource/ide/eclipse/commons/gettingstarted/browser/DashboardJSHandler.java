package org.springsource.ide.eclipse.commons.gettingstarted.browser;

import java.net.URI;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.commons.gettingstarted.dashboard.DashboardEditor;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

public class DashboardJSHandler {
	
	private DashboardEditor editor;

	public DashboardJSHandler(WebEngine engine, DashboardEditor editor) {
		this.editor = editor;
		JSObject window = (JSObject) engine.executeScript("window");
		window.setMember("ide", this);
	}
	
	public void openPage(String url) {
		if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.INTERNAL) {
			if (editor.openWebPage(url)) {
				return;
			}
		} else {
			UiUtil.openUrl(url);
		}
	}

	public void openDashboardPage(String path) {
		editor.setActivePage(path);
	}
	
	public void openImportWizard() {
			try {
				IWizardRegistry registry = PlatformUI.getWorkbench()
						.getImportWizardRegistry();
				IWizardDescriptor descriptor = registry
						.findWizard("org.springsource.ide.eclipse.gettingstarted.wizards.import.generic");
				if (descriptor != null) {
					IWorkbenchWizard wiz = descriptor.createWizard();

					// wiz.setEnableOpenHomePage(enableOpenHomepage);
					// wiz.setItem(guide);
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					WizardDialog dialog = new WizardDialog(shell, wiz);
					dialog.setBlockOnOpen(false);
					dialog.open();
				}
			} catch (CoreException e) {
				GettingStartedActivator.log(e);
			}
		}

/*	TODO: disabled for now. Code can't compile here. It needs access to the guides wizard.
	private boolean importGuideUrl(URI uri) {
		String host = uri.getHost();
		if ("github.com".equals(host)) {
			Path path = new Path(uri.getPath());
			//String org = path.segment(0); Curently ignore the org.
			String guideName = path.segment(1);
			if (guideName !=null && guideName.startsWith("gs-")) {
				//GuideImportWizard.open(getSite().getShell(), GettingStartedContent.getInstance().getGuide(guideName));
				GettingStartedGuide guide = GettingStartedContent.getInstance().getGuide(guideName);
				if (guide!=null) {
					GSImportWizard.open(getShell(), guide);
					return true;
				}
			}
		}
		return false;
	}*/
}
