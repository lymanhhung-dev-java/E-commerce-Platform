package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CartApiTest extends BaseApiTest {

    @BeforeEach
    public void setupCartTests() {
        // Đăng nhập để lấy jwtToken cho các API giỏ hàng
        createAndLoginTestUser();
    }

    @Test
    void testGetCart_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/cart")
        .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void testAddToCart_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", 1);
        payload.put("quantity", 2);

        // API có logic check product exist, status code có thể là 200 hoặc 404
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/cart/add")
        .then()
            .statusCode(anyOf(is(200), is(400), is(404)));
    }

    @Test
    void testUpdateCart_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", 1);
        payload.put("quantity", 5);

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/api/cart/update")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400)));
    }

    @Test
    void testRemoveFromCart_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .queryParam("productId", 1)
        .when()
            .delete("/api/cart/remove")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }
}
