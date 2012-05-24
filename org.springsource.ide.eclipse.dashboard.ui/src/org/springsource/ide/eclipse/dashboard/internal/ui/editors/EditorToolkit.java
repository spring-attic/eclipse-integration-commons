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

import java.util.Iterator;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class EditorToolkit {

	private class ActionContributorProxy {

		public void contextMenuAboutToShow(IMenuManager manager, boolean addClipboard) {
		}

		public void forceActionsEnabled() {
		}

		public ISelectionChangedListener getSelectionChangedListener() {
			return null;
		}

		public void updateSelectableActions(ISelection selection) {
		}

	}

	private final FormToolkit toolkit;

	private final IEditorSite editorSite;

	private ActionContributorProxy contributor;

	public EditorToolkit(FormToolkit toolkit, IEditorSite site) {
		this.toolkit = toolkit;
		this.editorSite = site;
	}

	protected void configureContextMenuManager(Control control) {
		MenuManager manager = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		};
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(listener);
		control.setMenu(manager.createContextMenu(control));
	}

	protected void contextMenuAboutToShow(IMenuManager manager) {
		IEditorActionBarContributor contributor = editorSite.getActionBarContributor();
		if (contributor instanceof StsTaskEditorActionContributor) {
			((StsTaskEditorActionContributor) contributor).contextMenuAboutToShow(manager, true);
		}
		else if (contributor instanceof TaskEditorActionContributor) {
			((TaskEditorActionContributor) contributor).contextMenuAboutToShow(manager, true);
		}
	}

	public Hyperlink createHyperlink(Composite parent, final String hyperlinkText, final String url) {
		Hyperlink link = toolkit.createHyperlink(parent, hyperlinkText, SWT.NONE);
		link.addHyperlinkListener(new IHyperlinkListener() {

			public void linkActivated(HyperlinkEvent e) {
				TasksUiUtil.openUrl(url);
			}

			public void linkEntered(HyperlinkEvent e) {
				// Nothing to do
			}

			public void linkExited(HyperlinkEvent e) {
				// Nothing to do
			}
		});

		return link;
	}

	public Label createLabel(Composite composite, String text) {
		Label label = toolkit.createLabel(composite, text);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
		return label;
	}

	public TextViewer createTextEditor(Composite composite, String text, boolean spellCheck, int style) {
		AnnotationModel annotationModel = new AnnotationModel();
		final SourceViewer textViewer = new SourceViewer(composite, null, null, true, style);
		textViewer.showAnnotations(false);
		textViewer.showAnnotationsOverview(false);

		IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
		final SourceViewerDecorationSupport support = new SourceViewerDecorationSupport(textViewer, null,
				annotationAccess, EditorsUI.getSharedTextColors());
		@SuppressWarnings("unchecked")
		Iterator e = new MarkerAnnotationPreferences().getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			support.setAnnotationPreference((AnnotationPreference) e.next());
		}
		support.install(EditorsUI.getPreferenceStore());

		textViewer.getTextWidget().setIndent(2);
		textViewer.getTextWidget().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				support.uninstall();
			}
		});

		IThemeManager themeManager = editorSite.getWorkbenchWindow().getWorkbench().getThemeManager();
		textViewer.getTextWidget().setFont(
				themeManager.getCurrentTheme().getFontRegistry().get(CommonThemes.FONT_EDITOR_COMMENT));

		final ActionContributorProxy actionContributor = getContributor();
		if (actionContributor.getSelectionChangedListener() != null) {
			textViewer.addSelectionChangedListener(actionContributor.getSelectionChangedListener());
		}
		textViewer.getTextWidget().addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				actionContributor.updateSelectableActions(textViewer.getSelection());
			}

			public void focusLost(FocusEvent e) {
				StyledText st = (StyledText) e.widget;
				st.setSelectionRange(st.getCaretOffset(), 0);
				actionContributor.forceActionsEnabled();
			}
		});
		textViewer.addTextListener(new ITextListener() {
			public void textChanged(TextEvent event) {
				actionContributor.updateSelectableActions(textViewer.getSelection());
			}
		});

		Document document = new Document(text);
		StsTextViewerConfiguration viewerConfig = new StsTextViewerConfiguration(spellCheck, false);
		textViewer.configure(viewerConfig);
		textViewer.setDocument(document, annotationModel);

		EditorUtil.setTextViewer(textViewer.getControl(), textViewer);
		configureContextMenuManager(textViewer.getControl());
		return textViewer;
	}

	protected ActionContributorProxy getContributor() {
		if (contributor == null) {
			IEditorActionBarContributor editorContributor = editorSite.getActionBarContributor();
			if (editorContributor instanceof StsTaskEditorActionContributor) {
				final StsTaskEditorActionContributor actionContributor = (StsTaskEditorActionContributor) editorContributor;
				contributor = new ActionContributorProxy() {
					@Override
					public void contextMenuAboutToShow(IMenuManager manager, boolean addClipboard) {
						actionContributor.contextMenuAboutToShow(manager, addClipboard);
					}

					@Override
					public void forceActionsEnabled() {
						actionContributor.forceActionsEnabled();
					}

					@Override
					public ISelectionChangedListener getSelectionChangedListener() {
						return actionContributor;
					}

					@Override
					public void updateSelectableActions(ISelection selection) {
						actionContributor.updateSelectableActions(selection);
					}
				};
			}
			else if (editorContributor instanceof TaskEditorActionContributor) {
				final TaskEditorActionContributor actionContributor = (TaskEditorActionContributor) editorContributor;
				contributor = new ActionContributorProxy() {
					@Override
					public void contextMenuAboutToShow(IMenuManager manager, boolean addClipboard) {
						actionContributor.contextMenuAboutToShow(manager, addClipboard);
					}

					@Override
					public void forceActionsEnabled() {
						actionContributor.forceActionsEnabled();
					}

					@Override
					public ISelectionChangedListener getSelectionChangedListener() {
						return actionContributor;
					}

					@Override
					public void updateSelectableActions(ISelection selection) {
						actionContributor.updateSelectableActions(selection);
					}
				};
			}
			else {
				contributor = new ActionContributorProxy();
			}
		}
		return contributor;
	}

}
