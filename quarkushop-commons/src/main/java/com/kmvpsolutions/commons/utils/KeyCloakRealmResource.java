package com.kmvpsolutions.commons.utils;

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

public class KeyCloakRealmResource implements QuarkusTestResourceLifecycleManager {

    @ClassRule
    public static DockerComposeContainer KEYCLOAK = new DockerComposeContainer(
            new File("src/main/docker/keycloak-test.yml")
    ).withExposedService("keycloak_1",
            9080,
            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));

    @Override
    public Map<String, String> start() {
        KEYCLOAK.start();

        String jwtIssueUrl = String.format(
                "http://%s:%s/auth/realms/quarkus-realm",
                KEYCLOAK.getServiceHost("keycloak_1", 9080),
                KEYCLOAK.getServicePort("keycloak_1", 9080)
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

        confMap.put("mp.jwt.verify.publickey.location", jwtIssueUrl.concat("/protocol/openid-connect/certs"));
        confMap.put("mp.jwt.verify.issuer", jwtIssueUrl);

        return confMap;
    }

    @Override
    public void stop() {
        KEYCLOAK.close();
    }
}
