/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.completions.constructors;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.complete.CompletionNodeFound;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.codeassist.complete.InvalidCursorLocation;
import org.eclipse.jdt.internal.codeassist.impl.Engine;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.HierarchyScope;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessConstructorRequestor;
import org.springsource.ide.eclipse.commons.completions.CompletionsActivator;

/**
 * Completion Engine that wraps JDT CompletionEngine to collect completion
 * proposals for constructors of specific type. Reflection is heavily used to
 * tweak wrapped JDT CompletionEngine to work with our Java index search
 * results.
 * 
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class ConstructorCompletionEngine {
	
	/**
	 * Wrapped JDT completion engine 
	 */
	private CompletionEngine engine;
		
	public ConstructorCompletionEngine(ICompilationUnit compilationUnit,
	CompletionRequestor requestor,
	SearchableEnvironment searchableEnv,
	JavaProject javaProject,
	WorkingCopyOwner owner,
	IProgressMonitor monitor) {
		this.engine = new CompletionEngine(searchableEnv, requestor, javaProject.getOptions(true), javaProject, DefaultWorkingCopyOwner.PRIMARY,
		monitor);
	}

	
	/**
	 * Collects constructor related completion proposals when there is no
	 * constructor prefix and expected type is set
	 * 
	 * @param sourceUnit
	 *            Compilation unit
	 * @param pos
	 *            Content Assist invocation offset
	 * @param root
	 *            Root type
	 * @param expectedType
	 *            Expected type
	 */
	public void complete(ICompilationUnit sourceUnit, int pos, ITypeRoot root, final IType expectedType) {

		getEngineFieldValue("requestor", CompletionRequestor.class).beginReporting();
		try {
			setEngineFieldValue("fileName", sourceUnit.getFileName());
			setEngineFieldValue("offset", 0);
			setEngineFieldValue("actualCompletionPosition", pos - 1);
			setEngineFieldValue("typeRoot", root);
			
			// for now until we can change the UI.
			CompilationResult result = new CompilationResult(sourceUnit, 1, 1, engine.compilerOptions.maxProblemsPerUnit);
			CompilationUnitDeclaration parsedUnit = ((CompletionParser)engine.getParser()).dietParse(sourceUnit, result, pos - 1);

			/*
			 * Skip imports and package related code since it shouldn't be applicable in this use case.
			 * Perform parsing similarly to JDT CompletionEngine 
			 */
			if (parsedUnit != null) {

				if (parsedUnit.types != null) {
					try {
						engine.lookupEnvironment.buildTypeBindings(parsedUnit, null /*no access restriction*/);

						setEngineFieldValue("unitScope", parsedUnit.scope);
						if (parsedUnit.scope != null) {
							setEngineFieldValue("source", sourceUnit.getContents());
							engine.lookupEnvironment.completeTypeBindings(parsedUnit, true);
							parsedUnit.scope.faultInTypes();
							parseBlockStatements(parsedUnit, pos - 1);
							parsedUnit.resolve();
						}
					} catch (CompletionNodeFound e) {
						//					completionNodeFound = true;
						if (e.astNode != null) {
							// if null then we found a problem in the completion node
							engine.lookupEnvironment.unitBeingCompleted = parsedUnit; // better resilient to further error reporting
							try {
								// Completion proposals should be provided. Search for possible completions ourselves
								complete(e.astNode, ((CompletionParser) engine.getParser()).assistNodeParent,
										parsedUnit, e.qualifiedBinding, e.scope, expectedType,
										getEngineFieldValue("monitor", IProgressMonitor.class));
							} catch (JavaModelException e1) {
								CompletionsActivator.log(e1.getStatus());
							}
						}
					}
				}
			}

		} catch (IndexOutOfBoundsException | InvalidCursorLocation | AbortCompilation | CompletionNodeFound e) { // work-around internal failure - 1GEMF6D
			CompletionsActivator.log(e);
		} finally {
			getEngineFieldValue("requestor", CompletionRequestor.class).endReporting();
			if (getEngineFieldValue("monitor", IProgressMonitor.class) != null) getEngineFieldValue("monitor", IProgressMonitor.class).done();
			try {
				Method method = CompletionEngine.class.getDeclaredMethod("reset");
				method.setAccessible(true);
				method.invoke(engine);
			} catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				CompletionsActivator.log(e);
			}
		}
	}
	
	/**
	 * Sets the value on a specified field of JDT CompletionEngine
	 * @param name The name of the field
	 * @param value The value for the field
	 */
	private void setEngineFieldValue(String name, Object value) {
		try {
			Field field = null;
			try {		
				field = engine.getClass().getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				field = Engine.class.getDeclaredField(name);
			}
			field.setAccessible(true);
			field.set(engine, value);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			CompletionsActivator.log(e);
		}
	}
	
	/**
	 * Gets the value of a specified field of JDT CompletionEngine
	 * @param name The name of the field
	 * @param clazz The type of the value
	 * @return The value of the field
	 */
	@SuppressWarnings("unchecked")
	private <T> T getEngineFieldValue(String name, Class<T> clazz) {
		try {
			Field field = engine.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(engine);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			CompletionsActivator.log(e);
		}
		return null;
	}

	/**
	 * Delegates to JDT CompletionEngine#parseBlockStatements() method via reflection
	 */
	private ASTNode parseBlockStatements(CompilationUnitDeclaration unit, int position) {
		try {
			Method method = Engine.class.getDeclaredMethod("parseBlockStatements", CompilationUnitDeclaration.class, int.class);
			method.setAccessible(true);
			return (ASTNode) method.invoke(engine, unit, position);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			CompletionsActivator.log(e);
		}
		return null;
	}
	
	/**
	 * Searches and collects completion proposals for constructors
	 * 
	 * @param astNode
	 * @param astNodeParent
	 * @param compilationUnitDeclaration
	 * @param qualifiedBinding
	 * @param scope
	 * @param expectedType
	 * @param monitor
	 * @throws JavaModelException
	 */
	private void complete(ASTNode astNode, 
			ASTNode astNodeParent, 
			CompilationUnitDeclaration compilationUnitDeclaration,
			Binding qualifiedBinding,
			Scope scope,
			IType expectedType,
			IProgressMonitor monitor) throws JavaModelException {
		try {
			/*
			 * Perform setup procedure below to init various fields on the JDT
			 * CompletionEngine to be able to correctly collect the completion
			 * results. See same named method on JDT CompletionEngine
			 */
			setEngineFieldValue("completionToken", new char[0]);

			Method m = engine.getClass().getDeclaredMethod("setSourceAndTokenRange", int.class, int.class);
			m.setAccessible(true);
			m.invoke(engine, astNode.sourceStart, astNode.sourceEnd);
	
			m = engine.getClass().getDeclaredMethod("computeForbiddenBindings", ASTNode.class, ASTNode.class, Scope.class);
			m.setAccessible(true);
			scope = (Scope) m.invoke(engine, astNode, astNodeParent, scope);
			
			m = engine.getClass().getDeclaredMethod("computeUninterestingBindings", ASTNode.class, ASTNode.class, Scope.class);
			m.setAccessible(true);
			m.invoke(engine, astNode, astNodeParent, scope);
			
			m = engine.getClass().getDeclaredMethod("buildContext", ASTNode.class, ASTNode.class, CompilationUnitDeclaration.class, Binding.class, Scope.class);
			m.setAccessible(true);
			m.invoke(engine, astNode, astNodeParent, compilationUnitDeclaration, qualifiedBinding, scope);
	
			/*
			 * Perform our own search for possible constructors on a hierarchy scope
			 */
			IJavaSearchScope hierarchyScope = new HierarchyScope(getEngineFieldValue("javaProject", IJavaProject.class), expectedType,  DefaultWorkingCopyOwner.PRIMARY, true, false, false);
			BasicSearchEngine basicEngine = new BasicSearchEngine();
			/*
			 * Search term is '*' meaning everything, i.e. any prefix
			 */
			basicEngine.searchAllConstructorDeclarations(null, "*".toCharArray(), SearchPattern.R_PATTERN_MATCH,
					hierarchyScope, new IRestrictedAccessConstructorRequestor() {
	
						@Override
						public void acceptConstructor(int modifiers, char[] simpleTypeName, int parameterCount,
								char[] signature, char[][] parameterTypes, char[][] parameterNames, int typeModifiers,
								char[] packageName, int extraFlags, String path, AccessRestriction access) {
							engine.acceptConstructor(modifiers, simpleTypeName, parameterCount, signature, parameterTypes, parameterNames, typeModifiers, packageName, extraFlags, path, access);
						}
	
					}, IJavaSearchConstants.FORCE_IMMEDIATE_SEARCH, monitor);
		
			/*
			 * Use JDT CompletionEngine to process our search results, create core completion proposals and then collect them
			 */
			m = engine.getClass().getDeclaredMethod("acceptConstructors", Scope.class);
			if (m != null) {
				m.setAccessible(true);
				m.invoke(engine, scope);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			CompletionsActivator.log(e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof OperationCanceledException) {
				throw (OperationCanceledException) e.getCause();
			} else {
				CompletionsActivator.log(e);
			}
		}
	}

}
