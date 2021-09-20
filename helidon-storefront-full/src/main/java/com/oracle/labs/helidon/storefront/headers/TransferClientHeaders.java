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