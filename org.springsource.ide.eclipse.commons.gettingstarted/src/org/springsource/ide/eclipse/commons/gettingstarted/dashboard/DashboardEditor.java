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
package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.EditorPart;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.dashboard.internal.ui.editors.DashboardReopener;
import org.springsource.ide.eclipse.dashboard.ui.actions.IDashboardWithPages;

/**
 * New Dashboard that will replace the old. On some yet to be determined
 * activation date.
 * 
 * @author Kris De Volder
 */
public class DashboardEditor extends EditorPart implements IDashboardWithPages {

	private CTabFolder folder;

	/**
	 * Assigns custom names to some urls that can be displayed in the dashboard.
	 * This is because the web page titles aren't allways good enough (too long,
	 * not clear in context etc.).
	 */
	private Map<String, String> customNames = null;

	public DashboardEditor() {
		DashboardReopener.ensure();
	}

	public void createPartControl(Composite _parent) {
		folder = new CTabFolder(_parent, SWT.BOTTOM | SWT.FLAT);
		CTabItem defaultSelection = null;
		for (final IDashboardPage page : createPages()) {
			if (shouldAdd(page)) {
				CTabItem pageWidget = createPageWidget(page);
				if (defaultSelection == null) {
					defaultSelection = pageWidget; // select the first
				}
			}
		}
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ensureSelectedTabInitialized();
			}
		});
		folder.setSelection(defaultSelection);
		ensureSelectedTabInitialized();
	}

	private boolean shouldAdd(IDashboardPage page) {
		if (page instanceof IEnablableDashboardPart) {
			return ((IEnablableDashboardPart) page).shouldAdd();
		}
		return true;
	}

	/**
	 * Creates a CTabItem and adds it to the dashboard. The contents of the page
	 * is provide by an IDashboardPage
	 */
	private CTabItem createPageWidget(final IDashboardPage _page) {
		final DashboardPageContainer page = new DashboardPageContainer(_page);
		int style = page.canClose() ? SWT.CLOSE : SWT.NONE;
		CTabItem pageWidget = new CTabItem(folder, style);
		pageWidget.setData(page);
		String name = page.getName();
		if (name == null) {
			name = "no name";
		}
		pageWidget.setText(name);
		page.setWidget(pageWidget);
		pageWidget.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				page.dispose();
			}
		});
		return pageWidget;
	}

	private void ensureSelectedTabInitialized() {
		if (folder != null && !folder.isDisposed()) {
			CTabItem tab = folder.getSelection();
			if (tab != null) {
				DashboardPageContainer page = (DashboardPageContainer) tab.getData();
				if (page != null) {
					page.initialize(getSite());
				}
			}
		}
	}

	/**
	 * Called when we need the initial pages. This will be called only once per
	 * DashboardEditor instance. Clients can override to create a different set
	 * of initial pages.
	 */
	protected List<IDashboardPage> createPages() {
		List<IDashboardPage> pages = new ArrayList<IDashboardPage>();
		InputStream input = null;
		try {
			WelcomeDashboardPage mainPage = new WelcomeDashboardPage(this);
			pages.add(mainPage);
			customizeUrlBehavior(StsProperties.getInstance(new NullProgressMonitor()));

		} catch (Exception e) {
			GettingStartedActivator.log(e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}

		pages.add(new DashboardExtensionsPage());
		return pages;
	}

	private void customizeUrlBehavior(StsProperties props) {
		customNames = new HashMap<String, String>();
		ArrayList<String> openExternal = new ArrayList<String>();
		for (String propName : props.getExplicitProperties()) {
			if (propName.endsWith(".url")) {
				String url = props.get(propName);
				String customLabel = props.get(propName + ".label");
				if (customLabel != null) {
					customNames.put(url, customLabel);
				}
			}
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInput(input);
	}

	@Override
	public void setFocus() {
		if (folder != null) {
			folder.setFocus();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// This isn't a real editor. So there's really nothing to save.
	}

	@Override
	public void doSaveAs() {
		// This isn't a real editor. So there's really nothing to save.
	}

	@Override
	public boolean isDirty() {
		// This isn't a real editor. It's never dirty.
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// This isn't a real editor. There's nothing to save.
		return false;
	}

	@Override
	public boolean setActivePage(String pageId) {
		DashboardPageContainer p = getPage(pageId);
		if (p != null) {
			if (pageId.equals(p.getPageId())) {
				setActivePage(p);
				return true;
			}
		}
		return false;
	}

	private void setActivePage(DashboardPageContainer p) {
		folder.setSelection(p.getWidget());
		ensureSelectedTabInitialized();
	}

	private DashboardPageContainer getPage(String pageId) {
		for (CTabItem item : folder.getItems()) {
			Object _p = item.getData();
			if (_p instanceof DashboardPageContainer) {
				DashboardPageContainer p = (DashboardPageContainer) _p;
				if (pageId.equals(p.getPageId())) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * Try to open a webpage in the dashboard. The url will be used as the
	 * 'page-id' for the page. If a page with the given id already exists then
	 * it will be reused rather than creating a new page.
	 * 
	 * @return
	 */
	public boolean openWebPage(String url) {
		DashboardPageContainer p = getPage(url);
		if (p != null) {
			IDashboardPage _wp = p.getPage();
			if (_wp instanceof WebDashboardPage) {
				WebDashboardPage wp = (WebDashboardPage) _wp;
				wp.goHome();
				setActivePage(p);
				return true;
			}
			return false;
		} else {
			String customName = getCustomName(url);
			WebDashboardPage page = new WebDashboardPage(customName, url);
			CTabItem widget = createPageWidget(page);
			setActivePage((DashboardPageContainer) widget.getData());
			page.getBrowserViewer().setVisible(true);
			return true;
		}
	}

	private String getCustomName(String url) {
		try {
			if (customNames != null) {
				return (String) customNames.get(url);
			}
		} catch (Exception e) {
			GettingStartedActivator.log(e);
		}
		return null;
	}
}
