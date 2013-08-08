/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.ui.actions;

/**
 * Interface that the new Dashboard implements so that this plugin can call methods on
 * it without creating a direct dependency on the new gettingstarted plugins.
 * 
 * @author Kris De Volder
 */
public interface IDashboardWithPages {

	/**
	 * Make page with given id visible. Does nothing if page with this id can not
	 * be found.
	 */
	void setActivePage(String pageId);
	
}
