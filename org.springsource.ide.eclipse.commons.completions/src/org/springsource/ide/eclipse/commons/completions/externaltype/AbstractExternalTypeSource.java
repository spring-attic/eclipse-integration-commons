package org.springsource.ide.eclipse.commons.completions.externaltype;

/**
 * Abstract base class to make it easier to implement an ExternalTypeSource
 * 
 * @author Kris De Volder
 */
public abstract class AbstractExternalTypeSource implements ExternalTypeSource {

	/**
	 * Default implementation provides null description. Override to provide
	 * something more interesting.
	 */
	@Override
	public String getDescription() {
		return null;
	}
	
}

