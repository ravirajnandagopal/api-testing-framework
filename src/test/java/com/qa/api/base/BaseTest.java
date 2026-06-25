package com.qa.api.base;

import com.qa.api.config.ConfigManager;
import com.qa.api.endpoints.Routes;
import com.qa.api.payloads.AuthCredentials;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Shared setup for every test class:
 *  - configures the base URI,
 *  - builds a reusable request specification (JSON in/out, logging),
 *  - obtains an auth token used by the write operations.
 */
public abstract class BaseTest {

    protected RequestSpecification requestSpec;
    protected String authToken;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        RestAssured.baseURI = ConfigManager.get("base.uri");

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

        authToken = createToken();
    }

    /**
     * Calls POST /auth and returns a valid token. Centralised here so any
     * write test can authenticate without repeating the handshake.
     */
    protected String createToken() {
        AuthCredentials credentials = new AuthCredentials(
                ConfigManager.get("auth.username"),
                ConfigManager.get("auth.password"));

        return given()
                .spec(requestSpec)
                .body(credentials)
        .when()
                .post(Routes.AUTH)
        .then()
                .statusCode(200)
                .body("token", notNullValue())
        .extract()
                .path("token");
    }
}
