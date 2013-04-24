/*******************************************************************************
 * Copyright (c) 2012 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springsource.ide.eclipse.commons.quicksearch.core.LineItem;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextQuery;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextQuery.TextRange;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class QuickSearchHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public QuickSearchHandler() {
	}

	private static void goToLineAndSelectQuery(IEditorPart editorPart, LineItem line, QuickTextQuery q) {
		int lineNumber = line.getLineNumber();
		if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
			return;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		IDocument document = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		if (document != null) {
			try {
				// line count internally starts with 0, and not with 1 like in GUI
				IRegion lineInfo = document.getLineInformation(lineNumber - 1);
				if (lineInfo != null) {
					//Select first match on a line or, if match not found, select whole line.
					int start = lineInfo.getOffset();
					int len = lineInfo.getLength();
					String lineText = document.get(start, len);
					TextRange match = q.findFirst(lineText);
					if (match!=null) {					
						start = start+match.start;
						len = match.len;
//						len = matches.get
					}
					editor.selectAndReveal(start, len);
				}
			} catch (BadLocationException e) {
				QuickSearchActivator.log(e);
			}
		}
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		doQuickSearch(window);
		return null;
	}

	public static void doQuickSearch(IWorkbenchWindow window) {
		try {
			QuickSearchDialog dialog = new QuickSearchDialog(window);
			initializeFromSelection(window, dialog);
			int code = dialog.open();
			if (code == QuickSearchDialog.OK) {
				LineItem selection = (LineItem) dialog.getFirstResult();
				QuickTextQuery q = dialog.getQuery();
				if (selection!=null) {
					IEditorPart editor = IDE.openEditor(window.getActivePage(), selection.getFile());
					goToLineAndSelectQuery(editor, selection, q);
				}
			}
		} catch (PartInitException e) {
			QuickSearchActivator.log(e);
		}
	}

	/**
	 * Based on the current active selection initialize the priority function and/or
	 * the initial contents of the search box.
	 */
	 static private void initializeFromSelection(IWorkbenchWindow workbench, QuickSearchDialog dialog) {
		if (workbench!=null) {
			ISelectionService selectionService = workbench.getSelectionService();
			ISelection selection = selectionService.getSelection();
			if (selection!=null && selection instanceof ITextSelection) {
				String text = ((ITextSelection) selection).getText();
				if (text!=null && !"".equals(text)) {
					dialog.setInitialPattern(text, QuickSearchDialog.FULL_SELECTION);
				}
			}
		}
//		IEditorPart editor = HandlerUtil.getActiveEditor(event);
//		if (editor!=null && editor instanceof ITextEditor) {
//			ITextEditor textEditor = (ITextEditor)editor;
//			ISelection selection = textEditor.getSelectionProvider().getSelection();
//			if (selection!=null && selection instanceof ITextSelection) {
//				String text = ((ITextSelection) selection).getText();
//				if (text!=null && !"".equals(text)) {
//					dialog.setInitialPattern(text, QuickSearchDialog.FULL_SELECTION);
//				}
//			}
//		}
	}
}
