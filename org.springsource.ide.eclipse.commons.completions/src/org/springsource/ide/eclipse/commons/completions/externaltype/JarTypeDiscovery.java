/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.externaltype;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springsource.ide.eclipse.commons.completions.CompletionsActivator;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;

/**
 * Discover external types from a jar file.
 * 
 * @author Kris De Volder
 */
public class JarTypeDiscovery extends AbstractExternalTypeSource implements ExternalTypeDiscovery {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder"); 
	
	private final File jarFile;
	
	public JarTypeDiscovery(File jarFile) {
		this.jarFile = jarFile; 
	}

	@Override
	public void getTypes(Requestor<ExternalTypeEntry> requestor) {
		//quick and dirty but fast implementation. It doesn't look inside the class
		//files but assumes the fq class names can be directly derived from the zip entries.
		//it also assumes that all types in file are public (again because not looking inside the .class files).
		ZipFile unzipper = null;
		try {
			unzipper = new ZipFile(jarFile);
			Enumeration<? extends ZipEntry> entries = unzipper.entries();
			boolean continu = true;
			while (entries.hasMoreElements() && continu) {
				ZipEntry e = entries.nextElement();
				String path = e.getName();
				//We are interested in class files that aren't inner or anonymous classes or classes in
				// the default package
				if (path.endsWith(".class") && !path.contains("$") && path.lastIndexOf('/')>1) {
					//TODO: can optimize a little to do less string copying here.
					int beg = path.charAt(0)=='/'?1:0;
					String fqName = path.substring(beg, path.length()-6/*".class".length()*/);
					fqName = fqName.replace('/', '.');
					continu = requestor.receive(debug(new ExternalTypeEntry(fqName, getTypeSource())));
				}
			}
		} catch (Exception e) {
			CompletionsActivator.log(e);
		} finally {
			if (unzipper!=null) {
				try {
					unzipper.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
	}

	private ExternalTypeEntry debug(ExternalTypeEntry externalTypeEntry) {
		if (DEBUG) {
			System.out.println(externalTypeEntry);
		}
		return externalTypeEntry;
	}

	protected ExternalTypeSource getTypeSource() {
		return this;
	}

	@Override
	public String toString() {
		return "JarTypeDiscovery["+jarFile+"]";
	}
	
	@Override
	public void addToClassPath(IJavaProject project, IProgressMonitor mon) {
		//We assume that this only gets called if the type that triggers this addition
		// is not already on the project's classpatht. The caller should ensure this.
		try {
			IClasspathEntry[] _cpes = project.getRawClasspath();
			IClasspathEntry[] cpes = new IClasspathEntry[_cpes.length+1];
			System.arraycopy(_cpes, 0, cpes, 0, _cpes.length);
			cpes[_cpes.length] = JavaCore.newLibraryEntry(new Path(jarFile.getAbsolutePath()), null, null);
			project.setRawClasspath(cpes, mon);
		} catch (Exception e) {
			CompletionsActivator.log(e);
		}
	}

}
