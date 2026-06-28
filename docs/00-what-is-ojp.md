# 00 — What is OJP?

## The one-sentence definition

Open J Proxy (OJP) is an open-source **Type 3 JDBC driver** paired with a **Layer 7 database proxy server** that moves connection pooling out of your application and into a shared, centrally-managed control plane.

## JDBC driver types (why "Type 3" matters)

JDBC drivers come in four historical types. The type tells you *where the database-specific logic lives* and *how the client reaches the database*.

| Type | Name | How it talks to the DB |
|------|------|------------------------|
| 1 | JDBC-ODBC bridge | Through a native ODBC layer (legacy, gone) |
| 2 | Native-API | Through vendor native client libraries on each machine |
| 3 | **Network-protocol (middleware)** | Client speaks a generic protocol to a **middleware server**, which then talks to the DB |
| 4 | Thin / pure-Java | Client speaks the DB's wire protocol directly |

Almost every driver you use today (the PostgreSQL driver, MySQL Connector/J, Oracle thin) is **Type 4** — it connects straight to the database. OJP is the rare **Type 3**: your app speaks a generic protocol (gRPC) to the **OJP middleware server**, and the server uses ordinary Type 4 drivers to reach the actual database. OJP is described as the only open-source Type 3 JDBC driver available.

That indirection is exactly what unlocks a control plane: because every query passes through middleware you control, you can add pooling, backpressure, routing, failover, and telemetry *without touching the database or rewriting the app*.

## The request path

```
Application
   │  (standard JDBC 4.2 calls: getConnection, prepareStatement, executeQuery …)
   ▼
OJP JDBC Driver  ──── returns a VIRTUAL connection (no physical DB conn yet)
   │  (on actual statement execution, serializes the call)
   ▼  gRPC over HTTP/2  (multiplexed, low-latency)
OJP Server
   │  owns the real connections, managed by a shared HikariCP pool
   ▼  ordinary Type 4 JDBC driver
PostgreSQL / MySQL / Oracle / SQL Server / DB2 / MariaDB / H2
```

## The "virtual connection" — the key insight

When your application calls `dataSource.getConnection()`, a normal driver immediately grabs a physical connection from a pool (or opens one). It then *holds* that connection through everything: preparing statements, binding parameters, running business logic between queries, think-time — all while a real database connection sits reserved.

OJP returns a **virtual connection** instead. No physical database connection is reserved. Only when a statement is actually **executed** does the driver send the work to the OJP Server over gRPC, and only then does the server briefly use a real pooled connection. The instant the work is done, that real connection returns to the central pool for any other app instance to use.

Consequence: a fleet of 100 app instances doing mostly CPU/logic work between queries no longer needs 100× pools of reserved connections. They share one well-tuned pool that's sized for *actual concurrent query execution*, not for *number of app instances*.

## What the OJP Server gives you (the control plane features)

- **Smart connection pooling** — one central HikariCP pool fronts the database; connections allocated only when a query actually runs.
- **Backpressure / connection-storm protection** — elastic app fleets can't exhaust the database; the server is the single gate.
- **Circuit breaker** — a fast-failing query gets tripped and blocked so it can't spam the database into the ground; it resets automatically.
- **Slow-query segregation** — long-running queries are kept in their own lane so they can't starve the connections needed by fast OLTP queries. (Think trucks vs. sports cars, each in its own lane.)
- **Client-side load balancing + failover** — multinode URLs (`jdbc:ojp[host1:port1,host2:port2]_...`) route load across multiple OJP servers with session stickiness and automatic failover — *no external load balancer required*.
- **Telemetry / observability** — OpenTelemetry + Prometheus hooks expose what's happening at the proxy layer.
- **Multi-database** — anything with a JDBC driver: PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, DB2, H2, and more.

## Why gRPC instead of just a TCP protocol?

gRPC runs over HTTP/2, which gives **multiplexing** (many concurrent requests over one connection), binary Protocol Buffers serialization (compact, fast), and a **language-neutral contract**. That last point is strategic: because the wire protocol is defined in `.proto` files, non-Java clients (Python, Node, Go) can eventually join the same control plane. The Java JDBC driver is just the first client.

## What OJP is *not*

- It is **not** an ORM or a query builder — your JPA/Hibernate/JdbcTemplate code is unchanged.
- It is **not** a database — it sits *in front of* your existing relational databases.
- It is **not** a Type 4 driver — it deliberately adds a middleware hop, and that hop is the point.
- It is **not** (yet) 1.0 GA — it's an actively developed beta, Apache 2.0 licensed.

## Requirements recap

- **OJP JDBC Driver:** Java 11+
- **OJP Server:** Java 21+

Next: [01 — The mental model](01-mental-model.md).
