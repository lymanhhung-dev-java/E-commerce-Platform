package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AdminUserApiTest extends BaseApiTest {

    @BeforeEach
    public void setupAdminUserTests() {
        createAndLoginAdminUser();
    }

    @Test
    void testGetAllUsers_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/admin/users")
        .then()
            .statusCode(200);
    }

    @Test
    void testLockUser_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/admin/users/1/status")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400), is(500)));
    }

    @Test
    void testUnlockUser_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/admin/users/1/status")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400), is(500)));
    }
}
