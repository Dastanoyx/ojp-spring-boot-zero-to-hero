# Level 7 — Multinode HA: load balancing & failover

A single OJP server is a single point of failure for all database access. This module runs **two OJP nodes** in front of the same PostgreSQL and lets the driver balance load and fail over between them — **with no external load balancer**, because OJP does it client-side.

## The multinode URL

List every node in the URL. The driver does load-aware routing across them, keeps a session sticky to one node, and fails over automatically when a node disappears:

```
jdbc:ojp[host1:port1,host2:port2]_<original-jdbc-url>
```

In this module:

```
jdbc:ojp[localhost:1059,localhost:1060]_postgresql://localhost:5432/defaultdb
```

That's the entire HA configuration on the app side — one line.

## Run it

```bash
# 1. PostgreSQL from infra must be running
cd ../infra && docker compose up -d postgres && cd ../07-multinode-ha

# 2. Put the JDBC drivers where the nodes can mount them
bash ../infra/drivers/download-drivers.sh drivers/ojp-libs

# 3. Start two OJP nodes (host ports 1059 and 1060)
docker compose -f docker-compose.multinode.yml up -d
docker compose -f docker-compose.multinode.yml ps

# 4. Run any app module against the multinode profile. Easiest: reuse Level 1's
#    app but point it at this property file, or copy application-multinode.properties
#    into a module's resources and run with that profile.
```

You can drop [`application-multinode.properties`](application-multinode.properties) into, e.g., the Level 1 app's `src/main/resources/` and run:

```bash
cd ../01-hello-ojp
cp ../07-multinode-ha/application-multinode.properties src/main/resources/application-multinode.properties
./mvnw spring-boot:run -Dspring-boot.run.profiles=multinode
```

## Experiment — kill a node, keep serving

```bash
# Start a steady trickle of traffic
while true; do curl -s -X POST localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"ha","author":"test"}' >/dev/null && echo -n "." ; sleep 0.3; done
```

While that loops, kill one node:

```bash
docker stop ojp-node-1
```

The dots should keep printing: the driver detects node-1 is gone and routes to node-2. Bring it back:

```bash
docker start ojp-node-1
```

Traffic rebalances across both nodes again.

## What to observe

- **Failover latency** — how quickly the driver gives up on the dead node and retries elsewhere.
- **Session stickiness** — within a transaction/session, requests stay on one node for consistency.
- **Rebalancing** — after recovery, load spreads back across nodes.

## Production notes

- Run nodes on **separate hosts / availability zones**, not one machine — co-locating them defeats the HA purpose (here they share a host only for the demo).
- See the canonical [Multinode Configuration](https://github.com/Open-J-Proxy/ojp/blob/main/documents/multinode/README.md) for routing/stickiness tuning and version-specific options.

## Advocacy angle

The usual objection to a database proxy is "now I've added a single point of failure." OJP's answer is built in: multinode with client-side failover, no extra HAProxy/NLB to run and pay for. That's a genuine differentiator versus traditional single-instance database proxies.

Next: [Level 8 — Spring Boot starter](../08-spring-boot-starter/).
