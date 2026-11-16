package com.qa.api.tests;

import com.qa.api.base.BaseTest;
import com.qa.api.config.ConfigManager;
import com.qa.api.endpoints.Routes;
import com.qa.api.payloads.AuthCredentials;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Authentication test suite for POST /auth.
 * Covers a successful handshake and rejection of bad credentials.
 */
public class AuthTests extends BaseTest {

    @Test(description = "Valid credentials return a non-null token")
    public void validCredentialsReturnToken() {
        AuthCredentials creds = new AuthCredentials(
                ConfigManager.get("auth.username"), ConfigManager.get("auth.password"));

        String token = given()
                .spec(requestSpec)
                .body(creds)
        .when()
                .post(Routes.AUTH)
        .then()
                .statusCode(200)
                .body("token", notNullValue())
        .extract()
                .path("token");

        assertNotNull(token, "Token should be issued for valid credentials");
    }

    @Test(description = "Invalid credentials return a 'Bad credentials' reason instead of a token")
    public void invalidCredentialsAreRejected() {
        AuthCredentials creds = new AuthCredentials(ConfigManager.get("auth.username"), "wrong_password");

        given()
                .spec(requestSpec)
                .body(creds)
        .when()
                .post(Routes.AUTH)
        .then()
                .statusCode(200)
                .body("reason", equalTo("Bad credentials"));
    }

    @Test(description = "createToken() helper provides a usable token to the suite")
    public void sharedTokenIsAvailable() {
        assertNotNull(authToken, "Shared auth token should be created in setup");
    }
}
