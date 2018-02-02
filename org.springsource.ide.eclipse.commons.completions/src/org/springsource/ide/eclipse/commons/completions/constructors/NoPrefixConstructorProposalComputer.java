/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.constructors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.FillArgumentNamesCompletionProposalCollector;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.springsource.ide.eclipse.commons.completions.CompletionsActivator;
import org.springsource.ide.eclipse.commons.frameworks.core.async.ConstructorSearchValueProvider;

/**
 * Computes constructor proposals for the the case of "List<String> l = new <Ctrl-Space>", e.g. no prefix case
 * 
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class NoPrefixConstructorProposalComputer implements IJavaCompletionProposalComputer {
	
	private static final String NEW_KEYWORD = "new";
	private static final long JAVA_CODE_ASSIST_TIMEOUT= Long.getLong("org.eclipse.jdt.ui.codeAssistTimeout", 5000).longValue() ; // ms //$NON-NLS-1$
	
	private ConstructorSearchValueProvider constructorValueProvider = new ConstructorSearchValueProvider();

	@Override
	public void sessionStarted() {
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext jdtContext = (JavaContentAssistInvocationContext) context;

			try {

				String prefix =jdtContext.computeIdentifierPrefix().toString();
				/*
				 * No prefix, "new" keyword preceding and certain type expected? Compute constructors based on expected type.
				 */
				if (prefix.isEmpty() && isNewKeywordPreceeding(jdtContext) && jdtContext.getExpectedType() != null) {

					CompletableFuture<List<ICompletionProposal>> future = CompletableFuture.supplyAsync(() -> {
						List<ICompletionProposal> proposals = new ArrayList<>();
						proposals.addAll(Arrays.asList(doComputeCompletionProposals(jdtContext, monitor)));
						return proposals;
					});

					try {
						long timeout = JAVA_CODE_ASSIST_TIMEOUT;
						timeout -= (timeout / 4); // run with slightly less time then the timeout to avoid hitting that
						return future.get(timeout, TimeUnit.MILLISECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						e.printStackTrace();
					}
				}
			} catch (BadLocationException e) {
				CompletionsActivator.log(e);
			}
		}

		return Collections.emptyList();
	}
	
	/**
	 * Checks whether "new" keyword is preceding the content assist invocation position
	 * @param context
	 * @return
	 */
	private boolean isNewKeywordPreceeding(JavaContentAssistInvocationContext context) {
		IDocument document= context.getDocument();
		if (document == null)
			return false;
		int end = skipWhiteSpaceBackward(document, context.getInvocationOffset());
		if (end >= NEW_KEYWORD.length() - 1) {
			try {
				if (NEW_KEYWORD.equals(document.get(end - 2, NEW_KEYWORD.length()))) {
					end = end - 2;
					if (end == 0) {
						return true;
					} else {
						return !Character.isJavaIdentifierPart(document.getChar(end - 1));
					}
				}
			} catch (BadLocationException e) {
				// ignore, shouldn't happen
			}
		}
		return false;
	}
	
	private int skipWhiteSpaceBackward(IDocument document, int end) {
		int start = end;
		while (--start >= 0) {
			try {
				if (Character.isJavaIdentifierPart(document.getChar(start)))
					break;
			} catch (BadLocationException e) {
				// ignore cannot be bad location
			}
		}
		return start;
	}
	
	private IJavaCompletionProposal[] doComputeCompletionProposals(JavaContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		/*
		 * Setup completion proposals collector to gather constructors related proposals
		 */
		CompletionProposalCollector collector = createCollector(context);
		
		collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, false);
		collector.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, false);
		collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
		
		try {
			JavaProject javaProject = (JavaProject) context.getProject();
			SearchableEnvironment searchableEnvironment = new SearchableEnvironment(javaProject, new ICompilationUnit[] { context.getCompilationUnit() });
			org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit)context.getCompilationUnit();
			/*
			 * Create special completion engine to collect constructor proposals
			 */
			ConstructorCompletionEngine engine = new ConstructorCompletionEngine(cu, collector, searchableEnvironment, javaProject, DefaultWorkingCopyOwner.PRIMARY, constructorValueProvider, monitor);
			/*
			 * Gather completion proposals
			 */
			engine.complete(cu, context.getInvocationOffset(), null, context.getExpectedType());
		} catch (JavaModelException e) {
			CompletionsActivator.log(e);
		} catch (OperationCanceledException e) {
			/*
			 * Timeout has occurred. This proposal computer took too much time
			 */
			CompletionsActivator.log(new Status(IStatus.WARNING, CompletionsActivator.PLUGIN_ID, "Constructor completion proposal timed out", e));
		}
		return collector.getJavaCompletionProposals();
	}
	
	private CompletionProposalCollector createCollector(JavaContentAssistInvocationContext context) {
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES))
			return new FillArgumentNamesCompletionProposalCollector(context);
		else
			return new CompletionProposalCollector(context.getCompilationUnit(), true);
	}
	
	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {
	}

}
