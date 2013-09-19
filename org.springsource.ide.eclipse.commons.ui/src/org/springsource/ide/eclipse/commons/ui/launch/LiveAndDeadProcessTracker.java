/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.launch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;

/**
 * A variation of LiveProcessTracker that retains launch configurations even
 * after all the associated processes have been terminated. (Thus allowing to
 * relaunch
 *
 * @author Kris De Volder
 */
public class LiveAndDeadProcessTracker extends LaunchList {

	private static LiveAndDeadProcessTracker instance;
	public synchronized static LaunchList getInstance() {
		if (instance==null) {
			instance = new LiveAndDeadProcessTracker();
		}
		return instance;
	}

	private final LinkedHashSet<ILaunchConfiguration> configs = new LinkedHashSet<ILaunchConfiguration>();
	private ILaunchConfiguration last = null;

	@Override
	protected void processTerminated(IProcess process) {
		//nothing... we keep the dead ones too!
	}

	@Override
	protected void processCreated(IProcess process) {
		synchronized (process) {
			ILaunch l = process.getLaunch();
			if (l!=null) {
				ILaunchConfiguration c = l.getLaunchConfiguration();
				if (c!=null) {
					last = c;
					configs.remove(c); //so the element moves to the end being now the 'most' recent.
					configs.add(c);
				}
			}
		}
		fireChangeEvent();
	}

	@Override
	public synchronized ILaunchConfiguration getLast() {
		return last;
	}

	@Override
	public synchronized Collection<ILaunchConfiguration> getLaunches() {
		//clean out invalid (i.e. deleted launch configs).
		Iterator<ILaunchConfiguration> iter = configs.iterator();
		return new ArrayList<ILaunchConfiguration>(configs);
	}

}
