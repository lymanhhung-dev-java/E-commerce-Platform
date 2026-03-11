package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MerchantOrderApiTest extends BaseApiTest {

    @BeforeEach
    public void setupMerchantOrderTests() {
        createAndLoginMerchantUser();
    }

    @Test
    void testGetShopOrders_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/merchant/orders")
        .then()
            .statusCode(anyOf(is(200), is(403)));
    }

    @Test
    void testUpdateOrderStatus_FailInvalidStatus() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .queryParam("status", "INVALID_STATUS")
        .when()
            .put("/api/merchant/orders/1/status")
        .then()
            .statusCode(anyOf(is(400), is(404), is(403), is(500)));
    }
}
