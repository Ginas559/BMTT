#!/usr/bin/env bash
set -e

export CATALINA_OPTS="$CATALINA_OPTS \
  -DDB_URL=${DB_URL} \
  -DDB_USER=${DB_USER} \
  -DDB_PASS=${DB_PASS}"

exec catalina.sh run