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
package org.springsource.ide.eclipse.commons.frameworks.core.internal.commands;

/**
 * Notifies listeners when parameter values have changed.
 * @author Nieraj Singh
 */
public interface IValueChangedNotifier {

	public IParameterValueListener addListener(IParameterValueListener listener);

	public IParameterValueListener removeListener(
			IParameterValueListener listener);

}
