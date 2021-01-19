package com.kmvpsolutions.order;

import com.kmvpsolutions.commons.utils.TestContainerResource;
import com.kmvpsolutions.order.util.ContextTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(TestContainerResource.class)
@QuarkusTestResource(ContextTestResource.class)
public class OrderItemResourceTest {
    private static final AtomicInteger COUNTER = new AtomicInteger(100);

    private static String ADMIN_BEARER_TOKEN;
    private static String TEST_BEARER_TOKEN;

    private static final String INSERT_WRONG_ORDER_IN_DB =
            "insert into orders values (6, current_timestamp, current_timestamp, 'Cité Safia 2',NULL, 'Ksour', 'TN', 7160, NULL, 'CREATION', 0, 4, NULL)";
    private static final String DELETE_WRONG_ORDER_IN_DB =
            "delete from order_items where order_id = 6; delete from orders where id = 6;";

    @Inject
    DataSource datasource;

    @BeforeAll
    static void init() {
        ADMIN_BEARER_TOKEN = System.getProperty("quarkus-admin-access-token");
        TEST_BEARER_TOKEN = System.getProperty("quarkus-test-access-token");
    }

    @Test
    void testFindByOrderId() {
        get("/order-items/order/1")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testFindById() {
        get("/order-items/1")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testCreate() {
        get("/orders/3")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testDelete() {
        delete("/orders/1")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testFinByOrderIdWithAdminRole() {
        given()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
            .get("/order-items/order/1")
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    void testFindByIdWithAdminRole() {
        var newCustomerId = COUNTER.incrementAndGet();

        var newCartId = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        var requestParams = new HashMap<>();
        var cart = new HashMap<>();
        cart.put("id", newCartId);

        requestParams.put("cart", cart);

        var address = new HashMap<>();
        address.put("address1", "Any Address");
        address.put("city", "Washington");
        address.put("country", "US");
        address.put("postcode", "20121");

        requestParams.put("shipmentAddress", address);

        var orderResponse =
                given()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/orders")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body()
                .jsonPath()
                .getMap("$");

        var newOrderId = orderResponse.get("id");

        var orderItemRequestParams = new HashMap<>();
        orderItemRequestParams.put("quantity", 1);
        orderItemRequestParams.put("productId", 3);
        orderItemRequestParams.put("orderId", newOrderId);

        var orderItemResponse =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(orderItemRequestParams)
                .post("/order-items")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body()
                .jsonPath()
                .getMap("$");

        var newOrderItemId = orderItemResponse.get("id");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/order-items/" + newOrderItemId)
                .then()
                .statusCode(OK.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/orders/" + newOrderId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/carts/" + newCartId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testCreateWithAdminRole() {
        this.executeSQL(INSERT_WRONG_ORDER_IN_DB);

        var totalPrice = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/orders/6")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getDouble("totalPrice");

        assertThat(totalPrice).isZero();

        var orderItemRequestParams = new HashMap<>();
        orderItemRequestParams.put("quantity", 1);
        orderItemRequestParams.put("productId", 3);
        orderItemRequestParams.put("orderId", 6);

        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(orderItemRequestParams)
                .post("/order-items")
                .then()
                .statusCode(OK.getStatusCode());

        var newTotalPrice = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/orders/6")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getDouble("totalPrice");

        assertThat(newTotalPrice).isEqualTo(totalPrice + 1999);

        this.executeSQL(DELETE_WRONG_ORDER_IN_DB);
    }

    @Test
    void testDeleteWithAdminRole() {
        var newCustomerId = COUNTER.incrementAndGet();

        var newCartId = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        var requestParams = new HashMap<>();
        var cart = new HashMap<>();
        cart.put("id", newCartId);

        requestParams.put("cart", cart);

        var address = new HashMap<>();
        address.put("address1", "Any Address");
        address.put("city", "Washington");
        address.put("country", "US");
        address.put("postcode", "20121");

        requestParams.put("shipmentAddress", address);

        var orderResponse =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                        .body(requestParams)
                        .post("/orders")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .body()
                        .jsonPath()
                        .getMap("$");

        var newOrderId = orderResponse.get("id");

        var orderItemRequestParams = new HashMap<>();
        orderItemRequestParams.put("quantity", 1);
        orderItemRequestParams.put("productId", 3);
        orderItemRequestParams.put("orderId", newOrderId);

        var orderItemResponse =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                        .body(orderItemRequestParams)
                        .post("/order-items")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .body()
                        .jsonPath()
                        .getMap("$");

        var newOrderItemId = orderItemResponse.get("id");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/order-items/" + newOrderItemId)
                .then()
                .statusCode(OK.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/order-items/" + newOrderItemId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/orders/" + newOrderId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/carts/" + newCartId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testFindByIdWithUserRole() {
        var newCustomerId = COUNTER.incrementAndGet();

        var newCartId = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        var requestParams = new HashMap<>();
        var cart = new HashMap<>();
        cart.put("id", newCartId);

        requestParams.put("cart", cart);

        var address = new HashMap<>();
        address.put("address1", "Any Address");
        address.put("city", "Washington");
        address.put("country", "US");
        address.put("postcode", "20121");

        requestParams.put("shipmentAddress", address);

        var orderResponse =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                        .body(requestParams)
                        .post("/orders")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .body()
                        .jsonPath()
                        .getMap("$");

        var newOrderId = orderResponse.get("id");

        var orderItemRequestParams = new HashMap<>();
        orderItemRequestParams.put("quantity", 1);
        orderItemRequestParams.put("productId", 3);
        orderItemRequestParams.put("orderId", newOrderId);

        var orderItemResponse =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                        .body(orderItemRequestParams)
                        .post("/order-items")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .body()
                        .jsonPath()
                        .getMap("$");

        var newOrderItemId = orderItemResponse.get("id");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/order-items/" + newOrderItemId)
                .then()
                .statusCode(OK.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/orders/" + newOrderId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/carts/" + newCartId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testCreateWithUserRole() {
        this.executeSQL(INSERT_WRONG_ORDER_IN_DB);

        var totalPrice = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/orders/6")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getDouble("totalPrice");

        assertThat(totalPrice).isZero();

        var orderItemRequestParams = new HashMap<>();
        orderItemRequestParams.put("quantity", 1);
        orderItemRequestParams.put("productId", 3);
        orderItemRequestParams.put("orderId", 6);

        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(orderItemRequestParams)
                .post("/order-items")
                .then()
                .statusCode(OK.getStatusCode());

        var newTotalPrice = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/orders/6")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getDouble("totalPrice");

        assertThat(newTotalPrice).isEqualTo(totalPrice + 1999);

        this.executeSQL(DELETE_WRONG_ORDER_IN_DB);
    }

    @Test
    void testDeleteWithUserRole() {
        var newCustomerId = COUNTER.incrementAndGet();

        var newCartId = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .post("/carts/customer/" + newCustomerId)
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getInt("id");

        var requestParams = new HashMap<>();
        var cart = new HashMap<>();
        cart.put("id", newCartId);

        requestParams.put("cart", cart);

        var address = new HashMap<>();
        address.put("address1", "Any Address");
        address.put("city", "Washington");
        address.put("country", "US");
        address.put("postcode", "20121");

        requestParams.put("shipmentAddress", address);

        var orderResponse =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                        .body(requestParams)
                        .post("/orders")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .body()
                        .jsonPath()
                        .getMap("$");

        var newOrderId = orderResponse.get("id");

        var orderItemRequestParams = new HashMap<>();
        orderItemRequestParams.put("quantity", 1);
        orderItemRequestParams.put("productId", 3);
        orderItemRequestParams.put("orderId", newOrderId);

        var orderItemResponse =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                        .body(orderItemRequestParams)
                        .post("/order-items")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .body()
                        .jsonPath()
                        .getMap("$");

        var newOrderItemId = orderItemResponse.get("id");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/order-items/" + newOrderItemId)
                .then()
                .statusCode(OK.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .delete("/order-items/" + newOrderItemId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/orders/" + newOrderId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/carts/" + newCartId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    private void executeSQL(String query) {
        try (var connection = this.datasource.getConnection()) {
            connection.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
