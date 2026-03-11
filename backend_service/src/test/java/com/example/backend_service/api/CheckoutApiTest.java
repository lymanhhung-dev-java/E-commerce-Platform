package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CheckoutApiTest extends BaseApiTest {

    @BeforeEach
    public void setupCheckoutTests() {
        createAndLoginTestUser();
    }

    @Test
    void testCheckout_InvalidAddress_Fail() {
        Map<String, Object> payload = new HashMap<>();
        // missing fields or invalid address id
        payload.put("addressId", 9999);
        payload.put("paymentMethod", "COD");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/checkout")
        .then()
            .statusCode(anyOf(is(404), is(400), is(500)));
    }

    @Test
    void testGetPaymentQr_NotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/checkout/999/payment-qr")
        .then()
            .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    void testGetPaymentStatus_NotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/checkout/999/payment-status")
        .then()
            .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    void testCancelOrder_NotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/checkout/999/cancel")
        .then()
            .statusCode(anyOf(is(404), is(400), is(500)));
    }
}
