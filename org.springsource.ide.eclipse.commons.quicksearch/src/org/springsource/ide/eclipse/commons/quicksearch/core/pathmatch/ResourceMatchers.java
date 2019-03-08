/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.core.pathmatch;

import org.eclipse.core.resources.IResource;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.ignore.internal.IMatcher;
import org.eclipse.jgit.ignore.internal.PathMatcher;

@SuppressWarnings("restriction")
public class ResourceMatchers {
	
	public static ResourceMatcher ANY = new ResourceMatcher() {
		@Override
		public String toString() {
			return "ResourceMatcher(ANY)";
		}
		@Override
		public boolean matches(IResource resource) {
			return true;
		}
	};

	public static ResourceMatcher commaSeparatedPaths(String text) {
		text = text.trim();
		if (text.isEmpty()) {
			return ANY;
		}
		String[] paths = text.split(",");
		if (paths.length==1) {
			return path(paths[0]);
		} else {
			ResourceMatcher[] matchers = new ResourceMatcher[paths.length];
			for (int i = 0; i < matchers.length; i++) {
				matchers[i] = path(paths[i]);
			}
			return either(matchers);
		}
	}

	private static ResourceMatcher either(ResourceMatcher... matchers) {
		return new ResourceMatcher() {
			
			@Override
			public String toString() {
				StringBuilder buf = new StringBuilder("ResourceMatcher(");
				for (int i = 0; i < matchers.length; i++) {
					if (i>0) {
						buf.append(", ");
					}
					buf.append(matchers[i]);
				}
				buf.append(")");
				return buf.toString();
			}
			
			@Override
			public boolean matches(IResource resource) {
				for (ResourceMatcher m : matchers) {
					if (m.matches(resource)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	private static ResourceMatcher path(String path) {
		try {
			IMatcher matcher = PathMatcher.createPathMatcher(path, '/', false);
			return new ResourceMatcher() {
				
				@Override
				public String toString() {
					return path;
				}
				
				@Override
				public boolean matches(IResource resource) {
					return matcher.matches(resource.getFullPath().toString(), resource.getType()==IResource.FOLDER, false);
				}
			};
			
		} catch (InvalidPatternException e) {
			return ANY;
		}
	}

}
