# Part 06 command sheet — rebalancing and scaling

```bash
docker compose up                  # from kafka/03-local-setup
docker exec -it <container-id> bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

## The demo setup

Consumer (run in two or more terminals):

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events \
  --property print.key=true \
  --group payment-service
```

`print.key=true` is what makes the key show up next to the message. Without it you only see the value.

Producer:

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic order-events \
  --property parse.key=true \
  --property key.separator=:

> 1:order-1
> 2:order-2
> 4:order-4
```

Without `parse.key` / `key.separator`, Kafka treats the whole line as a single string value — there is no key, so it cannot decide a partition from one.

## Watch the rebalance

Start a second consumer with the same `--group` and read the log:

```
[Consumer clientId=console-consumer, groupId=payment-service]
  Request joining group due to: group is already rebalancing
  Notifying assignor about the new Assignment: partitions=[order-events-0]
```

Then `Ctrl+C` one consumer and watch it happen again.

## Inspect the assignment

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group payment-service --describe
# GROUP  TOPIC  PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG  CONSUMER-ID  HOST  CLIENT-ID
```

This is the command that answers "which consumer owns which partition, and how far behind is it".

## Add partitions (the dangerous one)

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --describe
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events --alter --partitions 4
```

Partitions can only be **increased**, never decreased. And doing this remaps `hash(key) % partitionCount` for every existing key — read part 06 section 8 before running it on anything real.

## The safe cutover

```bash
# 1. create the new topic with the right partition count
kafka-topics.sh --bootstrap-server localhost:9092 --topic order-events-v2 \
  --partitions 12 --replication-factor 3 --create

# 2. tell the producer to switch (however your config/feature flag works)
curl -X POST http://order-service/admin/topic -d 'order-events-v2'

# 3. wait for the old topic to drain - LAG must reach 0
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group payment-service --describe

# 4. tell the consumers to switch
curl -X POST http://payment-service/admin/topic -d 'order-events-v2'
```
