/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class HeaderPart extends AbstractTaskEditorPart {

	private final String text;

	public HeaderPart(String text) {
		this.text = text;
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		layout.marginLeft = 0;
		layout.marginTop = 0;
		layout.marginRight = 10;
		layout.marginBottom = 0;
		composite.setLayout(layout);

		Label label = toolkit.createLabel(composite, text, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER)
				.applyTo(label);

		label = toolkit.createLabel(composite, null);
		label.setImage(IdeUiPlugin.getImage("prod/spring_logo_transparent.png"));

		setControl(composite);
	}

}
