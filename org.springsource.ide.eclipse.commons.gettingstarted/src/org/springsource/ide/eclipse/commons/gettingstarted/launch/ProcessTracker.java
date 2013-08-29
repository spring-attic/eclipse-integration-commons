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
package org.springsource.ide.eclipse.commons.gettingstarted.launch;

import static org.eclipse.debug.core.DebugEvent.CREATE;
import static org.eclipse.debug.core.DebugEvent.TERMINATE;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

/**
 * An instance of this class tracks running processes by attaching to the eclipse debugger.
 * 
 * @author Kris De Volder
 */
public class ProcessTracker {
	
	private LinkedList<IProcess> processes = new LinkedList<IProcess>();
	private IDebugEventSetListener debugListener;
	
	public ProcessTracker() {
		//Pick up any processes already running
		DebugPlugin.getDefault().addDebugEventListener(debugListener = new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				if (events!=null) {
					for (DebugEvent debugEvent : events) {
						handleDebugEvent(debugEvent);
					}
				}
			};
		});
		
		//What if processes got started before we attached the listener?
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		if (launches!=null) {
			for (ILaunch launch : launches) {
				for (IProcess process : launch.getProcesses()) {
					processCreated(process);
				}
			}
		}
	}
	
	public void dispose() {
		if (debugListener!=null) {
			DebugPlugin.getDefault().removeDebugEventListener(debugListener);
			debugListener = null;
		}
	}
	
	
	protected void handleDebugEvent(DebugEvent debugEvent) {
		int kind = debugEvent.getKind();
		switch (kind) {
		case CREATE:
			if (debugEvent.getSource() instanceof IProcess) {
				//We can only track one process. Ignore additional events.
				processCreated((IProcess)debugEvent.getSource());
			}
			break;
		case TERMINATE:
			if (debugEvent.getSource() instanceof IProcess) {
				processTerminated((IProcess)debugEvent.getSource());
			}
			break;
		default:
			break;
		}
	}
	
	protected void processTerminated(IProcess process) {
		boolean changed;
		synchronized (this) {
			changed = processes.remove(process);
		}
		if (changed) {
			changed();
		}
	}

	protected void processCreated(IProcess process) {
		boolean changed = false;
		synchronized (this) {
			if (!processes.contains(process)) {
				processes.add(process);
				changed = true;
			}
		}
		if (changed) {
			changed();
		}
	}
	
	public synchronized IProcess getLast() {
		if (!processes.isEmpty()) {
			return processes.getLast();
		}
		return null;
	}
	
	protected void changed() {
	}

	public synchronized Collection<ILaunch> getLaunches() {
		LinkedHashSet<ILaunch> launches = new LinkedHashSet<ILaunch>();
		for (IProcess p : processes) {
			launches.add(p.getLaunch());
		}
		return launches;
	}
	

}
