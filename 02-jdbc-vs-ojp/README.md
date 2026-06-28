# Level 2 — JDBC vs OJP, side by side

The exact same application, switched between **direct JDBC (with a local HikariCP pool)** and **OJP** using only a Spring profile. This is your advocacy proof: identical code, two connection strategies, observable difference.

## Run the "before" — direct JDBC

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=direct
```

```bash
curl localhost:8080/books/whoami
# → {"activeProfile":"direct","dataSourceClass":"com.zaxxer.hikari.HikariDataSource"}
```

Note the startup logs: `HikariPool-1 (DirectHikari)` initializes. This instance holds its own pool.

## Run the "after" — OJP

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=ojp
```

```bash
curl localhost:8080/books/whoami
# → {"activeProfile":"ojp","dataSourceClass":"org.springframework.jdbc.datasource.SimpleDriverDataSource"}
```

No Hikari pool starts. Connections are delegated to the OJP server.

## See the difference at the database

Open a psql session to PostgreSQL and watch active connections while the app is idle:

```sql
SELECT count(*), application_name, state
FROM pg_stat_activity
WHERE datname = 'defaultdb'
GROUP BY application_name, state;
```

- **direct profile, idle:** up to `maximum-pool-size` (10) connections sit open and idle, reserved by this one instance.
- **ojp profile, idle:** the app holds **zero** physical connections; only the OJP server keeps a small central pool.

Now imagine 20 instances. Direct = up to 200 reserved connections. OJP = still one central pool. That's the whole story in one experiment.

## The only thing that changed

Compare the two profile files — that's the entire diff:

- [`application-direct.properties`](src/main/resources/application-direct.properties)
- [`application-ojp.properties`](src/main/resources/application-ojp.properties)

The Java is byte-for-byte identical between modes. OJP is a configuration change, not a rewrite.

Next: [Level 3 — Multi-database](../03-multi-database/).
