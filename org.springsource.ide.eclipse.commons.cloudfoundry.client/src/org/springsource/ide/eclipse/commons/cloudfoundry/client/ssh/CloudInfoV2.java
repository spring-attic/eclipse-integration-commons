/*******************************************************************************
 * Copied from Spring Tool Suite. Original license:
 * 
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.cloudfoundry.client.ssh;

import java.net.URL;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.util.CloudUtil;
import org.cloudfoundry.client.lib.util.JsonUtil;
import org.springframework.web.client.RestTemplate;

/**
 * @author Kris De Volder
 */
public class CloudInfoV2 {

	private RestTemplate restTemplate;
	private URL ccUrl;
	private Map<String, Object> infoV2Map;

	public CloudInfoV2(CloudCredentials creds, URL url, HttpProxyConfiguration proxyConf, boolean selfSigned) {
		restTemplate = RestUtils.createRestTemplate(proxyConf, selfSigned, false);
		this.ccUrl = url;
	}

	public String getSshClientId() {
		return getProp("app_ssh_oauth_client"); //$NON-NLS-1$
	}
	
	public String getAuthorizationUrl() {
		return getProp("authorization_endpoint"); //$NON-NLS-1$
	}

	public String getProp(String name) {
		Map<String, Object> map = getMap();
		if (map!=null) {
			return CloudUtil.parse(String.class, map.get(name));
		}
		return null;
	}

	private Map<String, Object> getMap() {
		if (infoV2Map==null) {
			String infoV2Json = restTemplate.getForObject(getUrl("/v2/info"), String.class); //$NON-NLS-1$
			infoV2Map = JsonUtil.convertJsonToMap(infoV2Json);
		}
		return infoV2Map;
	}

	private String getUrl(String path) {
		return ccUrl + path;
	}

	public SshHost getSshHost() {
		String fingerPrint = getProp("app_ssh_host_key_fingerprint"); //$NON-NLS-1$
		String host = getProp("app_ssh_endpoint"); //$NON-NLS-1$
		int port = 22; //Default ssh port
		if (host!=null) {
			if (host.contains(":")) {
				String[] pieces = host.split(":");
				host = pieces[0];
				port = Integer.parseInt(pieces[1]);
			}
		}
		if (host!=null || fingerPrint!=null) {
			return new SshHost(host, port, fingerPrint);
		}
		return null;
	}
}
