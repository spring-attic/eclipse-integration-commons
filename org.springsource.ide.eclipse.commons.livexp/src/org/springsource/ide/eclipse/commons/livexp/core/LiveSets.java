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
package org.springsource.ide.eclipse.commons.livexp.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Kris De Volder
 */
public class LiveSets {

	@SuppressWarnings("rawtypes")
	private static final ObservableSet EMPTY_SET = ObservableSet.constant(ImmutableSet.of());

	@SuppressWarnings("unchecked")
	public static <T> ObservableSet<T> emptySet(Class<T> t) {
		return EMPTY_SET;
	}

	@SuppressWarnings("unchecked")
	public static <R, A extends R, B extends R> ObservableSet<R> union(ObservableSet<A> e1, ObservableSet<B> e2) {
		if (e1==EMPTY_SET) {
			return (ObservableSet<R>) e2;
		} else if (e2==EMPTY_SET) {
			return (ObservableSet<R>) e1;
		} else {
			return new LiveUnion<>(e1, e2);
		}
	}

	private static class LiveUnion<T, A extends T, B extends T> extends ObservableSet<T> {

		private ObservableSet<A> e1;
		private ObservableSet<B> e2;

		public LiveUnion(ObservableSet<A> e1, ObservableSet<B> e2) {
			this.e1 = e1;
			this.e2 = e2;
			this.dependsOn(e1);
			this.dependsOn(e2);
		}

		@Override
		protected ImmutableSet<T> compute() {
			return ImmutableSet.copyOf(Sets.union(e1.getValue(), e2.getValue()));
		}
	}

	@SuppressWarnings("unchecked")
	public static <R, A extends R, B extends R> ObservableSet<R> intersection(ObservableSet<A> a, ObservableSet<B> b) {
		if (a==EMPTY_SET || b==EMPTY_SET) {
			return EMPTY_SET;
		} else {
			return new LiveIntersection<>(a, b);
		}
	}

	private static final class LiveIntersection<T, A extends T, B extends T>  extends ObservableSet<T> {

		private ObservableSet<A> a;
		private ObservableSet<B> b;

		public LiveIntersection(ObservableSet<A> a, ObservableSet<B> b) {
			this.a = a;
			this.b = b;
			this.dependsOn(a);
			this.dependsOn(b);
		}
		@Override
		protected ImmutableSet<T> compute() {
			return ImmutableSet.copyOf(Sets.intersection(a.getValue(), b.getValue()));
		}
	}
}
