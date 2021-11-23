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

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.oracle.labs.helidon.storefront.data.ItemDetails;
import com.oracle.labs.helidon.storefront.data.ItemRequest;
import com.oracle.labs.helidon.storefront.data.MinimumChange;
import com.oracle.labs.helidon.storefront.exceptions.MinimumChangeException;
import com.oracle.labs.helidon.storefront.exceptions.NotEnoughItemsException;
import com.oracle.labs.helidon.storefront.exceptions.UnknownItemException;
import com.oracle.labs.helidon.storefront.resources.fallback.StorefrontFallbackHandler;
import com.oracle.labs.helidon.storefront.restclients.StockManager;

import io.helidon.security.annotations.Authenticated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/store")
@RequestScoped
// mark all REST calls in this resource as generating call counts. This will count the number of invocations, 
// if you want to track the number of currently active calls (E.g. entered, but not left a method) then you'd 
// use a @ConcurrentGauge
// Annotating the entire class will generate counters for each individual method, and also class level counters 
// on the totals for the class
@Counted
// Authenticated here means for any REST call to this class we have to have a user, the user authentication is
// automatically propagated to the stock management service when we call it
@Authenticated
// if we don't return from the method in 15 seconds return a timeout message
@Timeout(value = 15, unit = ChronoUnit.SECONDS)
//Have Lombok create a logger and no args constructor for us
@Slf4j
@NoArgsConstructor

public class StorefrontResource {

	private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

	@Inject
	private MinimumChange minimumChange;

	@Inject
	@RestClient
	private StockManager stockManager = null;

	@GET
	@Path("/stocklevel")
	@Produces(MediaType.APPLICATION_JSON)
	/*
	 * In the event of an error call the failedListStockItem method in this class,
	 * that method must have an identical signature to this one. This will also
	 * trigger the automatic generation of counters based on how often the method
	 * fails or succeeds
	 */
	@Fallback(fallbackMethod = "failedListStockItem")
	// add a timer to track how long is spent in this call
	@Timed(name = "listAllStockTimer")
	// add a Meter to track how often we're called, absolute=true means the name
	// given is used directly and not added to the class
	@Metered(name = "listAllStockMeter", absolute = true)
	@Operation(summary = "List stock items", description = "Returns a list of all of the stock items currently held in the database (the list may be empty if there are no items)")
	@APIResponse(description = "A set of ItemDetails representing the current data in the database", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDetails.class, type = SchemaType.ARRAY, example = "[{\"itemCount\": 10, \"itemName\": \"Pencil\"},"
			+ "{\"itemCount\": 50, \"itemName\": \"Eraserl\"}," + "{\"itemCount\": 4600, \"itemName\": \"Pin\"},"
			+ "{\"itemCount\": 100, \"itemName\": \"Book\"}]")))
	public Collection<ItemDetails> listAllStock() {
		// log the request
		log.info("Requesting listing of all stock");
		// get the list from the stock management service
		try {
			Collection<ItemDetails> items = stockManager.getAllStockLevels();
			// log the response
			log.info("Found " + items.size() + " items");
			// return the items
			return items;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

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
	// add a timer to track how long is spent in this call
	@Timed(name = "reserveStockTimer")
	/*
	 * if this fails call an external call back handler, This version of the
	 * annotation will call the handle method on the specified class (which must
	 * have the same return type as this method.) The class itself must
	 * be @Dependent annotated
	 * 
	 * This approach allows for significantly more detailed handling,but at the cost
	 * of more code
	 */
	@Fallback(StorefrontFallbackHandler.class)
	@Operation(summary = "Reserves a number of stock items", description = "reserves a number of stock items in the database. The number of stock items being reserved must be greater than the defined minimum change")
	@APIResponse(description = "The updated stock details for the item", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDetails.class, example = "{\"itemCount\": 10, \"itemName\": \"Pencil\"}")))
	@APIResponse(description = "The requested item does not exist", responseCode = "404")
	@APIResponse(description = "The requested change does not meet the minimum level required for the change (i.e. is <= the minimumChange value)", responseCode = "406")
	@APIResponse(description = "There are not enough of the requested item to fulfil your request", responseCode = "409")
	public ItemDetails reserveStockItem(
			@RequestBody(description = "The details of the item being requested", required = true, content = @Content(schema = @Schema(implementation = ItemRequest.class, example = "{\"requestedItem\",\"Pin\",\"requestedCount\",5}"))) ItemRequest itemRequest)
			throws MinimumChangeException, UnknownItemException, NotEnoughItemsException {
		log.info("Requesting the reservation of " + itemRequest.getRequestedCount() + " items of "
				+ itemRequest.getRequestedItem());
		// make sure the change is within the minimum change allowed
		// :-)
		if (itemRequest.getRequestedCount() < minimumChange.getMinimumChange()) {
			// didn't meet the minimum requirement, log the failed request and throw the log
			// message as an error
			String problemDetails = "The reservation of " + itemRequest.getRequestedCount() + " items of "
					+ itemRequest.getRequestedItem() + " fails because it's less than the minimum delta of "
					+ minimumChange.getMinimumChange();
			log.error(problemDetails);
			throw new MinimumChangeException(problemDetails);
		}
		// OK validated the basic data, let's make sure we have enough remaining stock
		// to reserve
		ItemDetails itemDetails = stockManager.getStockItem(itemRequest.getRequestedItem());
		if (itemDetails == null) {
			// can't find the stock item, log the failed request and throw the log message
			// as
			// an error
			String problemDetails = "The reservation of " + itemRequest.getRequestedCount() + " items of "
					+ itemRequest.getRequestedItem() + " fails because the item is not known";
			log.error(problemDetails);
			throw new UnknownItemException(problemDetails);
		}
		log.info("stock item " + itemDetails.getItemName() + " exists and currently there are "
				+ itemDetails.getItemCount() + " items in stock");
		// do we have enough items to reserve ?
		if (itemDetails.getItemCount() <= itemRequest.getRequestedCount()) {
			// not enough items, log the failed request and throw the log message as an
			// error
			String problemDetails = "The reservation of " + itemRequest.getRequestedCount() + " items of "
					+ itemRequest.getRequestedItem() + " fails because there are only " + itemDetails.getItemCount()
					+ " items available";
			log.error(problemDetails);
			throw new NotEnoughItemsException(problemDetails);
		}
		// Right, passed all checks
		// work out the new level
		int newItemCount = itemDetails.getItemCount() - itemRequest.getRequestedCount();
		// log the request
		log.info("The reservation of " + itemRequest.getRequestedCount() + " items of " + itemRequest.getRequestedItem()
				+ " is being sent to the database");
		// update the DB and get the result back (the updated info)
		ItemDetails updatedItemDetails = stockManager.setStockItemLevel(itemRequest.getRequestedItem(), newItemCount);
		// log the result
		log.info("The reservation of " + itemRequest.getRequestedCount() + " items of " + itemRequest.getRequestedItem()
				+ " suceeded, the stock manager reports " + updatedItemDetails.getItemCount() + " remain");
		// pass back the resulting updated item details
		return updatedItemDetails;
	}

	/*
	 * This is a simple handler, it doesn't get handed the fault details, but we can
	 * use it to return a default object, or in this case throw an error
	 */
	public Collection<ItemDetails> failedListStockItem() {
		log.info("The listing of items failed for some reason");
		throw new WebApplicationException(
				Response.status(424, "Failed Dependency")
						.entity(JSON.createObjectBuilder()
								.add("errormessage", "Unable to connect to the stock manager service").build())
						.build());
	}
}