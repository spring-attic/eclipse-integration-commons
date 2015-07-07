/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import org.eclipse.swt.widgets.Display;

/**
 * This class should be used instead of ValueListener when the code it
 * wants to execute upon receiving a 'gotValue' event is required
 * to run in the UIThread (i.e. typically this is code that needs to
 * update or read widgets in the UI).
 */
public abstract class UIValueListener<T> implements ValueListener<T> {

	/**
	 * This method is final. Implement 'uiGotValue' instead.
	 */
	public final void gotValue(final LiveExpression<T> exp, final T value) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				uiGotValue(exp, value);
			}
		});
	}

	protected Display getDisplay() {
		return Display.getDefault();
	}

	/**
	 * Subclasses should implement. This method will always be called in the UIThread.
	 */
	protected abstract void uiGotValue(LiveExpression<T> exp, T value);

}
