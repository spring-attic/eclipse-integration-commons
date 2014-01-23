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

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import netscape.javascript.JSObject;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.commons.javafx.browser.IJavaFxBrowserFunction;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.UpdateNotification;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.FeedMonitor;
import org.springsource.ide.eclipse.dashboard.internal.ui.feeds.IFeedListener;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * 
 * @author Miles Parker
 * 
 *         Code refactored from
 *         org.springsource.ide.eclipse.dashboard.internal.ui
 *         .editors.DashboardMainPage:
 * 
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class DashboardWebViewManager {

	private static final int FEEDS_DESCRIPTION_MAX = 200;

	public static final String RESOURCE_DASHBOARD_FEEDS_BLOGS = "dashboard.feeds.blogs";

	public static final String RESOURCE_DASHBOARD_FEEDS_UPDATE = "dashboard.feeds.update";

	private static final String ELEMENT_ID = "id";

	private static final String ELEMENT_CLASS = "class";

	private static final String ELEMENT_ICON = "icon";

	private static final String ELEMENT_NAME = "name";

	private static final String EXTENSION_ID_NEW_WIZARD = "org.eclipse.ui.newWizards";

	private static final String EXTENSION_ID_DASHBOARD_FUNCTION = "org.springsource.ide.common.dashboard.browser.function";

	private static final String GRAILS_WIZARD_ID = "org.grails.ide.eclipse.ui.wizard.newGrailsProjectWizard";

	private static final String ROO_WIZARD_ID = "com.springsource.sts.roo.ui.wizard.newRooProjectWizard";

	private static final String GROOVY_WIZARD_ID = "org.codehaus.groovy.eclipse.ui.groovyProjectWizard";

	private static final String SPRING_WIZARD_ID = "com.springsource.sts.wizard.template";

	private static final String JAVA_WIZARD_ID = "org.eclipse.jdt.ui.wizards.JavaProjectWizard";

	private WebEngine engine;

	private String feedHtml;
	private String wizardHtml;
	private String updateHtml;

	private WebView view;

	private Date lastUpdated = null;

	private Date currentUpdated = null;

	public DashboardWebViewManager() {
		IPreferenceStore prefStore = IdeUiPlugin.getDefault().getPreferenceStore();
		long lastUpdateLong = prefStore
				.getLong(IIdeUiConstants.PREF_FEED_ENTRY_LAST_UPDATE_DISPLAYED);
		lastUpdated = new Date(lastUpdateLong);
		currentUpdated = lastUpdated;
		FeedMonitor.getInstance().addListener(new IFeedListener() {
			
			@Override
			public void updated(String id) {
				checkUpdate();
			}
		});
	}

	public void setClient(WebView view) {
		this.view = view;
		this.engine = view.getEngine();
		JSObject window = (JSObject) engine.executeScript("window");
		window.setMember("ide", this);
		checkUpdate();
	}

	private boolean checkUpdate() {
		wizardHtml = buildCreateWizards();
		updateHtml = buildUpdates();
		feedHtml = buildFeeds();
		if (feedHtml != null && wizardHtml != null && updateHtml != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					JSObject js = (JSObject) engine.executeScript("window");
					js.call("setWizardHtml", wizardHtml);
					js.call("setRssHtml", feedHtml);
					js.call("setUpdateHtml", updateHtml);
					view.requestLayout();
					view.setVisible(true);
					// printPageHtml();
				}
			});
			return true;
		}
		return false;
	}

	private static IConfigurationElement getExtension(String extensionId, String id) {
		IExtensionRegistry registry = org.eclipse.core.runtime.Platform
				.getExtensionRegistry();
		IConfigurationElement[] configurations = registry
				.getConfigurationElementsFor(extensionId);
		for (IConfigurationElement element : configurations) {
			String elementId = element.getAttribute(ELEMENT_ID);
			if (elementId.equals(id)) {
				return element;
			}
		}
		return null;
	}

	private String buildCreateWizards() {
		String html = "";

		String[] ids = new String[] { JAVA_WIZARD_ID, SPRING_WIZARD_ID, ROO_WIZARD_ID,
				GROOVY_WIZARD_ID, GRAILS_WIZARD_ID };

		for (int i = 0; i < ids.length; i++) {
			IConfigurationElement element = getExtension(EXTENSION_ID_NEW_WIZARD, ids[i]);
			if (element != null && element.getAttribute(ELEMENT_CLASS) != null
					&& element.getAttribute(ELEMENT_NAME) != null
					&& element.getAttribute(ELEMENT_ICON) != null) {
				// We use github_download as that seems to provide the shape we
				// need
				html += "<a class=\"ide_widget btn btn-black uppercase\" href=\"\" onclick=\"ide.showWizard('"
						+ ids[i]
						+ "')\">"
						+ "Create "
						+ element.getAttribute(ELEMENT_NAME) + "</a>";
			}
		}
		return html;
	}

	public void call(String functionId, String argument) {
		try {
			IConfigurationElement element = getExtension(EXTENSION_ID_DASHBOARD_FUNCTION,
					functionId);
			if (element != null) {
				IJavaFxBrowserFunction function = (IJavaFxBrowserFunction) WorkbenchPlugin
						.createExtension(element, ELEMENT_CLASS);
				function.call(argument);
			} else {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
						"Could not find dashboard extension: " + functionId));
			}
		} catch (CoreException ex) {
			StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not find dashboard extension", ex));
			return;
		}
	}

	public void showWizard(String extensionId) {
		Object object;
		try {
			IConfigurationElement element = getExtension(EXTENSION_ID_NEW_WIZARD,
					extensionId);
			object = WorkbenchPlugin.createExtension(element, ELEMENT_CLASS);
		} catch (CoreException ex) {
			StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not read dashboard extension", ex));
			return;
		}
		if (!(object instanceof INewWizard)) {
			StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
					"Could not load " + object.getClass().getCanonicalName()
							+ " must implement " + INewWizard.class.getCanonicalName()));
			return;
		}

		INewWizard wizard = (INewWizard) object;
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}

	private String buildFeeds() {
		Set<SyndEntry> feedEntries = FeedMonitor.getInstance().getFeedEntries();
		if (feedEntries == null) {
			return null;
		}
		String html = "";
		List<SyndEntry> sortedEntries = new ArrayList<SyndEntry>(feedEntries);
		Collections.sort(sortedEntries, new Comparator<SyndEntry>() {
			public int compare(SyndEntry o1, SyndEntry o2) {
				Date o1Date = o1.getPublishedDate() != null ? o1.getPublishedDate() : o1
						.getUpdatedDate();
				Date o2Date = o2.getPublishedDate() != null ? o2.getPublishedDate() : o2
						.getUpdatedDate();
				if (o1Date == null && o2Date == null) {
					return 0;
				} else if (o1Date == null) {
					return -1;
				} else if (o2Date == null) {
					return 1;
				} else {
					return o2Date.compareTo(o1Date);
				}
			}
		});

		for (SyndEntry entry : sortedEntries) {
			html += buildFeed(entry);
		}
		return html;
	}

	private String buildUpdates() {
		List<UpdateNotification> updates = FeedMonitor.getInstance().getUpdates();
		if (updates == null) {
			return null;
		}
		String html = "";
		// make sure the entries are sorted correctly
		Collections.sort(updates, new Comparator<UpdateNotification>() {
			public int compare(UpdateNotification o1, UpdateNotification o2) {
				if (o2.getEntry() != null && o2.getEntry().getPublishedDate() != null
						&& o1.getEntry() != null) {
					return o2.getEntry().getPublishedDate()
							.compareTo(o1.getEntry().getPublishedDate());
				}
				return 0;
			}
		});

		for (UpdateNotification notification : updates) {
			String update = buildUpdate(notification);
			if (!update.isEmpty()) {
				html += update;
			}
		}
		return html;
	}

	private String buildUpdate(UpdateNotification notification) {
		String html = "";
		SyndEntry entry = notification.getEntry();
		html += "<div class=\"blog--container blog-preview\">";
		html += "	<div class=\"blog--title\">";
		html += "   <i class=\"fa fa-exclamation new-star\"></i>";
		html += "	<a href=\"\" onclick=\"ide.openPage('" + entry.getLink() + "')\"><b>"
				+ entry.getTitle() + "</b></a>";
		html += "	</div>";
		html += "</div>";
		return html;
	}

	private String buildFeed(SyndEntry entry) {
		String html = "";
		Date entryDate = new Date(0);
		if (entry.getUpdatedDate() != null) {
			entryDate = entry.getUpdatedDate();
		} else {
			entryDate = entry.getPublishedDate();
		}

		String dateString = "";
		if (entryDate != null) {
			dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(entryDate);
		}

		String entryAuthor = "";
		if (entry.getAuthor() != null && entry.getAuthor().trim() != "") {
			entryAuthor = entry.getAuthor();
		}
		html += "<div class=\"blog--container blog-preview\">";
		html += "	<div class=\"blog--title\">";
		if (lastUpdated.before(entryDate)) {
			html += "<i class=\"fa fa-star new-star\"></i>";
		}
		if (currentUpdated.before(entryDate)) {
			currentUpdated = entryDate;
		}
		html += "	<a href=\"\" onclick=\"ide.openPage('" + entry.getLink() + "')\">"
				+ entry.getTitle() + "</a>";
		html += "	</div>";
		html += "	<div class=\"blog--post\">";
		html += "		<div>";
		html += "			<p>" + trimText(buildDescription(entry));
		html += "		<span class=\"author\">" + entryAuthor + " <i>" + dateString
				+ "</i></span></p>";
		html += "		</div>";
		html += "	</div>";
		html += "</div>";
		return html;
	}

	private String trimText(String s) {
		// Remove html encoded entities
		s = StringEscapeUtils.unescapeHtml(s);

		// Remove line breaks and tabs
		s = s.replace("\n", " ");
		s = s.replace("\t", " ");

		// Remove whitespace between text
		String[] vals = s.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String v : vals) {
			if (v.trim().length() > 0) {
				sb.append(v).append(" ");
			}
		}

		if (sb.length() > FEEDS_DESCRIPTION_MAX) {
			return sb.substring(0, findEndOfWord(sb, FEEDS_DESCRIPTION_MAX)) + " ...";
			// return sb.substring(0, FEEDS_DESCRIPTION_MAX) + " ...";
		}
		return sb.toString();
	}

	private int findEndOfWord(StringBuilder sb, int pos) {
		Pattern pattern = Pattern.compile("\\w");
		while (pos < sb.length()) {
			if (pattern.matcher(sb.subSequence(pos, pos + 1)).matches()) {
				pos++;
			} else {
				return pos;
			}
		}
		return pos;
	}

	private String buildDescription(SyndEntry entry) {
		SyndContent content = entry.getDescription();
		if (content == null) {
			List nestedContent = entry.getContents();
			if (!nestedContent.isEmpty()) {
				Object obj = nestedContent.get(0);
				if (obj instanceof SyndContent) {
					content = (SyndContent) obj;
				}
			}
		}
		if (content == null) {
			return "";
		}

		String value = content.getValue();
		if (value == null) {
			return "";
		}

		if (value.startsWith("<form>")) {
			return value;
		}
		return removeHtmlEntities(value);
	}

	private String removeHtmlEntities(String value) {
		StringBuilder result = new StringBuilder();
		boolean tagOpened = false;
		for (char currChar : value.toCharArray()) {
			if (currChar == '<') {
				tagOpened = true;
			} else if (currChar == '>') {
				tagOpened = false;
			} else {
				if (!tagOpened) {
					result.append(currChar);
				}
			}
		}
		return StringEscapeUtils.unescapeHtml(result.toString());

	}

	public void openPage(String url) {
		try {
			WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(url));
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, GettingStartedActivator.JAVAFX_BROWSER_EDITOR_ID);
		} catch (MalformedURLException e) {
			GettingStartedActivator.warn("Bad page url: " + url);
		} catch (PartInitException e) {
			GettingStartedActivator.log(e);
		}
	}

	public void openDashboardPage(String path) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new NullEditorInput(), GettingStartedActivator.EXTENSIONS_EDITOR_ID);
		} catch (PartInitException e) {
			GettingStartedActivator.log(e);
		}
	}

	public void openImportWizard() {
		try {
			IWizardRegistry registry = PlatformUI.getWorkbench()
					.getImportWizardRegistry();
			IWizardDescriptor descriptor = registry
					.findWizard("org.springsource.ide.eclipse.gettingstarted.wizards.import.generic");
			if (descriptor != null) {
				IWorkbenchWizard wiz = descriptor.createWizard();
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell();
				WizardDialog dialog = new WizardDialog(shell, wiz);
				dialog.setBlockOnOpen(false);
				dialog.open();
			}
		} catch (CoreException e) {
			GettingStartedActivator.log(e);
		}
	}

	private void printPageHtml() {
		StringWriter sw = new StringWriter();
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(view.getEngine().getDocument()),
					new StreamResult(sw));
			System.out.println(sw.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void dispose() {
		FeedMonitor.getInstance().markRead();
	}
}
