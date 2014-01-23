/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.forms.widgets.BusyIndicator;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.eclipse.ui.internal.forms.widgets.TitleRegion;
import org.springsource.ide.eclipse.commons.ui.UiUtil;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.internal.ui.discovery.DashboardExtensionsPage;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPage;
import org.springsource.ide.eclipse.dashboard.ui.IEnabledDashboardPart;

/**
 * The editor for the dashboard.
 * <p>
 * Note: Must not have any dependencies on org.eclipse.mylyn.tasks.ui to avoid
 * class loader warnings.
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class MultiPageDashboardEditor extends SharedHeaderFormEditor {

	private static class ExtensionPageDescriptor extends PageDescriptor {

		private final IConfigurationElement element;

		public ExtensionPageDescriptor(IConfigurationElement element) {
			super(element.getAttribute(ATTRIBUTE_ID));
			this.element = element;
			setLabel(element.getAttribute(ATTRIBUTE_LABEL));
			setPath(element.getAttribute(ATTRIBUTE_PATH));
		}

		@Override
		public AbstractDashboardPage createPage() {
			try {
				Object object = WorkbenchPlugin.createExtension(element, ATTRIBUTE_CLASS);
				if (!(object instanceof AbstractDashboardPage)) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not load "
							+ object.getClass().getCanonicalName() + " must implement "
							+ AbstractDashboardPage.class.getCanonicalName()));
					return null;
				}

				return (AbstractDashboardPage) object;
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
						"Could not read dashboard extension", e));
			}
			return null;
		}

	}

	private static abstract class PageDescriptor {

		private final String id;

		private String path;

		private String label;

		public PageDescriptor(String id) {
			Assert.isNotNull(id);
			this.id = id;
		}

		public abstract AbstractDashboardPage createPage();

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			PageDescriptor other = (PageDescriptor) obj;
			return id.equals(other.id);
		}

		public final String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		public final String getPath() {
			return path;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id.hashCode();
			return result;
		}

		public final PageDescriptor setLabel(String label) {
			this.label = label;
			return this;
		}

		public final PageDescriptor setPath(String path) {
			this.path = path;
			return this;
		}

	}

	private static final String EXTENSION_ID_DASHBOARD = "com.springsource.sts.ide.ui.dashboard";

	private static final String ELEMENT_PAGE = "page";

	private static final String ATTRIBUTE_ID = "id";

	private static final String ATTRIBUTE_LABEL = "label";

	private static final String ATTRIBUTE_PATH = "path";

	private static final String ATTRIBUTE_CLASS = "class";

	private static final int LEFT_TOOLBAR_HEADER_TOOLBAR_PADDING = 3;

	private static final int VERTICAL_TOOLBAR_PADDING = 11;

	private static final String TITLE = "Spring Dashboard";

	public static String EDITOR_ID = "com.springsource.sts.internal.ide.ui.editors.MultiPageDashboardEditor";

	public static final String NEW_EDITOR_ID = "org.springsource.ide.eclipse.commons.gettingstarted.dashboard.WelcomeDashboard";

	/**
	 * Copied from TasksUiInternal to avoid initialization of
	 * org.eclipse.mylyn.tasks.ui.
	 */
	private static EvaluationContext createDiscoveryWizardEvaluationContext(IHandlerService handlerService) {
		EvaluationContext evaluationContext = new EvaluationContext(handlerService.getCurrentState(), Platform.class);
		// must specify this variable otherwise the PlatformPropertyTester won't
		// work
		evaluationContext.addVariable("platform", Platform.class); //$NON-NLS-1$
		return evaluationContext;
	}

	/**
	 * Copied from TasksUiInternal to avoid initialization of
	 * org.eclipse.mylyn.tasks.ui.
	 */
	private static Command getConfiguredDiscoveryWizardCommand() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command discoveryWizardCommand = service
				.getCommand("org.eclipse.mylyn.discovery.ui.discoveryWizardCommand"); //$NON-NLS-1$
		if (discoveryWizardCommand != null) {
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(
					IHandlerService.class);
			EvaluationContext evaluationContext = createDiscoveryWizardEvaluationContext(handlerService);
			// update enabled state in case something has changed (ProxyHandler
			// caches state)
			discoveryWizardCommand.setEnabled(evaluationContext);
		}
		return discoveryWizardCommand;
	}

	private BusyIndicator busyLabel;

	private Label titleLabel;

	private Image headerImage;

	private DashboardMainPage mainPage;

	public MultiPageDashboardEditor() {
		DashboardReopener.ensure();
	}

	@Override
	protected void addPages() {
		try {
			List<PageDescriptor> pageDescriptors = readExtensions();

			mainPage = new DashboardMainPage(this);
			int index = addPage(mainPage);
			setPageText(index, "Dashboard");

			addPages(pageDescriptors, "tasks");

			addPages(pageDescriptors, "knowledgeBase");

			DashboardExtensionsPage exPage = new DashboardExtensionsPage(this);
			if (exPage.shouldAdd()) {
				Command discoveryWizardCommand = getConfiguredDiscoveryWizardCommand();
				if (discoveryWizardCommand != null && discoveryWizardCommand.isEnabled()) {
					index = addPage(exPage);
					setPageText(index, "Extensions");
				}
			}

			addPages(pageDescriptors, "additions");

			getToolkit().decorateFormHeading(getHeaderForm().getForm().getForm());
		}
		catch (PartInitException e) {
			IdeUiPlugin.log(e);
		}
	}

	private List<AbstractDashboardPage> addPages(List<PageDescriptor> descriptors, String path) {
		final List<AbstractDashboardPage> pages = new ArrayList<AbstractDashboardPage>();
		final Iterator<PageDescriptor> it = descriptors.iterator();
		while (it.hasNext()) {
			final PageDescriptor descriptor = it.next();
			// only add selected pages
			if (path != null && !path.equals(descriptor.getPath())) {
				continue;
			}
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable e) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
							"Error creating dashboard page: \"" + descriptor.getId() + "\"", e)); //$NON-NLS-1$ //$NON-NLS-2$
				}

				public void run() throws Exception {
					AbstractDashboardPage page = descriptor.createPage();
					if (page != null) {
						page.initialize(MultiPageDashboardEditor.this);
						if (page instanceof IEnabledDashboardPart) {
							if (!((IEnabledDashboardPart) page).shouldAdd()) {
								return;
							}
						}
						int index = addPage(page);
						setPageText(index, descriptor.getLabel());
						pages.add(page);
					}
					it.remove();
				}
			});
		}
		return pages;
	}

	@Override
	public void close(boolean save) {
		mainPage.cancelUnfinishedJobs();
		super.close(save);
	}

	@Override
	protected Composite createPageContainer(Composite parent) {
		Composite composite = super.createPageContainer(parent);

		EditorUtil.initializeScrollbars(getHeaderForm().getForm());

		// create title label that replaces form heading label
		try {
			FormHeading heading = (FormHeading) getHeaderForm().getForm().getForm().getHead();

			Field field = FormHeading.class.getDeclaredField("titleRegion"); //$NON-NLS-1$
			field.setAccessible(true);

			TitleRegion titleRegion = (TitleRegion) field.get(heading);

			titleLabel = new Label(titleRegion, SWT.NONE);
			titleLabel.setForeground(heading.getForeground());
			titleLabel.setFont(heading.getFont());
			titleLabel.setText("Spring Tool Suite");
			titleLabel.setVisible(true);

			getBusyLabel();
			titleRegion.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					// do not create busyLabel to avoid recursion
					updateSizeAndLocations();
				}
			});

		}
		catch (Exception e) {
			// if (!toolBarFailureLogged) {
			StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN,
					"Failed to obtain busy label toolbar", e)); //$NON-NLS-1$
			// }
			if (titleLabel != null) {
				titleLabel.dispose();
				titleLabel = null;
			}
		}
		updateHeaderLabel();
		return composite;
	}

	@Override
	protected FormToolkit createToolkit(Display display) {
		return new FormToolkit(UiUtil.getFormColors(display));
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Nothing to do
	}

	@Override
	public void doSaveAs() {
		// Nothing to do
	}

	private BusyIndicator getBusyLabel() {
		if (busyLabel != null) {
			return busyLabel;
		}
		try {
			FormHeading heading = (FormHeading) getHeaderForm().getForm().getForm().getHead();
			// ensure that busy label exists
			heading.setBusy(true);
			heading.setBusy(false);

			Field field = FormHeading.class.getDeclaredField("titleRegion"); //$NON-NLS-1$
			field.setAccessible(true);

			TitleRegion titleRegion = (TitleRegion) field.get(heading);

			for (Control child : titleRegion.getChildren()) {
				if (child instanceof BusyIndicator) {
					busyLabel = (BusyIndicator) child;
				}
			}
			if (busyLabel == null) {
				return null;
			}
			busyLabel.addControlListener(new ControlAdapter() {
				@Override
				public void controlMoved(ControlEvent e) {
					updateSizeAndLocations();
				}
			});
			// the busy label may get disposed if it has no image
			busyLabel.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					busyLabel.setMenu(null);
					busyLabel = null;
				}
			});

			if (titleLabel != null) {
				titleLabel.moveAbove(busyLabel);
			}

			updateSizeAndLocations();

			return busyLabel;
		}
		catch (Exception e) {
			StatusHandler
					.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Failed to obtain busy label toolbar", e)); //$NON-NLS-1$
			busyLabel = null;
		}
		return busyLabel;
	}

	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		setSite(site);
		setInput(editorInput);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private List<PageDescriptor> readExtensions() {
		List<PageDescriptor> pageDescriptors = new ArrayList<PageDescriptor>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_DASHBOARD);
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(ELEMENT_PAGE)) {
					pageDescriptors.add(new ExtensionPageDescriptor(element));
				}
			}
		}
		return pageDescriptors;
	}

	private void setHeaderImage(final Image image) {
		BusyIndicator busyLabel = getBusyLabel();
		if (busyLabel == null) {
			return;
		}

		final Point size = busyLabel.getSize();
		Point titleSize = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		size.x += titleSize.x + LEFT_TOOLBAR_HEADER_TOOLBAR_PADDING;
		size.y = Math.max(titleSize.y, size.y) + VERTICAL_TOOLBAR_PADDING;

		// ensure image is at least one pixel wide to avoid SWT error
		final int padding = (size.x > 0) ? 10 : 1;
		final Rectangle imageBounds = (image != null) ? image.getBounds() : new Rectangle(0, 0, 0, 0);
		int tempHeight = (image != null) ? Math.max(size.y + 1, imageBounds.height) : size.y + 1;
		// avoid extra padding due to margin added by TitleRegion.VMARGIN
		final int height = (tempHeight > imageBounds.height + 5) ? tempHeight - 5 : tempHeight;

		CompositeImageDescriptor descriptor = new CompositeImageDescriptor() {
			@Override
			protected void drawCompositeImage(int width, int height) {
				if (image != null) {
					drawImage(image.getImageData(), size.x + padding, (height - image.getBounds().height) / 2);
				}
			}

			@Override
			protected Point getSize() {
				return new Point(size.x + padding + imageBounds.width, height);
			}

		};
		Image newHeaderImage = descriptor.createImage();

		// directly set on busyLabel since getHeaderForm().getForm().setImage()
		// does not update
		// the image if a message is currently displayed
		busyLabel.setImage(newHeaderImage);

		if (headerImage != null) {
			headerImage.dispose();
		}
		headerImage = newHeaderImage;

		// avoid extra padding due to large title font
		getHeaderForm().getForm().reflow(true);
	}

	private void updateHeaderLabel() {
		if (titleLabel != null) {
			titleLabel.setText(TITLE);
			getHeaderForm().getForm().setText(null);
			setHeaderImage(null);
		}
		else {
			getHeaderForm().getForm().setText(TITLE);
		}
	}

	private void updateSizeAndLocations() {
		// Point leftToolBarSize = new Point(0, 0);
		if (titleLabel != null && !titleLabel.isDisposed()) {
			// center align title text in title region
			Point size = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			// int y = (titleLabel.getParent().computeSize(SWT.DEFAULT,
			// SWT.DEFAULT, true).y - size.y) / 2;
			titleLabel.setBounds(busyLabel.getLocation().x + LEFT_TOOLBAR_HEADER_TOOLBAR_PADDING,
					busyLabel.getLocation().y + VERTICAL_TOOLBAR_PADDING, size.x + 200, size.y + 200);
		}
	}

}
