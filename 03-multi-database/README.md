# Level 3 — Multi-database through one OJP server

One Spring Boot app talking to **PostgreSQL and MySQL at the same time**, both routed through the **single** OJP server. This shows OJP as a database-agnostic control plane: the app only ever ships the OJP driver; the backend DB drivers live in the server.

## Key idea

The app has two `DataSource` beans, each a `SimpleDriverDataSource` using `org.openjproxy.jdbc.Driver`. They differ only in the URL after the `ojp[...]_` prefix:

```
jdbc:ojp[localhost:1059]_postgresql://localhost:5432/defaultdb   ← Customers
jdbc:ojp[localhost:1059]_mysql://localhost:3306/defaultdb        ← Orders
```

Same OJP server (`localhost:1059`), two different backends. The OJP server picks the right backend driver from its `ojp-libs` (the `download-drivers.sh` script already put PostgreSQL **and** MySQL drivers there).

## Run it

```bash
# infra must be up (postgres + mysql + ojp-server)
cd ../infra && docker compose up -d && cd ../03-multi-database
./mvnw spring-boot:run
```

## Exercise both databases

```bash
# PostgreSQL-backed entity
curl -X POST localhost:8080/customers -H "Content-Type: application/json" -d '{"name":"Ada"}'
curl localhost:8080/customers

# MySQL-backed entity
curl -X POST localhost:8080/orders -H "Content-Type: application/json" -d '{"product":"Widget"}'
curl localhost:8080/orders
```

Customers persist in PostgreSQL; orders persist in MySQL; both arrived through OJP.

## Files worth reading

- [`config/PostgresConfig.java`](src/main/java/com/example/multidb/config/PostgresConfig.java) — the `@Primary` PostgreSQL datasource + JPA wiring.
- [`config/MysqlConfig.java`](src/main/java/com/example/multidb/config/MysqlConfig.java) — the second datasource, same OJP server.
- [`application.properties`](src/main/resources/application.properties) — two `ojp[...]_` URLs, one per backend.

## Why this matters for advocacy

A traditional setup needs a different proxy per database family (PgBouncer for Postgres, ProxySQL for MySQL, …). OJP is one control plane for all of them, behind one JDBC-shaped door.

Next: [Level 4 — Resilience](../04-resilience/).
