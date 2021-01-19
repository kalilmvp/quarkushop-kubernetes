package com.kmvpsolutions.order.resource;

import com.kmvpsolutions.commons.dto.CartDTO;
import com.kmvpsolutions.order.service.CartService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Authenticated
@Path("/carts")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Cart", description = "All cart methods")
public class CartResource {

    @Inject
    CartService cartService;

    @GET
    public List<CartDTO> findAll() {
        return this.cartService.findAll();
    }

    @GET
    @Path("/active")
    public List<CartDTO> findAllActiveCarts() {
        return this.cartService.findAllActiveCarts();
    }

    @GET
    @Path("/customer/{id}")
    public CartDTO getActiveCartFromCustomer(@PathParam("id") Long customerId) {
        return this.cartService.getActiveCart(customerId);
    }

    @GET
    @Path("/{id}")
    public CartDTO findById(@PathParam("id") Long id) {
        return this.cartService.findById(id);
    }

    @POST
    @Path("/customer/{id}")
    public CartDTO create(@PathParam("id") Long customerId) {
        return this.cartService.createDTO(customerId);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.cartService.delete(id);
    }
}
