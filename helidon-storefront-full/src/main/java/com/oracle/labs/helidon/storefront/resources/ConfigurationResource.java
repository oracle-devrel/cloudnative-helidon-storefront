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

import java.util.Collections;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.oracle.labs.helidon.storefront.data.MinimumChange;

import io.helidon.security.annotations.Authenticated;
import lombok.extern.slf4j.Slf4j;

@Path("/minimumChange")
@ApplicationScoped
// Have Lombok create a logger for us
@Slf4j
public class ConfigurationResource {
	private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());
	@Inject
	private MinimumChange minimumChange;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	/**
	 * Note that this returns an HTTP Response
	 * 
	 * @return the current minimum change required
	 */
	public Response getMinimumChange() {
		log.debug("Request to getMinimumChange");
		return Response.ok(minimumChange.getMinimumChange()).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Authenticated
	@RolesAllowed({ "admin" })
	/**
	 * Set the minimum change required, musty be > 0 I included this to show that
	 * the system can correctly process body content other than JSON
	 * 
	 * @param newMinimum
	 * @return
	 */
	public Response setMinimumChange(int newMinimum) {
		log.info("Request to set minimumChange to " + newMinimum);
		// validate the request, it must be > 0
		if (newMinimum <= 0) {
			log.error("Request to set minimumChange to " + newMinimum + " failed as it's not > 0");
			return Response.status(Status.BAD_REQUEST)
					.entity(createErrorMessageJson("Minimum change (" + newMinimum + ") must be > 0 and an integer"))
					.build();
		}
		minimumChange.setMinimumDelta(newMinimum);
		log.info("Request to set minimumChange to " + newMinimum + " suceeded");
		return Response.ok().entity(minimumChange.getMinimumChange()).build();
	}

	private JsonObject createErrorMessageJson(String message) {
		return JSON.createObjectBuilder().add("errormessage", message).build();
	}
}
