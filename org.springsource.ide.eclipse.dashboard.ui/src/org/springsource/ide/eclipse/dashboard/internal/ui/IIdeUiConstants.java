/*******************************************************************************
 * Copyright (c) 2012 - 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.dashboard.internal.ui;

/**
 * @author Steffen Pingel
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 */
public interface IIdeUiConstants {

	public static final boolean DEFAULT_OPEN_DASHBOARD_STARTUP = true;
	public static final boolean DEFAULT_PREF_USE_OLD_DASHOARD = false;

	public static final String LABEL_SPRINGSOURCE_ANALYSES = "My Runtime Error Analyses";

	public static final String LABEL_SPRINGSOURCE_BUGS = "My SpringSource Issues";

	public static final String PREF_FEED_ENTRY_READ_STATE = IdeUiPlugin.PLUGIN_ID + "dashboard.feed.item.state";

	public static final String PREF_FEED_ENTRY_LAST_UPDATE_DISPLAYED = IdeUiPlugin.PLUGIN_ID + "dashboard.feed.last_update";

	public static final String PREF_OPEN_DASHBOARD_STARTUP = IdeUiPlugin.PLUGIN_ID + "dashboard.startup";
	public static final String PREF_USE_OLD_DASHOARD = IdeUiPlugin.PLUGIN_ID + "dashboard.use.old";
	public static final String PREF_UPDATE_DASHBOARD_NEWS_FEED = IdeUiPlugin.PLUGIN_ID + "dashboard.news.update";


	// public static final String PREF_LAST_READ_NEW_NOTEWORTHY_SIZE =
	// "com.springsource.sts.ide.ui.dashboard.lastReadNewAndNoteworthySize";

	public static final String QUERY_SPRINGSOURCE_ANALYSES = "https://issuetracker.springsource.com/secure/IssueNavigator.jspa?reset=true&type=5&pid=10000&reporterSelect=issue_current_user";

	public static final String QUERY_SPRINGSOURCE_BUGS = "https://issuetracker.springsource.com/secure/IssueNavigator.jspa?reset=true&pid=10010&reporterSelect=issue_current_user";

	public static final String SPRINGSOURCE_BUGS_COMPONENT = "Unknown";

	public static final String SPRINGSOURCE_BUGS_DESCRIPTION = "Steps to Reproduce:\n1.\n2.\n3.\n4.\n";

	public static final String SPRINGSOURCE_BUGS_ISSUE_TYPE = "Bug";

	public static final String SPRINGSOURCE_BUGS_PROJECT_NAME = "SpringSource Tool Suite";

	public final static String SPRINGSOURCE_BUGS_URL = "https://issuetracker.springsource.com";

	public static final String SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_COMPONENT = "Runtime Error Analysis";

	// public static final String
	// SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_PROJECT_KEY = "KB";

	public static final String SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_PROJECT_NAME = "Runtime Error Knowledge Base";

	public static final String SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_URL = "https://issuetracker.springsource.com";

	public static final String SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_VERSION = "1.0M3";

	public static final String SPRINGSOURCE_RUNTIME_ERROR_ANALYSIS_ISSUE_TYPE = "Analysis";

	public final static String SPRINGSOURCE_PORTAL_URL = ""; // "http://sts.staging6.springsource.com";

	public final static String SPRINGSOURCE_PORTAL_KIND = "net.covalent.support";

	public static final String SPRINGSOURCE_PORTAL_QUERY_LABEL = "My Support Incidents";

	public static final String SPRINGSOURCE_PORTAL_QUERY_URL = SPRINGSOURCE_PORTAL_URL;

	public static final String PREF_UPDATE_SECTION_COLLAPSE = IdeUiPlugin.PLUGIN_ID + "dashboard.update.collapse";

	public static final String PREF_TUTORIAL_SECTION_COLLAPSE = IdeUiPlugin.PLUGIN_ID + "dashboard.tutorial.collapse";

	public static final String PREF_HELP_SECTION_COLLAPSE = IdeUiPlugin.PLUGIN_ID + "dashboard.help.collapse";

	public static final int SECTION_COLLAPSED = 1;

	public static final int SECTION_EXPANDED = 2;

	public static final String PREF_IO_BLOGFEED_MIGRATION = "dashboard.feed.springio.migration";

}
