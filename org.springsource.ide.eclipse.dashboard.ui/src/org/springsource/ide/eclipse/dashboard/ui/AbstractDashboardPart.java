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
package org.springsource.ide.eclipse.dashboard.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Clients that contribute sections to the dashboard need to extend this class.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public abstract class AbstractDashboardPart extends AbstractFormPart {

	public static String ID_PATH_TOP = "com.springsource.sts.ide.ui.dashboard.top";

	public static String ID_PATH_DOC = "com.springsource.sts.ide.ui.dashboard.doc";

	public static String ID_PATH_NEWS = "com.springsource.sts.ide.ui.dashboard.news";

	public static String ID_PATH_BOTTOM = "com.springsource.sts.ide.ui.dashboard.bottom";

	private Control control;

	private String id;

	public final void createControl(Composite parent) {
		this.control = createPartContent(parent);
	}

	public abstract Control createPartContent(Composite parent);

	public final Control getControl() {
		return control;
	}

	public String getId() {
		return id;
	}

	protected FormToolkit getToolkit() {
		return getManagedForm().getToolkit();
	}

	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);
	}

	public void setId(String id) {
		this.id = id;
	}

}
