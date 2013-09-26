/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springsource.ide.eclipse.commons.quicksearch.ui.QuickSearchActivator;

/**
 * Useful utilities (private methods) copied from org.eclipse.search.internal.core.text.TextSearchVisitor
 * and rearanged / massaged to be a more reusable utility class.
 * <p>
 * These utilities allow us to access the contents of dirty editors so we can search/read in them as though they
 * are already saved but without actually requiring the use to save them.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class DocumentFetcher {

	private Map<IFile, IDocument> dirtyEditors;
	
	//Simple cache remembers the last fetched file and document. 
	private IFile lastFile = null;
	private IDocument lastDocument = null;
	
	IDocumentProvider provider = new TextFileDocumentProvider();

	public DocumentFetcher() {
		if (PlatformUI.isWorkbenchRunning()) {
			dirtyEditors = evalNonFileBufferDocuments();
		} else {
			dirtyEditors = Collections.emptyMap();
		}
	}
	
	public IDocument getDocument(IFile file) {
		if (file==lastFile) {
			return lastDocument;
		}
		lastFile = file;
		lastDocument = dirtyEditors.get(file);
		if (lastDocument==null) {
			lastDocument = getOpenDocument(file);
			if (lastDocument==null) {
				lastDocument = getClosedDocument(file);
			}
		}
		return lastDocument;
	}
	
	private IDocument getOpenDocument(IFile file) {
		ITextFileBufferManager bufferManager= FileBuffers.getTextFileBufferManager();
		ITextFileBuffer textFileBuffer= bufferManager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		if (textFileBuffer != null) {
			return textFileBuffer.getDocument();
		}
		return null;
	}

	private IDocument getClosedDocument(IFile file) {
		//No  in the manager yet. Try to create a temporary buffer then remove it again.
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath location = file.getFullPath(); //Must use workspace location, not fs location for API below.
		ITextFileBuffer buffer = null;
		try {
			bufferManager.connect(location, LocationKind.IFILE, new NullProgressMonitor());
			buffer = bufferManager.getTextFileBuffer(location, LocationKind.IFILE);
			if (buffer!=null) {
				return buffer.getDocument();
			}
		} catch (Throwable e) {
			QuickSearchActivator.log(e);
		} finally {
			try {
				bufferManager.disconnect(location, LocationKind.IFILE, new NullProgressMonitor());
			} catch (CoreException e) {
			}
		}
		return null;
	}
	
	/**
	 * @return returns a map from IFile to IDocument for all open, dirty editors.
	 */
	private Map<IFile, IDocument> evalNonFileBufferDocuments() {
		Map<IFile, IDocument> result= new HashMap<IFile, IDocument>();
		IWorkbench workbench= SearchPlugin.getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorReference[] editorRefs= pages[x].getEditorReferences();
				for (int z= 0; z < editorRefs.length; z++) {
					IEditorPart ep= editorRefs[z].getEditor(false);
					if (ep instanceof ITextEditor && ep.isDirty()) { // only dirty editors
						evaluateTextEditor(result, ep);
					}
				}
			}
		}
		return result;
	}

	private void evaluateTextEditor(Map<IFile, IDocument> result, IEditorPart ep) {
		IEditorInput input= ep.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) input).getFile();
			if (!result.containsKey(file)) { // take the first editor found
				ITextFileBufferManager bufferManager= FileBuffers.getTextFileBufferManager();
				ITextFileBuffer textFileBuffer= bufferManager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
				if (textFileBuffer != null) {
					// file buffer has precedence
					result.put(file, textFileBuffer.getDocument());
				} else {
					// use document provider
					IDocument document= ((ITextEditor) ep).getDocumentProvider().getDocument(input);
					if (document != null) {
						result.put(file, document);
					}
				}
			}
		}
	}
	
}
