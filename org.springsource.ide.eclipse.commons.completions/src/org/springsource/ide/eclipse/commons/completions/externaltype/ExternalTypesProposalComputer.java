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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.springsource.ide.eclipse.commons.completions.CompletionsActivator;
import org.springsource.ide.eclipse.commons.completions.externaltype.indexing.ExternalTypeIndexer;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;

/**
 * Computes content assist proposals based on set of known types taken from a set of jars.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class ExternalTypesProposalComputer implements IJavaCompletionProposalComputer {

	private static final boolean DEBUG = false;// (""+Platform.getLocation()).contains("kdvolder");
	private void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	private static final List<IContextInformation> NO_CONTEXTS = Arrays.asList(new IContextInformation[0]);
	private static final List<ICompletionProposal> NO_PROPOSALS = Arrays.asList(new ICompletionProposal[0]);
	
	public ExternalTypesProposalComputer() {
	}

	@Override
	public void sessionStarted() {
		debug("sessionStarted");
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {
		debug("sessionEnded");
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext _context, IProgressMonitor monitor) {
		if (_context instanceof JavaContentAssistInvocationContext) {
			final JavaContentAssistInvocationContext context = (JavaContentAssistInvocationContext) _context;
			try {
				final String idPrefix = context.computeIdentifierPrefix().toString();
				//System.out.println("IdPrefix = "+idPrefix);
				//Only compute proposals if we actually have something to look for. This avoids adding our suggestions
				// for completions like 'foo.^".
				if (idPrefix!=null && !"".equals(idPrefix)) {
					final ArrayList<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
					final ExternalTypeIndexer index = ExternalTypeIndexManager.indexFor(context.getProject());
					index.getByPrefix(idPrefix, new Requestor<ExternalType>() {
						public boolean receive(ExternalType type) {
							//TODO: limit number of results. If there's too many user can't look
							// at all of them anyway.
							try {
								if (type.getName().startsWith(idPrefix)) {
									IJavaProject project = context.getProject();
									if (project.findType(type.getFullyQualifiedName())==null) {
										CompletionProposal proposal = CompletionProposal.create(CompletionProposal.TYPE_REF, context.getInvocationOffset());
										proposal.setCompletion(type.getFullyQualifiedName().toCharArray());
										proposal.setDeclarationSignature(type.getPackage().toCharArray());
										proposal.setFlags(Flags.AccPublic); //TODO: This is some kind of bit mask and it should be derived from the type information.
										//proposal.setRelevance(relevance);
										proposal.setReplaceRange(context.getInvocationOffset()-idPrefix.length(), context.getInvocationOffset());
										proposal.setSignature(Signature.createTypeSignature(type.getFullyQualifiedName(), true).toCharArray());
										completions.add(new ExternalTypeCompletionProposal(proposal, context, type, index));
									}
								}
							} catch (Exception e) {
								CompletionsActivator.log(e);
							}
							return true;
						}
					});
					return completions;
				}
			} catch (Exception e) {
				CompletionsActivator.log(e);
			}
		}
		return NO_PROPOSALS;
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		//This doesn't seem to get called so it doesn't really matter what we put in here.
		return NO_CONTEXTS;
	}

}
