/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.configurator.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.internal.configurator.Activator;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter;
import org.springsource.ide.eclipse.commons.ui.ICoreRunnable;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;
import org.springsource.ide.eclipse.commons.ui.UiUtil;


/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class ConfiguratorPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button browseButton;

	private Button configureButton;

	private TableViewer extensionViewer;

	private ConfiguratorImporter importer;

	private Button installButton;

	private boolean installEnabled;

	private Label searchLocationsLabel;

	private Button useDefaultUserLocationButton;

	private Text userLocationText;

	List<ConfigurableExtension> elements;

	public void init(IWorkbench workbench) {
		updateElements();
	}

	public boolean isInstallEnabled() {
		return installEnabled;
	}

	@Override
	public boolean performOk() {
		if (useDefaultUserLocationButton.getSelection()) {
			Activator.getDefault().getPreferenceStore().setToDefault(Activator.PROPERTY_USER_INSTALL_PATH);
		}
		else {
			Activator.getDefault().getPreferenceStore()
					.setValue(Activator.PROPERTY_USER_INSTALL_PATH, userLocationText.getText());
		}
		return super.performOk();
	}

	public void setInstallEnabled(boolean installEnabled) {
		this.installEnabled = installEnabled;
		updateSelection(extensionViewer.getSelection());
		if (!installEnabled) {
			setMessage("Install Location is not set to a writeable directory. Installations are disabled.",
					IMessageProvider.WARNING);
		}
		else {
			setMessage(null);
		}
	}

	private void doRefresh() {
		updateElements();
		extensionViewer.setInput(elements.toArray());
	}

	private ConfigurableExtension getSelectedExtension(ISelection selection) {
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		ConfigurableExtension extension = (ConfigurableExtension) structuredSelection.getFirstElement();
		return extension;
	}

	private void handleResult(final ConfigurableExtension extension, final IStatus[] status) {
		if (status[0] != null) {
			if (!status[0].isOK()) {
				StatusManager.getManager().handle(status[0], StatusManager.SHOW | StatusManager.LOG);
			}
			else {
				MessageDialog.openInformation(UiUtil.getShell(), "Auto Configuration", status[0].getMessage());
			}

			extension.postConfiguration(status[0]);
		}
	}

	private void initWidgets() {
		resetUserLocation();
		useDefaultUserLocationButton.setSelection(userLocationText.getText().equals(
				getPreferenceStore().getDefaultString(Activator.PROPERTY_USER_INSTALL_PATH)));
		userLocationText.setEnabled(!useDefaultUserLocationButton.getSelection());
		browseButton.setEnabled(!useDefaultUserLocationButton.getSelection());
		if (searchLocationsLabel != null) {
			searchLocationsLabel.setText(StringUtils.collectionToDelimitedString(importer.getSearchLocations(), ", "));
		}
	}

	private void resetUserLocation() {
		File location = importer.getInstallLocation();
		if (location != null) {
			userLocationText.setText(location.getAbsolutePath());
		}
		else {
			userLocationText.setText("");
			setInstallEnabled(false);
		}
	}

	private void updateElements() {
		elements = new ArrayList<ConfigurableExtension>();
		importer = new ConfiguratorImporter();
		importer.setFirstMatchOnly(false);
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					elements.addAll(importer.detectExtensions(monitor));
				}
			});
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"An error occurred loading configurable items", e));
		}
		catch (InterruptedException e) {
			// ignore
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		Label label = new Label(composite, SWT.WRAP);
		label.setText("Configurable Extensions:");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);

		extensionViewer = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		extensionViewer.getTable().setHeaderVisible(true);
		extensionViewer.getTable().setLinesVisible(true);
		extensionViewer.setSorter(new ViewerSorter() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				String name1 = ((ConfigurableExtension) e1).getLabel();
				String name2 = ((ConfigurableExtension) e2).getLabel();
				return getComparator().compare(name1, name2);
			}
		});
		extensionViewer.setContentProvider(new IStructuredContentProvider() {

			Object[] elements;

			public void dispose() {
				// ignore
			}

			public Object[] getElements(Object inputElement) {
				return elements;
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				this.elements = (Object[]) newInput;
			}
		});
		GridDataFactory.fillDefaults().grab(true, true).applyTo(extensionViewer.getControl());

		TableViewerColumn statusColumn = new TableViewerColumn(extensionViewer, SWT.LEFT);
		statusColumn.getColumn().setText("");
		statusColumn.getColumn().setToolTipText("Configured");
		statusColumn.getColumn().setWidth(20);
		statusColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (((ConfigurableExtension) element).isConfigured()) {
					return CommonImages.getImage(CommonImages.COMPLETE);
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				return "";
			}
		});

		TableViewerColumn extensionColumn = new TableViewerColumn(extensionViewer, SWT.LEFT);
		extensionColumn.getColumn().setText("Extension");
		extensionColumn.getColumn().setWidth(250);
		extensionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ConfigurableExtension) element).getLabel();
			}
		});
		TableViewerColumn locationColumn = new TableViewerColumn(extensionViewer, SWT.LEFT);
		locationColumn.getColumn().setText("Location");
		locationColumn.getColumn().setWidth(150);
		locationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ConfigurableExtension) element).getLocation();
			}
		});
		extensionViewer.setInput(elements.toArray());

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(buttonComposite);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.fill = true;
		layout.marginLeft = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginBottom = 0;
		buttonComposite.setLayout(layout);

		configureButton = new Button(buttonComposite, SWT.NONE);
		configureButton.setText(" &Configure ");
		configureButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ConfigurableExtension extension = getSelectedExtension(extensionViewer.getSelection());
				if (extension != null) {
					doConfigure(extension);
				}
			}
		});
		installButton = new Button(buttonComposite, SWT.NONE);
		installButton.setText(" &Install ");
		installButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) extensionViewer.getSelection();
				ConfigurableExtension extension = (ConfigurableExtension) selection.getFirstElement();
				if (extension != null) {
					doInstall(extension);
				}
			}
		});

		Button refreshButton = new Button(buttonComposite, SWT.NONE);
		refreshButton.setText(" &Refresh ");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				doRefresh();
			}
		});

		extensionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelection(event.getSelection());
			}
		});

		updateSelection(extensionViewer.getSelection());

		Group locationGroup = new Group(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(locationGroup);
		locationGroup.setText("Runtime Locations");
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(locationGroup);

		File systemLocation = importer.getSystemLocation();
		if (systemLocation != null) {
			label = new Label(locationGroup, SWT.WRAP);
			label.setText("Search path for runtimes:");
			GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).span(3, 1).applyTo(label);
			searchLocationsLabel = new Label(locationGroup, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).span(3, 1)
					.applyTo(searchLocationsLabel);
		}

		useDefaultUserLocationButton = new Button(locationGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(useDefaultUserLocationButton);
		useDefaultUserLocationButton.setText("Use Default");
		useDefaultUserLocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				userLocationText.setEnabled(!useDefaultUserLocationButton.getSelection());
				browseButton.setEnabled(!useDefaultUserLocationButton.getSelection());
				if (useDefaultUserLocationButton.getSelection()) {
					resetUserLocation();
				}
			}
		});

		label = new Label(locationGroup, SWT.WRAP);
		label.setText("Install Location:");

		userLocationText = new Text(locationGroup, SWT.BORDER);
		userLocationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		userLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				try {
					File file = new File(userLocationText.getText());
					if (!file.canWrite()) {
						setErrorMessage(NLS.bind("''{0}'' is not writeable. Please select a different directory.",
								userLocationText.getText()));
						setInstallEnabled(false);
					}
					else {
						setErrorMessage(null);
						setInstallEnabled(true);
					}
				}
				catch (Exception e) {
					setErrorMessage(NLS.bind("''{0}'' is not a valid path.", userLocationText.getText()));
					setInstallEnabled(false);
				}
			}
		});

		browseButton = new Button(locationGroup, SWT.NONE);
		browseButton.setText("Directory...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Select the root directory for installing extensions.");
				String path = userLocationText.getText();
				path = path.replaceAll("\\\\", "/");
				dialog.setFilterPath(path);
				path = dialog.open();
				if (path == null || path.equals("")) { //$NON-NLS-1$
					return;
				}
				path = path.replaceAll("\\\\", "/");
				userLocationText.setText(path);
			}
		});

		initWidgets();

		return composite;
	}

	protected void doConfigure(final ConfigurableExtension extension) {
		try {
			final IStatus[] status = new IStatus[1];
			UiUtil.busyCursorWhile(new ICoreRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					status[0] = extension.configure(monitor);
				}
			});
			handleResult(extension, status);
			doRefresh();
		}
		catch (OperationCanceledException ignored) {
			// cancelled
		}
		catch (CoreException e) {
			UiStatusHandler.logAndDisplay(e.getStatus());
		}
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	protected void doInstall(final ConfigurableExtension extension) {
		// FIXME use directory where extension is installed
		final File installDirectory = new File(userLocationText.getText());
		if (!installDirectory.canWrite()) {
			UiStatusHandler.logAndDisplay(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(
					"Installation failed. The directory ''{0}'' is not writeable.", userLocationText.getText())));
			return;
		}
		try {
			final IStatus[] status = new IStatus[1];
			UiUtil.busyCursorWhile(new ICoreRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					status[0] = extension.install(installDirectory, monitor);
					if (status[0].getSeverity() != IStatus.ERROR) {
						IStatus configurationStatus = extension.configure(monitor);
						if (status[0].getSeverity() != IStatus.OK) {
							MultiStatus result = new MultiStatus(Activator.PLUGIN_ID, 0, NLS.bind(
									"The installation of {0} generated warning. See error log for details.",
									extension.getLabel()), null);
							result.add(status[0]);
							result.add(configurationStatus);
							status[0] = result;
						}
						else {
							status[0] = configurationStatus;
						}
					}
				}
			});
			handleResult(extension, status);
			doRefresh();
		}
		catch (OperationCanceledException ignored) {
			// cancelled
		}
		catch (CoreException e) {
			UiStatusHandler.logAndDisplay(e.getStatus());
		}
	}

	@Override
	protected void performApply() {
		super.performApply();
		doRefresh();
		initWidgets();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		Activator.getDefault().getPreferenceStore().setToDefault(Activator.PROPERTY_USER_INSTALL_PATH);
		doRefresh();
		initWidgets();
	}

	protected void updateSelection(ISelection selection) {
		configureButton.setEnabled(!selection.isEmpty());
		ConfigurableExtension extension = getSelectedExtension(selection);
		installButton.setEnabled(extension != null && extension.isInstallable() && isInstallEnabled());
	}

}
