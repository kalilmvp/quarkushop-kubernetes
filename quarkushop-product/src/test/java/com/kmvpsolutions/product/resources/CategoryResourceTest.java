package com.kmvpsolutions.product.resources;

import com.kmvpsolutions.commons.utils.KeyCloakRealmResource;
import com.kmvpsolutions.commons.utils.TestContainerResource;
import io.quarkus.test.common.QuarkusTestResource;
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

@QuarkusTest
@QuarkusTestResource(TestContainerResource.class)
@QuarkusTestResource(KeyCloakRealmResource.class)
public class CategoryResourceTest {

    private static String ADMIN_BEARER_TOKEN;
    private static String TEST_BEARER_TOKEN;

    @BeforeAll
    static void init() {
        ADMIN_BEARER_TOKEN =  System.getProperty("quarkus-admin-access-token");
        TEST_BEARER_TOKEN = System.getProperty("quarkus-test-access-token");
    }

    @Test
    void testFindAll() {
        get("/categories")
                .then()
                    .statusCode(OK.getStatusCode())
                    .body("size()", greaterThan(0));
    }

    @Test
    void testFindAllWithAdminRole() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/categories")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(2))
                .body(containsString("Phones & Smartphones"))
                .body(containsString("Mobile"))
                .body(containsString("Computers and Laptops"))
                .body(containsString("PC"));
    }

    @Test
    void testFindById() {
        get("/categories/2")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("description"))
                .body(containsString("Computers and Laptops"));
    }

    @Test
    void testProductsOfSpecificCategory() {
        get("/categories/2/products")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("description"));
    }

    @Test
    void testDoesNotFindProductsOfSpecificCategory() {
        get("/categories/10/products")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", equalTo(0));
    }

    @Test
    void testDeleteFailBecauseOnlyAdminCanDoThisOperation() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
        .delete("/categories/1")
                .then()
                .statusCode(FORBIDDEN.getStatusCode());
    }

    @Test
    void testDeleteFailBecauseThereIsProductAssociated() {
        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/categories/1")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .body(containsString(INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }

    @Test
    void testCreate() {
        var requestParams = new HashMap<>();
        requestParams.put("name", "Category test 01");
        requestParams.put("description", "Description for the category 01");
        requestParams.put("email", "kalilmvp@gmail.com");

        // create the new category
        var response =
                given().when()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .body(requestParams)
                    .post("/categories")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .jsonPath()
                        .getMap("$");

        assertThat(response.get("id")).isNotNull();
        assertThat(response).containsEntry("name", "Category test 01");
        assertThat(response).containsEntry("description", "Description for the category 01");
        assertThat(response.get("products").equals(0L));
    }
}
