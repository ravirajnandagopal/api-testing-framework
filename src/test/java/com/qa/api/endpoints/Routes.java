package com.qa.api.endpoints;

/**
 * Central catalogue of API routes, so endpoint paths are never duplicated
 * across tests.
 */
public final class Routes {

    public static final String PING = "/ping";
    public static final String AUTH = "/auth";
    public static final String BOOKING = "/booking";
    public static final String BOOKING_BY_ID = "/booking/{id}";

    private Routes() {
    }
}
