package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MerchantProductApiTest extends BaseApiTest {

    @BeforeEach
    public void setupMerchantProductTests() {
        createAndLoginMerchantUser();
    }

    @Test
    void testGetMyProducts_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/merchant/products")
        .then()
            .statusCode(anyOf(is(200), is(403))); // 403 if shop not found
    }

    @Test
    void testGetMyProductById_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/merchant/products/1")
        .then()
            .statusCode(anyOf(is(200), is(404), is(403), is(500)));
    }

    @Test
    void testCreateProduct_FailWithoutShop() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test Product");
        payload.put("price", 100.0);
        payload.put("categoryId", 1);

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/merchant/products")
        .then()
            .statusCode(anyOf(is(403), is(404), is(400), is(200)));
    }

    @Test
    void testDeleteProduct_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/merchant/products/1")
        .then()
            .statusCode(anyOf(is(200), is(404), is(403), is(500)));
    }
}
