/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import java.util.List;

/**
 * Sections that are composed of other sections may implement this interface
 * so that some code can operate on such composite section's and their children
 * generically.
 *
 * @author Kris De Volder
 */
public interface CompositeSection extends IPageSection {

	public List<IPageSection> getChildren();

}
