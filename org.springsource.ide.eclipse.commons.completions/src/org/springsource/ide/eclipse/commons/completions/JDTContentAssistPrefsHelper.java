package org.springsource.ide.eclipse.commons.completions;

import static org.eclipse.jdt.ui.PreferenceConstants.CODEASSIST_CATEGORY_ORDER;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;

@SuppressWarnings("restriction")
public class JDTContentAssistPrefsHelper {

	private static final String JAR_TYPE_SEARCH_CATEGORY_ID = "org.springsource.ide.eclipse.commons.completions.externalTypesProposalCategory";

	/**
	 * Modify JDT content assist preferences to disable Jar Type Search completely
	 */
	public static void disableJarTypeSearch() {
		try {
			String[] _excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
			LinkedHashSet<String> excluded = new LinkedHashSet<String>();
			if (_excluded!=null) {
				excluded.addAll(Arrays.asList(_excluded));
			}
			excluded.add(JAR_TYPE_SEARCH_CATEGORY_ID);
			PreferenceConstants.setExcludedCompletionProposalCategories(excluded.toArray(new String[excluded.size()]));

			String[] _order = getCompletionProposalCategoryOrder();
			LinkedHashSet<String> order = new LinkedHashSet<>();
			if (_order!=null) {
				order.addAll(Arrays.asList(_order));
				order.remove(JAR_TYPE_SEARCH_CATEGORY_ID);
				setCompletionProposalCategoryOrder(order.toArray(new String[order.size()]));
			}
		} catch (Exception e) {
			CompletionsActivator.log(e);
		}
	}

	private static String[] getCompletionProposalCategoryOrder() {
		String encodedPreference= PreferenceConstants.getPreference(CODEASSIST_CATEGORY_ORDER, null);
		StringTokenizer tokenizer= new StringTokenizer(encodedPreference, "\0"); //$NON-NLS-1$
		String[] result= new String[tokenizer.countTokens()];
		for (int i= 0; i < result.length; i++)
			result[i]= tokenizer.nextToken();
		return result;
	}

	/**
	 * Sets the completion proposal categories which are excluded from the
	 * default proposal list and reloads the registry.
	 *
	 * @param categories the array with the IDs of the excluded categories
	 * @see #CODEASSIST_EXCLUDED_CATEGORIES
	 * @since 3.4
	 */
	public static void setCompletionProposalCategoryOrder(String[] categories) {
		Assert.isLegal(categories != null);
		StringBuffer buf= new StringBuffer(50 * categories.length);
		for (int i= 0; i < categories.length; i++) {
			buf.append(categories[i]);
			buf.append('\0');
		}
		PreferenceConstants.getPreferenceStore().setValue(CODEASSIST_CATEGORY_ORDER, buf.toString());
		CompletionProposalComputerRegistry.getDefault().reload();
	}

}
