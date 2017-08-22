package org.springsource.ide.eclipse.commons.browser;

/**
 * Allows generic function calls from StsBrowser embedded javascript call.
 *
 * @author Miles Parker
 * @author Kris De Volder
 */
public interface IBrowserToEclipseFunction {

	public void call(String argument);
}
