package com.kmvpsolutions.commons.health;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Liveness
@Slf4j
@ApplicationScoped
public class KeycloakConnectionHealth implements HealthCheck {
    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "false")
    Provider<String> keyCloakUrl;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse
                .named("Keycloak Connection health check");

        try {
            keyCloakConnectionVerification();
            responseBuilder.up();
        } catch (IllegalStateException ise) {
            // cannot access keycloak
            responseBuilder.down().withData("error", ise.getMessage());
        }

        return responseBuilder.build();
    }

    private void keyCloakConnectionVerification() {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(3000)).build();

        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(this.keyCloakUrl.get())).build();

        HttpResponse<String> response = null;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("IOException", e);
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
        }

        if (response == null || response.statusCode() != 200) {
            throw new IllegalStateException("Cannot contact Keycloak");
        }
    }
}
