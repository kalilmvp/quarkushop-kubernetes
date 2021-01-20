package com.kmvpsolutions.commons.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.RequestScoped;
import javax.inject.Provider;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@RequestScoped
public class TokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "undefined")
    Provider<String> jwtIssuerUrlProvider;

    @ConfigProperty(name = "keycloak.credentials.client-id", defaultValue = "undefined")
    Provider<String> clientIdProvider;

    @Counted(name="accessTokenRequestsCounter", description = "How many access_tokens have been requested")
    @Timed(name = "getAccessTokenRequestTimer", description = "Measuring time to get an acess_token", unit = MetricUnits.MILLISECONDS)
    public String getAccessToken(String userName,
                                 String password) throws IOException, InterruptedException {
        return this.getAccessToken(this.jwtIssuerUrlProvider.get(), userName, password, this.clientIdProvider.get(), null);
    }

    public String getAccessToken(
            String jwtIssuerUrlProvider,
            String userName,
            String password,
            String clientId,
            String clientSecret)
            throws IOException, InterruptedException {

        String keyCloakTokenEndpoint = jwtIssuerUrlProvider
                .concat("/protocol/openid-connect/token");

        String requestBody = "username=".concat(userName).concat("&password=").concat(password)
                .concat("&grant_type=password").concat("&client_id=").concat(clientId);

        if (clientSecret != null) {
            requestBody = requestBody.concat("&client_secret=").concat(clientSecret);
        }

        HttpClient client = HttpClient.newBuilder().build();

        int tentativas = 0;
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(keyCloakTokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = null;

        while(tentativas < 3) {
            try {
                 response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                 break;
            } catch (IOException io) {
                tentativas += 1;
                System.out.println("Tentativa de conexão " + tentativas + ". Realizando uma nova.");
            }
        }

        String acessToken = null;

        if (response.statusCode() == 200) {
            acessToken = new ObjectMapper().readTree(response.body()).get("access_token").textValue();
        } else {
            throw new UnauthorizedException();
        }

        return acessToken;
    }
}
