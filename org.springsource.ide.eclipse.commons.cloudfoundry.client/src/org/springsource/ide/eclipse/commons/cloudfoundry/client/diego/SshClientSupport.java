/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.cloudfoundry.client.diego;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryException;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * @author Kris De Volder
 */
public class SshClientSupport extends CfClientSideCart {

	private String authorizationUrl;
	private String sshClientId;

	public SshClientSupport(CloudFoundryOperations client, CloudInfoV2 cloudInfo, boolean trustSelfSigned, HttpProxyConfiguration httpProxyConfiguration) {
		super(client, cloudInfo, trustSelfSigned, httpProxyConfiguration);
		this.authorizationUrl = cloudInfo.getAuthorizationUrl();
		this.sshClientId = cloudInfo.getSshClientId();
	}

	public String getSshCode() {
		try {
			URIBuilder builder = new URIBuilder(authorizationUrl + "/oauth/authorize"); //$NON-NLS-1$

			builder.addParameter("response_type" //$NON-NLS-1$
					, "code"); //$NON-NLS-1$
			builder.addParameter("grant_type", //$NON-NLS-1$
					"authorization_code"); //$NON-NLS-1$
			builder.addParameter("client_id", sshClientId); //$NON-NLS-1$

			URI url = new URI(builder.toString());

			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode!=HttpStatus.FOUND) {
				throw new CloudFoundryException(statusCode);
			}

			String loc = response.getHeaders().getFirst("Location"); //$NON-NLS-1$
			if (loc==null) {
				throw new CloudOperationException("No 'Location' header in redirect response"); //$NON-NLS-1$
			}
			List<NameValuePair> qparams = URLEncodedUtils.parse(new URI(loc), "utf8"); //$NON-NLS-1$
			for (NameValuePair pair : qparams) {
				String name = pair.getName();
				if (name.equals("code")) { //$NON-NLS-1$
					return pair.getValue();
				}
			}
			throw new CloudOperationException("No 'code' param in redirect Location: "+loc); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			throw new CloudOperationException(e);
		}
	}

	public SshHost getSshHost() {
		return cloudInfo.getSshHost();
	}

	public static SshClientSupport create(final CloudFoundryOperations client, CloudCredentials creds, HttpProxyConfiguration proxyConf, boolean selfSigned) {
		CloudInfoV2 cloudInfo = new CloudInfoV2(
				creds,
				client.getCloudControllerUrl(),
				proxyConf,
				selfSigned
		);

		return new SshClientSupport(client, cloudInfo, selfSigned, proxyConf);
	}

	/**
	 * When connecting an ssh client to CF the 'username' is derived from, and identifies a
	 * specific app instance. This method formats the 'username' from an appGuid and an instance number.
	 */
	public String getSshUser(UUID appGuid, int instance) {
		return "cf:"+appGuid+"/" + instance;
	}
}
