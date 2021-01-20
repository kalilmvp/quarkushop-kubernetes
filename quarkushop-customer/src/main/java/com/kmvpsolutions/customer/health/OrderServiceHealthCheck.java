package com.kmvpsolutions.customer.health;

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

@Slf4j
@Liveness
@ApplicationScoped
public class OrderServiceHealthCheck implements HealthCheck {

    @ConfigProperty(name = "order-service.url", defaultValue = "false")
    Provider<String> orderServiceURL;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder healthCheckResponseBuilder =
                HealthCheckResponse.named("Order service Connection check!!");

        try {
            this.orderServiceConnectionVerification();
            healthCheckResponseBuilder.up();
        } catch (IllegalStateException ise) {
            healthCheckResponseBuilder.down().withData("error", ise.getMessage());
        }

        return healthCheckResponseBuilder.build();
    }

    private void orderServiceConnectionVerification() {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(3000)).build();

        HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(URI.create(this.orderServiceURL.get() + "/health")).build();

        HttpResponse<String> response = null;

        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("IOException", e);
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
            Thread.currentThread().interrupt();
        }

        if (response == null || response.statusCode() != 200) {
            throw new IllegalStateException("Cannot contact Order Service");
        }
    }
}
