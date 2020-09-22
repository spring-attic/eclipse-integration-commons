package org.springsource.ide.eclipse.commons.boot.ls.remoteapps;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class ExentionPointLoader {


	@SuppressWarnings("unchecked")
	public static <T> List<T> load(Class<T> contributionType) {
		String extensionPointId = contributionType.getCanonicalName();
		List<T> constributions = new ArrayList<>();
		for (IConfigurationElement ce : Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId)) {
			try {
				Object contribution = ce.createExecutableExtension("class");
				if (!contributionType.isInstance(contribution)) {
					throw new IllegalStateException("Invalid contribution to extension point "+extensionPointId);
				} else {
					constributions.add((T) contribution);
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return constributions;
	}

}
