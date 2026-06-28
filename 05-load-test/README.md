# Level 5 — Load test: the connection-storm proof

This is your advocacy centerpiece: numbers. Run the *same* load against the **direct-JDBC** app (Level 2, `direct` profile) and the **OJP** app, and compare what happens at the database.

The headline metric is **not** request latency — it's **database connection count** and **error rate under a spike**. That's where OJP's value is visible.

## Prerequisites

- [k6](https://k6.io/docs/get-started/installation/) installed (`brew install k6` / `apt install k6` / Docker).
- `psql` client installed (for the connection sampler), or use the Docker exec equivalent.
- The Level 2 app, runnable in both profiles.

## The experiment

### Run 1 — direct JDBC (the "before")

```bash
# Terminal 1: start the app in direct mode
cd ../02-jdbc-vs-ojp && ./mvnw spring-boot:run -Dspring-boot.run.profiles=direct

# Terminal 2: sample DB connections
cd ../05-load-test && bash capture-db-connections.sh results/direct.csv

# Terminal 3: unleash the storm
k6 run -e BASE_URL=http://localhost:8080 k6/connection-storm.js
```

### Run 2 — OJP (the "after")

```bash
# Terminal 1: restart the app in OJP mode
cd ../02-jdbc-vs-ojp && ./mvnw spring-boot:run -Dspring-boot.run.profiles=ojp

# Terminal 2: sample again, new file
cd ../05-load-test && bash capture-db-connections.sh results/ojp.csv

# Terminal 3: identical storm
k6 run -e BASE_URL=http://localhost:8080 k6/connection-storm.js
```

## What to compare

| Signal | Where | Direct JDBC | OJP |
|--------|-------|-------------|-----|
| Peak DB connections | `results/*.csv` | climbs toward the pool max per instance | stays near the OJP central pool size |
| `http_req_failed` rate | k6 summary | rises during the spike (connection refusals) | stays low |
| `p(95)` latency | k6 summary | spikes under contention | flatter |
| Recovery after spike | csv tail | connections linger (pool stays full) | drains quickly |

To make the contrast dramatic, scale the *direct* run to several instances (different ports) so their pools sum past PostgreSQL's `max_connections` — that's when you reproduce a real connection storm and watch direct-JDBC fail while OJP absorbs it.

## Turning this into a talk slide

Plot both CSVs on one chart (connections vs time). The direct line marches toward the ceiling and flatlines at failure; the OJP line stays low and calm. One image, whole argument.

> Numbers depend heavily on your hardware, pool sizes, and `max_connections`. Always publish your test parameters alongside results — OJP's own materials note performance figures are encouraging but preliminary, so be precise and reproducible rather than absolute.

Next: [Level 6 — Observability](../06-observability/).
