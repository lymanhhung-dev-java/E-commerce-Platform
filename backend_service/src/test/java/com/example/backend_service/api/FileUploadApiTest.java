package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class FileUploadApiTest extends BaseApiTest {

    @BeforeEach
    public void setupFileUploadTests() {
        createAndLoginTestUser();
    }

    @Test
    void testUploadAvatar_FailWithoutFile() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            // No multi-part file provided
        .when()
            .post("/api/upload/avatar")
        .then()
            .statusCode(anyOf(is(400), is(415), is(500))); 
    }

    @Test
    void testUploadProduct_FailWithoutFile() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            // No multi-part file provided
        .when()
            .post("/api/upload/product")
        .then()
            .statusCode(anyOf(is(400), is(415), is(500)));
    }
}
