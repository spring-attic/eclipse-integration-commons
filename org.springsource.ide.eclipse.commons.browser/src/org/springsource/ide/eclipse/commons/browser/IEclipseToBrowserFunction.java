package org.springsource.ide.eclipse.commons.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

/**
 * Allows insertion of generic html into an element.
 * 
 * @author Miles Parker
 * 
 */
public interface IEclipseToBrowserFunction {

	public interface Callback {
		public void ready(IEclipseToBrowserFunction function);
	}

	public abstract class Extension implements IEclipseToBrowserFunction, IExecutableExtension {

		String functionName;

		List<String> argumentIds = new ArrayList<String>();

		Map<String, String> literalArguments = new HashMap<String, String>();

		private Callback callback;

		public String getDynamicArgumentValue(String id) {
			return null;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void dispose() {
		}

		public void notifyIfReady() {
			if (callback != null) {
				callback.ready(this);
			}
		}

		public void addLiteralArgument(String id, String argument) {
			literalArguments.put(id, argument);
		}

		@Override
		public void setCallback(Callback client) {
			this.callback = client;
			notifyIfReady();
		}

		@Override
		public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
				throws CoreException {
			functionName = config.getAttribute(BrowserExtensions.ELEMENT_FUNCTION_NAME);
			IConfigurationElement[] arguments = config.getChildren(BrowserExtensions.ELEMENT_ARGUMENT);
			for (IConfigurationElement argumentElement : arguments) {
				String id = argumentElement.getAttribute("id");
				argumentIds.add(id);
				String dynamic = argumentElement.getAttribute(BrowserExtensions.ELEMENT_DYNAMIC);
				if (!dynamic.equals("true")) {
					String literal = argumentElement.getAttribute(BrowserExtensions.ELEMENT_LITERAL);
					literalArguments.put(id, literal);
				}
			}
			notifyIfReady();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springsource.ide.eclipse.commons.browser.
		 * IEclipseToBrowserFunctionCall#getFunctionCall(java.lang.String)
		 */
		@Override
		public String getFunctionName() {
			return functionName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springsource.ide.eclipse.commons.browser.
		 * IEclipseToBrowserFunctionCall#getArguments()
		 */
		@Override
		public String[] getArguments() {
			String[] arguments = new String[argumentIds.size()];
			int i = 0;
			for (String id : argumentIds) {
				String value = literalArguments.get(id);
				if (value == null) {
					value = getDynamicArgumentValue(id);
				}
				arguments[i++] = value;
			}
			return arguments;
		}
	}

	String getFunctionName();

	String[] getArguments();

	boolean isReady();

	void dispose();

	void setCallback(Callback callback);
}
