#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_container="genesis-security-test-$(date +%s)-$$"
test_password="$(od -An -N24 -tx1 /dev/urandom | tr -d ' \n')"
cleanup() { docker rm -f "$test_container" >/dev/null 2>&1 || true; }
trap cleanup EXIT
docker run --detach --name "$test_container" \
  -e MYSQL_ROOT_PASSWORD="$test_password" -e MYSQL_DATABASE=genesis_auth_test \
  -p 127.0.0.1::3306 mysql:8.4 >/dev/null
ready=false
for ((i=0; i<120; i++)); do
  if docker exec -e MYSQL_PWD="$test_password" "$test_container" mysqladmin ping --silent >/dev/null 2>&1; then ready=true; break; fi
  sleep 1
done
if [[ "$ready" != true ]]; then echo "MySQL de prueba no ha arrancado." >&2; exit 1; fi
export APP_ENV=development MYSQLHOST=127.0.0.1 MYSQLUSER=root MYSQLPASSWORD="$test_password"
export MYSQLDATABASE=genesis_auth_test MYSQL_SSL_MODE=DISABLED
MYSQLPORT="$(docker port "$test_container" 3306/tcp | sed 's/.*://')"
export MYSQLPORT
bash ./mvnw -Pintegration verify
node --test tests/ui.test.cjs
