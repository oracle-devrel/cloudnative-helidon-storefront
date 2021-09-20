/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates.
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
package com.oracle.labs.helidon.storefront.resources;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/status")
@RequestScoped
public class StatusResource {
	public final static String VERSION = "0.0.1";
	private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());
	private String storename = "Not set";
	private static final SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");

	@Inject
	public StatusResource(@ConfigProperty(name = "app.storename") String storename) {
		this.storename = storename;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * very simple little thing to acknowledge we're alive, is less overhead than
	 * interrogating the health status
	 * 
	 * @return
	 */
	public JsonObject isAlive() throws InterruptedException {
		return JSON.createObjectBuilder().add("name", storename).add("alive", true).add("version", VERSION)
				.add("timestamp", format.format(new Date())).build();
	}
}
