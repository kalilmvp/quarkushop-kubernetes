package com.kmvpsolutions.customer;

import com.kmvpsolutions.commons.utils.KeyCloakRealmResource;
import com.kmvpsolutions.commons.utils.TestContainerResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@DisabledOnNativeImage
@QuarkusTest
@QuarkusTestResource(TestContainerResource.class)
@QuarkusTestResource(KeyCloakRealmResource.class)
public class CustomerResourceTest {

    private static String ADMIN_BEARER_TOKEN;
    private static String TEST_BEARER_TOKEN;

    @BeforeAll
    static void init() {
        ADMIN_BEARER_TOKEN = System.getProperty("quarkus-admin-access-token");
        TEST_BEARER_TOKEN = System.getProperty("quarkus-test-access-token");
    }

    @Test
    void testAll() {
        get("/customers")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testAllActiveUsers() {
        get("/customers/active")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testAllInactiveUsers() {
        get("/customers/inactive")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testFindById() {
        get("/customers/1")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testCreate() {
        var requestParams = new HashMap<>();
        requestParams.put("firstName", "Kalil");
        requestParams.put("lastName", "Peixoto");
        requestParams.put("email", "call.saul@mail.com");

        given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(requestParams).post("/customers")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testDeleteThenCustomerIsDisabled() {
        get("/customers/active").then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testAllWithAdminRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", greaterThanOrEqualTo(3))
                .body(containsString("jason.bourne@mail.hello"))
                .body(containsString("homer.simpson@mail.hello"))
                .body(containsString("peter.quinn@mail.hello"));
    }

    @Test
    void testAllActiveUsersWithAdminRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/active")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Simpson"))
                .body(containsString("Homer"));
    }

    @Test
    void testAllInactiveUsersWithAdminRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/inactive")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("peter.quinn@mail.hello"));
    }

    @Test
    void testFindByIdWithAdminRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/1")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Jason"))
                .body(containsString("Bourne"));
    }

    @Test
    void testCreateWithAdminRole() {
        var requestParams = new HashMap<>();
        requestParams.put("firstName", "Kalil");
        requestParams.put("lastName", "Peixoto");
        requestParams.put("email", "call.saul@mail.com");

        var newCustomerId = given().when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/customers")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        assertThat(newCustomerId).isNotZero();

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/" + newCustomerId)
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Kalil"))
                .body(containsString("Peixoto"))
                .body(containsString("call.saul@mail.com"));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/customers/" + newCustomerId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteThenCustomerIsDisabledWithAdminRole() {
        var newCustomerId = getNewCustomerId();

        var initialActiveCount = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/active")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("size()");

        var initialInactiveCount = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/inactive")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("size()");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/customers/" + newCustomerId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        System.out.println("total active: " + initialActiveCount);

        var newActiveCount = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/active")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("size()");

        System.out.println("total new active: " + newActiveCount);

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/active")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(initialActiveCount - 1));

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/inactive")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(initialInactiveCount + 1));
    }

    private int getNewCustomerId() {
        var requestParams = new HashMap<>();
        requestParams.put("firstName", "Kalil teste");
        requestParams.put("lastName", "Peixoto teste");
        requestParams.put("email", "kalilteste@mail.com");

        var newCustomerId = given().when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/customers")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        assertThat(newCustomerId).isNotZero();

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/customers/" + newCustomerId)
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Kalil teste"))
                .body(containsString("Peixoto teste"))
                .body(containsString("kalilteste@mail.com"));

        return newCustomerId;
    }

    @Test
    void testAllWithUserRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", greaterThanOrEqualTo(3))
                .body(containsString("jason.bourne@mail.hello"))
                .body(containsString("homer.simpson@mail.hello"))
                .body(containsString("peter.quinn@mail.hello"));
    }

    @Test
    void testAllActiveUsersWithUserRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers/active")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Jason"))
                .body(containsString("Bourne"));
    }

    @Test
    void testAllInactiveUsersWithUserRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers/inactive")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("peter.quinn@mail.hello"));
    }

    @Test
    void testFindByIdWithUserRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers/1")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Jason"))
                .body(containsString("Bourne"));
    }

    @Test
    void testCreateWithUserRole() {
        var newCustomerId = this.getNewCustomerId();

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/customers/" + newCustomerId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteThenCustomerIsDisabledWithUserRole() {
        var newCustomerId = getNewCustomerId();

        var initialActiveCount = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers/active")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("size()");

        var initialInactiveCount = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers/inactive")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("size()");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .delete("/customers/" + newCustomerId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers/active")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(initialActiveCount - 1));

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/customers/inactive")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(initialInactiveCount + 1));

    }
}
