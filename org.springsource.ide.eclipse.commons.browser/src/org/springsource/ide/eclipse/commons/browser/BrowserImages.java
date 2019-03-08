/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.browser;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.statushandlers.StatusManager;

public class BrowserImages {

	public static final String IMG_NAV_HOME = "IMG_NAV_HOME";

	// the image registry
	private static ImageRegistry imageRegistry;

	public static Image getImage(String key) {
		init();
		return imageRegistry.get(key);
	}

	public static ImageDescriptor getDescriptor(String key) {
		init();
		return imageRegistry.getDescriptor(key);
	}

	private synchronized static void init() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			img(IMG_NAV_HOME, "icons/home_16.png");
		}
	}

	private static void img(String key, String path) {
		try {
			URL url = Platform.getBundle(BrowserPlugin.PLUGIN_ID).getEntry(path);
			ImageDescriptor image = ImageDescriptor.createFromURL(url);
			imageRegistry.put(key, image);
		}
		catch (Throwable e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, BrowserPlugin.PLUGIN_ID, "Unexpected error.", e));
		}
	}

}
