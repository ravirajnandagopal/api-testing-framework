# API Testing Framework

API test automation for the public [restful-booker](https://restful-booker.herokuapp.com) service, delivered two ways:

1. A **Postman collection** (runnable headlessly with Newman), and
2. A **REST Assured** framework (Java + Maven + TestNG).

Both cover **token authentication** and the **full CRUD lifecycle** of a booking.

![API Tests](https://github.com/YOUR_USERNAME/api-testing-framework/actions/workflows/api-tests.yml/badge.svg)

## What's covered

| Operation        | HTTP             | Auth required | REST Assured                | Postman                       |
| ---------------- | ---------------- | ------------- | --------------------------- | ----------------------------- |
| Health check     | `GET /ping`      | No            | `HealthCheckTests`          | Health Check                  |
| Authenticate     | `POST /auth`     | —             | `AuthTests`                 | Auth - Create Token           |
| Bad credentials  | `POST /auth`     | —             | `AuthTests`                 | Auth - Invalid Credentials    |
| Create           | `POST /booking`  | No            | `BookingCrudTests`          | Create Booking                |
| Read one / all   | `GET /booking`   | No            | `BookingCrudTests`          | Get Booking / Get All         |
| Update (full)    | `PUT /booking`   | **Yes**       | `BookingCrudTests`          | Update Booking (PUT)          |
| Update (partial) | `PATCH /booking` | **Yes**       | `BookingCrudTests`          | Partial Update (PATCH)        |
| Delete           | `DELETE /booking`| **Yes**       | `BookingCrudTests`          | Delete Booking                |
| Auth guard       | `DELETE` no token| —             | `BookingCrudTests`          | —                             |

## Project structure

```
api-testing-framework/
├── pom.xml
├── testng.xml
├── postman/
│   ├── restful-booker.postman_collection.json
│   └── restful-booker.postman_environment.json
├── src/test/
│   ├── java/com/qa/api/
│   │   ├── base/BaseTest.java          # baseURI, request spec, token handshake
│   │   ├── config/ConfigManager.java   # properties + env/system overrides
│   │   ├── endpoints/Routes.java       # route catalogue
│   │   ├── payloads/                   # Booking, BookingDates, AuthCredentials POJOs
│   │   └── tests/
│   │       ├── HealthCheckTests.java
│   │       ├── AuthTests.java
│   │       └── BookingCrudTests.java
│   └── resources/config.properties
└── .github/workflows/api-tests.yml
```

## Part 1 — REST Assured (Java)

### Prerequisites
- JDK 17+
- Maven 3.8+

### Run

```bash
mvn test
```

This runs the suite defined in `testng.xml`. To point at a different host or credentials without editing files:

```bash
mvn test -Dbase.uri=https://restful-booker.herokuapp.com -Dauth.username=admin -Dauth.password=password123
```

(Environment variables `BASE_URI`, `AUTH_USERNAME`, `AUTH_PASSWORD` also work — handy in CI.)

### Design notes
- **`BaseTest`** builds a reusable `RequestSpecification` (JSON in/out + request/response logging) and performs the `POST /auth` handshake once, exposing a token to the write tests.
- **POJOs + Jackson** serialize request bodies, so tests describe data, not JSON strings.
- **`BookingCrudTests`** runs as an ordered journey (create → read → update → patch → delete → verify-404), and includes a negative case proving writes are rejected without a token.
- Reports land in `target/surefire-reports/`.

## Part 2 — Postman / Newman

### Run in Postman
Import both files from `postman/`, select the *Restful Booker - Public* environment, and run the collection. The Auth request stores the token as a collection variable that the write requests reuse automatically.

### Run headlessly with Newman

```bash
npm install -g newman newman-reporter-htmlextra

newman run postman/restful-booker.postman_collection.json \
  -e postman/restful-booker.postman_environment.json \
  -r cli,htmlextra \
  --reporter-htmlextra-export newman/report.html
```

Each request carries `pm.test(...)` assertions on status codes and response bodies, and the token / booking id are chained between requests via collection variables.

## CI/CD

`.github/workflows/api-tests.yml` runs two parallel jobs on every push and pull request:
- **rest-assured** — `mvn test`, uploading Surefire reports.
- **postman-newman** — runs the collection with Newman and uploads an HTML report.

> Replace `YOUR_USERNAME` in the badge URL after pushing to GitHub.

## Note on the public API

restful-booker is a free, shared sandbox that occasionally resets data or sleeps when idle. A test run may need a few seconds to wake it. The data created by these tests is disposable.

## License

MIT
