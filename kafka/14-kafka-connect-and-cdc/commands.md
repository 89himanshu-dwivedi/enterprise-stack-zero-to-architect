# Commands — Kafka Connect and CDC

Everything from the walkthrough, in the order you would actually run it.

---

## 1. Bring the stack up

```bash
cd project
docker compose up -d --build
```

The `--build` matters the first time: the `kafka-connect` image has to resolve
the connector jars with Maven before the worker can load them.

```bash
docker ps -a
```

You should see: `kafka`, `orders-db`, `analytics-db`, `minio`, `kafka-connect`,
`order-service`, `analytics-service`, plus two containers that ran once and
exited — `minio-init` and `connector-init`.

Follow the worker while it starts:

```bash
docker logs -f kafka-connect
```

---

## 2. Talk to the Connect REST API

This only exists in **distributed** mode.

```bash
# which connectors are registered
curl -s http://localhost:8083/connectors | jq

# the plugins the worker found on its plugin path
curl -s http://localhost:8083/connector-plugins | jq '.[].class'

# the config of one connector - identical to the json you posted
curl -s http://localhost:8083/connectors/orders-db-source/config | jq

# the status - this is read out of the connect-status topic
curl -s http://localhost:8083/connectors/orders-db-source/status | jq
curl -s http://localhost:8083/connectors/orders-to-analytics-jdbc-sink/status | jq
curl -s http://localhost:8083/connectors/orders-to-s3-sink/status | jq
```

Expected shape of a healthy status:

```json
{
  "name": "orders-db-source",
  "connector": { "state": "RUNNING", "worker_id": "kafka-connect:8083" },
  "tasks": [ { "id": 0, "state": "RUNNING", "worker_id": "kafka-connect:8083" } ],
  "type": "source"
}
```

Managing connectors at runtime — no restart needed:

```bash
curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @../connectors/s3-orders-sink.json
curl -X PUT  http://localhost:8083/connectors/orders-to-s3-sink/pause
curl -X PUT  http://localhost:8083/connectors/orders-to-s3-sink/resume
curl -X POST http://localhost:8083/connectors/orders-to-s3-sink/restart
curl -X POST http://localhost:8083/connectors/orders-to-s3-sink/tasks/0/restart
curl -X DELETE http://localhost:8083/connectors/orders-to-s3-sink
```

---

## 3. Look at the source database — before anything happens

```bash
docker exec -it orders-db psql -U order_user -d orders
```

```sql
select * from orders;
--  (0 rows)

-- the CDC plumbing the init script created
select slot_name, plugin, active from pg_replication_slots;
select pubname, tablename from pg_publication_tables;
\q
```

---

## 4. Create an order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "product": "Mechanical Keyboard", "quantity": 1, "amount": 5499.00}'
```

```json
{ "id": 1, "orderNumber": "ORD-9F3A21C4", "customerId": 1, "status": "PENDING", ... }
```

Confirm the row exists:

```bash
docker exec -it orders-db psql -U order_user -d orders -c "select id, order_number, status from orders;"
```

---

## 5. Watch it arrive in Kafka

```bash
docker exec -it kafka bash
cd /opt/kafka/bin
```

```bash
./kafka-topics.sh --bootstrap-server localhost:29092 --list
```

You will see the three internal topics — created automatically, you never asked
for them:

```
connect-configs
connect-offsets
connect-status
order_service.public.orders
```

Read the data topic from the beginning:

```bash
./kafka-console-consumer.sh \
  --bootstrap-server localhost:29092 \
  --topic order_service.public.orders \
  --from-beginning
```

```json
{"id":1,"order_number":"ORD-9F3A21C4","customer_id":1,"product":"Mechanical Keyboard","quantity":1,"amount":5499.00,"status":"PENDING", ...}
```

Peek inside the internal topics too — this is where the whole design becomes obvious:

```bash
./kafka-console-consumer.sh --bootstrap-server localhost:29092 \
  --topic connect-configs --from-beginning --property print.key=true

