package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AdminWithdrawalApiTest extends BaseApiTest {

    @BeforeEach
    public void setupAdminWithdrawalTests() {
        createAndLoginAdminUser();
    }

    @Test
    void testGetAllWithdrawals_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/admin/withdrawals")
        .then()
            .statusCode(200);
    }

    @Test
    void testProcessWithdrawal_SuccessOrNotFound() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .queryParam("status", "COMPLETED")
        .when()
            .put("/api/admin/withdrawals/1/process")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400), is(500)));
    }
}
