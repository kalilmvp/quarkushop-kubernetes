package com.kmvpsolutions.product.resource;

import com.kmvpsolutions.commons.dto.ProductDTO;
import com.kmvpsolutions.product.service.ProductService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Product", description = "All product methods")
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    public List<ProductDTO> findAll() {
        return this.productService.findAll();
    }

    @GET
    @Path("/{id}")
    public ProductDTO findById(@PathParam("id") Long id) {
        return this.productService.findById(id);
    }

    @GET
    @Path("/count")
    public Long countAllProducts() {
        return this.productService.countAll();
    }

    @GET
    @Path("/category/{id}")
    public List<ProductDTO> findByCategoryId(@PathParam("id") Long id) {
        return this.productService.findByCategoryId(id);
    }

    @GET
    @Path("/count/category/{id}")
    public Long countByCategoryId(@PathParam("id") Long id) {
        return this.productService.countByCategoryId(id);
    }

    @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ProductDTO create(ProductDTO productDTO) {
        return this.productService.create(productDTO);
    }

    @RolesAllowed("admin")
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.productService.delete(id);
    }
}
