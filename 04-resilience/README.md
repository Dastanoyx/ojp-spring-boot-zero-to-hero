# Level 4 — Resilience: circuit breaker & slow-query segregation

OJP's defensive features live in the **server**, not your app. This module gives you three endpoints — fast, slow, and broken — so you can *watch* the server protect the database.

## The features

**Circuit breaker.** When the same query keeps failing, OJP trips a breaker and fails subsequent calls fast — without forwarding them to the database — then resets after a cooldown. A broken query can't spam your database into the ground.

**Slow-query segregation (SQS).** Long-running queries are kept in their own lane so they can't consume every connection that fast OLTP queries need. Picture a highway where trucks (slow analytical queries) stay in one lane while sports cars (fast queries) keep moving. Strongly recommended for **mixed** fast+slow workloads; usually unnecessary for pure OLTP or pure OLAP.

**Client-side reactive throttling.** When the server is under pressure it signals clients to back off; they recover on their own once pressure eases.

> These are configured on the OJP **server**. Exact property keys evolve between releases — see the canonical
> [OJP Server Configuration](https://github.com/Open-J-Proxy/ojp/blob/main/documents/configuration/ojp-server-configuration.md)
> and [Slow Query Segregation](https://github.com/Open-J-Proxy/ojp/blob/main/documents/designs/SLOW_QUERY_SEGREGATION.md) docs
> for the version you run. This module demonstrates the **behavior**; tune the keys per those docs.

## Run it

```bash
cd ../infra && docker compose up -d && cd ../04-resilience
./mvnw spring-boot:run
```

## Experiment A — slow-query segregation

In one terminal, saturate the slow lane:

```bash
# Fire 10 slow (5s) queries in the background
for i in $(seq 1 10); do curl -s "localhost:8080/slow?seconds=5" & done
```

Immediately in another terminal, hammer the fast endpoint:

```bash
for i in $(seq 1 20); do curl -s localhost:8080/fast; echo; done
```

**Without segregation:** fast calls block/queue behind the slow ones (connection starvation).
**With segregation enabled on the server:** fast calls keep returning `{"type":"fast","result":1}` promptly because they ride a separate lane.

## Experiment B — circuit breaker

```bash
# Repeatedly call the broken query
for i in $(seq 1 30); do curl -s localhost:8080/broken; echo; done
```

Watch the OJP server logs (`docker logs -f ojp-server`). After a threshold of failures, the breaker trips: further `/broken` calls fail **fast** at the proxy instead of repeatedly hitting PostgreSQL. After the cooldown, OJP probes recovery.

Confirm the database wasn't hammered: while the breaker is open, the count of failed statements at PostgreSQL stops climbing.

## Why this matters for advocacy

A single pathological query — a missing index, a runaway report, a typo'd table name deployed at 2am — is one of the most common ways a healthy database gets taken down. OJP turns "one bad query takes everything down" into "one bad query is isolated and the rest keeps serving."

Next: [Level 5 — Load test](../05-load-test/).
