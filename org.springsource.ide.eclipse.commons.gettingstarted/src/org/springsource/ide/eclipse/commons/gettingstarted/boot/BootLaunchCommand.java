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
package org.springsource.ide.eclipse.commons.gettingstarted.boot;

import static org.eclipse.debug.core.DebugEvent.CREATE;
import static org.eclipse.debug.core.DebugEvent.TERMINATE;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.ProjectFilter;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.SelectionUtils;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;

/**
 * Default handler for Spring boot launch command toolbar button.
 */
public class BootLaunchCommand extends AbstractHandler implements IElementUpdater {
	
	public static final ImageDescriptor RUN_ICON = ImageDescriptor.createFromURL(iconUrl("lrun_obj.gif"));
	public static final ImageDescriptor RESTART_ICON = ImageDescriptor.createFromURL(iconUrl("term_restart.gif"));
	
	private static URL iconUrl(String name) {
		try {
			URL baseUrl = GettingStartedActivator.getDefault().getBundle().getEntry("resources/icons");
			return new URL(baseUrl, name);
		} catch (MalformedURLException e) {
			GettingStartedActivator.log(e);
		}
		return null;
	}
	
	private static final String RUN_MODE = "run";
	public static final String COMMAND_ID = "org.springsource.ide.eclipse.boot.launch.command";
	
	
	ICommandService comandService = null;
	

	private IDebugEventSetListener debugListener;
	
	public BootLaunchCommand() {
		instances++;
		Assert.isTrue(instances==1);
		System.out.println("Registerin launch listener");
		
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
	}
	
	@Override
	public void dispose() {
		if (debugListener!=null) {
			DebugPlugin.getDefault().removeDebugEventListener(debugListener);
		}
		super.dispose();
	}
	
	/**
	 * @return Bootstrap enabled project associated with the launch or null.
	 */
	public static IProject getBootProject(ILaunch launch) {
		try {
			ILaunchConfiguration conf = launch.getLaunchConfiguration();
			String projectName = conf.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName!=null) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (BootPropertyTester.isBootProject(project)) {
					return project;
				}
			}
		} catch (CoreException e) {
			GettingStartedActivator.log(e);
		}
		return null;
	}
	
	private IProject currentProject = null;
	private ILaunch currentLaunch = null;
	private IProcess currentProcess = null;

	private ILaunch relaunch = null; //Set to non-null value to indicate an asynchronous request to relaunch.
									// the request will be processed after the currentProcess is terminated
	
	static int instances = 0;

	protected void handleDebugEvent(DebugEvent debugEvent) {
		int kind = debugEvent.getKind();
		switch (kind) {
		case CREATE:
			if (currentProcess==null && debugEvent.getSource() instanceof IProcess) {
				//We can only track one process. Ignore additional events.
				processCreated((IProcess)debugEvent.getSource());
			}
			break;
		case TERMINATE:
			if (currentProcess!=null && debugEvent.getSource()==currentProcess) {
				currentProcessTerminated();
			}
			break;
		default:
			break;
		}
	}
	
	private void processCreated(IProcess process) {
		ILaunch launch = process.getLaunch();
		IProject project = getBootProject(launch);
		if (project!=null) {
			currentProcess = process;
			currentLaunch = launch;
			currentProject = project;
		}
		changed();
	}
	
	private void changed() {
		if (comandService!=null) { 
			comandService.refreshElements(COMMAND_ID, null);
		}
	}

	private void currentProcessTerminated() {
		currentProcess = null;
		currentLaunch = null;
		currentProject = null;
		if (relaunch!=null) {
			ILaunchConfiguration conf = relaunch.getLaunchConfiguration();
			relaunch = null;
			DebugUITools.launch(conf, RUN_MODE);
		}
		changed();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (currentProcess==null) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			//This stinks but how else will we get a reference to command service
			// when we need it later to refresh the Icon when the process is started?
			comandService = (ICommandService)window.getService(ICommandService.class);
			start(event);
		} else {
			restart(event);
		}
		return null;
	}
	
	private void start(ExecutionEvent event) {
		IProject project = getBootProject(event);
		if (project!=null) {
			BootLaunchShortcut.launch(project, RUN_MODE);
		}
	}

	private void restart(ExecutionEvent event) {
		try {
			relaunch = currentLaunch; //Do this before calling terminate. We don't know if termination is asynch or not.
									// if synch then calling terminate may already trigger the terminate event so we must 
									// make note of the relaunch request beforehand!
			currentProcess.terminate();
		} catch (DebugException e) {
			GettingStartedActivator.log(e);
		}
	}
	
	private static final ProjectFilter isBootProject = new ProjectFilter() {
		@Override
		public boolean isAcceptable(IProject project) {
			return BootPropertyTester.isBootProject(project);
		}
	};

	/**
	 * Determine a spring-boot enabled project to run. 
	 * In any case, if there's only one such project in the workspace
	 * then that one will be used.
	 * @return 
	 */
	private IProject getBootProject(ExecutionEvent event) {
		ISelectionService selectionService = HandlerUtil.getActiveWorkbenchWindow(event).getSelectionService();
		ISelection selection = selectionService.getSelection();
		if (selection!=null) {
			List<IProject> projects = SelectionUtils.getProjects(selection, isBootProject);
			if (!projects.isEmpty()) {
				return projects.get(0);
			}
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		if (currentProcess==null) {
			element.setTooltip("Launch Boot App");
			element.setIcon(RUN_ICON);
		} else {
			String projectName = currentProject.getName();
			element.setIcon(RESTART_ICON);
			element.setTooltip("Restart "+projectName);
		}
	}


}
