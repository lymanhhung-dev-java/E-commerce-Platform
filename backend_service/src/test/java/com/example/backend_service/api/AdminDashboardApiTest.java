package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AdminDashboardApiTest extends BaseApiTest {

    @BeforeEach
    public void setupAdminDashboardTests() {
        createAndLoginAdminUser();
    }

    @Test
    void testGetDashboardStats_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/admin/dashboard/stats")
        .then()
            .statusCode(200)
            .body(notNullValue());
    }
}
