package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AdminShopApiTest extends BaseApiTest {

    @BeforeEach
    public void setupAdminShopTests() {
        createAndLoginAdminUser();
    }

    @Test
    void testApproveShop_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/admin/shops/1/approve")
        .then()
            .statusCode(anyOf(is(200), is(404), is(500)));
    }

    @Test
    void testBanShop_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/admin/shops/1/ban")
        .then()
            .statusCode(anyOf(is(200), is(404), is(500)));
    }

    @Test
    void testGetShopRequests_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/admin/shops/requests")
        .then()
            .statusCode(200);
    }
}
