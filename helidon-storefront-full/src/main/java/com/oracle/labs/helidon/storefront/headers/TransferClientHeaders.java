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

package com.oracle.labs.helidon.storefront.headers;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransferClientHeaders implements ClientHeadersFactory {

	@Override
	public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
			MultivaluedMap<String, String> outgoingHeaders) {
		log.info("Incoming headers - " + incomingHeaders);
		log.info("Provided outgoing headers - " + outgoingHeaders);
		// we need to remove some headers as by default they are restricted
		MultivaluedMap<String, String> sanitisedIncomingHeaders = new MultivaluedHashMap<>(incomingHeaders);
		sanitisedIncomingHeaders.remove("Host");
		// Helidon may have handled the Authorization for us.
		sanitisedIncomingHeaders.remove("Authorization");
		// Need a multi valued map as a header can be repeated multiple times.
		MultivaluedMap<String, String> transferredHeaders = new MultivaluedHashMap<>();
		// add all of the headers that have already been setup for us
		transferredHeaders.putAll(sanitisedIncomingHeaders);
		// now add all of the incoming ones
		transferredHeaders.putAll(outgoingHeaders);
		log.info("Combined headers - " + transferredHeaders);
		// return the new map
		return transferredHeaders;
	}
}