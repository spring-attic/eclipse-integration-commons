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

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.springsource.ide.eclipse.commons.completions.CompletionsActivator;
import org.springsource.ide.eclipse.commons.completions.externaltype.indexing.ExternalTypeIndexer;

@SuppressWarnings("restriction")
public class ExternalTypeCompletionProposal extends LazyJavaTypeCompletionProposal {

	private ExternalTypeIndexer index;
	private ExternalType type;
	private IJavaProject project;
	
	private Image icon = null;

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

	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor mon) {
		ExternalTypeSource source = index.getSource(type);
		if (source!=null) {
			String info= source.getDescription();
			if (info != null && info.length() > 0) {
				StringBuffer buffer= new StringBuffer();
				HTMLPrinter.insertPageProlog(buffer, 0, getCSSStyles());

				buffer.append(info);

//				IJavaElement element= null;
//				try {
//					element= getProposalInfo().getJavaElement();
//					if (element instanceof IMember) {
//						String base= JavaDocLocations.getBaseURL(element, ((IMember) element).isBinary());
//						if (base != null) {
//							int endHeadIdx= buffer.indexOf("</head>"); //$NON-NLS-1$
//							buffer.insert(endHeadIdx, "\n<base href='" + base + "'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
//						}
//					}
//				} catch (JavaModelException e) {
//					JavaPlugin.log(e);
//				}

				HTMLPrinter.addPageEpilog(buffer);
				info= buffer.toString();

				return info;
				//return new JavadocBrowserInformationControlInput(null, element, info, 0);
			}
		}
		return null;
	}
	
	@Override
	protected Image computeImage() {
		try {
			if (icon==null) {
				ImageDescriptor descriptor = ImageDescriptor.createFromURL(
						new URL("platform:/plugin/org.springsource.ide.eclipse.commons.completions/resources/greyed-class.png")
				);
				icon = descriptor.createImage();
			}
		} catch (Exception e) {
			CompletionsActivator.log(e);
		}
		if (icon==null) {
			return super.computeImage();
		}
		return icon;
	}
	
}
