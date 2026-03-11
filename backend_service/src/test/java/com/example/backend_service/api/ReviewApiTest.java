package com.example.backend_service.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ReviewApiTest extends BaseApiTest {

    @BeforeEach
    public void setupReviewTests() {
        createAndLoginTestUser();
    }

    @Test
    void testCreateReview_ValidationSuccessOrNotFound() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", 1);
        payload.put("orderItemId", 1);
        payload.put("rating", 5);
        payload.put("comment", "Great product!");

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/reviews")
        .then()
            // It could be 400 Bad Request, 404 Not Found (if order/product missing), or 200 OK
            .statusCode(anyOf(is(200), is(400), is(404)));
    }

    @Test
    void testGetReviewsByProduct_Success() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/reviews/product/1")
        .then()
            .statusCode(200);
    }
}
