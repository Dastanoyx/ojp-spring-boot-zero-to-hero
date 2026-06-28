# Troubleshooting — the three classic mistakes

OJP integration is "change three lines," but each line has a failure mode. Here's how to recognize and fix each.

## 1. You forgot to exclude HikariCP

**Symptom:** App starts fine, connections work, but you see HikariCP initializing in the logs (`HikariPool-1 - Starting...`) and you get *none* of OJP's benefits. Under load you still storm the database, because every instance is pooling locally — OJP is just an extra hop in front of a local pool.

**Fix:** Exclude HikariCP from `spring-boot-starter-jdbc`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

Verify: no `HikariPool` lines in the startup log.

## 2. You didn't set SimpleDriverDataSource

**Symptom:** Spring Boot can't find a pooling DataSource (you removed Hikari) and either fails to start (`Failed to determine a suitable driver class` / `Cannot determine embedded datasource`) or silently falls back to another pool.

**Fix:** Force the simple, non-pooling datasource so connection lifecycle is delegated to OJP:

```properties
spring.datasource.type=org.springframework.jdbc.datasource.SimpleDriverDataSource
```

`SimpleDriverDataSource` opens and closes a connection on demand — which is exactly what you want, because the OJP driver returns cheap virtual connections and the real pooling happens server-side.

## 3. The JDBC URL prefix is wrong

**Symptom:** `No suitable driver found for jdbc:ojp...`, or the driver tries to connect directly to the database and OJP is bypassed, or connection refused on the wrong port.

**The format is exact:**

```
jdbc:ojp[<ojp-host>:<ojp-port>]_<your-original-jdbc-url>
```

Examples:

```properties
# PostgreSQL
spring.datasource.url=jdbc:ojp[localhost:1059]_postgresql://localhost:5432/mydb
# MySQL
spring.datasource.url=jdbc:ojp[localhost:1059]_mysql://localhost:3306/mydb
# Oracle
spring.datasource.url=jdbc:ojp[localhost:1059]_oracle:thin:@localhost:1521/XEPDB1
# SQL Server
spring.datasource.url=jdbc:ojp[localhost:1059]_sqlserver://localhost:1433;databaseName=mydb
```

Common slips:
- Missing the `_` between `]` and the original URL.
- Pointing `[host:port]` at the **database** instead of the **OJP server** (it must be the OJP server — default port `1059`).
- Forgetting to set the driver class: `spring.datasource.driver-class-name=org.openjproxy.jdbc.Driver`.

## 4. (v0.4.0-beta and later) Database drivers not mounted into the server

**Symptom:** OJP server starts but fails to connect to your database with a "driver not found" / `ClassNotFoundException` for the backend DB driver.

**Why:** Since v0.4.0-beta, the OJP server does **not** bundle every JDBC driver. You must download the drivers and mount them into the server's `ojp-libs` directory (open-source ones via the provided script; proprietary ones like Oracle/SQL Server/DB2 you add yourself).

**Fix (Docker):**
```bash
mkdir -p ojp-libs
bash download-drivers.sh ojp-libs          # H2, PostgreSQL, MySQL, MariaDB
docker run --rm -d --network host \
  -v $(pwd)/ojp-libs:/opt/ojp/ojp-libs \
  rrobetti/ojp:0.4.7-beta
```

## 5. `max_prepared_transactions` on PostgreSQL

**Symptom:** Errors around prepared transactions when exercising certain transactional paths.

**Fix:** Start PostgreSQL with `-c max_prepared_transactions=100` (the `infra/docker-compose.yml` in this repo already does this).

## Quick sanity checklist

- [ ] No `HikariPool` lines in app startup logs
- [ ] `spring.datasource.type` is `SimpleDriverDataSource`
- [ ] URL is `jdbc:ojp[ojp-host:1059]_<original-url>` with the `_`
- [ ] `driver-class-name` is `org.openjproxy.jdbc.Driver`
- [ ] OJP server is running and reachable on `1059`
- [ ] The backend DB driver JAR is mounted into the server's `ojp-libs`
