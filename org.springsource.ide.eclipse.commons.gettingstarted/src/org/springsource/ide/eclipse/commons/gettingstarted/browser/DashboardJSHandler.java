package org.springsource.ide.eclipse.commons.gettingstarted.browser;

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.commons.gettingstarted.dashboard.DashboardEditor;
import org.springsource.ide.eclipse.commons.ui.UiUtil;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.AggregateFeedJob;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class DashboardJSHandler {

	private static final int FEEDS_DESCRIPTION_MAX = 200;

	public static final String RESOURCE_DASHBOARD_FEEDS_BLOGS = "dashboard.feeds.blogs";

	private DashboardEditor editor;

	private List<SyndEntry> displayedEntries = new ArrayList<SyndEntry>();

	private Set<AggregateFeedJob> unfinishedJobs = new CopyOnWriteArraySet<AggregateFeedJob>();

	private WebEngine engine;

	private WebView view;

	public DashboardJSHandler(WebView view, DashboardEditor editor) {
		this.view = view;
		this.engine = view.getEngine();
		this.editor = editor;
		JSObject window = (JSObject) engine.executeScript("window");
		window.setMember("ide", this);
		displayFeeds();
	}

	// It's not really clear why, but I'm not able to get getElementById to work
	// as expected
	private Element getElement(Element element, String id) {
		String idAttr = element.getAttribute("id");
		if (idAttr != null && idAttr.equals(id)) {
			return element;
		}
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item instanceof Element) {
				Element child = getElement((Element) item, id);
				if (child != null) {
					return child;
				}
			}
		}
		return null;
	}

	public String toDocumentString(Document doc) {
		StringWriter sw = new StringWriter();
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void displayFeeds() {
		final Map<String, String> springMap = new HashMap<String, String>();
		String[] urls = ResourceProvider.getUrls(RESOURCE_DASHBOARD_FEEDS_BLOGS);
		for (String url : urls) {
			springMap.put(url, null);
		}
		final AggregateFeedJob job = new AggregateFeedJob(springMap, "Feeds");
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				unfinishedJobs.remove(job);
				Map<SyndEntry, SyndFeed> entryToFeed = job.getFeedReader()
						.getFeedsWithEntries();
				Set<SyndEntry> entries = entryToFeed.keySet();
				final String innerHtml = displayFeeds(entries);
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						JSObject js = (JSObject) engine.executeScript("window");
						js.call("setRssHtml", innerHtml);
						final Document doc = engine.getDocument();
						System.out.println(toDocumentString(doc));
						view.requestLayout();
					}

				});
			}
		});
		unfinishedJobs.add(job);
		job.schedule();
	}

	private String displayFeeds(Set<SyndEntry> entries) {
		String html = "";
		// make sure the entries are sorted correctly
		List<SyndEntry> sortedEntries = new ArrayList<SyndEntry>(entries);
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
			// Element row = engine.getDocument().createElement("tr");
			// table.appendChild(row);
			// Element cell = engine.getDocument().createElement("td");
			// row.appendChild(cell);
			html += displayFeed(entry);
		}
		return html;
	}

	private String displayFeed(SyndEntry entry) {
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
		html += "<article>";
		html += "<header class=\"rssTitle\"><h1>" + entry.getTitle() + "</h1></header>";
		html += "<p class=\"rssText\">" + trimText(getDescription(entry)) + "</p>";
		html += "<footer class=\"rssAuthor\">" + entryAuthor + " <i>" + dateString + "</i></footer>";
		html += "</article>";
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

	private String getDescription(SyndEntry entry) {
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

	// private void displayUpdate(final SyndEntry entry, String severity,
	// Composite composite, int pos, Control[] children) {
	// ImageHyperlink link;
	// FormText text;
	//
	// if (pos < children.length / 2) {
	// link = (ImageHyperlink) children[pos * 2];
	// link.setVisible(true);
	// text = (FormText) children[pos * 2 + 1];
	// text.setVisible(true);
	// } else {
	// link = toolkit.createImageHyperlink(composite, SWT.NONE);
	// text = new FormText(composite, SWT.WRAP | SWT.MULTI | SWT.NO_BACKGROUND
	// | SWT.NO_FOCUS);
	// text.setHyperlinkSettings(toolkit.getHyperlinkGroup());
	// final TableWrapData data = new TableWrapData();
	// data.indent = UPDATE_INDENTATION;
	// data.maxWidth = updateSection.getSize().x - UPDATE_TEXT_WRAP_INDENT;
	// text.setLayoutData(data);
	// text.addPaintListener(new PaintListener() {
	//
	// public void paintControl(PaintEvent e) {
	// data.maxWidth = updateSection.getSize().x - UPDATE_TEXT_WRAP_INDENT;
	// }
	// });
	//
	// text.setBackground(toolkit.getColors().getBackground());
	// text.addHyperlinkListener(new HyperlinkAdapter() {
	//
	// @Override
	// public void linkActivated(HyperlinkEvent e) {
	// if (e.data instanceof String) {
	// TasksUiUtil.openUrl((String) e.data);
	// }
	// }
	// });
	// }
	//
	// link.setText(entry.getTitle());
	//
	// TableWrapData linkData = new TableWrapData();
	// if ("important".equals(severity)) {
	// link.setImage(StsUiImages.getImage(StsUiImages.IMPORTANT));
	// linkData.indent = 0;
	// } else if ("warning".equals(severity)) {
	// link.setImage(StsUiImages.getImage(StsUiImages.WARNING));
	// linkData.indent = 0;
	// } else {
	// // link.setImage(IdeUiPlugin.getImage(ICON_BLOG_BLANK));
	// linkData.indent = UPDATE_INDENTATION - 1;
	// }
	// link.setLayoutData(linkData);
	//
	// link.addHyperlinkListener(new HyperlinkAdapter() {
	// @Override
	// public void linkActivated(HyperlinkEvent e) {
	// String url = entry.getLink();
	//
	// if (url == null) {
	// return;
	// }
	// TasksUiUtil.openUrl(url);
	// }
	// });
	//
	// String description = getDescription(entry);
	// if (entry.getPublishedDate() != null &&
	// description.endsWith("</p></form>")) {
	// String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
	// entry.getPublishedDate());
	// description = description.replace("</p></form>", " (" + dateString
	// + ")</p></form>");
	// } else if (entry.getPublishedDate() != null) {
	// String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
	// entry.getPublishedDate());
	// description = description + " (" + dateString + ")";
	// }
	//
	// text.setText(description, description.startsWith("<form>"), true);
	// text.setForeground(feedColor);
	// text.setBackground(toolkit.getColors().getBackground());
	// addImages(text, description);
	// }
	//
	// private void displayUpdates(final Composite composite, final PageBook
	// pagebook,
	// final Composite disclaimer) {
	// Map<String, String> map = new HashMap<String, String>();
	// map.put(ResourceProvider.getUrl(RESOURCE_DASHBOARD_FEEDS_UPDATE), null);
	//
	// final AggregateFeedJob job = new AggregateFeedJob(map, "Updates");
	// job.addJobChangeListener(new JobChangeAdapter() {
	// @Override
	// public void done(IJobChangeEvent event) {
	// unfinishedJobs.remove(job);
	//
	// Display display = PlatformUI.getWorkbench().getDisplay();
	// if (display != null && !display.isDisposed()) {
	// display.asyncExec(new Runnable() {
	// public void run() {
	// if (getManagedForm().getForm().isDisposed()) {
	// return;
	// }
	//
	// List<UpdateNotification> notifications = job
	// .getNotifications();
	// if (notifications.isEmpty()) {
	// pagebook.showPage(disclaimer);
	// return;
	// }
	//
	// // make sure the entries are sorted correctly
	// Collections.sort(notifications,
	// new Comparator<UpdateNotification>() {
	// public int compare(UpdateNotification o1,
	// UpdateNotification o2) {
	// return o2
	// .getEntry()
	// .getPublishedDate()
	// .compareTo(
	// o1.getEntry()
	// .getPublishedDate());
	// }
	// });
	//
	// int counter = 0;
	// Control[] children = composite.getChildren();
	// for (UpdateNotification notification : notifications) {
	// displayUpdate(notification.getEntry(),
	// notification.getSeverity(), composite, counter,
	// children);
	// counter++;
	// }
	//
	// for (int i = counter * 2; i < children.length; i++) {
	// children[i].dispose();
	// }
	//
	// composite.changed(composite.getChildren());
	// composite.pack(true);
	// composite.redraw();
	// composite.getParent().redraw();
	// }
	// });
	// }
	// }
	// });
	// unfinishedJobs.add(job);
	// job.schedule();
	// }

	// public void updateFeeds() {
	// final Map<String, String> springMap = new HashMap<String, String>();
	// String[] urls = ResourceProvider.getUrls(RESOURCE_DASHBOARD_FEEDS_BLOGS);
	// for (String url : urls) {
	// springMap.put(url, null);
	// }
	// FeedsReader reader = new FeedsReader();
	// CachedFeedsManager manager = new CachedFeedsManager("Feeds", springMap,
	// reader);
	// try {
	// manager.readCachedFeeds(null);
	// Set<SyndEntry> entries = new HashSet<SyndEntry>();
	// for (SyndFeed entry : reader.getFeeds()) {
	// entries.addAll(entry.getEntries());
	// }
	// } catch (IllegalArgumentException e) {
	// StatusHandler
	// .log(new Status(
	// IStatus.ERROR,
	// IdeUiPlugin.PLUGIN_ID,
	// "An unexpected error occurred while retrieving feed content from cache.",
	// e));
	// } catch (FeedException e) {
	// StatusHandler
	// .log(new Status(
	// IStatus.ERROR,
	// IdeUiPlugin.PLUGIN_ID,
	// "An unexpected error occurred while retrieving feed content from cache.",
	// e));
	// }
	//
	// }

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

	/*
	 * TODO: disabled for now. Code can't compile here. It needs access to the
	 * guides wizard. private boolean importGuideUrl(URI uri) { String host =
	 * uri.getHost(); if ("github.com".equals(host)) { Path path = new
	 * Path(uri.getPath()); //String org = path.segment(0); Curently ignore the
	 * org. String guideName = path.segment(1); if (guideName !=null &&
	 * guideName.startsWith("gs-")) {
	 * //GuideImportWizard.open(getSite().getShell(),
	 * GettingStartedContent.getInstance().getGuide(guideName));
	 * GettingStartedGuide guide =
	 * GettingStartedContent.getInstance().getGuide(guideName); if (guide!=null)
	 * { GSImportWizard.open(getShell(), guide); return true; } } } return
	 * false; }
	 */
}
