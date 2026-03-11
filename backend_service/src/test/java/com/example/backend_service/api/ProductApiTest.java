package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ProductApiTest extends BaseApiTest {

    @Test
    void testGetProducts_Success() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("pageable", notNullValue());
    }

    @Test
    void testGetProductDetail_SuccessOrNotFound() {
        // ID 1 might or might not exist, checking structure or generic responses
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/products/1")
        .then()
            .statusCode(anyOf(is(200), is(404))); 
    }

    @Test
    void testGetProductsByShop_Success() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/products/shop/1")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }
}
