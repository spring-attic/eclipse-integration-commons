/*******************************************************************************
 * Copyright (c) 2012, 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.search.TextSearchControl;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.forms.widgets.FormFonts;
import org.eclipse.ui.part.PageBook;
import org.osgi.framework.Bundle;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.springsource.ide.eclipse.dashboard.internal.ui.IIdeUiConstants;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.discovery.DashboardExtensionsPage;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPage;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPart;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

/**
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class DashboardMainPage extends AbstractDashboardPage implements PropertyChangeListener {

	public static final String RESOURCE_DASHBOARD_BUG_TRACKER = "dashboard.bug.tracker";

	public static final String RESOURCE_DASHBOARD_FEEDS_BLOGS = "dashboard.feeds.blogs";

	public static final String RESOURCE_DASHBOARD_FEEDS_UPDATE = "dashboard.feeds.update";

	public static final String RESOURCE_DASHBOARD_LOGO = "dashboard.logo";

	public static final String RESOURCE_DASHBOARD_SUBSCRIBE = "dashboard.subscribe";

	public static final String RESOURCE_DASHBOARD_NEW_AND_NOTEWORTHY = "dashboard.new.and.noteworthy";

	public static final String RESOURCE_DASHBOARD_PRODUCT_PAGE = "dashboard.product.page";

	public static final String RESOURCE_DASHBOARD_SEARCH = "dashboard.search";

	public static final String RESOURCE_DASHBOARD_SUPPORT_COMMERCIAL = "dashboard.support.commercial";

	public static final String RESOURCE_DASHBOARD_SUPPORT_COMMUNITY = "dashboard.support.community";

	private static final String LOGO_SPRINGSOURCE = "prod/spring_logo_transparent.png";

	private static final String SUBSCRIBE_SPRINGSOURCE = "prod/newsletter_subscription.gif";

	public static final String PAGE_ID = "com.springsource.sts.ide.ui.dashboard.page.overview";

	private static final Pattern PATTERN = Pattern.compile("<img.*href=\"(.*?)\"/>");

	private final MultiPageDashboardEditor dashboardEditor;

	private FormToolkit toolkit;

	private ScrolledForm form;

	private Text searchBox;

	private IPreferenceStore prefStore;

	private Color feedColor;

	private Section updateSection;

	private Section helpSection;

	private static final String PROXY_PREF_PAGE_ID = "org.eclipse.ui.net.NetPreferences";

	private static final String ELEMENT_CLASS = "class";

	private static final String ELEMENT_ICON = "icon";

	private static final String ELEMENT_NAME = "name";

	private static final String EXTENSION_ID_NEW_WIZARD = "org.eclipse.ui.newWizards";

	private static final String GRAILS_WIZARD_ID = "org.grails.ide.eclipse.ui.wizard.newGrailsProjectWizard";

	private static final String ROO_WIZARD_ID = "com.springsource.sts.roo.ui.wizard.newRooProjectWizard";

	private static final String GROOVY_WIZARD_ID = "org.codehaus.groovy.eclipse.ui.groovyProjectWizard";

	private static final String SPRING_WIZARD_ID = "com.springsource.sts.wizard.template";

	private static final String JAVA_WIZARD_ID = "org.eclipse.jdt.ui.wizards.JavaProjectWizard";

	private static final int UPDATE_INDENTATION = 22;

	private static final int UPDATE_TEXT_WRAP_INDENT = 65;

	private static final int FEEDS_TEXT_WRAP_INDENT = 80;

	private final List<SyndEntry> displayedEntries;

	private Composite feedsComposite;

	private ScrolledComposite feedsScrolled;

	private static final String ICON_BLOG_INCOMING = "rss/overlay-incoming.png";

	private static final String ICON_BLOG_BLANK = "rss/blank.png";

	private static final int FEEDS_DESCRIPTION_MAX = 200;

	protected static final String URL_CONFIGURATION_ID = "com.springsource.sts.ide.ui.preferencePage";

	// TODO e3.5 replace by SWT.UNDERLINE_LINK
	public static final int SWT_UNDERLINE_LINK = 4;

	private Set<Control> feedControls;

	private Action refreshFeedsAction;

	private Action refreshUpdatesAction;

	private ScrolledComposite updateScrolled;

	private Set<AggregateFeedJob> unfinishedJobs;

	private List<AbstractDashboardPart> parts;

	public DashboardMainPage(MultiPageDashboardEditor editor) {
		super(editor, PAGE_ID, "Dashboard");
		dashboardEditor = editor;
		displayedEntries = new ArrayList<SyndEntry>();
	}

	public void cancelUnfinishedJobs() {
		for (AggregateFeedJob job : unfinishedJobs) {
			job.cancel();
		}
		unfinishedJobs.clear();
	}

	@Override
	public void dispose() {
		ResourceProvider.getInstance().removePropertyChangeListener(this);
		if (feedColor != null) {
			feedColor.dispose();
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();

		if (propertyName.equals(RESOURCE_DASHBOARD_FEEDS_BLOGS)) {
			refreshFeedsAction.run();
		}
		else if (propertyName.equals(RESOURCE_DASHBOARD_FEEDS_UPDATE)) {
			refreshUpdatesAction.run();
		}
	}

	private void addImages(FormText text, String description) {
		if (description.contains("<img")) {
			Matcher matcher = PATTERN.matcher(description);
			while (matcher.find()) {
				String url = matcher.group(1);
				text.setImage(url, IdeUiPlugin.getImage(url));
			}
		}
	}

	private void adjustCollapsableSections() {
		GridDataFactory.fillDefaults().grab(true, updateSection.isExpanded()).applyTo(updateSection);
		for (AbstractDashboardPart part : parts) {
			if (part.getControl() instanceof Section) {
				Section section = (Section) part.getControl();
				GridDataFactory.fillDefaults().grab(true, section.isExpanded()).applyTo(section);
			}
		}
		GridDataFactory
				.fillDefaults()
				.grab(true,
						!updateSection.isExpanded() && areAllContributedSectionsCollapsed() && helpSection.isExpanded())
				.applyTo(helpSection);

		form.getBody().layout(true, true);
		form.reflow(true);
	}

	private boolean areAllContributedSectionsCollapsed() {
		for (AbstractDashboardPart part : parts) {
			if (part.getControl() instanceof Section) {
				if (((Section) part.getControl()).isExpanded()) {
					return false;
				}
			}
		}
		return true;
	}

	private void clearText() {
		if (searchBox != null && !searchBox.isDisposed()) {
			searchBox.setText("");
		}
	}

	private void createFeedsSection(Composite parent, final String title, FeedType feedType, String feedName) {
		final Section section = new Section(parent, ExpandableComposite.TITLE_BAR) {
			@Override
			public void redraw() {
				GridData compositeData = (GridData) feedsScrolled.getLayoutData();
				compositeData.widthHint = getSize().x - 35;
				compositeData.heightHint = getSize().y - 40;
				compositeData.grabExcessHorizontalSpace = false;
				compositeData.grabExcessVerticalSpace = false;

				super.redraw();
			}
		};

		form.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				GridData data = (GridData) section.getLayoutData();
				data.heightHint = form.getSize().y - FEEDS_TEXT_WRAP_INDENT;
			}
		});

		toolkit.adapt(section);
		section.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TB_TOGGLE));
		section.setTitleBarBackground(toolkit.getColors().getColor(IFormColors.TB_BG));
		section.setTitleBarBorderColor(toolkit.getColors().getColor(IFormColors.TB_BORDER));
		section.setFont(FormFonts.getInstance().getBoldFont(getSite().getShell().getDisplay(), section.getFont()));

		section.setText(title);
		section.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).hint(300, 300).applyTo(section);

		final Composite headerComposite = toolkit.createComposite(section, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		headerComposite.setLayout(rowLayout);
		headerComposite.setBackground(null);

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.createControl(headerComposite);

		final PageBook pagebook = new PageBook(section, SWT.NONE);
		toolkit.adapt(pagebook);
		pagebook.setLayoutData(new GridLayout());

		final Composite disclaimer = createDisclaimer(pagebook);

		feedsScrolled = new ScrolledComposite(pagebook, SWT.V_SCROLL);

		feedsScrolled.setExpandVertical(false);
		feedsScrolled.setLayout(new GridLayout());
		feedsScrolled.setAlwaysShowScrollBars(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(feedsScrolled);
		toolkit.adapt(feedsScrolled);

		feedsComposite = toolkit.createComposite(feedsScrolled);
		feedsComposite.setLayout(new TableWrapLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(feedsComposite);
		feedsScrolled.setContent(feedsComposite);

		feedsComposite.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				GridData data = (GridData) feedsComposite.getLayoutData();
				data.widthHint = section.getSize().x - FEEDS_TEXT_WRAP_INDENT;
				data.heightHint = form.getSize().y - 50;
			}
		});
		section.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				GridData data = (GridData) feedsScrolled.getLayoutData();
				data.heightHint = form.getSize().y - 50;
				data.grabExcessVerticalSpace = false;
				feedsScrolled.setSize(section.getSize().x - 40, form.getSize().y - 50);

				for (Control feedControl : feedControls) {
					if (!feedControl.isDisposed()) {
						((TableWrapData) feedControl.getLayoutData()).maxWidth = section.getSize().x
								- FEEDS_TEXT_WRAP_INDENT;
						// Point size = feedControl.computeSize(data.widthHint,
						// 400);
						// feedControl.setSize(size);
						// feedControl.pack(true);
					}
				}

				feedsComposite.pack();
			};
		});

		feedControls = new HashSet<Control>();

		final Map<String, String> springMap = new HashMap<String, String>();
		String[] urls = ResourceProvider.getUrls(RESOURCE_DASHBOARD_FEEDS_BLOGS);
		for (String url : urls) {
			springMap.put(url, null);
		}

		getEditorSite().getShell().getDisplay().asyncExec(new Runnable() {

			public void run() {
				FeedsReader reader = new FeedsReader();
				CachedFeedsManager manager = new CachedFeedsManager(title, springMap, reader);
				try {
					manager.readCachedFeeds(null);
					Set<SyndEntry> entries = new HashSet<SyndEntry>();
					for (SyndFeed entry : reader.getFeeds()) {
						entries.addAll(entry.getEntries());
					}

					if (!getManagedForm().getForm().isDisposed()) {
						displayFeeds(entries, feedsComposite, feedsScrolled, pagebook, disclaimer, section);
					}
				}
				catch (IllegalArgumentException e) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
							"An unexpected error occurred while retrieving feed content from cache.", e));
				}
				catch (FeedException e) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
							"An unexpected error occurred while retrieving feed content from cache.", e));
				}
			}
		});

		refreshFeedsAction = new Action("Refresh Feeds", CommonImages.REFRESH) {
			@Override
			public void run() {
				Map<String, String> springMap = getFeedsMap();

				displayFeeds(feedsComposite, feedsScrolled, pagebook, disclaimer, springMap, title, section);
			}

		};

		Action configureURLsAction = new Action("Configure URLs", StsUiImages.RSS_CONFIGURE) {
			@Override
			public void run() {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getSite().getShell(),
						URL_CONFIGURATION_ID, new String[] { URL_CONFIGURATION_ID }, null);
				dialog.open();
			}
		};

		section.setClient(pagebook);
		section.setTextClient(headerComposite);

		toolBarManager.add(configureURLsAction);
		toolBarManager.add(refreshFeedsAction);
		toolBarManager.update(true);

		refreshFeedsAction.run();
	}

	private Composite createDisclaimer(Composite parent) {
		FormText disclaimer = toolkit.createFormText(parent, true);
		disclaimer.setForeground(feedColor);
		disclaimer
				.setText(
						"<form><p>No entries found. Ensure <a href=\"proxy\">firewall and proxy settings</a> are appropriately configured.</p></form>",
						true, false);
		disclaimer.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if ("proxy".equals(e.data)) {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, PROXY_PREF_PAGE_ID, null,
							null);
					dialog.open();
				}
			}

		});
		return disclaimer;
	}

	private void createHeader() {
		IManagedForm headerForm = dashboardEditor.getHeaderForm();
		Form topForm = headerForm.getForm().getForm();
		final IToolBarManager toolBarManager = topForm.getToolBarManager();

		toolBarManager.removeAll();
		toolBarManager.update(true);

		toolBarManager.add(new ControlContribution("springSource_search") {
			@Override
			protected Control createControl(Composite parent) {
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setBackground(null);

				GridLayout layout = new GridLayout(2, false);
				layout.marginRight = 0;
				layout.marginHeight = 0;
				layout.marginTop = 5;
				layout.verticalSpacing = 1;
				composite.setLayout(layout);

				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).grab(false, true).applyTo(composite);

				createSearchBox(composite);

				return composite;
			}
		});

		toolBarManager.add(new ControlContribution("subscribe") {
			@Override
			protected Control createControl(Composite parent) {
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setBackground(null);

				GridLayout layout = new GridLayout(2, false);
				layout.marginRight = 2;
				layout.marginLeft = 0;
				layout.marginHeight = 0;
				layout.marginTop = 12;
				layout.verticalSpacing = 1;
				composite.setLayout(layout);

				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).grab(false, true).applyTo(composite);

				Button subscribeButton = new Button(composite, SWT.PUSH);
				subscribeButton.setText("Subscribe");
				subscribeButton.setImage(IdeUiPlugin.getImageDescriptor(SUBSCRIBE_SPRINGSOURCE).createImage());
				subscribeButton.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

				subscribeButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						TasksUiUtil.openUrl(ResourceProvider.getUrl(RESOURCE_DASHBOARD_SUBSCRIBE));
					}

					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

				return composite;
			}
		});

		toolBarManager.add(new Action("Spring", IdeUiPlugin.getImageDescriptor(LOGO_SPRINGSOURCE)) {
			@Override
			public void run() {
				TasksUiUtil.openUrl(ResourceProvider.getUrl(RESOURCE_DASHBOARD_LOGO));
			}
		});

		toolBarManager.update(true);
	}

	private void createHelpSection(Composite parent) {
		helpSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		helpSection.setText("Help and Documentation");
		helpSection.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(helpSection);

		Composite composite = toolkit.createComposite(helpSection);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Hyperlink link = toolkit.createHyperlink(composite, "Community Support Forums", SWT_UNDERLINE_LINK);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				TasksUiUtil.openUrl(ResourceProvider.getUrl(RESOURCE_DASHBOARD_SUPPORT_COMMUNITY));
			}
		});

		link = toolkit.createHyperlink(composite, "New and Noteworthy", SWT_UNDERLINE_LINK);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				TasksUiUtil.openUrl(ResourceProvider.getUrl(RESOURCE_DASHBOARD_NEW_AND_NOTEWORTHY));
			}
		});

		link = toolkit.createHyperlink(composite, "Issue and Bug Tracker", SWT_UNDERLINE_LINK);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				TasksUiUtil.openUrl(ResourceProvider.getUrl(RESOURCE_DASHBOARD_BUG_TRACKER));
			}
		});

		link = toolkit.createHyperlink(composite, "Extensions", SWT_UNDERLINE_LINK);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				try {
					IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
					FormEditor editor = (FormEditor) page.openEditor(DashboardEditorInput.INSTANCE,
							MultiPageDashboardEditor.EDITOR_ID);
					editor.setActivePage(DashboardExtensionsPage.ID);
				}
				catch (PartInitException ex) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not open dashboard", ex));
				}
			}
		});

		link = toolkit.createHyperlink(composite, "SpringSource Commercial Support", SWT_UNDERLINE_LINK);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				TasksUiUtil.openUrl(ResourceProvider.getUrl(RESOURCE_DASHBOARD_SUPPORT_COMMERCIAL));
			}
		});

		link = toolkit.createHyperlink(composite, "Product Page", SWT_UNDERLINE_LINK);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				TasksUiUtil.openUrl(ResourceProvider.getUrl(RESOURCE_DASHBOARD_PRODUCT_PAGE));
			}
		});

		helpSection.setClient(composite);

		setUpExpandableSection(helpSection, IIdeUiConstants.PREF_HELP_SECTION_COLLAPSE, true);
	}

	private void createNewProjectFromExtension(final Composite container, final IConfigurationElement element) {
		if (element == null) {
			return;
		}

		try {
			Object object = WorkbenchPlugin.createExtension(element, ELEMENT_CLASS);
			if (!(object instanceof INewWizard)) {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not load "
						+ object.getClass().getCanonicalName() + " must implement "
						+ INewWizard.class.getCanonicalName()));
				return;
			}

			String title = element.getAttribute(ELEMENT_NAME);

			String pathName = element.getAttribute(ELEMENT_ICON);
			String plugin = element.getContributor().getName();
			Bundle bundle = Platform.getBundle(plugin);
			URL iconLocation = bundle.getResource(pathName);

			Image image = null;
			// TODO: fix this hack...need to read wildcard entries
			if (element.getAttribute("id").equals(JAVA_WIZARD_ID)) {
				image = StsUiImages.getImage(StsUiImages.NEW_JAVA_PROJECT);
			}
			else {
				if (iconLocation != null) {
					image = StsUiImages.getImage(ImageDescriptor.createFromURL(iconLocation));
				}
			}

			createNewProjectLink(container, title, image, new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					Object object;
					try {
						object = WorkbenchPlugin.createExtension(element, ELEMENT_CLASS);
					}
					catch (CoreException ex) {
						StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
								"Could not read dashboard extension", ex));
						return;
					}
					if (!(object instanceof INewWizard)) {
						StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not load "
								+ object.getClass().getCanonicalName() + " must implement "
								+ INewWizard.class.getCanonicalName()));
						return;
					}

					INewWizard wizard = (INewWizard) object;
					wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
					WizardDialog dialog = new WizardDialog(container.getShell(), wizard);
					dialog.open();
				}
			});
		}
		catch (CoreException e) {
			StatusHandler
					.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not read dashboard extension", e));
		}

	}

	private void createNewProjectLink(Composite parent, String name, Image image, IHyperlinkListener listener) {
		ImageHyperlink link = toolkit.createImageHyperlink(parent, SWT.NONE);
		link.setText(name);
		link.setImage(image);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(link);

		if (listener != null) {
			link.addHyperlinkListener(listener);
		}
	}

	private void createNewProjectsSection(Composite parent) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText("Create");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(section);
		section.setLayout(new GridLayout());

		final Composite headerComposite = toolkit.createComposite(section, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		headerComposite.setLayout(rowLayout);
		headerComposite.setBackground(null);

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.createControl(headerComposite);
		section.setTextClient(headerComposite);

		toolBarManager.add(new NewWizardAction(getSite().getWorkbenchWindow()));
		toolBarManager.update(true);

		Composite container = toolkit.createComposite(section);
		container.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(container);

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_NEW_WIZARD);
		IExtension[] extensions = extensionPoint.getExtensions();

		IConfigurationElement[] foundElements = new IConfigurationElement[6];
		String[] ids = new String[] { JAVA_WIZARD_ID, SPRING_WIZARD_ID, ROO_WIZARD_ID,
				GROOVY_WIZARD_ID, GRAILS_WIZARD_ID };

		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				String id = element.getAttribute("id");
				for (int i = 0; i < ids.length; i++) {
					if (ids[i].equals(id) && element.getAttribute(ELEMENT_CLASS) != null
							&& element.getAttribute(ELEMENT_NAME) != null && element.getAttribute(ELEMENT_ICON) != null) {
						foundElements[i] = element;
					}
				}
			}
		}

		for (IConfigurationElement element : foundElements) {
			createNewProjectFromExtension(container, element);
		}

		section.setClient(container);
	}

	private void createSearchBox(Composite composite) {
		TextSearchControl searchControl = new TextSearchControl(composite, false, null);
		searchControl.getTextControl().setMessage("Search spring.io");
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.FILL, SWT.CENTER).hint(200, SWT.DEFAULT)
				.applyTo(searchControl);
		searchBox = searchControl.getTextControl();

		searchBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.ESC && e.doit) {
					clearText();
				}
			}
		});

		searchControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == TextSearchControl.ICON_CANCEL) {
					clearText();
				}
				else {
					searchSpringSource();
				}
			}
		});
	}

	private void createUpdateSection(final Composite parent) {
		updateSection = new Section(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE) {
			@Override
			public void redraw() {
				if (getStoredExpandedState(IIdeUiConstants.PREF_UPDATE_SECTION_COLLAPSE, true)) {
					GridData compositeData = (GridData) updateScrolled.getLayoutData();
					compositeData.widthHint = getSize().x - 55;
					compositeData.heightHint = getSize().y - 40;
					compositeData.grabExcessHorizontalSpace = false;
					compositeData.grabExcessVerticalSpace = false;
				}

				super.redraw();
			}
		};
		// updateSection = toolkit.createSection(parent,
		// ExpandableComposite.TITLE_BAR);
		toolkit.adapt(updateSection);
		updateSection.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TB_TOGGLE));
		updateSection.setTitleBarBackground(toolkit.getColors().getColor(IFormColors.TB_BG));
		updateSection.setTitleBarBorderColor(toolkit.getColors().getColor(IFormColors.TB_BORDER));
		updateSection.setFont(FormFonts.getInstance().getBoldFont(getSite().getShell().getDisplay(),
				updateSection.getFont()));

		updateSection.setText("Updates");
		updateSection.setLayout(new GridLayout());
		GridDataFactory.fillDefaults()
				.grab(true, getStoredExpandedState(IIdeUiConstants.PREF_UPDATE_SECTION_COLLAPSE, true))
				.applyTo(updateSection);

		setUpExpandableSection(updateSection, IIdeUiConstants.PREF_UPDATE_SECTION_COLLAPSE, true);

		final Composite headerComposite = toolkit.createComposite(updateSection, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		headerComposite.setLayout(rowLayout);
		headerComposite.setBackground(null);

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.createControl(headerComposite);

		updateSection.setTextClient(headerComposite);

		final PageBook pagebook = new PageBook(updateSection, SWT.NONE);
		toolkit.adapt(pagebook);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(pagebook);

		final Composite disclaimer = createDisclaimer(pagebook);

		updateScrolled = new ScrolledComposite(pagebook, SWT.V_SCROLL);
		updateScrolled.setExpandVertical(false);
		updateScrolled.setLayout(new GridLayout());
		updateScrolled.setAlwaysShowScrollBars(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(updateScrolled);
		toolkit.adapt(updateScrolled);

		final Composite composite = toolkit.createComposite(updateScrolled);
		updateScrolled.setContent(composite);
		composite.setLayout(new TableWrapLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		displayUpdates(composite, pagebook, disclaimer);

		refreshUpdatesAction = new Action("Refresh Updates", CommonImages.REFRESH) {
			@Override
			public void run() {
				displayUpdates(composite, pagebook, disclaimer);
			}
		};
		toolBarManager.add(refreshUpdatesAction);
		toolBarManager.update(true);

		pagebook.showPage(composite);

		updateSection.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				if (updateSection.isExpanded()) {
					GridData data = (GridData) updateScrolled.getLayoutData();
					data.grabExcessVerticalSpace = false;
					updateScrolled.setSize(updateSection.getSize().x - 14, updateSection.getSize().y - 33);

					for (Control child : composite.getChildren()) {
						((TableWrapData) child.getLayoutData()).maxWidth = updateSection.getSize().x
								- UPDATE_TEXT_WRAP_INDENT;
					}

					composite.pack();
				}
			}
		});

		updateSection.setClient(pagebook);
	}

	private void displayFeed(SyndEntry entry, Composite composite, final Section section, final int pos,
			Control[] children) {
		ImageHyperlink link;
		FormText text;

		if (pos < children.length / 2) {
			link = (ImageHyperlink) children[pos * 2];
			link.setVisible(true);

			text = (FormText) children[pos * 2 + 1];
			text.setVisible(true);
		}
		else {
			final ImageHyperlink newLink = toolkit.createImageHyperlink(composite, SWT.NONE);
			feedControls.add(newLink);
			link = newLink;
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					Object source = e.getSource();
					if (source instanceof ImageHyperlink && ((ImageHyperlink) source).getData() != null) {
						SyndEntry entry = (SyndEntry) ((ImageHyperlink) source).getData();
						String url = entry.getLink();

						if (url == null) {
							return;
						}
						int urlPos = url.indexOf("?");
						String newUrl = url;
						if (urlPos > 0) {
							newUrl = url.substring(0, urlPos + 1) + url.substring(urlPos + 1).replaceAll("\\?", "&");
						}

						TasksUiUtil.openUrl(url);
						prefStore.setValue(IIdeUiConstants.PREF_FEED_ENTRY_READ_STATE + ":" + newUrl, true);
						IdeUiPlugin.getDefault().savePluginPreferences();
						newLink.setImage(IdeUiPlugin.getImage(ICON_BLOG_BLANK));
					}
				}
			});

			// text = new Text(composite, SWT.WRAP | SWT.MULTI |
			// SWT.NO_BACKGROUND);
			text = new FormText(composite, SWT.WRAP | SWT.MULTI | SWT.NO_BACKGROUND | SWT.NO_FOCUS);
			text.setHyperlinkSettings(toolkit.getHyperlinkGroup());
			feedControls.add(text);
			final TableWrapData data = new TableWrapData();
			data.indent = UPDATE_INDENTATION;
			data.maxWidth = section.getSize().x - FEEDS_TEXT_WRAP_INDENT;
			data.grabVertical = true;
			text.setLayoutData(data);
			text.addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					data.maxWidth = section.getSize().x - FEEDS_TEXT_WRAP_INDENT;
				}
			});

			text.setForeground(feedColor);
			text.setBackground(toolkit.getColors().getBackground());
		}

		String title = entry.getTitle();

		Date entryDate = new Date(0);
		if (entry.getUpdatedDate() != null) {
			entryDate = entry.getUpdatedDate();
		}
		else {
			entryDate = entry.getPublishedDate();
		}

		String dateString = "";
		if (entryDate != null) {
			dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(entryDate);
		}

		String entryAuthor = "";
		if (entry.getAuthor() != null && entry.getAuthor().trim() != "") {
			entryAuthor = " by " + entry.getAuthor();
		}

		if (dateString.length() > 0 && entryAuthor.length() > 0) {
			link.setText(removeHtmlEntities(title));
		}

		TableWrapData linkData = new TableWrapData();
		if (!prefStore.getBoolean(IIdeUiConstants.PREF_FEED_ENTRY_READ_STATE + ":" + entry.getLink())) {
			link.setImage(IdeUiPlugin.getImage(ICON_BLOG_INCOMING));
			linkData.indent = 0;
		}
		else {
			// link.setImage(IdeUiPlugin.getImage(ICON_BLOG_BLANK));
			linkData.indent = UPDATE_INDENTATION - 1;
		}
		link.setLayoutData(linkData);
		link.setData(entry);

		String description = trimText(getDescription(entry));
		text.setText(description + " (" + dateString + entryAuthor + ")", false, false);
	}

	private void displayFeeds(final Composite composite, final ScrolledComposite scrolled, final PageBook pagebook,
			final Control disclaimer, Map<String, String> map, String feedName, final Section section) {
		final AggregateFeedJob job = new AggregateFeedJob(map, feedName);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				unfinishedJobs.remove(job);
				IWorkbenchPartSite site = getSite();
				if (site != null && site.getShell() != null && !site.getShell().isDisposed()
						&& site.getShell().getDisplay() != null && !site.getShell().getDisplay().isDisposed()) {
					site.getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							Map<SyndEntry, SyndFeed> entryToFeed = job.getFeedReader().getFeedsWithEntries();
							Set<SyndEntry> entries = entryToFeed.keySet();

							if (!getManagedForm().getForm().isDisposed()) {
								displayFeeds(entries, composite, scrolled, pagebook, disclaimer, section);
							}
						}
					});
				}
			}
		});
		unfinishedJobs.add(job);
		job.schedule();
	}

	private void displayFeeds(Set<SyndEntry> entries, Composite composite, ScrolledComposite scrolled,
			PageBook pagebook, Control disclaimer, Section section) {

		// make sure the entries are sorted correctly
		List<SyndEntry> sortedEntries = new ArrayList<SyndEntry>(entries);
		Collections.sort(sortedEntries, new Comparator<SyndEntry>() {
			public int compare(SyndEntry o1, SyndEntry o2) {
				Date o1Date = o1.getPublishedDate() != null ? o1.getPublishedDate() : o1.getUpdatedDate();
				Date o2Date = o2.getPublishedDate() != null ? o2.getPublishedDate() : o2.getUpdatedDate();
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

		if (sortedEntries.isEmpty()) {
			pagebook.showPage(disclaimer);
			return;
		}

		if (displayedEntries.containsAll(entries) && entries.containsAll(displayedEntries)) {
			return;
		}
		else {
			displayedEntries.clear();
			displayedEntries.addAll(entries);
		}

		Control[] children = composite.getChildren();

		int counter = 0;
		for (SyndEntry entry : sortedEntries) {
			displayFeed(entry, composite, section, counter, children);
			counter++;
		}

		for (int i = counter * 2; i < children.length; i++) {
			children[i].dispose();
		}

		pagebook.showPage(scrolled);
		composite.pack(true);

	}

	private void displayUpdate(final SyndEntry entry, String severity, Composite composite, int pos, Control[] children) {
		ImageHyperlink link;
		FormText text;

		if (pos < children.length / 2) {
			link = (ImageHyperlink) children[pos * 2];
			link.setVisible(true);
			text = (FormText) children[pos * 2 + 1];
			text.setVisible(true);
		}
		else {
			link = toolkit.createImageHyperlink(composite, SWT.NONE);
			text = new FormText(composite, SWT.WRAP | SWT.MULTI | SWT.NO_BACKGROUND | SWT.NO_FOCUS);
			text.setHyperlinkSettings(toolkit.getHyperlinkGroup());
			final TableWrapData data = new TableWrapData();
			data.indent = UPDATE_INDENTATION;
			data.maxWidth = updateSection.getSize().x - UPDATE_TEXT_WRAP_INDENT;
			text.setLayoutData(data);
			text.addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					data.maxWidth = updateSection.getSize().x - UPDATE_TEXT_WRAP_INDENT;
				}
			});

			text.setBackground(toolkit.getColors().getBackground());
			text.addHyperlinkListener(new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					if (e.data instanceof String) {
						TasksUiUtil.openUrl((String) e.data);
					}
				}
			});
		}

		link.setText(entry.getTitle());

		TableWrapData linkData = new TableWrapData();
		if ("important".equals(severity)) {
			link.setImage(StsUiImages.getImage(StsUiImages.IMPORTANT));
			linkData.indent = 0;
		}
		else if ("warning".equals(severity)) {
			link.setImage(StsUiImages.getImage(StsUiImages.WARNING));
			linkData.indent = 0;
		}
		else {
			// link.setImage(IdeUiPlugin.getImage(ICON_BLOG_BLANK));
			linkData.indent = UPDATE_INDENTATION - 1;
		}
		link.setLayoutData(linkData);

		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				String url = entry.getLink();

				if (url == null) {
					return;
				}
				TasksUiUtil.openUrl(url);
			}
		});

		String description = getDescription(entry);
		if (entry.getPublishedDate() != null && description.endsWith("</p></form>")) {
			String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(entry.getPublishedDate());
			description = description.replace("</p></form>", " (" + dateString + ")</p></form>");
		}
		else if (entry.getPublishedDate() != null) {
			String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(entry.getPublishedDate());
			description = description + " (" + dateString + ")";
		}

		text.setText(description, description.startsWith("<form>"), true);
		text.setForeground(feedColor);
		text.setBackground(toolkit.getColors().getBackground());
		addImages(text, description);
	}

	private void displayUpdates(final Composite composite, final PageBook pagebook, final Composite disclaimer) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ResourceProvider.getUrl(RESOURCE_DASHBOARD_FEEDS_UPDATE), null);

		final AggregateFeedJob job = new AggregateFeedJob(map, "Updates");
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				unfinishedJobs.remove(job);

				Display display = PlatformUI.getWorkbench().getDisplay();
				if (display != null && !display.isDisposed()) {
					display.asyncExec(new Runnable() {
						public void run() {
							if (getManagedForm().getForm().isDisposed()) {
								return;
							}

							List<UpdateNotification> notifications = job.getNotifications();
							if (notifications.isEmpty()) {
								pagebook.showPage(disclaimer);
								return;
							}

							// make sure the entries are sorted correctly
							Collections.sort(notifications, new Comparator<UpdateNotification>() {
								public int compare(UpdateNotification o1, UpdateNotification o2) {
									return getDate(o2).compareTo(getDate(o1));
								}

								/**
								 * Make sure 'date' is never null so we have something to pass to 
								 * 'compare'.
								 * <p>
								 * See https://issuetracker.springsource.com/browse/STS-3844
								 */
								private Date getDate(UpdateNotification o) {
									if (o!=null) {
										SyndEntry e = o.getEntry();
										if (e!=null) {
											Date d = e.getPublishedDate();
											if (d!=null) {
												return d;
											}
										}
									}
									//Treat anything that has no date as very very old.
									return new Date(0);
								}
							});

							int counter = 0;
							Control[] children = composite.getChildren();
							for (UpdateNotification notification : notifications) {
								displayUpdate(notification.getEntry(), notification.getSeverity(), composite, counter,
										children);
								counter++;
							}

							for (int i = counter * 2; i < children.length; i++) {
								children[i].dispose();
							}

							composite.changed(composite.getChildren());
							composite.pack(true);
							composite.redraw();
							composite.getParent().redraw();
						}
					});
				}
			}
		});
		unfinishedJobs.add(job);
		job.schedule();
	}

	private int findEndOfWord(StringBuilder sb, int pos) {
		Pattern pattern = Pattern.compile("\\w");
		while (pos < sb.length()) {
			if (pattern.matcher(sb.subSequence(pos, pos + 1)).matches()) {
				pos++;
			}
			else {
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

	private boolean getStoredExpandedState(String prefId, boolean defaultExpanded) {
		int storedState = prefStore.getInt(prefId);
		if (storedState == IIdeUiConstants.SECTION_EXPANDED) {
			return true;
		}
		else if (storedState == IIdeUiConstants.SECTION_COLLAPSED) {
			return false;
		}
		else {
			return defaultExpanded;
		}

	}

	private String removeHtmlEntities(String value) {
		StringBuilder result = new StringBuilder();
		boolean tagOpened = false;
		for (char currChar : value.toCharArray()) {
			if (currChar == '<') {
				tagOpened = true;
			}
			else if (currChar == '>') {
				tagOpened = false;
			}
			else {
				if (!tagOpened) {
					result.append(currChar);
				}
			}
		}
		return StringEscapeUtils.unescapeHtml(result.toString());

	}

	private void searchSpringSource() {
		String searchTerms = searchBox.getText();
		String url = ResourceProvider.getUrl(RESOURCE_DASHBOARD_SEARCH) + searchTerms.replaceAll(" ", "+");
		TasksUiUtil.openUrl(url);
	}

	private void setUpExpandableSection(final Section section, final String prefId, boolean defaultExpanded) {
		section.setExpanded(getStoredExpandedState(prefId, defaultExpanded));

		section.addExpansionListener(new ExpansionAdapter() {

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if (e.getState()) {
					prefStore.setValue(prefId, IIdeUiConstants.SECTION_EXPANDED);
				}
				else {
					prefStore.setValue(prefId, IIdeUiConstants.SECTION_COLLAPSED);
				}

				if (isContributedSection(section)) {
					// If we're expanding a contributed section, close the
					// update section...
					updateSection.setExpanded(false);
					prefStore.setValue(IIdeUiConstants.PREF_UPDATE_SECTION_COLLAPSE, IIdeUiConstants.SECTION_COLLAPSED);

				}

				// ... close all contributed section that are not the section of
				// interest
				for (AbstractDashboardPart part : parts) {
					if (!section.equals(part.getControl()) && part.getControl() instanceof Section) {
						((Section) part.getControl()).setExpanded(false);
						prefStore.setValue(getExpansionPropertyId(part), IIdeUiConstants.SECTION_COLLAPSED);
					}
				}

				adjustCollapsableSections();
			}
		});
	}

	private boolean isContributedSection(Section section) {
		for (AbstractDashboardPart part : parts) {
			if (section.equals(part.getControl())) {
				return true;
			}
		}
		return false;
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

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		unfinishedJobs = new CopyOnWriteArraySet<AggregateFeedJob>();

		// get dark gray color as FormText display it lighter as other widgets
		feedColor = new Color(Display.getDefault(), 70, 70, 70);

		// getHeaderForm().setText(null);// "SpringSource Tool Suite");
		toolkit.decorateFormHeading(form.getForm());

		prefStore = IdeUiPlugin.getDefault().getPreferenceStore();

		GridLayout compositeLayout = new GridLayout(2, true);
		compositeLayout.marginHeight = 0;
		compositeLayout.marginTop = 5;
		compositeLayout.verticalSpacing = 0;

		Composite body = form.getBody();
		body.setLayout(compositeLayout);

		Composite leftComposite = toolkit.createComposite(body);
		leftComposite.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(leftComposite);

		Composite rightComposite = toolkit.createComposite(body);
		rightComposite.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(rightComposite);

		createHeader();

		createNewProjectsSection(leftComposite);
		createUpdateSection(leftComposite);

		parts = contributeParts(leftComposite, AbstractDashboardPart.ID_PATH_DOC);
		for (AbstractDashboardPart part : parts) {
			if (part.getControl() instanceof Section) {
				String expansionProp = getExpansionPropertyId(part);
				Section section = (Section) part.getControl();
				GridDataFactory.fillDefaults().grab(false, getStoredExpandedState(expansionProp, false))
						.applyTo(section);
				setUpExpandableSection(section, expansionProp, false);
			}
		}

		createHelpSection(leftComposite);

		createFeedsSection(rightComposite, "Feeds", FeedType.BLOG, "blog");

		searchBox.setFocus();

		ResourceProvider.getInstance().addPropertyChangeListener(this);
	}

	private String getExpansionPropertyId(AbstractDashboardPart part) {
		return part.getId() + ".expansion";
	}

	protected ScrolledForm getHeaderForm() {
		if (dashboardEditor == null || dashboardEditor.getHeaderForm() == null) {
			return null;
		}
		return dashboardEditor.getHeaderForm().getForm();
	}

	public static Map<String, String> getFeedsMap() {
		Map<String, String> springMap = new HashMap<String, String>();
		String[] urls = ResourceProvider.getUrls(RESOURCE_DASHBOARD_FEEDS_BLOGS);
		for (String url : urls) {
			if (url != null && url.length() > 0) {
				springMap.put(url, null);
			}
		}
		return springMap;
	}

	public enum FeedType {
		BLOG, DOWNLOAD, BUZZ, RESEARCH
	}

}
