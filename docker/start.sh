#!/usr/bin/env bash
set -e

# Map ENV -> JVM system properties
# Giá trị của các biến môi trường Render được truyền vào CATALINA_OPTS dưới dạng -D
export CATALINA_OPTS="$CATALINA_OPTS \
  -DDB_URL=${DB_URL} \
  -DDB_USER=${DB_USER} \
  -DDB_PASS=${DB_PASS}"

# Khởi động Tomcat
exec catalina.sh run