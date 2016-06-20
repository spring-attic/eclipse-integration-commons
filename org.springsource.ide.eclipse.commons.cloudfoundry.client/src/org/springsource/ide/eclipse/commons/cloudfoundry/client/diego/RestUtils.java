/*
 * Copied from Spring Tool Suite and cf-java-client. Original license:
 * 
 * Copyright 2009-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.ide.eclipse.commons.cloudfoundry.client.diego;

import static org.apache.http.conn.ssl.SSLSocketFactory.*;

import java.security.GeneralSecurityException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.rest.CloudControllerResponseErrorHandler;
import org.cloudfoundry.client.lib.rest.LoggingRestTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Some helper utilities for creating classes used for the REST support.
 *
 * @author Kris De Volder
 * @author Thomas Risberg
 */
public class RestUtils {
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  Select methods, copied from cf-java-client lib's RestUtil with minor modifications to allow 
	//  disabling redirect handling

	public static RestTemplate createRestTemplate(HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts, boolean disableRedirectHandling) {
		RestTemplate restTemplate = new LoggingRestTemplate();
		restTemplate.setRequestFactory(createRequestFactory(httpProxyConfiguration, trustSelfSignedCerts, disableRedirectHandling));
		restTemplate.setErrorHandler(new CloudControllerResponseErrorHandler());
		//restTemplate.setMessageConverters(getHttpMessageConverters());
		//TODO ^^^ But, we don't seem to need custom message convertors for what we do right now.
		return restTemplate;
	}
	
	public static ClientHttpRequestFactory createRequestFactory(HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts, boolean disableRedirectHandling) {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();

		if (trustSelfSignedCerts) {
			httpClientBuilder.setSslcontext(buildSslContext());
			httpClientBuilder.setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
		}
		
		if (disableRedirectHandling) {
			httpClientBuilder.disableRedirectHandling();
		}

		if (httpProxyConfiguration != null) {
			HttpHost proxy = new HttpHost(httpProxyConfiguration.getProxyHost(), httpProxyConfiguration.getProxyPort());
			httpClientBuilder.setProxy(proxy);

			if (httpProxyConfiguration.isAuthRequired()) {
				BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(
						new AuthScope(httpProxyConfiguration.getProxyHost(), httpProxyConfiguration.getProxyPort()),
						new UsernamePasswordCredentials(httpProxyConfiguration.getUsername(), httpProxyConfiguration.getPassword()));
				httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}

			HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			httpClientBuilder.setRoutePlanner(routePlanner);
		}

		HttpClient httpClient = httpClientBuilder.build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

		return requestFactory;
	}

	private static javax.net.ssl.SSLContext buildSslContext()  {
		try {
			return new SSLContextBuilder().useSSL().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		} catch (GeneralSecurityException gse) {
			throw new RuntimeException("An error occurred setting up the SSLContext", gse); //$NON-NLS-1$
		}
	}



}
