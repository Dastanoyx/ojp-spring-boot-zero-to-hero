# 02 — Glossary

**Backpressure** — A flow-control mechanism: when the downstream (the database) can't keep up, the system signals upstream (the apps) to slow down rather than piling on more load. OJP applies backpressure at the server so a stressed database isn't stormed.

**Circuit breaker** — A resilience pattern. When a query or path keeps failing, the breaker "trips" and fails subsequent calls fast (without hitting the database) for a cooldown period, then resets to test recovery. Prevents a failing query from spamming the database to death.

**Connection storm** — The cascade where many app instances (often during autoscaling) simultaneously demand more database connections than the database allows, causing refusals, restarts, and a self-reinforcing outage.

**Control plane** — The layer that manages *how* traffic flows (routing, pooling, policy, observability), as opposed to the data plane that carries the traffic itself. OJP is a database control plane.

**gRPC** — A high-performance RPC framework over HTTP/2 using Protocol Buffers. OJP's driver↔server communication runs on gRPC: multiplexed, binary, and language-neutral.

**HikariCP** — The de facto fastest, most reliable JDBC connection pool in the Java ecosystem. In a normal app it lives per-instance; with OJP it lives once, inside the OJP Server.

**JDBC** — Java Database Connectivity, the standard Java API for relational databases. OJP is fully JDBC-compatible, so your app code doesn't change.

**Layer 7 proxy** — A proxy operating at the application layer (it understands the protocol's semantics — here, JDBC operations), as opposed to a Layer 4 proxy that just forwards TCP bytes.

**Multinode** — Running several OJP Servers together for high availability and load distribution. Clients use a multinode URL: `jdbc:ojp[host1:port1,host2:port2]_...`.

**OJP JDBC Driver** — The thin Type 3 driver you put in your app. Class: `org.openjproxy.jdbc.Driver`. Needs Java 11+.

**OJP Server** — The middleware service that owns the real connection pool and enforces all control-plane policy. Needs Java 21+. Default port **1059**.

**Session stickiness** — In multinode setups, keeping a given client session bound to the same OJP server node so stateful operations (like transactions) stay consistent.

**SimpleDriverDataSource** — A Spring `DataSource` that creates and closes connections on demand without pooling. Required with OJP so pooling is fully delegated to the OJP server.

**Slow-query segregation** — Isolating long-running queries into a separate lane so they can't consume all the connections that fast queries need. Recommended for mixed fast+slow workloads; usually unnecessary for pure OLTP or pure OLAP.

**Type 3 JDBC driver** — A driver that speaks a generic network protocol to a middleware server (which then reaches the database), rather than connecting to the database directly (Type 4). OJP is described as the only open-source Type 3 driver.

**Virtual connection** — The connection object OJP's driver hands back from `getConnection()`. It reserves no physical database connection; a real one is borrowed from the central pool only while a statement actually executes.
