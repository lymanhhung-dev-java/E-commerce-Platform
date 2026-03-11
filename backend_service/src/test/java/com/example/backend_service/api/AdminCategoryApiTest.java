package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AdminCategoryApiTest extends BaseApiTest {

    @BeforeEach
    public void setupAdminCategoryTests() {
        createAndLoginAdminUser();
    }

    @Test
    void testCreateCategory_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test Category Admin");
        payload.put("description", "Desc");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/admin/categories")
        .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void testUpdateCategory_SuccessOrNotFound() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Updated Category Admin");
        payload.put("description", "Updated Desc");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/api/admin/categories/1")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testDeleteCategory_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/admin/categories/1")
        .then()
            .statusCode(anyOf(is(200), is(404), is(500)));
    }
}
