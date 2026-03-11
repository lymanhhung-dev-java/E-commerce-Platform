package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserApiTest extends BaseApiTest {

    @BeforeEach
    public void setupUserTests() {
        createAndLoginTestUser();
    }

    @Test
    void testGetProfile_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/profile/me")
        .then()
            .statusCode(200)
            .body("username", equalTo(this.testUsername))
            .body("email", equalTo(this.testEmail));
    }

    @Test
    void testUpdateProfile_Success() {
        Map<String, String> payload = new HashMap<>();
        payload.put("fullName", "Updated User Name");
        payload.put("phoneNumber", "0999999999");
        payload.put("avatarUrl", "http://example.com/new-avatar.jpg");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/api/profile/update")
        .then()
            .statusCode(200)
            .body("fullName", equalTo("Updated User Name"))
            .body("phoneNumber", equalTo("0999999999"));
    }

    @Test
    void testChangePassword_Success() {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldPassword", this.testPassword);
        payload.put("newPassword", "NewPass123@");
        payload.put("confirmPassword", "NewPass123@");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/api/profile/change-password")
        .then()
            .statusCode(200);
            
        // Test login with new password
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", this.testUsername);
        loginPayload.put("password", "NewPass123@");

        given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
        .when()
            .post("/api/auth/acces-token")
        .then()
            .statusCode(200)
            .body("accessToken", notNullValue());
    }

    @Test
    void testChangePassword_Fail_WrongOldPassword() {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldPassword", "WrongPass123!");
        payload.put("newPassword", "NewPass123@");
        payload.put("confirmPassword", "NewPass123@");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/api/profile/change-password")
        .then()
            .statusCode(400); // Bad Request typically if old password doesn't match
    }
}
