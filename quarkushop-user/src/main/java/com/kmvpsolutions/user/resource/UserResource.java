package com.kmvpsolutions.user.resource;

import com.kmvpsolutions.commons.security.TokenService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Authenticated
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "user", description = "All the user methods")
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    TokenService tokenService;

    @POST
    @PermitAll
    @Path("/access-token")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccessToken(@QueryParam("username") String userName,
                                       @QueryParam("password") String password) throws IOException, InterruptedException {

        return this.tokenService.getAccessToken(userName, password);
    }

    @GET
    @RolesAllowed("user")
    @Path("/current/info")
    public JsonWebToken getCurrentUserInfo() {
        return this.jwt;
    }

    @GET
    @RolesAllowed("user")
    @Path("/current/info/claims")
    public Map<String, Object> getCurrentInfoClaims() {
        return this.jwt.getClaimNames()
                .stream()
                .map(name -> Map.entry(name, this.jwt.getClaim(name)))
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue()
                ));
    }

    @GET()
    @Path("/current/info-alternative")
    public Principal getCurrentUserInfoAlternative(@Context SecurityContext ctx) {
        return ctx.getUserPrincipal();
    }
}
