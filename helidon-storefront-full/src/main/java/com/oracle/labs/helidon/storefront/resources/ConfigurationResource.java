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
