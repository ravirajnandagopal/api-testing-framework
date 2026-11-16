package com.qa.api.tests;

import com.qa.api.base.BaseTest;
import com.qa.api.endpoints.Routes;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * Verifies the API is reachable before running functional suites.
 * restful-booker's /ping returns 201 Created when healthy.
 */
public class HealthCheckTests extends BaseTest {

    @Test(description = "API health check responds with 201")
    public void apiIsHealthy() {
        given()
                .spec(requestSpec)
        .when()
                .get(Routes.PING)
        .then()
                .statusCode(201);
    }
}
