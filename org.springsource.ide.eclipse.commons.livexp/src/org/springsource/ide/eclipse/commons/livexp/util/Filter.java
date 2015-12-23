/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.util;

/**
 * General interface for filtering elements.
 *
 * TODO: consider replacing all uses of this type with com.google.common.base.Predicate
 *
 * @author Alex Boyko
 *
 * @param <T>
 */
public interface Filter<T> {

	boolean accept(T t);

}
