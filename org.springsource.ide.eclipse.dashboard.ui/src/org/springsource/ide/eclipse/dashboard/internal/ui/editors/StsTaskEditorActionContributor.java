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
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class StsTaskEditorActionContributor extends MultiPageEditorActionBarContributor implements
		ISelectionChangedListener {

	private class GlobalAction extends Action {

		private final String actionId;

		public GlobalAction(String actionId) {
			this.actionId = actionId;
		}

		@Override
		public void run() {
			if (getEditor().getActivePageInstance() instanceof AbstractFormPage) {
				AbstractFormPage page = (AbstractFormPage) getEditor().getActivePageInstance();
				page.doAction(actionId);
				updateSelectableActions(getEditorSelection());
			}
		}

		public void selectionChanged(ISelection selection) {
			if (getEditor().getActivePageInstance() instanceof AbstractFormPage) {
				AbstractFormPage page = (AbstractFormPage) getEditor().getActivePageInstance();
				setEnabled(page.canPerformAction(actionId));
			}
		}
	}

	private SubActionBars sourceActionBars;

	private FormEditor editor;

	private final GlobalAction cutAction;

	private final GlobalAction undoAction;

	private final GlobalAction redoAction;

	private final GlobalAction copyAction;

	private final GlobalAction pasteAction;

	private final GlobalAction selectAllAction;

	public StsTaskEditorActionContributor() {
		cutAction = new GlobalAction(ActionFactory.CUT.getId());
		cutAction.setText(WorkbenchMessages.Workbench_cut);
		cutAction.setToolTipText(WorkbenchMessages.Workbench_cutToolTip);
		cutAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		cutAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		cutAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
		cutAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.CUT);

		pasteAction = new GlobalAction(ActionFactory.PASTE.getId());
		pasteAction.setText(WorkbenchMessages.Workbench_paste);
		pasteAction.setToolTipText(WorkbenchMessages.Workbench_pasteToolTip);
		pasteAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteAction.setDisabledImageDescriptor(WorkbenchImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		pasteAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.PASTE);

		copyAction = new GlobalAction(ActionFactory.COPY.getId());
		copyAction.setText(WorkbenchMessages.Workbench_copy);
		copyAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		copyAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);

		undoAction = new GlobalAction(ActionFactory.UNDO.getId());
		undoAction.setText(WorkbenchMessages.Workbench_undo);
		undoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		undoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		undoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
		undoAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.UNDO);

		redoAction = new GlobalAction(ActionFactory.REDO.getId());
		redoAction.setText(WorkbenchMessages.Workbench_redo);
		redoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		redoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		redoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO_DISABLED));
		redoAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.REDO);

		selectAllAction = new GlobalAction(ActionFactory.SELECT_ALL.getId());
		selectAllAction.setText(WorkbenchMessages.Workbench_selectAll);
		selectAllAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.SELECT_ALL);
		selectAllAction.setEnabled(true);
	}

	public void addClipboardActions(IMenuManager manager) {
		manager.add(undoAction);
		manager.add(redoAction);
		manager.add(new Separator());
		manager.add(cutAction);
		manager.add(copyAction);
		manager.add(pasteAction);
		manager.add(selectAllAction);
		manager.add(new Separator());
	}

	public void contextMenuAboutToShow(IMenuManager mng) {
		boolean addClipboard = this.getEditor().getActivePageInstance() != null
				&& (this.getEditor().getActivePageInstance() instanceof AbstractFormPage);
		contextMenuAboutToShow(mng, addClipboard);
	}

	public void contextMenuAboutToShow(IMenuManager manager, boolean addClipboard) {
		if (editor != null) {
			updateSelectableActions(getEditorSelection());
		}

		if (addClipboard) {
			addClipboardActions(manager);
		}
	}

	@Override
	public void contributeToCoolBar(ICoolBarManager cbm) {
	}

	@Override
	public void contributeToMenu(IMenuManager mm) {
	}

	@Override
	public void contributeToStatusLine(IStatusLineManager slm) {
	}

	@Override
	public void contributeToToolBar(IToolBarManager tbm) {
	}

	@Override
	public void dispose() {
		sourceActionBars.dispose();
		super.dispose();
	}

	public void forceActionsEnabled() {
		cutAction.setEnabled(true);
		copyAction.setEnabled(true);
		pasteAction.setEnabled(true);
		selectAllAction.setEnabled(true);
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
	}

	public FormEditor getEditor() {
		return editor;
	}

	public ISelection getEditorSelection() {
		if (editor != null && editor.getSite().getSelectionProvider() != null) {
			return editor.getSite().getSelectionProvider().getSelection();
		}
		else {
			return StructuredSelection.EMPTY;
		}
	}

	public IStatusLineManager getStatusLineManager() {
		return getActionBars().getStatusLineManager();
	}

	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		sourceActionBars = new SubActionBars(bars);
	}

	@Override
	public void init(IActionBars bars, IWorkbenchPage page) {
		super.init(bars, page);
		registerGlobalHandlers(bars);

	}

	public void registerGlobalHandlers(IActionBars bars) {
		bars.setGlobalActionHandler(ActionFactory.CUT.getId(), cutAction);
		bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);
		bars.updateActionBars();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		updateSelectableActions(event.getSelection());
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof FormEditor) {
			editor = (FormEditor) targetEditor;
			updateSelectableActions(getEditorSelection());
		}
	}

	@Override
	public void setActivePage(IEditorPart newEditor) {
		if (getEditor() != null) {
			updateSelectableActions(getEditorSelection());
		}
	}

	public void unregisterGlobalHandlers(IActionBars bars) {
		bars.setGlobalActionHandler(ActionFactory.CUT.getId(), null);
		bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), null);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), null);
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), null);
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(), null);
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), null);
		bars.updateActionBars();
	}

	public void updateSelectableActions(ISelection selection) {
		if (editor != null) {
			cutAction.selectionChanged(selection);
			copyAction.selectionChanged(selection);
			pasteAction.selectionChanged(selection);
			undoAction.selectionChanged(selection);
			redoAction.selectionChanged(selection);
			selectAllAction.selectionChanged(selection);
		}
	}

}
