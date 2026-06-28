# Level 9 — Production hardening

Everything you need to think about before OJP fronts a real database in production: TLS, environment-specific tuning, JVM settings, deployment, and operational guardrails.

> Reminder: OJP is currently a `-beta` (pre-1.0). Treat production use as a careful pilot — bake in observability (Level 6), HA (Level 7), rollback, and load testing (Level 5) before you depend on it. Track the [ROADMAP](https://github.com/Open-J-Proxy/ojp/blob/main/ROADMAP.md) toward 1.0 GA.

## 1. TLS / SSL everywhere

Two hops to secure:

1. **App ↔ OJP server** (the gRPC channel) — encrypt so JDBC payloads and credentials aren't in cleartext on the wire.
2. **OJP server ↔ database** — the server holds the real connections, so configure the backend TLS there (Postgres `sslmode`, MySQL `useSSL`, etc.).

OJP supports server-side property placeholders for certificates across PostgreSQL, MySQL, Oracle, SQL Server, and DB2. Configure these per the canonical guide:
[SSL/TLS Certificate Configuration](https://github.com/Open-J-Proxy/ojp/blob/main/documents/configuration/ssl-tls-certificate-placeholders.md).

Rule of thumb: terminate TLS at the OJP server for backend connections, and enable transport security on the gRPC channel between driver and server.

## 2. Environment-specific configuration

OJP's JDBC driver reads an `ojp.properties` family so you can tune pool behavior per environment. This repo ships starting points:

- [`config/ojp-dev.properties`](config/ojp-dev.properties) — small pools, generous timeouts
- [`config/ojp-staging.properties`](config/ojp-staging.properties) — mid-size, production-like
- [`config/ojp-prod.properties`](config/ojp-prod.properties) — full-size pools, fail-fast timeouts

> The exact `ojp.*` keys vary by version — verify against
> [Connection Pool Configuration](https://github.com/Open-J-Proxy/ojp/blob/main/documents/configuration/ojp-jdbc-configuration.md).
> Note OJP also supports **multi-datasource** client config and a separate **XA** path (Apache Commons Pool 2) for distributed transactions.

Size the central server pool to the **database's** capacity (e.g., a bit under PostgreSQL `max_connections`), not to the number of app instances. That inversion is the whole value proposition — don't undo it by over-provisioning.

## 3. JVM tuning

- Run the server JVM in **UTC** (`-Duser.timezone=UTC`) to avoid timestamp surprises.
- In containers, prefer `-XX:MaxRAMPercentage=75.0` over fixed `-Xmx` so the heap tracks the container limit.
- Server needs **Java 21+**; the driver needs **Java 11+**.

## 4. Deployment

### Docker
See the canonical [Docker Deployment Guide](https://github.com/Open-J-Proxy/ojp/blob/main/documents/configuration/DOCKER_DEPLOYMENT.md) for JVM params, production examples, and troubleshooting. Remember (v0.4.0-beta+) to **mount JDBC drivers** into `ojp-libs`.

### Kubernetes
This module includes example manifests:

- [`k8s/ojp-server-deployment.yaml`](k8s/ojp-server-deployment.yaml) — 2+ replicas, spread across nodes, headless Service for client-side LB, resource limits, probes.
- [`k8s/app-deployment.yaml`](k8s/app-deployment.yaml) — a Spring Boot app scaled to 10 replicas (the database won't feel it), credentials via Secret.

There's also an official Helm chart project: [`ojp-helm`](https://github.com/Open-J-Proxy/ojp-helm) — prefer it over hand-rolled manifests where you can.

## 5. Drop-in proprietary drivers

Oracle, SQL Server, DB2, Oracle UCP — drop the vendor JARs into the server's `ojp-libs` (no rebuild). In K8s, deliver them via a PVC or an initContainer that downloads them at pod start. See [Drop-In External Libraries](https://github.com/Open-J-Proxy/ojp/blob/main/documents/configuration/DRIVERS_AND_LIBS.md).

## 6. Don't enable the experimental SQL enhancer in prod

OJP has an optional Apache Calcite-based SQL enhancer. It is **disabled by default**, marked experimental/not-recommended, and has known limitations with traditional JDBC databases. Leave it off unless you're deliberately testing it.

## 7. Production checklist

- [ ] TLS on the driver↔server gRPC channel
- [ ] TLS on server↔database connections
- [ ] Server pool sized to the database, not the fleet
- [ ] Env-specific `ojp.properties` (dev/staging/prod)
- [ ] 2+ OJP server replicas across zones (Level 7 HA)
- [ ] Multinode URL or service discovery so clients fail over
- [ ] Prometheus + Grafana wired (Level 6)
- [ ] Load test reproduced at expected peak + headroom (Level 5)
- [ ] Backend DB drivers mounted into `ojp-libs`
- [ ] SQL enhancer left OFF
- [ ] Rollback plan: revert URL prefix + restore native driver to bypass OJP fast
- [ ] Credentials in a secret manager, never in properties files

## The fast rollback

Because OJP is "a URL prefix + a driver," your emergency rollback is to remove the `ojp[...]_` prefix and swap the driver back to native. Keep that change one config-flip away — it's a strong safety story when proposing OJP to a cautious team.

That's the whole journey — from "what is this" to running it safely in production. Back to the [top-level README](../README.md).
