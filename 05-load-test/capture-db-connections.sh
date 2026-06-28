#!/usr/bin/env bash
# Samples PostgreSQL active connection count once per second into a CSV so you can
# chart "connections vs time" for direct-JDBC vs OJP runs.
#
#   bash capture-db-connections.sh results/direct.csv
#   bash capture-db-connections.sh results/ojp.csv

set -euo pipefail
OUT="${1:-results/connections.csv}"
mkdir -p "$(dirname "$OUT")"
echo "timestamp,connections" > "$OUT"

echo "Sampling pg_stat_activity every 1s -> $OUT  (Ctrl-C to stop)"
while true; do
  TS=$(date +%s)
  COUNT=$(PGPASSWORD=testpassword psql -h localhost -U testuser -d defaultdb -tAc \
    "SELECT count(*) FROM pg_stat_activity WHERE datname='defaultdb';" 2>/dev/null || echo "ERR")
  echo "$TS,$COUNT" >> "$OUT"
  sleep 1
done
