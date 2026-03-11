package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthApiTest extends BaseApiTest {

    @Test
    void testRegisterUser_Success() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        Map<String, String> regPayload = new HashMap<>();
        regPayload.put("username", "newUser_" + randomSuffix);
        regPayload.put("email", "newuser_" + randomSuffix + "@example.com");
        regPayload.put("phone", "0123456789");
        regPayload.put("fullName", "New User");
        regPayload.put("password", "Pass123@");
        regPayload.put("confirmPassword", "Pass123@");

        given()
            .contentType(ContentType.JSON)
            .body(regPayload)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void testRegisterUser_Fail_DuplicateUsername() {
        // Tạo trước 1 user thông qua helper
        createAndLoginTestUser();

        // Thử đăng ký lại với cùng username
        Map<String, String> regPayload = new HashMap<>();
        regPayload.put("username", this.testUsername);
        regPayload.put("email", "anotheremail@example.com");
        regPayload.put("phone", "0123456789");
        regPayload.put("fullName", "Another User");
        regPayload.put("password", "Pass123@");
        regPayload.put("confirmPassword", "Pass123@");

        given()
            .contentType(ContentType.JSON)
            .body(regPayload)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400);
    }

    @Test
    void testLogin_Success() {
        createAndLoginTestUser(); // Đã bao gồm đăng ký 1 user

        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", this.testUsername);
        loginPayload.put("password", this.testPassword);

        given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
        .when()
            .post("/api/auth/acces-token")
        .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("refreshToken", notNullValue());
    }

    @Test
    void testLogin_Fail_WrongPassword() {
        createAndLoginTestUser();

        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", this.testUsername);
        loginPayload.put("password", "WrongPass123!");

        given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
        .when()
            .post("/api/auth/acces-token")
        .then()
            .statusCode(isIn(new Integer[]{400, 401})); // Tuỳ cách xử lý exception
    }
}
