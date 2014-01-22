/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration;

/**
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class StsTextViewerConfiguration extends RepositoryTextViewerConfiguration {

	private final boolean showHyperlinks;

	public StsTextViewerConfiguration(boolean checkSpelling, boolean showHyperlinks) {
		super(null, checkSpelling);
		this.showHyperlinks = showHyperlinks;
	}

	@Override
	public IHyperlinkPresenter getHyperlinkPresenter(final ISourceViewer sourceViewer) {
		if (showHyperlinks) {
			return super.getHyperlinkPresenter(sourceViewer);
		}
		return null;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		if (showHyperlinks) {
			return super.getPresentationReconciler(sourceViewer);
		}
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new RuleBasedScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

}
