# Part 09 command sheet — offset reset and replay

```bash
docker compose up                    # from kafka/03-local-setup
docker ps -a
docker exec -it kafka bash
```

## Setup

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --partitions 2 --create
```

Producer:

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic order-events
```

Consumer — `print.key` shows the partition key, `print.offset` shows the offset:

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events \
  --property print.key=true \
  --property print.offset=true \
  --group payment-service
```

## The `--from-beginning` gotcha

```bash
# with an existing group -> NOTHING. The group already has a committed offset.
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --group payment-service --from-beginning

# without a group -> EVERYTHING, because Kafka creates a throwaway group for you
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --from-beginning

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
# payment-service
# console-consumer-49032     <- you never created this one
```

## See the lag

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group payment-service --describe
```

Total pending = **sum of the lag on every partition**.

## Reset offsets

Always dry-run first. **The group must have no active members** — stop the consumers.

```bash
# --- rewind N records (negative back, positive forward)
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group payment-service --topic order-events \
  --reset-offsets --shift-by -1 --dry-run

kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group payment-service --topic order-events \
  --reset-offsets --shift-by -1 --execute

# --- everything from the start
--reset-offsets --to-earliest --execute

# --- skip the whole backlog, lag becomes 0
--reset-offsets --to-latest --execute

# --- go back by a period
--reset-offsets --by-duration PT1H --execute

# --- go back to an exact moment
--reset-offsets --to-datetime 2026-08-31T09:00:00.000 --execute

# --- a specific offset, or all topics for the group
--reset-offsets --to-offset 42 --execute
--reset-offsets --all-topics --to-latest --execute
```

## Blue/green

```bash
# phase 1 - green runs alongside blue as a second group, replaying history
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --group payment-service-v2 --from-beginning

# phase 2 - retire blue
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --delete --group payment-service

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
# payment-service-v2
```

## Clearing a huge lag after downtime

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group payment-service-v2 --describe          # LAG: 2,900,431

# rehydrate from the source of truth first, THEN:
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group payment-service-v2 --topic order-events \
  --reset-offsets --to-latest --execute

kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group payment-service-v2 --describe          # LAG: 0
```

In application code the same thing is:

```java
consumer.seekToEnd(consumer.assignment());   // explicit jump to the end
// or, for a brand new group only:
// auto.offset.reset=latest
```

> Copying these commands out of a web page can drag in stray escape characters and line continuations. If a command fails oddly, retype the backslashes.
