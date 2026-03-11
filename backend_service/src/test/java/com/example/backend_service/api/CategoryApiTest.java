package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CategoryApiTest extends BaseApiTest {

    @Test
    void testGetCategories_Success() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/categories")
        .then()
            .statusCode(200);
    }

    @Test
    void testGetFlatCategories_Success() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/categories/flat")
        .then()
            .statusCode(200);
    }
}
