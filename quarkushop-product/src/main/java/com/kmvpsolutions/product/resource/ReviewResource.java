package com.kmvpsolutions.product.resource;

import com.kmvpsolutions.commons.dto.ReviewDTO;
import com.kmvpsolutions.product.service.ReviewService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Review", description = "All review methods")
public class ReviewResource {

    @Inject
    ReviewService reviewService;

    @GET
    public List<ReviewDTO> findAll() {
        return this.reviewService.findAll();
    }

    @GET
    @Path("/{id}")
    public ReviewDTO findById(@PathParam("id") Long id) {
        return this.reviewService.findById(id);
    }

    @GET
    @Path("/product/{id}")
    public List<ReviewDTO> findByCategoryId(@PathParam("id") Long id) {
        return this.reviewService.findReviewsByProductId(id);
    }

    @RolesAllowed("user")
    @POST
    @Path("/product/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public ReviewDTO create(ReviewDTO reviewDTO, @PathParam("id") Long id) {
        return this.reviewService.create(reviewDTO, id);
    }

    @RolesAllowed("admin")
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.reviewService.delete(id);
    }
}
