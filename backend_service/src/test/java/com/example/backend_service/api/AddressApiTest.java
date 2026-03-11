package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AddressApiTest extends BaseApiTest {

    @BeforeEach
    public void setupAddressTests() {
        createAndLoginTestUser();
    }

    @Test
    void testGetAddresses_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/addresses")
        .then()
            .statusCode(200)
            .body("$", notNullValue());
    }

    @Test
    void testCreateAddress_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("receiverName", "Test Receiver");
        payload.put("phone", "0987654321");
        payload.put("city", "Hanoi");
        payload.put("ward", "Dich Vong");
        payload.put("street", "Xuan Thuy");
        payload.put("isDefault", true);

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/addresses")
        .then()
            .statusCode(200)
            .body("receiverName", equalTo("Test Receiver"));
    }

    @Test
    void testUpdateAddress_SuccessOrNotFound() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("receiverName", "Updated Receiver");
        payload.put("phone", "0123456789");
        payload.put("city", "HCM");
        payload.put("ward", "Ben Nghe");
        payload.put("street", "Nguyen Hue");
        payload.put("isDefault", false);

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/api/addresses/999")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400)));
    }

    @Test
    void testDeleteAddress_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/addresses/999")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400)));
    }
}
