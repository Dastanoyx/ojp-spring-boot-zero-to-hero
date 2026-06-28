# OJP + Spring Boot — Zero to Hero

> A complete, hands-on guide to **Open J Proxy (OJP)** with Spring Boot, built as a progressive set of runnable modules — from "what is this thing" to production hardening.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![OJP](https://img.shields.io/badge/OJP-0.4.7--beta-orange.svg)](https://github.com/Open-J-Proxy/ojp)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

---

## What is OJP, in one paragraph

**Open J Proxy (OJP)** is the first open-source **Type 3 JDBC driver** combined with a **Layer 7 database proxy server**. Instead of every application instance holding its own connection pool, your app talks plain JDBC to a thin OJP driver, which forwards execution over **gRPC (HTTP/2)** to a remote **OJP Server**. The server owns the *real* database connections through a shared **HikariCP** pool. The result: a smart "control plane" between your apps and your relational databases that gives you backpressure, circuit breaking, slow-query segregation, client-side load balancing, failover, and observability — all behind a standard JDBC API, with no application rewrite. Apache 2.0 licensed.

```
  ┌─────────────┐   JDBC    ┌──────────────┐   gRPC/HTTP2   ┌────────────┐   real conns   ┌────────────┐
  │ Spring Boot │ ────────▶ │  OJP JDBC    │ ─────────────▶ │ OJP Server │ ─────────────▶ │ PostgreSQL │
  │     App     │           │   Driver     │                │ (HikariCP) │                │   MySQL    │
  └─────────────┘           └──────────────┘                └────────────┘                └────────────┘
       app holds NO            returns a "virtual"            owns + pools the           right-sized,
       physical conn           connection until exec          actual connections         protected
```

## Why it matters (the elevator pitch for advocacy)

Traditional setup: 50 app instances × a 10-connection pool each = up to 500 connections fighting over a database that can comfortably serve 100. When you autoscale, you create a **connection storm** that takes the database down — exactly when you need it most. OJP inverts this: apps open **zero** physical connections; a single, well-tuned, central pool fronts the database. You can scale the app tier elastically while the database stays calm and *smaller* (and cheaper).

The surprising part: because the OJP driver returns a **virtual connection** and only forwards work when a statement actually executes, apps stop hoarding connections during business logic, prepare/parameter phases, and think-time.

---

## The learning path (9 levels)

| Level | Module | What you'll learn |
|------|--------|-------------------|
| 0 | [`docs/`](docs/) | Concept, mental model, the connection-storm problem, glossary |
| 1 | [`01-hello-ojp/`](01-hello-ojp/) | Minimal Spring Boot + OJP + PostgreSQL CRUD |
| 2 | [`02-jdbc-vs-ojp/`](02-jdbc-vs-ojp/) | Same app, two profiles — direct JDBC vs OJP, side by side |
| 3 | [`03-multi-database/`](03-multi-database/) | PostgreSQL **and** MySQL through one OJP server |
| 4 | [`04-resilience/`](04-resilience/) | Circuit breaker + slow-query segregation in action |
| 5 | [`05-load-test/`](05-load-test/) | Simulate a connection storm; capture before/after numbers |
| 6 | [`06-observability/`](06-observability/) | OpenTelemetry + Prometheus + Grafana dashboards |
| 7 | [`07-multinode-ha/`](07-multinode-ha/) | Client-side load balancing + failover with multinode URLs |
| 8 | [`08-spring-boot-starter/`](08-spring-boot-starter/) | The `spring-boot-starter-ojp` auto-config path |
| 9 | [`09-production-hardening/`](09-production-hardening/) | SSL/TLS, env profiles, JVM tuning, Docker/K8s |

Each module has its own `README.md` and is independently runnable.

---

## Prerequisites

- **Java 21+** (the OJP *server* requires 21; the *driver* only needs 11, but we standardize on 21)
- **Docker** + **Docker Compose**
- **Maven 3.9+**
- ~4 GB free RAM for the full multi-database + observability stack

## 60-second quick start

```bash
# 1. Start PostgreSQL, MySQL, and the OJP server
cd infra && docker compose up -d

# 2. Run the simplest example
cd ../01-hello-ojp && ./mvnw spring-boot:run

# 3. Create a book through OJP
curl -X POST localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"OJP Zero to Hero","author":"You"}'

# 4. Read it back
curl localhost:8080/books
```

That's it — your CRUD traffic just flowed through OJP instead of hitting PostgreSQL directly.

---

## The three things everyone gets wrong

OJP's Spring Boot integration is "change three lines," but those three lines are subtle. Every module obeys these rules:

1. **Exclude HikariCP** from `spring-boot-starter-jdbc`. Pooling now lives in the OJP server; a local pool would defeat the purpose.
2. **Use `SimpleDriverDataSource`** (`spring.datasource.type=org.springframework.jdbc.datasource.SimpleDriverDataSource`) so Spring creates/closes connections on demand instead of pooling them.
3. **Prefix the JDBC URL**: `jdbc:ojp[host:port]_<your-normal-jdbc-url>` and set the driver to `org.openjproxy.jdbc.Driver`.

See [`docs/troubleshooting.md`](docs/troubleshooting.md) for the symptoms when you forget one.

---

## Versions used in this repo

| Component | Version | Notes |
|-----------|---------|-------|
| OJP server + driver | `0.4.7-beta` | OJP is pre-1.0; beta is the current release line |
| Spring Boot | `3.3.x` | |
| PostgreSQL | `17` | |
| MySQL | `8.4` | |
| Java | `21` | |

> **Note on "stable":** OJP has not yet shipped a 1.0 GA, so every release today is a `-beta`. We pin to the current published beta and call out the [ROADMAP](https://github.com/Open-J-Proxy/ojp/blob/main/ROADMAP.md) for the path to production-ready 1.0.

---

## Credits & references

- OJP project: https://github.com/Open-J-Proxy/ojp
- Website: https://openjproxy.com
- Created by Rogerio Robetti; Apache 2.0 licensed
- This repo is an independent community learning guide, not an official OJP project.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
