package com.kmvpsolutions.commons.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

import javax.ws.rs.core.Application;

@SecurityScheme(
        securitySchemeName = "jwt",
        description = "JWT authentication with bearer token",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "Bearer [token]"
)
@OpenAPIDefinition(
        info = @Info(
                title = "QuarkuShop API",
                description = "API´s of the quarkushop API",
                contact = @Contact(
                        name = "Kalil Peixoto",
                        email = "kalilmvp@gmail.com",
                        url = "https://github.com/kalilmvp"
                ),
                version = "1.0.0-SNAPSHOT"
        ),
        security = @SecurityRequirement(name = "jwt")
)
public class OpenApiConfig extends Application {
}
