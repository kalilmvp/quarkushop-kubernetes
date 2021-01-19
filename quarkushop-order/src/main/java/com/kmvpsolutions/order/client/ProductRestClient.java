package com.kmvpsolutions.order.client;

import com.kmvpsolutions.commons.dto.ProductDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/products")
@RegisterRestClient
public interface ProductRestClient {

    @GET
    @Path("/{id}")
    ProductDTO findById(@PathParam("id") Long id);
}
