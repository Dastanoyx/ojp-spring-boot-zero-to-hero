# Level 6 — Observability

A proxy you can't see into is a liability. OJP ships **OpenTelemetry traces** and **Prometheus metrics** covering its pools, query admission, classification, and throttling. This module wires Prometheus + Grafana so you can watch the control plane work.

## What OJP exposes

- **Prometheus metrics** — connection pool stats (active/idle/wait), admission decisions, slow-vs-fast classification, throttling events.
- **OpenTelemetry traces** — spans across the driver↔server↔database path, so a slow request is traceable end to end.

> **Verify the exact endpoint and metric names** for your OJP version against the canonical
> [Telemetry and Observability](https://github.com/Open-J-Proxy/ojp/blob/main/documents/telemetry/README.md) doc.
> The scrape target in `prometheus/prometheus.yml` and the metric names in the Grafana dashboard are
> **placeholders** — update them to match what your build emits. (This repo deliberately doesn't hardcode a
> port that might be wrong for your version.)

## Run it

```bash
# 1. Make sure the infra stack (incl. OJP server) is up
cd ../infra && docker compose up -d && cd ../06-observability

# 2. Enable telemetry on the OJP server per the telemetry docs (env vars / config),
#    then point prometheus.yml at the server's metrics endpoint.

# 3. Start Prometheus + Grafana
docker compose -f docker-compose.observability.yml up -d
```

- Prometheus UI: http://localhost:9090
- Grafana: http://localhost:3000 (admin / admin) → folder **OJP** → dashboard **OJP Overview**

## Suggested experiment

Run the Level 4 resilience experiments (slow queries, broken queries) and the Level 5 load test while watching Grafana. You should be able to *see*:

- pool utilization climb and plateau (backpressure holding the line),
- slow queries segregated into their own lane,
- the circuit breaker tripping (admission rejections rise, DB error rate flattens),
- throttling kicking in under load and recovering after.

## Files

- [`prometheus/prometheus.yml`](prometheus/prometheus.yml) — scrape config (update the target).
- [`grafana/dashboards/ojp-overview.json`](grafana/dashboards/ojp-overview.json) — starter dashboard (update metric names).
- [`docker-compose.observability.yml`](docker-compose.observability.yml) — Prometheus + Grafana.

## Advocacy angle

"Add a proxy" is a hard sell if it's a black box. "Add a proxy and get a Grafana dashboard of exactly how your database is being protected" is a much easier one. Observability is how you turn OJP from a leap of faith into an operational tool teams trust.

Next: [Level 7 — Multinode HA](../07-multinode-ha/).
