#!/usr/bin/env bash
#
# Downloads the open-source JDBC drivers the OJP Server needs and places them
# in ./ojp-libs (the directory mounted into the server container).
#
# Since OJP v0.4.0-beta the server does NOT bundle JDBC drivers — you mount them.
# This grabs PostgreSQL + MySQL (this repo's two databases) plus H2 for tests.
#
# Usage:  bash download-drivers.sh [target-dir]
#         (defaults to ./ojp-libs relative to this script)
#
# For proprietary databases (Oracle, SQL Server, DB2) download those JARs from the
# vendor and drop them in the same directory — no rebuild required.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="${1:-$SCRIPT_DIR/ojp-libs}"
mkdir -p "$TARGET_DIR"

# version pins — bump as needed
PG_VERSION="42.7.4"
MYSQL_VERSION="9.1.0"
H2_VERSION="2.3.232"

fetch() {
  local url="$1" out="$2"
  if [[ -f "$TARGET_DIR/$out" ]]; then
    echo "✓ $out already present"
  else
    echo "↓ downloading $out"
    curl -fsSL "$url" -o "$TARGET_DIR/$out"
  fi
}

echo "Downloading JDBC drivers into: $TARGET_DIR"

fetch "https://repo1.maven.org/maven2/org/postgresql/postgresql/${PG_VERSION}/postgresql-${PG_VERSION}.jar" \
      "postgresql-${PG_VERSION}.jar"

fetch "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_VERSION}/mysql-connector-j-${MYSQL_VERSION}.jar" \
      "mysql-connector-j-${MYSQL_VERSION}.jar"

fetch "https://repo1.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar" \
      "h2-${H2_VERSION}.jar"

echo
echo "Done. Drivers in $TARGET_DIR:"
ls -1 "$TARGET_DIR"
echo
echo "Now start the stack:  docker compose up -d"
