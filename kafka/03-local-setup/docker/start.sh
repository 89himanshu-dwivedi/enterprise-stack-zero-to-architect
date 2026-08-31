#!/usr/bin/env bash
set -euo pipefail

CONFIG=/opt/kafka/config/kraft/server.properties

: "${KAFKA_CLUSTER_ID:?KAFKA_CLUSTER_ID must be set (see docker-compose.yml)}"

# Lay down the metadata/checkpoints before any data can be written,
# the same way you create a schema before inserting rows. Re-runs are ignored.
kafka-storage.sh format \
  --cluster-id "${KAFKA_CLUSTER_ID}" \
  --config "${CONFIG}" \
  --ignore-formatted

exec kafka-server-start.sh "${CONFIG}"
