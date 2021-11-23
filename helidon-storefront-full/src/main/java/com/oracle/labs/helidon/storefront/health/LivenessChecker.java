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

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

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
@Liveness
@Slf4j
public class LivenessChecker implements HealthCheck {
	public final static int FROZEN_TIME = 60;

	private static long startTime = System.currentTimeMillis();

	private String storeName;

	/**
	 * Save the resource away so we can access it later, strictly as the fields we
	 * want are static wed don't need to do this, but this way demos the use of
	 * the @Inject annotation
	 * 
	 * @param stockResource
	 */
	@Inject
	public LivenessChecker(@ConfigProperty(name = "app.storename") String storeName) {
		this.storeName = storeName;
	}

	@Override
	public HealthCheckResponse call() {
		// don't test anything here, we're just reporting that we are running, not that
		// any of the underlying connections to thinks like the database are active

		// if there is a file /frozen then just lock up for 60 seconds
		// this let's us emulate a lockup which will trigger a pod restart
		// if we have enabled liveliness testing against this API
		if (new File("/frozen").exists()) {
			log.info("/frozen exists, locking for " + FROZEN_TIME + " seconds");
			try {
				Thread.sleep(FROZEN_TIME * 1000);
			} catch (InterruptedException e) {
				// ignore for now
			}
			return HealthCheckResponse.named("storefront-live").up()
					.withData("uptime", System.currentTimeMillis() - startTime).withData("storename", storeName)
					.withData("frozen", true).build();
		}
		log.info("Not frozen, Returning alive status true, storename " + storeName);
		return HealthCheckResponse.named("storefront-live").up()
				.withData("uptime", System.currentTimeMillis() - startTime).withData("storename", storeName)
				.withData("frozen", false).build();
	}

}
