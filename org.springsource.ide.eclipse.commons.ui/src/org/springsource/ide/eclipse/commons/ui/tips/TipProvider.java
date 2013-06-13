/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.tips;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.Platform;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * Provides a list of tips o'the day
 * @author Andrew Eisenberg
 * @since 3.3.0
 */
public class TipProvider {

	private static final Random RAND = new Random();

	private TipInfo[] tips;

	private int pointer;

	private Exception error;

	public TipProvider() {
		refresh();
	}

	public void refresh() {
		error = null;
		InputStreamReader tipReader = new InputStreamReader(TipProvider.class.getClassLoader().getResourceAsStream(
				"org/springsource/ide/eclipse/commons/ui/tips/tip_of_the_day.json"));
		JSONTokener tokener = new JSONTokener(tipReader);
		List<TipInfo> tipList = Collections.emptyList();
		try {
			JSONArray json = new JSONArray(tokener);
			int length = json.length();
			tipList = new ArrayList<TipInfo>(length);
			for (int i = 0; i < length; i++) {
				JSONObject object = (JSONObject) json.get(i);
				if (object.has("required")) {
					String requiredPlugin = object.getString("required");
					if (Platform.getBundle(requiredPlugin) == null) {
						continue;
					}
				}
				tipList.add(new TipInfo(object.getString("infoText"), object.getString("linkText"), object
						.has("keyBindingId") ? object.getString("keyBindingId") : null));
			}
		}
		catch (JSONException e) {
			CorePlugin.log("Error parsing tips of the day file", e);
			error = e;
		}

		if (tipList.size() == 0) {
			tips = new TipInfo[] { new TipInfo("There's a lot going on with Spring",
					"Read the latest <a href\"http://www.springsource.org/\">Spring news</a>.") };
		}
		else {
			tips = tipList.toArray(new TipInfo[tipList.size()]);
		}

		pointer = RAND.nextInt(tips.length);
	}

	public TipInfo nextTip() {
		return tips[(++pointer % tips.length)];
	}

	public TipInfo previousTip() {
		if (pointer == 0) {
			pointer = tips.length;
		}
		return tips[(--pointer % tips.length)];
	}

	public Exception getError() {
		return error;
	}
}
