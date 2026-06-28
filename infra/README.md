# infra — shared infrastructure

Everything in this repo runs against one shared stack: **PostgreSQL**, **MySQL**, and the **OJP Server**.

## Start it

```bash
# 1. Download the JDBC drivers the OJP server needs (one-time)
bash drivers/download-drivers.sh

# 2. Bring up Postgres + MySQL + OJP server
docker compose up -d

# 3. Check everything is healthy
docker compose ps
```

| Service | Port | Credentials |
|---------|------|-------------|
| PostgreSQL 17 | 5432 | testuser / testpassword, db `defaultdb` |
| MySQL 8.4 | 3306 | testuser / testpassword, db `defaultdb` |
| OJP Server | 1059 | — |

## Why these specific settings

- **`max_prepared_transactions=100`** on Postgres — required for some transactional paths through OJP.
- **`network_mode: host`** on the OJP server — keeps JDBC URLs simple (`localhost:1059`) and lets the server reach the DBs the same way your app does.
- **Mounted `ojp-libs`** — since v0.4.0-beta the server doesn't bundle JDBC drivers; `download-drivers.sh` fetches PostgreSQL, MySQL, and H2 into `drivers/ojp-libs`, which is mounted into the container.

## Stop it

```bash
docker compose down       # keep data
docker compose down -v    # wipe data volumes too
```

## Adding a proprietary database (Oracle / SQL Server / DB2)

Download the vendor JDBC JAR and drop it into `drivers/ojp-libs/`, then restart the OJP server container. No rebuild needed — this is OJP's drop-in external library support.
