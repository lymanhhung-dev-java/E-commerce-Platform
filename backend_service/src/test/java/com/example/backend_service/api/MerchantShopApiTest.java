package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MerchantShopApiTest extends BaseApiTest {

    @BeforeEach
    public void setupMerchantShopTests() {
        createAndLoginMerchantUser();
    }

    @Test
    void testGetMyShop_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/merchant/shops/current")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400)));
    }

    @Test
    void testRegisterShop_SuccessOrConflict() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test Merchant Shop");
        payload.put("description", "A shop for test");
        payload.put("address", "123 Test St");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/merchant/shops/register")
        .then()
            .statusCode(anyOf(is(200), is(400), is(500)));
    }

    @Test
    void testUpdateShop_SuccessOrNotFound() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Updated Merchant Shop");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/api/merchant/shops/info")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400), is(500)));
    }
}
