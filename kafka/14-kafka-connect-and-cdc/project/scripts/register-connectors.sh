#!/bin/sh
# Registers every connector json against the Connect REST API.
#
# This is pure convenience. In distributed mode you can POST these by hand at
# any time - that is the whole point of the REST API. Automating it here just
# means "docker compose up" gives you a working pipeline in one command.

set -e

CONNECT_URL="${CONNECT_URL:-http://kafka-connect:8083}"

echo "waiting for Kafka Connect at ${CONNECT_URL} ..."
until curl -s -f -o /dev/null "${CONNECT_URL}/"; do
  sleep 3
done
echo "Kafka Connect is up"

register() {
  file="$1"
  name=$(basename "$file" .json)
  echo "---> registering ${name}"
  code=$(curl -s -o /tmp/resp.txt -w "%{http_code}" \
      -X POST "${CONNECT_URL}/connectors" \
      -H "Content-Type: application/json" \
      -d @"${file}")
  if [ "$code" = "201" ] || [ "$code" = "200" ]; then
    echo "     created"
  elif [ "$code" = "409" ]; then
    echo "     already exists, skipping"
  else
    echo "     FAILED (http ${code})"
    cat /tmp/resp.txt
    echo ""
  fi
}

# Order matters only for readability - the source first, then the sinks.
register /connectors/debezium-orders-source.json
register /connectors/jdbc-analytics-sink.json
register /connectors/s3-orders-sink.json

echo ""
echo "registered connectors:"
curl -s "${CONNECT_URL}/connectors"
echo ""
