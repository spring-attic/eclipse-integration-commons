/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.core.runtime.CoreException;

/**
 * The 'model' associated (for example) a dialog that has an ok / cancel button must
 * probide a method to perform its operation when the ok button is pressed.
 * 
 * @author Kris De Volder
 */
public interface OkButtonHandler {

	/**
	 * Called by the hosting UI (e.g. a DialogWithSections) when the user presses the ok button.
	 */
	void performOk() throws Exception;
	
}
