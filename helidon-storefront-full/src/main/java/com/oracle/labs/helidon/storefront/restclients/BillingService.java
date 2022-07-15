package com.oracle.labs.helidon.storefront.restclients;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.oracle.labs.helidon.storefront.data.BillingEntry;
import com.oracle.labs.helidon.storefront.data.BillingEntryResponse;
import com.oracle.labs.helidon.storefront.data.BillingInfo;
import com.oracle.labs.helidon.storefront.headers.TransferClientHeaders;

@RegisterRestClient(configKey = "Logger")
@RegisterClientHeaders(TransferClientHeaders.class)
@Path("/billing")
@ApplicationScoped
public interface BillingService {
	@Path("/saveentry")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@POST
	public BillingEntryResponse postBillingEntry(BillingEntry billingEntry);

	@Path("/billinginfo")
	@Counted(name = "billingInfoCounter")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public BillingInfo getBillingInfo();
}
