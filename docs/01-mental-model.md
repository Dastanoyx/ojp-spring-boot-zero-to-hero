# 01 — The mental model

To advocate for OJP convincingly you need to *feel* the problem it solves. This page builds that intuition.

## The problem: the connection storm

A relational database can only handle a finite number of concurrent connections. PostgreSQL's default `max_connections` is around 100, and each connection costs real memory and scheduling overhead on the server. Connections are a **scarce, expensive, shared resource**.

Now look at a normal Spring Boot deployment. Each app instance carries its own HikariCP pool:

```
                     ┌──────────────────────────────────────┐
   App instance 1 ──▶│ HikariCP pool (max 10) ──┐             │
   App instance 2 ──▶│ HikariCP pool (max 10) ──┤             │
   App instance 3 ──▶│ HikariCP pool (max 10) ──┼──▶ DATABASE │  max_connections = 100
        ...          │          ...             │             │
   App instance N ──▶│ HikariCP pool (max 10) ──┘             │
                     └──────────────────────────────────────┘
```

With 5 instances × 10 = 50 connections, you're fine. But the whole point of cloud-native is **elastic autoscaling**. Traffic spikes → the orchestrator spins up 20 more instances → 25 × 10 = **250 connection slots demanded** against a database that allows 100.

What happens next is the connection storm:

1. New instances start, each trying to fill its pool.
2. The database hits `max_connections` and starts **refusing** new connections.
3. App instances throw `connection refused` / `too many clients` errors.
4. Health checks fail → the orchestrator kills and restarts instances → they try to reconnect → **more** connection pressure.
5. The database, now also burning memory on connection overhead, slows down for *everyone*, including the healthy instances.

The cruel irony: the failure happens **exactly during the traffic spike** — the moment you most needed to scale.

## Why "just lower the pool size" doesn't fix it

You could set each pool to `max 2`. Now 25 instances = 50 connections, safe. But under load each instance only has 2 connections for *all* its concurrent requests, so requests queue locally and latency explodes. You've traded a database outage for an app-tier bottleneck. There's no per-instance pool size that's simultaneously safe at scale *and* fast under load, because the pool is in the wrong place.

## The OJP inversion

OJP moves the pool out of every instance and into one central server:

```
   App instance 1 ──┐                ┌────────────────┐
   App instance 2 ──┤   gRPC          │   OJP Server   │
   App instance 3 ──┼───────────────▶ │  ONE HikariCP  │──▶ DATABASE   max_connections = 100
        ...         │   (virtual      │  pool (max 80) │
   App instance N ──┘    conns)       └────────────────┘
```

Key shifts:

- **Apps hold zero physical DB connections.** They get virtual connections; the real one is borrowed only for the microseconds a statement executes.
- **One pool, sized to the database**, not to the number of app instances. Add 100 more app instances and the database still sees at most 80 connections.
- **The server is the single gate.** When the database is saturated, OJP applies backpressure and clients back off — the database never gets stormed.

You can now scale the app tier freely. The database stays calm, and you can even run a *smaller* (cheaper) database than you'd otherwise need.

## The cost: one extra network hop

OJP is honest about the tradeoff — it adds a gRPC hop between app and database. Two reasons it's usually a net win:

1. The hop is HTTP/2 multiplexed and the server is typically co-located (same cluster/VPC), so the added latency is small.
2. Connection acquisition under a healthy central pool is often *faster and more predictable* than fighting for connections in a stressed per-instance setup. Early performance tests reported throughput improving rather than degrading under load — because you removed the contention, not just relocated it.

Rule of thumb: **OJP's value shows up under pressure** — heavy traffic, scaling events, erratic query mixes. For a single low-traffic instance, you won't see much; for an elastic fleet, it's transformative.

## Mapping the problem to the levels

| You're worried about... | Go to level |
|--------------------------|-------------|
| "Show me it works at all" | [01-hello-ojp](../01-hello-ojp/) |
| "Prove it's different from plain JDBC" | [02-jdbc-vs-ojp](../02-jdbc-vs-ojp/) |
| "I have more than one database" | [03-multi-database](../03-multi-database/) |
| "What about a bad query taking everything down?" | [04-resilience](../04-resilience/) |
| "Show me the connection-storm numbers" | [05-load-test](../05-load-test/) |
| "How do I see what's happening?" | [06-observability](../06-observability/) |
| "What if the OJP server itself dies?" | [07-multinode-ha](../07-multinode-ha/) |
| "Make the Spring wiring cleaner" | [08-spring-boot-starter](../08-spring-boot-starter/) |
| "Ship it to production" | [09-production-hardening](../09-production-hardening/) |

Next: [02 — Glossary](02-glossary.md).
