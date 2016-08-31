/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.cloudfoundry.client;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;

public class RefreshTokenUtil {

	public static CloudCredentials credentialsFromRefreshToken(String _refreshToken) {
		DefaultOAuth2RefreshToken refreshToken = new DefaultOAuth2RefreshToken(_refreshToken);
		DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken("fake-token"); // we want it refreshed immediately so don't care
		token.setRefreshToken(refreshToken);
		return new CloudCredentials(token);
	}
	
}
