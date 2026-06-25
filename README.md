# API Testing Framework

This project automates testing of a booking API (the public restful-booker service), covering authentication and the full create-read-update-delete lifecycle of a booking. It's built two ways:

1. A **Postman collection** (runnable headlessly with Newman), and
2. A **REST Assured** framework (Java + Maven + TestNG).

Both cover **token authentication** and the **full CRUD lifecycle** of a booking.

![API Tests](https://github.com/ravirajnandagopal/api-testing-framework/actions/workflows/api-tests.yml/badge.svg)

## What I learned

Building this project taught me:

- How to set up a Java + Maven test project and run it with `mvn test`.
- How REST Assured sends API requests and checks responses (status codes, JSON fields).
- How to debug a real failure: the create-booking endpoint returned a 418 status because REST Assured was sending a multi-value `Accept` header. Setting `Accept: application/json` explicitly fixed it.
- How token authentication works — getting a token from `/auth` and using it to authorise the update and delete requests.

## License

MIT
