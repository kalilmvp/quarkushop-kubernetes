package com.kmvpsolutions.customer.resource;

import com.kmvpsolutions.commons.dto.PaymentDTO;
import com.kmvpsolutions.customer.service.PaymentService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Authenticated
@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Payment", description = "All payment methods")
public class PaymentResource {

    @Inject
    PaymentService paymentService;

    @RolesAllowed("admin")
    @GET
    public List<PaymentDTO> findAll() {
        return this.paymentService.findAll();
    }

    @GET
    @Path("/{id}")
    public PaymentDTO findById(@PathParam("id") Long id) {
        return this.paymentService.findById(id);
    }

    @GET
    @Path("/price/{max}")
    public List<PaymentDTO> findById(@PathParam("max") Double max) {
        return this.paymentService.findByPriceRange(max);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public PaymentDTO create(PaymentDTO paymentDTO) {
        return this.paymentService.create(paymentDTO);
    }

    @RolesAllowed("admin")
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.paymentService.delete(id);
    }
}
