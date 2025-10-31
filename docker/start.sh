#!/usr/bin/env sh
set -e

# fallback khi chạy local
: "${PORT:=8080}"

# Cho Tomcat biết PORT dưới dạng system property
export JAVA_OPTS="$JAVA_OPTS -DPORT=${PORT} -Djava.net.preferIPv4Stack=true"

# DB (tuỳ bạn đang dùng Postgres)
export CATALINA_OPTS="$CATALINA_OPTS \
  -DDB_URL=${DB_URL:-} \
  -DDB_USER=${DB_USER:-} \
  -DDB_PASS=${DB_PASS:-}"

# Log để debug nhanh khi cần
echo "[start.sh] PORT=${PORT}"
echo "[start.sh] DB_URL=${DB_URL}"

exec catalina.sh run
