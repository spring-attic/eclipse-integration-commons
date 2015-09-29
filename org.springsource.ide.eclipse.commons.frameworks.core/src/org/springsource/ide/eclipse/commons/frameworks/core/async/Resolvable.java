package org.springsource.ide.eclipse.commons.frameworks.core.async;

public interface Resolvable<T> {

	void resolve(T value);
	void reject(Exception e);

}
