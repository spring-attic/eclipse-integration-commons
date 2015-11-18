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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudEntity.Meta;
import org.cloudfoundry.client.lib.util.CloudEntityResourceMapper;
import org.cloudfoundry.client.lib.util.JsonUtil;

public class BuildpackSupport extends CfClientSideCart {

	public static class Buildpack extends CloudEntity {

		public Buildpack(Meta meta, String name) {
			super(meta, name);
		}

	}

	public BuildpackSupport(CloudFoundryOperations client, CloudInfoV2 cloudInfo, boolean trustSelfSigned,
			HttpProxyConfiguration httpProxyConfiguration) {
		super(client, cloudInfo, trustSelfSigned, httpProxyConfiguration);
	}

	public List<Buildpack> getBuildpacks() {
		List<Buildpack> buildpacks = new ArrayList<Buildpack>();
		String json = restTemplate.getForObject(url("/v2/buildpacks"), String.class);
		if (json != null) {
			Map<String, Object> resource = JsonUtil.convertJsonToMap(json);
			if (resource != null) {
				List<Map<String, Object>> newResources = (List<Map<String, Object>>) resource.get("resources");
				if (newResources != null) {
					for (Map<String, Object> res : newResources) {
						String name = CloudEntityResourceMapper.getEntityAttribute(res, "name", String.class);
						Meta meta = CloudEntityResourceMapper.getMeta(res);
						if (name != null && meta != null) {
							Buildpack pack = new Buildpack(meta, name);
							buildpacks.add(pack);
						}
					}
				}
			}
		}
		return buildpacks;
	}

	public static BuildpackSupport create(final CloudFoundryOperations client, CloudCredentials creds,
			HttpProxyConfiguration proxyConf, boolean selfSigned) {
		CloudInfoV2 cloudInfo = new CloudInfoV2(creds, client.getCloudControllerUrl(), proxyConf, selfSigned);

		return new BuildpackSupport(client, cloudInfo, selfSigned, proxyConf);
	}

}
