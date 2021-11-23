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
package com.oracle.labs.helidon.storefront.resources.fallback;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import com.oracle.labs.helidon.storefront.data.ItemDetails;
import com.oracle.labs.helidon.storefront.exceptions.MinimumChangeException;
import com.oracle.labs.helidon.storefront.exceptions.NotEnoughItemsException;
import com.oracle.labs.helidon.storefront.exceptions.UnknownItemException;

import lombok.extern.slf4j.Slf4j;

@Dependent
@Slf4j
public class StorefrontFallbackHandler implements FallbackHandler<ItemDetails> {
	// List the specific exceptions here, if there isn;t a match it will default to
	// 500 INTERNAL_SERVER_ERROR
	private final static List<ExceptionMap> exceptionToCode = Arrays.asList(
			new ExceptionMap(UnknownHostException.class.getName(), 424),
			new ExceptionMap(ConnectException.class.getName(), 424),
			new ExceptionMap(MinimumChangeException.class.getName(), Status.NOT_ACCEPTABLE.getStatusCode()),
			new ExceptionMap(UnknownItemException.class.getName(), Status.NOT_FOUND.getStatusCode()),
			new ExceptionMap(NotEnoughItemsException.class.getName(), Status.CONFLICT.getStatusCode()),
			new ExceptionMap(WebApplicationException.class.getName(), 424));
	private final static Map<String, Integer> exceptionsToCode = exceptionToCode.stream()
			.collect(Collectors.toMap(info -> info.getCause(), info -> info.getStatus()));

	/*
	 * This really attempts to show a whole bunch of possibilities for handling a
	 * problem making a call. In reality it's unlikely you'd want to do all of
	 * these, but this may give you some ideas
	 */
	@Override
	public ItemDetails handle(ExecutionContext context) {
		// for now we just extract the details out, but in reality we'd look at the
		// exception to see what's happened and do different processing based on that
		JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());
		// get the expected param types for the method
		String paramTypes = Arrays.stream(context.getMethod().getParameters()).map(param -> param.getType().getName())
				.collect(Collectors.joining(","));
		// build the basic info to let us know what class / method and it's param types
		// were called
		JsonObjectBuilder partial = JSON.createObjectBuilder().add("Problem processing request in ",
				context.getMethod().getDeclaringClass().getName() + "." + context.getMethod().getName() + "("
						+ paramTypes + ")");
		// get the args as a array of strings
		List<String> paramsList = Arrays.stream(context.getParameters()).map(obj -> obj.toString())
				.collect(Collectors.toList());
		JsonArray params = JSON.createArrayBuilder(paramsList).build();
		partial.add("param values", params);
		// the top level cause is probabaly a JAX-RX problem of some kind, it will
		// contain the embedded cause
		Throwable cause = context.getFailure();
		String causeName = "NULL";
		if (cause != null) {
			// in some situations the cause will be some form or wrapper, for example a
			// JAX-RS persistence exception, that's pretty boring, so see if it contains an
			// embedded cause, if not just use the one we were given
			Throwable embeddedCause = cause.getCause();
			if (embeddedCause == null) {
				embeddedCause = cause;
			}
			if (embeddedCause != null) {
				causeName = embeddedCause.getClass().getName();
				log.info("Cause name is " + causeName);
				if (causeName.equals(WebApplicationException.class.getName())) {
					partial.add("Exception", causeName);
					WebApplicationException wae = (WebApplicationException) embeddedCause;
					partial.add("Embeded details", wae.getResponse().toString());
					String respBody = wae.getResponse().getEntity().toString();
					if (respBody != null) {
						partial.add("Embeded body", respBody);
					}
				} else {
					partial.add("Exception", causeName);
					if (embeddedCause.getMessage() == null) {
						partial.add("Exception", "Message is null");
					} else {
						partial.add("Exception message", embeddedCause.getMessage());
					}
				}
			}
		}
		JsonObject errorDetails = partial.build();
		// get the response code to map the exception to from the map
		Integer respStatus = exceptionsToCode.get(causeName);
		if (respStatus == null) {
			respStatus = Status.INTERNAL_SERVER_ERROR.getStatusCode();
		}
		Response resp = Response.status(respStatus).entity(errorDetails).build();
		// package it all up and throw it, the runtime will convert it into the proper
		// response structure with the redault we provided
		throw new WebApplicationException("Problem processing request", cause, resp);

	}

}
