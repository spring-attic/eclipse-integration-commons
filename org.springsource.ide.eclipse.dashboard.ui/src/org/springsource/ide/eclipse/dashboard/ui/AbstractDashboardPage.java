/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;

/**
 * A dashboard page that support part contributions.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Terry Denney
 */
public abstract class AbstractDashboardPage extends FormPage {

	private static class ExtensionPartDescriptor extends PartDescriptor {

		private final IConfigurationElement element;

		public ExtensionPartDescriptor(IConfigurationElement element) {
			super(element.getAttribute(ATTRIBUTE_ID));
			this.element = element;
			setPath(element.getAttribute(ATTRIBUTE_PATH));
		}

		@Override
		public AbstractDashboardPart createPart() {
			try {
				Object object = WorkbenchPlugin.createExtension(element, ATTRIBUTE_CLASS);
				if (!(object instanceof AbstractDashboardPart)) {
					StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID, "Could not load "
							+ object.getClass().getCanonicalName() + " must implement "
							+ AbstractDashboardPart.class.getCanonicalName()));
					return null;
				}

				return (AbstractDashboardPart) object;
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
						"Could not read dashboard extension", e));
			}
			return null;
		}

	}

	private static abstract class PartDescriptor {

		private final String id;

		private String path;

		public PartDescriptor(String id) {
			Assert.isNotNull(id);
			this.id = id;
		}

		public abstract AbstractDashboardPart createPart();

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			PartDescriptor other = (PartDescriptor) obj;
			return id.equals(other.id);
		}

		public final String getId() {
			return id;
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

		public final PartDescriptor setPath(String path) {
			this.path = path;
			return this;
		}

	}

	private static final String EXTENSION_ID_DASHBOARD = "com.springsource.sts.ide.ui.dashboard";

	private static final String ELEMENT_PART = "part";

	private static final String ATTRIBUTE_ID = "id";

	private static final String ATTRIBUTE_PATH = "path";

	private static final String ATTRIBUTE_PAGE = "page";

	private static final String ATTRIBUTE_CLASS = "class";

	private List<PartDescriptor> partDescriptors;

	public AbstractDashboardPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	public AbstractDashboardPage(String id, String title) {
		super(id, title);
	}

	protected List<AbstractDashboardPart> contributeParts(Composite parent, String path) {
		if (partDescriptors == null) {
			readExtensions();
		}
		return createParts(parent, path, partDescriptors);
	}

	private List<AbstractDashboardPart> createParts(final Composite parent, String path,
			Collection<PartDescriptor> descriptors) {
		final List<AbstractDashboardPart> parts = Collections.synchronizedList(new ArrayList<AbstractDashboardPart>());
		for (Iterator<PartDescriptor> it = descriptors.iterator(); it.hasNext();) {
			final PartDescriptor descriptor = it.next();
			if (path == null || path.equals(descriptor.getPath())) {
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable e) {
						StatusHandler.log(new Status(IStatus.ERROR, IdeUiPlugin.PLUGIN_ID,
								"Error creating dashboard part: \"" + descriptor.getId() + "\"", e)); //$NON-NLS-1$ //$NON-NLS-2$
					}

					public void run() throws Exception {
						AbstractDashboardPart part = descriptor.createPart();
						if (part != null) {
							if (part instanceof IEnabledDashboardPart) {
								if (!((IEnabledDashboardPart) part).shouldAdd()) {
									return;
								}
							}

							part.setId(descriptor.getId());
							parts.add(part);
							initializePart(parent, part);
						}
					}
				});
				it.remove();
			}
		}
		return parts;
	}

	private void initializePart(Composite parent, AbstractDashboardPart part) {
		getManagedForm().addPart(part);
		part.initialize(getManagedForm());
		part.createControl(parent);
		if (part.getControl() != null) {
			if (parent.getLayout() instanceof GridLayout) {
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(false, true).applyTo(part.getControl());
			}
			else if (parent.getLayout() instanceof TableWrapLayout) {
				part.getControl().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			}
		}
	}

	private void readExtensions() {
		this.partDescriptors = new ArrayList<PartDescriptor>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_DASHBOARD);
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(ELEMENT_PART)) {
					readPartExtension(element);
				}
			}
		}
	}

	private void readPartExtension(IConfigurationElement element) {
		String page = element.getAttribute(ATTRIBUTE_PAGE);
		if (getId().equals(page)) {
			partDescriptors.add(new ExtensionPartDescriptor(element));
		}
	}

}
