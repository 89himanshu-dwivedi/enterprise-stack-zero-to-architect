# Part 07 command sheet — offsets and lag

```bash
docker compose up                    # from kafka/03-local-setup
docker ps
docker exec -it <container-id> bash
```

## Set up the demo

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
kafka-topics.sh --bootstrap-server localhost:9092 --topic test --create
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Producer (terminal 1):

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
> hello
> hi
> bye
```

Consumer (terminal 2) — the `--group` is what makes an offset get stored:

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic test --group test-cg
```

## Read `__consumer_offsets` directly

Raw — unreadable, because Kafka stores everything in binary:

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic __consumer_offsets --from-beginning
```

With a formatter — human readable:

```bash
# Kafka 3.7+
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic __consumer_offsets --from-beginning \
  --formatter "org.apache.kafka.tools.consumer.OffsetsMessageFormatter"

# older builds
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic __consumer_offsets --from-beginning \
  --formatter "kafka.coordinator.group.GroupMetadataManager\$OffsetsMessageFormatter"
```

Output:

```
[test-cg,test,0]::OffsetAndMetadata(offset=5, leaderEpoch=Optional[0],
   metadata=, commitTimestamp=1756612345678, expireTimestamp=None)
```

| Piece | Meaning |
|---|---|
| `test-cg` | the consumer group |
| `test` | the topic |
| `0` | the partition |
| `offset=5` | how far this group has consumed |
| `commitTimestamp` | when it was last committed |
| `leaderEpoch` | used around leader election / rebalancing |

The first three together are the **offset key**. The rest is the **offset value**.

## Consumer lag

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group test-cg --describe
```

```
GROUP    TOPIC  PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
test-cg  test   0          5               10              5
```

- **CURRENT-OFFSET** — how far the group has consumed
- **LOG-END-OFFSET** — how many records the producer has written
- **LAG** — `LOG-END-OFFSET − CURRENT-OFFSET`, i.e. what is still pending

Stop the consumer, produce more, run `--describe` again → the lag grows. Restart the consumer → lag returns to `0`.

## Commit behaviour

```bash
# default: the broker commits for you
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --group test-cg \
  --consumer-property enable.auto.commit=true

# manual: your code decides when
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --group test-cg \
  --consumer-property enable.auto.commit=false
```

## Useful extras

```bash
# reset a group back to the start (group must have no active members)
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group test-cg --topic test --reset-offsets --to-earliest --execute

# where does __consumer_offsets live on disk
ls /tmp/kraft-combined-logs | grep __consumer_offsets
```
