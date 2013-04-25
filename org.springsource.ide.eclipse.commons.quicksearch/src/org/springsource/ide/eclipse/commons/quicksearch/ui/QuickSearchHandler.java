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
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.internal.ui.text.EditorOpener;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
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
@SuppressWarnings("restriction")
public class QuickSearchHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public QuickSearchHandler() {
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
				LineItem item = (LineItem) dialog.getFirstResult();
				if (item!=null) {
					QuickTextQuery q = dialog.getQuery();
					TextRange range = q.findFirst(item.getText());
					EditorOpener opener = new EditorOpener();
					IWorkbenchPage page = window.getActivePage();
					if (page!=null) {
						opener.openAndSelect(page, item.getFile(), range.getOffset()+item.getOffset(), 
							range.getLength(), true);
					}
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
