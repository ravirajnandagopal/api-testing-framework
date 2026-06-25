package com.qa.api.tests;

import com.qa.api.base.BaseTest;
import com.qa.api.endpoints.Routes;
import com.qa.api.payloads.Booking;
import com.qa.api.payloads.BookingDates;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertTrue;

/**
 * Full CRUD lifecycle against /booking.
 *
 * Methods are ordered with priorities so they form one coherent journey:
 * create -> read -> update (PUT) -> partial update (PATCH) -> delete -> verify.
 * Write operations authenticate with the token created in {@link BaseTest}.
 */
public class BookingCrudTests extends BaseTest {

    private int bookingId;

    private Booking sampleBooking() {
        return new Booking(
                "Jim",
                "Brown",
                111,
                true,
                new BookingDates("2024-01-01", "2024-01-10"),
                "Breakfast");
    }

    @Test(priority = 1, description = "CREATE: POST /booking returns a new booking id")
    public void createBooking() {
        Response response = given()
                .spec(requestSpec)
                .body(sampleBooking())
        .when()
                .post(Routes.BOOKING)
        .then()
                .statusCode(200)
                .body("bookingid", notNullValue())
                .body("booking.firstname", equalTo("Jim"))
                .body("booking.lastname", equalTo("Brown"))
                .body("booking.totalprice", equalTo(111))
        .extract()
                .response();

        bookingId = response.path("bookingid");
        assertTrue(bookingId > 0, "A positive booking id should be returned");
    }

    @Test(priority = 2, dependsOnMethods = "createBooking",
            description = "READ: GET /booking/{id} returns the created booking")
    public void readBooking() {
        given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
        .when()
                .get(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(200)
                .body("firstname", equalTo("Jim"))
                .body("lastname", equalTo("Brown"))
                .body("bookingdates.checkin", equalTo("2024-01-01"));
    }

    @Test(priority = 3, description = "READ ALL: GET /booking returns a non-empty list of ids")
    public void readAllBookings() {
        given()
                .spec(requestSpec)
        .when()
                .get(Routes.BOOKING)
        .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("bookingid[0]", notNullValue());
    }

    @Test(priority = 4, dependsOnMethods = "createBooking",
            description = "UPDATE: PUT /booking/{id} fully replaces the booking (requires auth)")
    public void updateBooking() {
        Booking updated = new Booking(
                "James",
                "Brown",
                250,
                false,
                new BookingDates("2024-02-01", "2024-02-05"),
                "Late checkout");

        given()
                .spec(requestSpec)
                .header("Cookie", "token=" + authToken)
                .pathParam("id", bookingId)
                .body(updated)
        .when()
                .put(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(200)
                .body("firstname", equalTo("James"))
                .body("totalprice", equalTo(250))
                .body("depositpaid", equalTo(false));
    }

    @Test(priority = 5, dependsOnMethods = "updateBooking",
            description = "PARTIAL UPDATE: PATCH /booking/{id} changes only selected fields (requires auth)")
    public void partiallyUpdateBooking() {
        String partial = "{ \"firstname\": \"Jamie\", \"totalprice\": 300 }";

        given()
                .spec(requestSpec)
                .header("Cookie", "token=" + authToken)
                .pathParam("id", bookingId)
                .body(partial)
        .when()
                .patch(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(200)
                .body("firstname", equalTo("Jamie"))
                .body("totalprice", equalTo(300))
                .body("lastname", equalTo("Brown")); // unchanged
    }

    @Test(priority = 6, dependsOnMethods = "createBooking",
            description = "DELETE: DELETE /booking/{id} removes the booking (requires auth)")
    public void deleteBooking() {
        given()
                .spec(requestSpec)
                .header("Cookie", "token=" + authToken)
                .pathParam("id", bookingId)
        .when()
                .delete(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(201);

        // Verify it is gone: a follow-up GET should now return 404.
        given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
        .when()
                .get(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(404);
    }

    @Test(priority = 7, description = "AUTH GUARD: write without a token is rejected")
    public void deleteWithoutTokenIsForbidden() {
        // Create a throwaway booking to attempt an unauthenticated delete on.
        int throwawayId = given()
                .spec(requestSpec)
                .body(sampleBooking())
        .when()
                .post(Routes.BOOKING)
        .then()
                .statusCode(200)
        .extract()
                .path("bookingid");

        given()
                .spec(requestSpec)
                .pathParam("id", throwawayId)
        .when()
                .delete(Routes.BOOKING_BY_ID)
        .then()
                .statusCode(403); // Forbidden without an auth token
    }
}
