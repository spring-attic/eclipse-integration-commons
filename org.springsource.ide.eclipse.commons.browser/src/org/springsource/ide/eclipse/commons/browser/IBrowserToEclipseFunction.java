package org.springsource.ide.eclipse.commons.browser;

/**
 * Allows generic function calls from javafx embedded javascript call.
 * @author Miles Parker
 * 
 */
public interface IBrowserToEclipseFunction {

	public void call(String argument);
}
