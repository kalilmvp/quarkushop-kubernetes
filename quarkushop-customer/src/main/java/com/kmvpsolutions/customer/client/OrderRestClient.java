package com.kmvpsolutions.customer.client;

import com.kmvpsolutions.commons.dto.OrderDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Optional;

@Path("/orders")
@RegisterRestClient
public interface OrderRestClient {

    @GET
    @Path("/{id}")
    Optional<OrderDTO> findById(@PathParam("id") Long id);

    @GET
    @Path("/payment/{id}")
    Optional<OrderDTO> findByPaymentId(@PathParam("id") Long id);

    @POST
    OrderDTO save(OrderDTO orderDTO);
}
