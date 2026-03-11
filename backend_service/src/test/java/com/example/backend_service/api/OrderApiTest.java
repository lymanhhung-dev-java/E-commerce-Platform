package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderApiTest extends BaseApiTest {

    @BeforeEach
    public void setupOrderTests() {
        // Đăng nhập lấy jwtToken cho các API Order
        createAndLoginTestUser();
    }

    @Test
    void testGetMyOrders_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/orders/my-orders")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void testGetMyOrderDetails_NotFoundOrForbiddenForRandomId() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/orders/my-orders/999")
        .then()
            .statusCode(anyOf(is(403), is(404), is(400)));
    }
}
