/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.wizard.guides.boot;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;

/** 
 * LiveExpression that computes a URL String based on a number of input fields.
 */
public class UrlMaker extends LiveExpression<String> {

	List<StringFieldModel> inputs = new ArrayList<StringFieldModel>();
	private LiveExpression<String> baseUrl;
	
	public UrlMaker(String baseUrl) {
		this(LiveExpression.constant(baseUrl));
	}
	
	public UrlMaker(LiveExpression<String> baseUrl) {
		this.baseUrl = baseUrl;
		dependsOn(baseUrl);
	}

	public UrlMaker addField(StringFieldModel field) {
		inputs.add(field);
		dependsOn(field.getVariable()); //Recompute my value when the input changes.
		return this;
	}
	
	@Override
	protected String compute() {
		String baseUrl = this.baseUrl.getValue();
		if (baseUrl==null) {
			baseUrl = "";
		} else {
			baseUrl = baseUrl.trim();
		}
		try {
			URIBuilder builder = new URIBuilder(baseUrl);
			for (StringFieldModel f : inputs) {
				String paramValue = f.getValue();
				if (paramValue!=null) {
					builder.addParameter(f.getName(), paramValue);
				}
			}
			return builder.toString();
		} catch (URISyntaxException e) {
			//most likely baseUrl is unparseable. Can't add params then.
			GettingStartedActivator.log(e);
			return baseUrl;
		}
	}

}
