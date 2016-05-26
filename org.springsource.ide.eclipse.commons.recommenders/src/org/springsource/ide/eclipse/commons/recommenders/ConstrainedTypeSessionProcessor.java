package org.springsource.ide.eclipse.commons.recommenders;

import static org.eclipse.jdt.core.CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION;
import static org.eclipse.jdt.core.CompletionProposal.CONSTRUCTOR_INVOCATION;
import static org.eclipse.jdt.core.CompletionProposal.TYPE_REF;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;

public class ConstrainedTypeSessionProcessor extends SessionProcessor {

    @Override
    public boolean startSession(IRecommendersCompletionContext context) {
        IType type = context.getExpectedType().orNull();
        if (type != null) {
            Set<ITypeName> types = new HashSet<>();
            addType(types, type);
            try {
                for (IType subtype : type.newTypeHierarchy(context.getProject(), new NullProgressMonitor())
                        .getAllSubtypes(type)) {
                    addType(types, subtype);
                }
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
            context.getProposals().values().removeIf(v -> !isAcceptableProposal(v, types));
            return true;
        }
        return super.startSession(context);
    }

    private static void addType(Set<ITypeName> types, IType type) {
        String key = type.getKey();
        int index = key.lastIndexOf(';');
        try {
            types.add(VmTypeName.get(index < 0 ? key : key.substring(0, index)));
        } catch (IllegalArgumentException e) {
            // ignore non-static inner classes that cannot be parsed
        }
    }

    private static boolean isAcceptableProposal(CompletionProposal compilerProposal, Set<ITypeName> types) {
        if (compilerProposal.getKind() == CONSTRUCTOR_INVOCATION || compilerProposal.getKind() == TYPE_REF
                || compilerProposal.getKind() == ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION) {
            char[] signature = compilerProposal.getKind() == TYPE_REF ? compilerProposal.getSignature()
                    : compilerProposal.getDeclarationSignature();

            if (Signature.getArrayCount(signature) > 0) {
                // No support for the subtype relation amongst array types yet.
                return true;
            }
            if (signature.length == 1) {
                // no support for primitive or void types
                return true;
            }

            Set<ITypeName> typeNames = RecommendersCompletionContext.createTypeNamesFromSignatures(new char[][] { signature });

            // There should be only one entry in the typeNames set anyway
            return types.containsAll(typeNames);
        }
        return true;
    }

}
