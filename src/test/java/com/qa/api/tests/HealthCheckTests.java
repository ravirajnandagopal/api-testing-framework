package com.qa.api.tests;

import com.qa.api.base.BaseTest;
import com.qa.api.endpoints.Routes;
import com.qa.api.payloads.Booking;
import com.qa.api.payloads.BookingDates;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;



/**
 * Negative scenarios: the API must reject writes without a valid token
 * and return 404 for bookings that do not exist.
 * Each test class creates its own booking, so these tests do not depend
 * on BookingCrudTests.
 */
public class BookingNegativeTests extends BaseTest {

    private static final int NON_EXISTENT_ID = 999_999_999;

    private int bookingId;

    private Booking sampleBooking() {
        return new Booking("Negative", "Test", 100, true,
                new BookingDates("2024-03-01", "2024-03-05"), "None");
    }

    // Runs after BaseTest.setUp(): TestNG runs a parent class's @BeforeClass first.
    @BeforeClass
    public void createBookingForTests() {
        bookingId = given()
                .spec(requestSpec)
                .body(sampleBooking())
        .when()
                .post(Routes.BOOKING)
        .then()
                .statusCode(200)
        .extract()
                .path("bookingid");
    }

    @Test(description = "PUT without a token is rejected with 403 Forbidden")
    public void updateWithoutTokenIsForbidden() {
        given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .body(sampleBooking())
        .when()
                .put(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(403);
    }

    @Test(description = "DELETE with an invalid token is rejected with 403 Forbidden")
    public void deleteWithInvalidTokenIsForbidden() {
        given()
                .spec(requestSpec)
                .header("Cookie", "token=invalid-token")
                .pathParam("id", bookingId)
        .when()
                .delete(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(403);
    }

    @Test(description = "GET for a booking that does not exist returns 404 Not Found")
    public void getNonExistentBookingReturns404() {
        given()
                .spec(requestSpec)
                .pathParam("id", NON_EXISTENT_ID)
        .when()
                .get(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(404);
    }
}

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
