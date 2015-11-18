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

import java.util.UUID;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Kris De Volder
 */
public class HealthCheckSupport extends CfClientSideCart {

	public static final String HC_NONE = "none";
	public static final String HC_PORT = "port";
	public static final String[] HC_ALL = {HC_NONE, HC_PORT};

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(Include.NON_NULL)
	public static class HealthCheck {

		/**
		 * For jackson to be able to deserialize
		 */
		public HealthCheck() {
		}

		public HealthCheck(String type) {
			this.healthCheckType = type;
		}

		@JsonProperty("health_check_type")
		private String healthCheckType;

		public String getHealthCheckType() {
			return healthCheckType;
		}

		public void setHealthCheckType(String healthCheckType) {
			this.healthCheckType = healthCheckType;
		}
	}

	public HealthCheckSupport(CloudFoundryOperations client, CloudInfoV2 cloudInfo, boolean trustSelfSigned, HttpProxyConfiguration httpProxyConfiguration) {
		super(client, cloudInfo, trustSelfSigned, httpProxyConfiguration);
	}

	protected RestTemplate createRestTemplate(boolean trustSelfSigned, HttpProxyConfiguration httpProxyConfiguration) {
		return RestUtils.createRestTemplate(httpProxyConfiguration, trustSelfSigned, /*disableRedirects*/false);
	}

	public String getHealthCheck(UUID guid) {
		HealthCheck summary = restTemplate.getForObject(url("/v2/apps/{guid}/summary"), HealthCheck.class, guid);
		if (summary!=null) {
			return summary.getHealthCheckType();
		}
		return null;
	}


	public String getHealthCheck(CloudApplication app) {
		return getHealthCheck(app.getMeta().getGuid());
	}

	public void setHealthCheck(UUID guid, String type) {
		restTemplate.put(url("/v2/apps/{guid}"), new HealthCheck(type), guid);
	}

	public void setHealthCheck(CloudApplication app, String type) {
		setHealthCheck(app.getMeta().getGuid(), type);
	}

	public static HealthCheckSupport create(final CloudFoundryOperations client, CloudCredentials creds, HttpProxyConfiguration proxyConf, boolean selfSigned) {
		CloudInfoV2 cloudInfo = new CloudInfoV2(
				creds,
				client.getCloudControllerUrl(),
				proxyConf,
				selfSigned
		);

		return new HealthCheckSupport(client, cloudInfo, selfSigned, proxyConf);
	}

}
