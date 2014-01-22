/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class DashboardEditorInput implements IEditorInput, IPersistableElement {

	public final static DashboardEditorInput INSTANCE = new DashboardEditorInput();

	private DashboardEditorInput() {
	}

	public boolean exists() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getFactoryId() {
		return DashboardEditorInputFactory.FACTORY_ID;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "Dashboard";
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		return "Dashboard";
	}

	public void saveState(IMemento memento) {
		DashboardEditorInputFactory.save(memento);
	}

}
