/*
 * Copyright (c) 2019, 2020, 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.labs.helidon.storefront.health;

import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.oracle.labs.helidon.storefront.restclients.StockManagerStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Readiness is different form Liveness. Readiness ensures that we actually are
 * actually ready to process transactions and are fully configured, liveliness
 * is more of a Hello World situation
 * 
 * @author tg13456
 *
 */
@ApplicationScoped
@Readiness
@Slf4j
public class ReadinessChecker implements HealthCheck {

	private String storeName, statusURL;

	@Inject
	@RestClient
	private StockManagerStatus stockManagerStatus;

	/**
	 * Save the resource away so we can access it later, strictly as the fields we
	 * want are static wed don't need to do this, but this way demos the use of
	 * the @Inject annotation
	 * 
	 * @param stockResource
	 * @throws URISyntaxException
	 */
	@Inject
	public ReadinessChecker(@ConfigProperty(name = "app.storename") String storeName,
			@ConfigProperty(name = "com.oracle.labs.helidon.storefront.restclients.StockManagerStatus/mp-rest/url") String statusURL)
			throws URISyntaxException {
		log.info("Readiness started with store " + storeName + ", status url " + statusURL);
		this.storeName = storeName;
		this.statusURL = statusURL;
		log.info("Built the StockManagerStatus");
	}

	@Override
	// would like to use a fallback here, but they don't seem to work with
	// health checks
	public HealthCheckResponse call() {
		// try to make a call to the back end service, if it fails the fall back shoud
		// be triggered
		log.info("Requesting stock manager status");
		JsonObject alive;
		try {
			alive = stockManagerStatus.isAlive();
		} catch (Exception e) {
			log.error("Error getting status " + e.getMessage());
			return HealthCheckResponse.named("storefront-ready").down().withData("storename", storeName)
					.withData("exception", e.getMessage()).withData("statusURL", statusURL).build();
		}
		log.info("Got stock manager status of " + alive);
		return HealthCheckResponse.named("storefront-ready").up().withData("storename", storeName).build();
	}
}
