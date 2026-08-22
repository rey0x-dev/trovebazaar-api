# TroveBazaar API

Backend for **TroveBazaar**, a multi-branch point-of-sale system for secondhand clothing retail (*ropa de paca*).

Merchandise is grouped by price tier rather than tracked per garment. Each tier has a QR code; the cashier scans it to build the ticket, and every sale is recorded against the branch where it happened.

> **Status:** early development. Requirements gathering in progress.

---

## Tech stack

### Core

| Technology | Purpose |
|---|---|
| **Java 21 (LTS)** | Language runtime. Virtual threads enabled. |
| **Spring Boot 4** | Application framework |
| **Maven** | Build and dependency management |
| **PostgreSQL** | Primary datastore |
| **Flyway** | Versioned schema migrations |

### Data access

| Technology | Purpose |
|---|---|
| **Spring Data JPA** | CRUD and entity mapping |
| **JdbcClient** | Raw SQL for dashboard aggregations |
| **Caffeine** | In-process cache for price resolution |

Dashboard queries are written as plain SQL rather than forced through the ORM. Time-bucketed aggregations are a poor fit for JPA, and the resulting queries are clearer and faster.

### Security

| Technology | Purpose |
|---|---|
| **Spring Security** | Authentication and authorization |
| **JWT** | Short-lived access token, held in memory |
| **httpOnly cookie** | Refresh token storage |

Tokens are never written to `localStorage`.

### Testing

| Technology | Purpose |
|---|---|
| **JUnit 5** | Test runner |
| **Testcontainers** | Real PostgreSQL instance per test run |
| **AssertJ** | Fluent assertions |

H2 is deliberately not used. Several domain invariants are enforced by PostgreSQL-specific features that an in-memory database cannot reproduce, so testing against anything else would give false confidence.

### API and observability

| Technology | Purpose |
|---|---|
| **springdoc-openapi** | OpenAPI 3 specification and Swagger UI |
| **Problem Details (RFC 7807)** | Standardized error responses |
| **Spring Actuator** | Health and readiness endpoints |
| **Micrometer** | Metrics collection |
| **Prometheus + Grafana** | Metrics storage and dashboards |

### Utilities

| Technology | Purpose |
|---|---|
| **ZXing** | QR code generation for price tags |

### Infrastructure

| Technology | Purpose |
|---|---|
| **Docker** | Multi-stage build on `eclipse-temurin:21-jre-alpine` |
| **GitHub Actions** | CI — full test suite with Testcontainers |
| **Dokploy + Traefik** | Deployment and reverse proxy |

---

## Design decisions

**The QR code carries an opaque identifier, not a price.** Embedding prices in the code would mean reprinting every label on a price change, would let anyone print a cheaper label, and would leave no record of what was actually sold. The code is a pointer; the price is resolved server-side at scan time.

**Prices are versioned per branch over time.** A single tag can cost a different amount at different locations, and prices can change without reprinting anything. Overlapping validity periods are prevented by a PostgreSQL `EXCLUDE USING gist` constraint rather than by application-level checks.

**Sale lines store a snapshot.** Each line copies the tag code, label and unit price at the time of sale. Later price changes never alter historical tickets.

**Every sale belongs to an open cash session.** Closed sessions are immutable — no retroactive sales, no edits.

**Nothing is deleted.** Voiding a ticket records a reversing entry.

**Money is stored as integer cents** (`bigint` / `long`). No floating point anywhere in the money path.

---

## Related repositories

- [`trovebazaar-pos`](https://github.com/rey0x-dev/trovebazaar-pos) — POS frontend
