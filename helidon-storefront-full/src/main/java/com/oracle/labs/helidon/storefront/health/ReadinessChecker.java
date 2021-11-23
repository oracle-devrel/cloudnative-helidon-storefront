/*Copyright (c) 2021 Oracle and/or its affiliates.

The Universal Permissive License (UPL), Version 1.0

Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both

(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),

without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.

This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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
