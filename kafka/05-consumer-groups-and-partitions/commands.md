# Part 05 command sheet — consumer groups and partitions

Three terminals, all inside the container.

```bash
docker compose up                 # from kafka/03-local-setup
docker ps                         # find the container
docker exec -it <container-id> bash
```

---

## 1. Reproduce the duplicate problem (no group)

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --create
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Two consumers, **no `--group`**:

```bash
# terminal 2 and terminal 3 - run the same command in both
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-events
```

Producer:

```bash
# terminal 1
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic order-events
> order-1
> order-3
> order-4
```

Both consumers receive everything. Duplicate processing, no scaling.

---

## 2. Add a consumer group

```bash
# terminal 2 and terminal 3
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --group payment-service
```

Publish `5 6 7 8 ... 15` — only **one** consumer receives them. The other is idle.

A consumer in a **different** group gets its own copy of everything:

```bash
# terminal 4
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --group product-service
```

---

## 3. Recreate the topic with 2 partitions

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --delete
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --partitions 2 --create
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --describe
# PartitionCount: 2
```

Consumers (same group, one each):

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --group payment-service
```

Producer **with a partition key** (no `--group` on a producer):

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic order-events \
  --property parse.key=true \
  --property key.separator=:

> order-1:booked
> order-2:booked
> order-3:booked
> order-1:updated     # goes back to the SAME consumer as order-1:booked
> order-2:updated     # goes back to the SAME consumer as order-2:booked
```

Show which partition each record landed on:

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --group payment-service \
  --property print.key=true \
  --property print.partition=true \
  --property print.offset=true
```

---

## 4. Watch a rebalance

```bash
# kill one consumer with Ctrl+C, then publish again - the key moves to the survivor
# start it back up and watch the log:
#   Request joining group due to: group is already rebalancing
#   Notifying assignor about the new Assignment: partitions=[order-events-0, order-events-1]
```

---

## 5. Inspect groups

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group payment-service --describe
# TOPIC  PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG  CONSUMER-ID  HOST
```

`LAG` is the number of records the group has not consumed yet — the single most useful production metric here.

---

## On disk

```bash
ls /tmp/kraft-combined-logs
# order-events-0/   order-events-1/   __consumer_offsets-N/   __cluster_metadata-0/
```

`order-events-0` = topic `order-events`, partition `0`.
