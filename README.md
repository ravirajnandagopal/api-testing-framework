# API Testing Framework

![API Tests](https://github.com/ravirajnandagopal/api-testing-framework/actions/workflows/api-tests.yml/badge.svg)

Automated tests for the public [restful-booker](https://restful-booker.herokuapp.com) booking API, covering token authentication, the full create-read-update-delete lifecycle, JSON schema (contract) validation and negative scenarios. The same scenarios are implemented twice:

1. **REST Assured** framework – Java 17, Maven, TestNG
2. **Postman collection** – run headlessly in CI with Newman

Both run on every push via GitHub Actions.

## Tech stack

Java 17 · REST Assured · TestNG · Maven · Jackson · JSON Schema Validator · Postman · Newman · GitHub Actions

## Project structure

```
├── src/test/java/com/qa/api/
│   ├── base/BaseTest.java            # base URI, shared request spec, auth token
│   ├── config/ConfigManager.java     # config: system property > env variable > file
│   ├── endpoints/Routes.java         # all API paths in one place
│   ├── payloads/                     # Booking, BookingDates, AuthCredentials (POJOs)
│   └── tests/
│       ├── HealthCheckTests.java
│       ├── AuthTests.java
│       ├── BookingCrudTests.java
│       └── BookingNegativeTests.java
├── src/test/resources/
│   ├── config.properties
│   └── schemas/booking-schema.json   # JSON schema for GET /booking/{id}
├── postman/                          # collection + environment for Newman
├── testng.xml
└── pom.xml
```

## How to run

Requires Java 17+ and Maven.

```bash
mvn test                                          # run the REST Assured suite
mvn test -Dbase.uri=https://my-staging-server     # point at another environment
```

Postman collection (requires Node.js):

```bash
npm install -g newman
newman run postman/restful-booker.postman_collection.json \
  -e postman/restful-booker.postman_environment.json
```

## What's tested

| Area | Scenarios |
| --- | --- |
| Health | `GET /ping` responds (201) |
| Authentication | Valid credentials return a token · invalid credentials return "Bad credentials" |
| CRUD lifecycle | Create → read (incl. JSON schema check) → read all → full update (PUT) → partial update (PATCH, other fields unchanged) → delete → deleted booking returns 404 |
| Negative | PUT without token → 403 · DELETE with invalid token → 403 · unknown booking id → 404 |

## Design decisions

- **Shared request specification** – content type, `Accept` header and request/response logging are defined once in `BaseTest`.
- **POJO payloads** – request bodies are Java objects serialised by Jackson, so tests are readable and typos are caught at compile time.
- **Externalised config** – `ConfigManager` reads `config.properties` but lets CI override any value with a system property or environment variable (e.g. `BASE_URI`), so the same tests run against any environment and secrets can come from CI secrets.
- **Contract testing** – the read test validates the response against a JSON schema (required fields, types, date format), catching breaking API changes that single-field checks miss.
- **Ordered lifecycle with fail-safe dependencies** – the CRUD tests follow one booking through its life using `priority` and `dependsOnMethods`, so if *create* fails the dependent tests are skipped instead of producing a cascade of misleading failures. The negative tests create their own data and run independently.
- **Debugging note** – create-booking initially returned **418** because REST Assured sent a multi-value `Accept` header; setting `Accept: application/json` explicitly in the shared spec fixed it.

## API quirks worth knowing

restful-booker deliberately deviates from common REST conventions: `GET /ping` and `DELETE` return **201**, and wrong credentials return **200** with `{"reason": "Bad credentials"}` instead of 401. The tests assert the API's actual documented behaviour.

## Related projects

- [playwright-automation-framework](https://github.com/ravirajnandagopal/playwright-automation-framework) – Playwright + TypeScript UI tests
- [cicd-integration](https://github.com/ravirajnandagopal/cicd-integration) – merged cross-browser reports on GitHub Pages and a nightly regression


MIT
