package org.springsource.ide.eclipse.commons.completions;

import static org.eclipse.jdt.ui.PreferenceConstants.CODEASSIST_CATEGORY_ORDER;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
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

			Map<String,Integer> ranks = getCompletionProposalCategoryOrder();
			Integer existingRank = ranks.get(JAR_TYPE_SEARCH_CATEGORY_ID);
			if (existingRank != null) {
				if (existingRank>=0xFFFF) {
					//already disabled nothing to do
					return;
				} else {
					ranks.put(JAR_TYPE_SEARCH_CATEGORY_ID, 0xFFFF+existingRank);
				}
			} else {
				ranks.put(JAR_TYPE_SEARCH_CATEGORY_ID, 0xFFFF + 100);
			}

			setCompletionProposalCategoryOrder(ranks);
		} catch (Exception e) {
			CompletionsActivator.log(e);
		}
	}

	private static Map<String,Integer> getCompletionProposalCategoryOrder() {
		String encodedPreference= PreferenceConstants.getPreference(CODEASSIST_CATEGORY_ORDER, null);
		StringTokenizer tokenizer= new StringTokenizer(encodedPreference, "\0"); //$NON-NLS-1$
		int count = tokenizer.countTokens();
		Map<String, Integer> result = new HashMap<>();
		for (int i= 0; i < count; i++) {
			String pair = tokenizer.nextToken();
			String[] pieces = pair.split(":");
			result.put(pieces[0], Integer.parseInt(pieces[1]));
		}
		return result;
	}

	public static void setCompletionProposalCategoryOrder(Map<String, Integer> ranks) {
		Assert.isLegal(ranks != null);
		StringBuffer buf= new StringBuffer(50 * ranks.size());
		for (Entry<String, Integer> pair : ranks.entrySet()) {
			buf.append(pair.getKey()+":"+pair.getValue());
			buf.append('\0');
		}
		PreferenceConstants.getPreferenceStore().setValue(CODEASSIST_CATEGORY_ORDER, buf.toString());
		CompletionProposalComputerRegistry.getDefault().reload();
	}

}
