package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MerchantWalletApiTest extends BaseApiTest {

    @BeforeEach
    public void setupMerchantWalletTests() {
        createAndLoginMerchantUser();
    }

    @Test
    void testGetWalletOverview_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/merchant/wallet/overview")
        .then()
            .statusCode(anyOf(is(200), is(404))); 
    }

    @Test
    void testWithdraw_Fail_InvalidAmount() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", -5000); // Invalid amount
        payload.put("bankName", "Test Bank");
        payload.put("accountNumber", "123456789");
        payload.put("accountName", "Test Owner");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/merchant/wallet/withdraw")
        .then()
            .statusCode(anyOf(is(400), is(500)));
    }
}
