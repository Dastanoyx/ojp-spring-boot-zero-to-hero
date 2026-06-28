# FAQ

**Does OJP require changing my application code?**
No. You keep standard JDBC / JdbcTemplate / Spring Data JPA / Hibernate. You change three configuration lines (dependency exclusion, datasource type, URL prefix). No business-logic rewrite.

**Will the extra hop make my app slower?**
It adds one HTTP/2 gRPC hop, usually to a co-located server, so the added latency is small. Under load, removing per-instance connection contention often *improves* throughput. The benefit is most visible under pressure (spikes, autoscaling, mixed query loads), least visible for a single idle instance.

**Is OJP production-ready?**
It's actively developed and Apache 2.0 licensed, currently on a `-beta` line (pre-1.0). It's "battle-test-it-yourself" territory: great for evaluation, staging, and careful production pilots. Check the project ROADMAP for the path to 1.0 GA.

**Which databases work?**
Tested: PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, DB2, H2. In principle anything with a JDBC driver (CockroachDB and others reported).

**Do I still need an external load balancer in front of multiple OJP servers?**
No — OJP does client-side load balancing and failover natively via multinode URLs. That's a deliberate differentiator from traditional database proxies.

**Can non-Java apps use OJP?**
The wire protocol is gRPC + Protocol Buffers (language-neutral), so non-Java clients are on the roadmap. Today the mature client is the Java JDBC driver.

**What happens if the OJP server goes down?**
Single server = single point of failure for DB access. That's exactly why level 07 covers multinode HA: run two or more servers and let the driver fail over.

**How is this different from PgBouncer / ProxySQL?**
Those are database-specific (Postgres / MySQL) Layer-4-ish poolers. OJP is database-agnostic, operates at Layer 7 (understands JDBC operations), exposes a standard JDBC API to the app, does client-side LB/failover without an external balancer, and adds circuit breaking + slow-query segregation + OpenTelemetry.

**Does it support transactions?**
Yes — standard JDBC transactional semantics are preserved. On PostgreSQL, set `max_prepared_transactions` appropriately (see troubleshooting).

**What about connection metadata — how does the server know which DB to hit?**
When the driver opens its session, it sends the database URL, credentials, and driver details to the server, which then creates/manages the backend connections. No separate server-side config of each database is required for the basic case.
