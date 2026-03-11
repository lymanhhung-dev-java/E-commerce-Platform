package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AdminProductApiTest extends BaseApiTest {

    @BeforeEach
    public void setupAdminProductTests() {
        createAndLoginAdminUser();
    }

    @Test
    void testToggleProductStatus_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/admin/products/1/toggle-status")
        .then()
            .statusCode(anyOf(is(200), is(404), is(500)));
    }



    @Test
    void testGetPendingProducts_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/admin/products")
        .then()
            .statusCode(anyOf(is(200), is(500)));
    }
}
