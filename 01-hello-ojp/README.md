# Level 1 — Hello OJP

The smallest possible thing that proves OJP works: a Spring Boot REST app doing CRUD on PostgreSQL, with **every query flowing through OJP** instead of hitting the database directly.

## Prerequisites

The shared stack must be running:

```bash
cd ../infra
bash drivers/download-drivers.sh
docker compose up -d
```

## Run it

```bash
./mvnw spring-boot:run
```

Watch the startup logs and confirm **no `HikariPool` lines appear** — that proves pooling has been delegated to the OJP server.

## Exercise the API

```bash
# Create
curl -X POST localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello OJP","author":"You"}'

# Read all
curl localhost:8080/books

# Read one
curl localhost:8080/books/1

# Update
curl -X PUT localhost:8080/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello OJP (2nd ed.)","author":"You"}'

# Delete
curl -X DELETE localhost:8080/books/1
```

## What just happened

```
curl → BookController → BookRepository (JPA)
     → SimpleDriverDataSource → OJP JDBC Driver
     → [virtual connection] → gRPC → OJP Server
     → [real pooled connection] → PostgreSQL
```

Your `BookRepository` is ordinary Spring Data JPA. It has no idea OJP exists. The only OJP-aware part of the whole app is three lines in `application.properties` plus the dependency setup in `pom.xml`.

## The two files worth reading

- **`pom.xml`** — note the `<exclusion>` removing HikariCP from `spring-boot-starter-data-jpa`, and the `ojp-jdbc-driver` dependency replacing the native PostgreSQL driver on the app side.
- **`src/main/resources/application.properties`** — the three OJP lines, each commented.

## Things to try

1. Stop the OJP server (`docker stop ojp-server`) and hit the API — see how failure surfaces. Restart it and recover.
2. Set `logging.level.com.zaxxer.hikari=DEBUG` (already on) and confirm Hikari never initializes.
3. Connect to PostgreSQL directly (`psql -h localhost -U testuser defaultdb`) and run `SELECT count(*) FROM pg_stat_activity;` while your app is idle vs. busy — observe how few connections OJP keeps open.

Next: [Level 2 — JDBC vs OJP side by side](../02-jdbc-vs-ojp/).
