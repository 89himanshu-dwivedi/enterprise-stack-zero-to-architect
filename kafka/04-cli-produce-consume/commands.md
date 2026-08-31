# Kafka CLI cheat sheet

Everything used in part 04, in one place. Run these **inside the container**.

```bash
docker compose up -d          # from kafka/03-local-setup
docker ps                     # confirm the container is running
docker exec -it kafka bash    # interactive shell inside the broker
```

`--bootstrap-server` is how you connect to the cluster. It is required in almost every command.

---

## Topics

```bash
# create
kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-world --create
kafka-topics.sh --bootstrap-server localhost:9092 --topic test --create
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --create

# create with explicit settings
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --create \
  --partitions 3 --replication-factor 1

# delete  (same command, --delete instead of --create)
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --delete

# list  (no --topic needed - we want all of them)
kafka-topics.sh --bootstrap-server localhost:9092 --list

# describe
kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-world --describe
```

Defaults when you create a topic with just a name: **partitions = 1**, **replication factor = 1**, partition ids start at **0**.

---

## Producer

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic hello-world
# you get a shell - every line you type becomes a record

# lower the batching window (default 1000 ms)
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic hello-world --timeout 50

# send a key with each record
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic hello-world \
  --property parse.key=true --property key.separator=:
```

---

## Consumer

```bash
# default - only NEW messages
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-world

# read the topic from offset 0
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-world --from-beginning

# show the offset of every record
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-world \
  --property print.offset=true

# show key, offset, partition and timestamp
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-world --from-beginning \
  --property print.key=true \
  --property print.offset=true \
  --property print.partition=true \
  --property print.timestamp=true

# batch size per poll
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-world \
  --consumer-property max.poll.records=50
```

---

## On-disk look-around

```bash
ls /tmp/kraft-combined-logs                 # hello-world-0, test-0, __cluster_metadata-0, ...
ls /tmp/kraft-combined-logs/hello-world-0   # .log .index .timeindex
cat /tmp/kraft-combined-logs/hello-world-0/00000000000000000000.log   # binary, unreadable

# readable dump of a log segment
kafka-dump-log.sh --files /tmp/kraft-combined-logs/hello-world-0/00000000000000000000.log --print-data-log

# retention settings
grep retention /opt/kafka/config/kraft/server.properties
```

---

## Key parameters

| Parameter | Side | Default | What it does |
|---|---|---|---|
| `--timeout` / `linger.ms` | producer | 1000 ms | how long records are batched before being sent |
| `max.poll.records` | consumer | 500 | how many records one `poll()` returns |
| `--from-beginning` | consumer | off | start at offset 0 instead of only new records |
| `print.offset` | consumer | false | print the offset next to every record |
| `log.retention.hours` | broker | 168 (7 days) | delete records older than this |
| `log.retention.bytes` | broker | -1 (off) | delete oldest segments past this size |