./kafka-console-consumer.sh --bootstrap-server localhost:29092 \
  --topic connect-offsets --from-beginning --property print.key=true

./kafka-console-consumer.sh --bootstrap-server localhost:29092 \
  --topic connect-status --from-beginning --property print.key=true
```

The sink connectors are ordinary consumer groups. Prove it:

```bash
./kafka-consumer-groups.sh --bootstrap-server localhost:29092 --list
./kafka-consumer-groups.sh --bootstrap-server localhost:29092 \
  --group connect-orders-to-analytics-jdbc-sink --describe
```

`exit` when done.

---

## 6. Check the analytics database

Nobody wrote a consumer for this. The JDBC sink did it.

```bash
docker exec -it analytics-db psql -U analytics_user -d analytics -c "select * from order_events;"
```

Or through the service:

```bash
curl -s http://localhost:8081/api/analytics/orders | jq
```

The `order_number` must match the one in the Kafka payload and the one in
`orders-db`. Three copies, one write.

---

## 7. Check S3 (MinIO)

Console: <http://localhost:9001> — user `minioadmin`, password `minioadmin`.

Navigate: bucket `order-events` → `topics/order_service.public.orders/partition=0/`

Or from the CLI:

```bash
docker run --rm --network project_connect-net minio/mc:latest /bin/sh -c \
  "mc alias set local http://minio:9000 minioadmin minioadmin && \
   mc ls --recursive local/order-events"
```

Through the service:

```bash
curl -s http://localhost:8081/api/analytics/s3/objects | jq
curl -s http://localhost:8081/api/analytics/s3/buckets | jq
```

---

## 8. Create a second order and watch all three land

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 10, "product": "DEF Monitor", "quantity": 2, "amount": 21998.00}'
```

```bash
curl -s http://localhost:8081/api/analytics/orders | jq 'length'   # 2
docker exec -it analytics-db psql -U analytics_user -d analytics -c "select count(*) from order_events;"
```

Refresh MinIO — a second object.

## 8b. Prove it is CDC, not polling

An `UPDATE` also goes through the WAL:

```bash
curl -X PATCH "http://localhost:8080/api/orders/1/status?status=SHIPPED"
```

The console consumer emits a new record with `"status":"SHIPPED"`, and the
JDBC sink upserts it in place because `pk.fields=id`.

---

## 9. Prove fault tolerance

```bash
docker restart kafka-connect
docker logs -f kafka-connect
```

While it is down, create an order. When the worker comes back it reads
`connect-configs` for its assignment and `connect-offsets` for its position,
and picks up from the next WAL record. Nothing is lost, nothing is duplicated.

Add a second worker to see a real rebalance:

```bash
docker compose up -d --scale kafka-connect=2   # requires removing container_name
curl -s http://localhost:8083/connectors/orders-db-source/status | jq '.tasks[].worker_id'
```

---

## 10. Tear down

```bash
docker compose down          # keep the volumes
docker compose down -v       # wipe everything, including the replication slot
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Source task `FAILED`, "logical decoding requires wal_level >= logical" | Postgres started without the flag | check the `command:` block on `orders-db`, then `docker compose down -v` |
| "must be superuser or replication role" | the `debezium` role has no `REPLICATION` | re-run `init/orders-db.sql`; it only runs on a **fresh** volume |
| "publication dbz_publication does not exist" | name mismatch | `publication.name` in the source json must equal the SQL `CREATE PUBLICATION` |
| Connector missing from `/connectors` but no error | `connector-init` exited early | `docker logs connector-init` |
| `ClassNotFoundException` on the connector class | jar not on the plugin path | `curl -s localhost:8083/connector-plugins` and check the Dockerfile copy paths |
| Sink writes nothing, source works | topic name mismatch | the sink's `topics` must equal `<topic.prefix>.<schema>.<table>` |
| `NoSuchMethodError` at worker startup | two connectors sharing one plugin folder | one directory per connector — Connect isolates classloaders per folder |
| Slot exists but nothing streams | an old slot from a previous run is `active=false` | `select pg_drop_replication_slot('debezium_orders');` |
