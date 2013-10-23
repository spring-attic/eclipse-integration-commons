/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.externaltype;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.springsource.ide.eclipse.commons.completions.CompletionsActivator;
import org.springsource.ide.eclipse.commons.completions.externaltype.indexing.ExternalTypeIndexer;

@SuppressWarnings("restriction")
public class ExternalTypeCompletionProposal extends LazyJavaTypeCompletionProposal {

	private ExternalTypeIndexer index;
	private ExternalType type;
	private IJavaProject project;

	public ExternalTypeCompletionProposal(CompletionProposal proposal, JavaContentAssistInvocationContext context, ExternalType et, ExternalTypeIndexer index) {
		super(proposal, context);
		this.index = index;
		this.type = et;
		this.project = context.getProject();
	}

	public void apply(IDocument document, char trigger, int offset) {
		try {
			super.apply(document, trigger, offset);
			IType existingType = project.findType(type.getFullyQualifiedName());
			if (existingType==null) {
				index.getSource(type).addToClassPath(project, new NullProgressMonitor());
			}
		} catch (Exception e) {
			CompletionsActivator.log(e);
		}
	}

}
