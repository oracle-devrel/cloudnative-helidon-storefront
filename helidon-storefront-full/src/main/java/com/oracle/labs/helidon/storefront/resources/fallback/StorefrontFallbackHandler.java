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
