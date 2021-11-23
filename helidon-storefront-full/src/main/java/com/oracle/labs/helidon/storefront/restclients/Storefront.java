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

package com.oracle.labs.helidon.storefront.restclients;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import com.oracle.labs.helidon.storefront.data.ItemDetails;
import com.oracle.labs.helidon.storefront.data.ItemRequest;
import com.oracle.labs.helidon.storefront.exceptions.MinimumChangeException;
import com.oracle.labs.helidon.storefront.exceptions.NotEnoughItemsException;
import com.oracle.labs.helidon.storefront.exceptions.UnknownItemException;

/**
 * This is provided as an example interface for the StorefrontResource if you
 * wanted to create a RestClient for it.
 * 
 * For the purposes of this lab the StorefrontResource doesn't actually
 * implement this, that's because the lab takes you through the steps of
 * building the REST API, and this represents a completed API, so you'd have to
 * manage overrides and so on.
 * 
 * In a real (i.e. non lab) situation you would usually define the interface
 * first, then implement it.
 */
@Path("/store")
public interface Storefront {

	@GET
	@Path("/stocklevel")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "List stock items", description = "Returns a list of all of the stock items currently held in the database (the list may be empty if there are no items)")
	@APIResponse(description = "A set of ItemDetails representing the current data in the database", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDetails.class, type = SchemaType.ARRAY, example = "[{\"itemCount\": 10, \"itemName\": \"Pencil\"},"
			+ "{\"itemCount\": 50, \"itemName\": \"Eraserl\"}," + "{\"itemCount\": 4600, \"itemName\": \"Pin\"},"
			+ "{\"itemCount\": 100, \"itemName\": \"Book\"}]")))
	public Collection<ItemDetails> listAllStock();

	/**
	 * adjust the available levels for the item for example curl -X POST -u
	 * user:password -d '{"requestedItem":"wrench", "requestedCount":5}'
	 * 
	 * @param itemRequest the wonders of JAX and JSON Serialization mean that the
	 *                    framework will process the JSON Payload into the
	 *                    ItemRequest object for us
	 * @return
	 */
	@POST
	@Path("/reserveStock")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Reserves a number of stock items", description = "reserves a number of stock items in the database. The number of stock items being reserved must be greater than the defined minimum change")
	@APIResponse(description = "The updated stock details for the item", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDetails.class, example = "{\"itemCount\": 10, \"itemName\": \"Pencil\"}")))
	@APIResponse(description = "The requested item does not exist", responseCode = "404")
	@APIResponse(description = "The requested change does not meet the minimum level required for the change (i.e. is <= the minimumChange value)", responseCode = "406")
	@APIResponse(description = "There are not enough of the requested item to fulfil your request", responseCode = "409")
	public ItemDetails reserveStockItem(
			@RequestBody(description = "The details of the item being requested", required = true, content = @Content(schema = @Schema(implementation = ItemRequest.class, example = "{\"requestedItem\", \"Pin\", \"requestedCount\",5}"))) ItemRequest itemRequest)
			throws MinimumChangeException, UnknownItemException, NotEnoughItemsException;

}