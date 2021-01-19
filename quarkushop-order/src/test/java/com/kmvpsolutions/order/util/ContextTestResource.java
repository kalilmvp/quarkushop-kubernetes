package com.kmvpsolutions.order.util;

import com.kmvpsolutions.commons.security.TokenService;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.junit.ClassRule;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ContextTestResource implements QuarkusTestResourceLifecycleManager {

    @ClassRule
    public static DockerComposeContainer ECOSYSTEM = new DockerComposeContainer(
            new File("src/main/docker/context-test.yml"))
        .withExposedService("quarkushop-product_1",
                8080,
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
        .withExposedService("keycloak_1",
                9080,
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));

    @Override
    public Map<String, String> start() {
        ECOSYSTEM.start();

        String jwtIssueUrl = String.format(
                "http://%s:%s/auth/realms/quarkus-realm",
                ECOSYSTEM.getServiceHost("keycloak_1", 9080),
                ECOSYSTEM.getServicePort("keycloak_1", 9080)
        );

        final TokenService service = new TokenService();

        Map<String, String> confMap = new HashMap<>();

        try {
            String adminAccessToken = service.getAccessToken(
                    jwtIssueUrl,
                    "admin",
                    "test",
                    "quarkus-client",
                    "mysecret");

            String testAccessToken = service.getAccessToken(
                    jwtIssueUrl,
                    "test",
                    "test",
                    "quarkus-client",
                    "mysecret");

            confMap.put("quarkus-admin-access-token", adminAccessToken);
            confMap.put("quarkus-test-access-token", testAccessToken);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        String productServiceUrl = String.format(
                "http://%s:%s/api",
                    ECOSYSTEM.getServiceHost("quarkushop-product_1", 8080),
                    ECOSYSTEM.getServicePort("quarkushop-product_1", 8080)
        );

        confMap.put("mp.jwt.verify.publickey.location", jwtIssueUrl.concat("/protocol/openid-connect/certs"));
        confMap.put("mp.jwt.verify.issuer", jwtIssueUrl);

        confMap.put("product-service.url", productServiceUrl);

        return confMap;
    }

    @Override
    public void stop() {
        ECOSYSTEM.stop();
    }
}
