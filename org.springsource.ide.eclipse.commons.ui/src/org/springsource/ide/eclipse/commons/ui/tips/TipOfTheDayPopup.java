/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.tips;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;

/**
 * AutomaticUpdatesPopup is an async popup dialog for notifying the user of
 * updates.
 * 
 * @since 3.4
 */
public class TipOfTheDayPopup extends PopupDialog {
	private static final String DIALOG_SETTINGS_SECTION = "TipOfTheDayPopup";

	private static final int POPUP_OFFSET = 20;

	IPreferenceStore prefs;

	Composite dialogArea;

	Link preferenceLink;

	int tipCounter = 0;

	private Button dontRemindButton;

	private Composite tipComposite;

	private final TipProvider provider;

	public TipOfTheDayPopup(Shell parentShell, IPreferenceStore prefs, TipProvider provider) {
		super(parentShell, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE | SWT.MODELESS, false, true, true, false, false,
				"Tip o'the day", null);
		this.prefs = prefs;
		this.provider = provider;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		dialogArea.setLayout(layout);
		createTip(provider.nextTip());
		createNextPrevArea(dialogArea);
		createDontRemindSection(dialogArea);

		return dialogArea;

	}

	private void createTip(TipInfo tip) {
		if (tipComposite != null) {
			Control[] children = tipComposite.getChildren();
			for (Control element : children) {
				element.dispose();
			}
		}
		else {
			tipComposite = new Composite(dialogArea, SWT.NONE);
			tipComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			tipComposite.setLayout(new GridLayout(1, false));
		}

		createTipSection(tipComposite, tip);
		tipComposite.layout(true, true);
	}

	private void createTipSection(Composite parent, final TipInfo info) {
		final IWorkbenchWindow activeWindow = UiPlugin.getActiveWorkbenchWindow();
		String infoText = info.infoText;
		String linkText = info.linkText;
		String bindingText = info.getKeyBindingText(activeWindow);

		SelectionListener linkAction = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				info.invokeAction(event.text, activeWindow);
				close();
			}
		};

		Label infoLabel = new Label(parent, SWT.NONE);
		infoLabel.setText(infoText);
		infoLabel.setLayoutData(new GridData(GridData.FILL_BOTH));

		preferenceLink = new Link(parent, SWT.MULTI | SWT.WRAP | SWT.RIGHT);
		preferenceLink.addSelectionListener(linkAction);
		preferenceLink.setText(linkText);
		preferenceLink.setLayoutData(new GridData(GridData.FILL_BOTH));
		preferenceLink.setBackground(parent.getBackground());

		if (bindingText != null) {
			Label bindingLabel = new Label(parent, SWT.NONE);
			bindingLabel.setText(bindingText);
			bindingLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
	}

	private void createDontRemindSection(Composite parent) {
		dontRemindButton = new Button(parent, SWT.CHECK);
		dontRemindButton.setText("Don't show this again");
		dontRemindButton.setSelection(!prefs.getBoolean(UiPlugin.SHOW_TIP_O_DAY));
		dontRemindButton.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/*
	 * Overridden so that clicking in the title menu area closes the dialog.
	 * Also creates a close box menu in the title area.
	 */
	@Override
	protected Control createTitleMenuArea(Composite parent) {
		Composite titleComposite = (Composite) super.createTitleMenuArea(parent);

		ToolBar toolBar = new ToolBar(titleComposite, SWT.FLAT);
		ToolItem closeButton = new ToolItem(toolBar, SWT.PUSH, 0);

		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(toolBar);
		closeButton.setImage(StsUiImages.TIP_CLOSE.createImage());
		closeButton.setHotImage(StsUiImages.TIP_CLOSE_HOT.createImage());
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=177183
		titleComposite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				close();
			}
		});
		return titleComposite;
	}

	private Control createNextPrevArea(Composite parent) {
		Composite nextPrevComposite = new Composite(parent, SWT.NONE);
		nextPrevComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		nextPrevComposite.setLayout(new GridLayout(3, false));
		// draw a horiz line???
		Button navButton = new Button(nextPrevComposite, SWT.FLAT);
		navButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		navButton.setImage(StsUiImages.getImage(StsUiImages.TIP_PREV));
		navButton.setToolTipText("Show previous tip");
		navButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createTip(provider.previousTip());
			}
		});
		navButton = new Button(nextPrevComposite, SWT.FLAT);
		navButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		navButton.setImage(StsUiImages.getImage(StsUiImages.TIP_NEXT));
		navButton.setToolTipText("Show next tip");
		navButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createTip(provider.nextTip());
			}
		});

		return nextPrevComposite;
	}

	/*
	 * Overridden to adjust the span of the title label. Reachy, reachy....
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.PopupDialog#createTitleControl(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createTitleControl(Composite parent) {
		Control control = super.createTitleControl(parent);
		Object data = control.getLayoutData();
		if (data instanceof GridData) {
			((GridData) data).horizontalSpan = 1;
		}
		return control;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	@Override
	protected Point getInitialLocation(Point initialSize) {
		Shell parent = getParentShell();
		Point parentSize, parentLocation;

		if (parent != null) {
			parentSize = parent.getSize();
			parentLocation = parent.getLocation();
		}
		else {
			Rectangle bounds = getShell().getDisplay().getBounds();
			parentSize = new Point(bounds.width, bounds.height);
			parentLocation = new Point(0, 0);
		}
		// We have to take parent location into account because SWT considers
		// all
		// shell locations to be in display coordinates, even if the shell is
		// parented.
		return new Point(parentSize.x - initialSize.x + parentLocation.x - POPUP_OFFSET, parentSize.y - initialSize.y
				+ parentLocation.y - POPUP_OFFSET);
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = UiPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		return section;
	}

	@Override
	public boolean close() {
		prefs.setValue(UiPlugin.SHOW_TIP_O_DAY, !dontRemindButton.getSelection());
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Tip o'the day");
	}
}
