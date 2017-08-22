/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.browser.swt;

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class SelectionListenerUtil {

	/**
	 * Static helper method to create a <code>SelectionListener</code> for the
	 * {@link #widgetSelected(SelectionEvent e)}) method, given a lambda expression
	 * or a method reference.
	 *
	 * @param c the consumer of the event
	 * @return SelectionListener
	 */
	public static SelectionListener widgetSelectedAdapter(Consumer<SelectionEvent> c) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				c.accept(e);
			}
		};
	}

}
