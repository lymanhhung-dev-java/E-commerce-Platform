package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ShopApiTest extends BaseApiTest {

    @BeforeEach
    public void setupShopTests() {
        createAndLoginTestUser();
    }

    @Test
    void testGetShopById_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/shops/1")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }
}
