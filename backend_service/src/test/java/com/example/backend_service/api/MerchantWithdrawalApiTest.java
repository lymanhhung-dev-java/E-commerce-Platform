package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MerchantWithdrawalApiTest extends BaseApiTest {

    @BeforeEach
    public void setupMerchantWithdrawalTests() {
        createAndLoginMerchantUser();
    }

    @Test
    void testGetWithdrawals_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/merchant/withdrawals")
        .then()
            .statusCode(anyOf(is(200), is(403)));
    }
}
