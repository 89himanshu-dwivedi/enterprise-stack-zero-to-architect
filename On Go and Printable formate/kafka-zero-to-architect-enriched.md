# Apache Kafka — Zero → Architect Master Notes (Enriched Edition v2)
## 38 Modules (00–37) • Easy Hinglish • Interview Ready • Current Scene

> **Goal:** Kafka ko sirf commands ya definitions se nahi, balki **event → topic → partition → producer → broker → replication → consumer group → offset → replay → reliability → Java → KRaft → Connect/CDC → security → Streams → multi-cluster DR → enterprise architecture** ke flow mein samajhna.

**What's new in this enriched edition:** every module (including the newly added architect-track modules 15–37) now has a **🧪 Try It Yourself** hands-on lab or design exercise, a **💡 Extra Insight** that goes one layer deeper, and a **🩹 Common Error & Fix** for the mistake people hit first. A new **Glossary**, **Common Error Messages Reference**, and **Command Reference by Task** — both expanded to cover the new modules — sit at the end, along with two additional real-world scenarios.

---

# 🧭 MASTER ROADMAP

| # | Module | Core Focus |
|---|---|---|
| 00 | Kafka with Salesforce | Salesforce ↔ Kafka integration patterns |
| 01 | Why Kafka & What is Kafka | Event streaming + Kafka mental model |
| 02 | Kafka Fundamentals | Cluster, broker, topic, partition, replication, KRaft |
| 03 | Local Setup | Binary/Docker, Compose, server configuration |
| 04 | CLI Produce/Consume | Topic commands, produce/consume, serde, retention, offsets |
| 05 | Consumer Groups & Partitions | Parallelism, keys, ownership |
| 06 | Rebalancing & Scaling | Rebalance + safe partition scaling |
| 07 | Consumer Offsets & Lag | `__consumer_offsets`, commits, lag |
| 08 | Why Kafka is Fast | Sequential I/O, batching, zero-copy, page cache |
| 09 | Offset Reset & Replay | Replay, reset policies, recovery |
| 10 | Java Consumer Core API | Poll loop, config, commits, static membership |
| 11 | Java Producer & Idempotency | Producer API, idempotence, transactions |
| 12 | Rebalance Strategies & Callbacks | Assignors, cooperative sticky, callbacks |
| 13 | Kafka Cluster Setup | Multi-broker KRaft, listeners, quorum, security |
| 14 | Kafka Connect & CDC | Debezium, JDBC, S3, source/sink pipelines |
| 15 | Log Internals | Segments, indexes, headers, page cache |
| 16 | Log Compaction & Tombstones | Key-based cleanup, latest-state topics |
| 17 | Kafka Security | AuthN/AuthZ, TLS, SASL, ACLs, encryption |
| 18 | Quotas & Throttling | Producer/consumer quotas, noisy-neighbour control |
| 19 | AdminClient & Operations | Topic/config/offset/ACL administration |
| 20 | Partition & Broker Sizing | Capacity planning inputs |
| 21 | Multi-AZ / Rack Awareness | Failure-domain-aware replica placement |
| 22 | Disaster Recovery & Multi-Cluster | RPO/RTO, MirrorMaker 2 |
| 23 | Tiered Storage | Hot vs remote object storage |
| 24 | Schema Registry & Event Contracts | Compatibility modes, evolution |
| 25 | Retry, DLQ/DLT & Poison Messages | Bounded retry, backoff, replay |
| 26 | Kafka Streams | KStream, KTable, state stores |
| 27 | Advanced Stream Processing | Event time, watermarks, windows |
| 28 | Modern Consumer / Share-Group Awareness | ShareConsumer vs classic groups |
| 29 | Modern Consumer Group Protocols | Coordination, heartbeats, assignment |
| 30 | Producer Internals | Accumulator, sender, batching pipeline |
| 31 | Broker / Network Request Path | Request handling, replication path |
| 32 | Exactly-Once Deep Dive | Idempotency + transactions + external correctness |
| 33 | Upgrades & Compatibility | Rolling upgrades, protocol compatibility |
| 34 | Testing & Failure Injection | Chaos testing, load testing |
| 35 | Advanced Observability | Full metric taxonomy |
| 36 | Enterprise Event Governance | Ownership, SLAs, deprecation policy |
| 37 | Production Failure Scenarios | End-to-end incident playbooks |

---

# 00 — KAFKA WITH SALESFORCE

## 🟢 Simple

Salesforce mein business event generate hota hai:

```text
Salesforce → Business Event → Integration Layer → Kafka Topic → Consumers
   ├── Data Lake
   ├── CRM/ERP
   ├── Analytics
   ├── Notification
   └── AI/ML
```

Kafka ko Salesforce ka database replacement mat samjho. Kafka ka kaam primarily: event/data movement, buffering, decoupling, replay, fan-out, asynchronous processing, integration backbone.

## Salesforce → Kafka patterns

### Pattern 1 — Salesforce publishes event

```text
Salesforce → Platform Event / CDC → Kafka connector/integration → Kafka topic → downstream consumers
```

### Pattern 2 — Middleware publishes to Kafka

```text
Salesforce → MuleSoft / Integration Service → Kafka → Consumers
```

### Pattern 3 — Kafka → Salesforce

```text
External System → Kafka → Consumer / Integration Layer → Salesforce API
```

## Real-world example

```text
Opportunity Closed Won → Event → Kafka: opportunity-events
 ┌──────┼────────┬─────────┐
 ↓      ↓        ↓         ↓
ERP   Analytics  Email     AI
```

One producer, multiple independent consumers.

## 🔥 Gotchas

Kafka does not automatically make Salesforce API calls safe. Consumer retry/idempotency is still required. Salesforce API limits remain relevant. Don't publish every internal field blindly. Define event contract/schema. Avoid tight synchronous coupling. Handle duplicate events.

## Architect question

**Q: Why put Kafka between Salesforce and 5 downstream systems?** Because direct synchronous integrations create tight coupling and amplify failures. Kafka provides durable asynchronous fan-out so each consumer can process independently.

### 🧪 Try It Yourself

Design exercise: sketch your own org's "Opportunity Closed Won" (or equivalent) event. Write out its JSON shape, list every downstream system that currently gets notified *today*, and mark which ones are synchronous calls made directly from Salesforce/Apex — those are your Kafka migration candidates.

### 💡 Extra Insight

The real win isn't "Kafka is fast" — it's that **Salesforce's Apex governor limits and callout timeouts make synchronous fan-out to 5 systems fragile by construction**. One slow downstream system can eat your callout budget and fail the whole transaction. Publishing one event and letting Kafka fan it out removes Salesforce from the critical path of every downstream system's uptime.

### 🩹 Common Error & Fix

Symptom: "we added Kafka but our Apex code still directly calls the ERP API too, just now also publishes an event." This is integration sprawl, not decoupling — the event should *replace* the direct call, or you've doubled the coupling instead of removing it.

## 🎤 Interview

**🟢:** Kafka Salesforce ka database replacement hai kya?
**🔵:** Platform Event vs CDC vs middleware-published event mein kya farak hai?
**🟡:** Ek hi opportunity event 5 consumers ko bhejte waqt duplicate processing kaise avoid karoge?
**🔴:** Salesforce + Kafka event backbone ka schema ownership aur versioning strategy design karo.

---

# 01 — WHY KAFKA AND WHAT IS KAFKA?

## 🟢 Simple

```text
Producer → Kafka Topic → Consumers
WRITE → DURABLE LOG → READ / REPLAY → PROCESS
```

Kafka stores events durably and consumers track their own position.

## Problem Kafka solves

```text
App A → App B/C/D (tight coupling)
     vs
Producer → Kafka → Consumer B/C/D (producer doesn't need to know every consumer)
```

## Kafka vs traditional queue

| Concept | Kafka |
|---|---|
| Data model | Distributed log |
| Consumers | Independent consumer groups |
| Replay | Yes |
| Ordering | Within partition |
| Scaling | Partitions |
| Durability | Replicated logs |
| Retention | Configurable |
| Fan-out | Native through groups |

## Kafka is NOT

Simply Redis Pub/Sub, simply RabbitMQ, a database replacement, automatically exactly-once end-to-end, automatically ordered globally.

## Why Kafka became fast

Sequential disk access, batching, compression, page cache, zero-copy transfer, partition parallelism.

## 🔵 Junior

**Topic** — logical stream (`orders`, `payments`). **Partition** — topic split into ordered shards (`P0..P3`). **Offset** — position inside a partition (`0 → 1 → 2 → 3`), not a global message ID.

## 🟡 Senior

More partitions = more parallelism/throughput potential, but more metadata and operational complexity. Global ordering would create a serialization bottleneck — Kafka chooses **ordering per partition + horizontal scalability across partitions.**

## 🔴 Architect

```text
Distributed + Persistent + Replicated + Partitioned + Replayable + Append-oriented + Event-streaming platform
```

Current Kafka architecture uses **KRaft**; ZooKeeper-based Kafka is legacy.

### 🧪 Try It Yourself

Put 3 consumers in **3 different consumer groups** reading the same topic — all 3 will independently receive every message (unlike a competing-consumers queue, where only one gets each message). Write out why that changes your integration design.

### 💡 Extra Insight

If you need global ordering across *all* events regardless of key, the honest options are: (a) a single partition (kills parallelism), or (b) redesign so "global order" isn't actually required — usually only order *within an entity* matters, which maps cleanly to partition-per-key.

### 🩹 Common Error & Fix

Symptom: "message #5 didn't process before message #8." Check whether they landed in the same partition — if the producer didn't set a key, ordering across partitions was never guaranteed.

## 🎤 Interview

**🟢:** Kafka queue se kaise different hai?
**🔵:** Topic, partition, offset define karo.
**🟡:** Global ordering kyun nahi milti by default?
**🔴:** Per-customer ordering guaranteed rakhte hue high throughput ka design banao.

---

# 02 — KAFKA FUNDAMENTALS

```text
Kafka Cluster: Broker 1, Broker 2, Broker 3, Controller quorum
```

**Broker** — Kafka server storing partitions. **Topic** — logical name. **Partition** — ordered append-only log (`P0: 0 1 2 3 4`). **Replication** — RF=3 means Leader + 2 replicas. **ISR** — In-Sync Replicas, sufficiently caught up with the leader.

## KRaft

```text
Kafka ├── Brokers └── Controller quorum
```

Removes separate ZooKeeper dependency; controller quorum manages metadata directly.

## 🟠 Lead — Leader election

```text
Old Leader X → Eligible replica → New Leader
```

## 🔴 Architect

```text
DATA PLANE:       Producer ↔ Broker ↔ Consumer
METADATA/CONTROL: Controllers ↔ cluster metadata
STORAGE:          Partition logs + replicas
```

### 🧪 Try It Yourself

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic payments
```

Expected-shape output:

```text
Topic: payments  PartitionCount: 3  ReplicationFactor: 3
    Partition: 0  Leader: 1  Replicas: 1,2,3  Isr: 1,2,3
```

### 💡 Extra Insight

`Isr` shrinking below `Replicas` (e.g. `Isr: 1,3` when `Replicas: 1,2,3`) is one of the most important health signals — under-replicated-partitions is usually the first alert worth having on any real cluster.

### 🩹 Common Error & Fix

```text
NOT_LEADER_OR_FOLLOWER
```
Stale cached metadata after a leader election — client should refresh metadata and retry; most clients handle this automatically.

### 🎤 Interview

**Q: Topic vs partition?** Logical stream vs ordered append-only shard. **Q: Ordering guarantee?** Within a partition. **Q: Why replication?** Fault tolerance/durability. **Q: Broker dies?** Partitions fail over to eligible replicas; clients refresh metadata.

---

# 03 — LOCAL SETUP

```text
Docker → Kafka → Create topic → Produce → Consume
```

## KRaft local mental model

```text
Kafka process ├── broker role └── controller role
```

Combined for learning; production commonly separates roles.

## Important configuration areas

```text
node.id
process.roles
controller.quorum.voters
listeners / advertised.listeners / listener.security.protocol.map
```

## 🔥 Biggest local setup gotcha

```text
listeners            → where Kafka binds
advertised.listeners → what address clients are told to use
```

**Rule: Bootstrap server se connect hona enough nahi hai. Client ko advertised broker addresses bhi reachable hone chahiye.**

### 🧪 Try It Yourself

Reproduce the gotcha deliberately with `KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092` while a client runs in a separate container — bootstrap succeeds, then the reconnect using the advertised address fails. Fix: point advertised listeners at the Docker service name (`kafka:9092`), not `localhost`.

### 💡 Extra Insight

The same failure mode shows up in cloud deployments when `advertised.listeners` points at a private IP but the client connects from outside — the fix is always "what address will the client actually be able to reach, from where it's actually connecting?"

### 🩹 Common Error & Fix

```text
TimeoutException: Topic ... not present in metadata after 60000 ms
```
after a successful `--list` — classic wrong-advertised-listener symptom.

### 🎤 Interview

**🟢:** Local Kafka setup ke liye Docker kyun convenient hai?
**🔵:** `listeners` vs `advertised.listeners`?
**🟡:** KRaft mode mein roles combine kyun ho sakte hain locally?
**🔴:** Internal + external clients dono ke liye listener design kaise karoge?

---

# 04 — CLI PRODUCE / CONSUME

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic orders --partitions 3 --replication-factor 1
kafka-topics.sh --bootstrap-server localhost:9092 --list
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic orders
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic orders
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders --from-beginning
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders --group order-service
```

## Serialization

```text
Object → Serializer → bytes → Kafka → bytes → Deserializer → Object
```

Formats: String, JSON, Avro, Protobuf, JSON Schema.

## Retention

Time- or size-based; compaction is a different mechanism.

## 🔥 Gotcha

**Consumed ≠ deleted.**

### 🧪 Try It Yourself

Run the same consumer command with a *different* `--group` in a third terminal after two other groups have already read messages — the new group still receives both messages from the start, proving consumption doesn't delete records.

### 💡 Extra Insight

`--from-beginning` only matters the *first* time a group reads a topic — once a group has committed offsets, restarting resumes from the last committed position regardless of the flag. This trips people up in testing.

### 🩹 Common Error & Fix

```text
Connection to node -1 could not be established
```
Bootstrap address/port mismatch, or broker not up yet — verify with `--list` first.

### 🎤 Interview

**🟢:** `--from-beginning` kya karta hai?
**🔵:** Serializer/deserializer ka role?
**🟡:** Retention vs compaction?
**🔴:** Multiple groups ko independent processing dene ka architecture design karo.

---

# 05 — CONSUMER GROUPS, PARTITIONS & KEYS

```text
Partitions = 4, Consumers = 4 → C1→P0, C2→P1, C3→P2, C4→P3
Partitions = 3, Consumers = 5 → 2 consumers idle
```

Multiple groups on the same topic = pub/sub behavior.

## Partition key

```text
key = customerId → customer-101 consistently → P2
```

## 🔥 Gotcha

99% traffic on one key = hot partition. Ask: **what entity must remain ordered?** Use that as the key.

### 🧪 Try It Yourself

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic orders \
  --property "parse.key=true" --property "key.separator=:"
> customer-101:{"orderId":"O1"}
> customer-202:{"orderId":"O3"}
```

Consume per-partition (`--partition 0`, `1`, `2`, `3`) and confirm `customer-101` always lands in the same one.

### 💡 Extra Insight

Keyless records aren't distributed strictly round-robin per message in modern clients — the default partitioner uses a "sticky" strategy, batching several keyless records onto one partition before switching, to improve batching efficiency.

### 🩹 Common Error & Fix

Symptom: one partition's consumer maxed on CPU, others idle — check key distribution before assuming it's a consumer code problem; a hot key is a data/design issue.

### 🎤 Interview

**🟢:** Consumer group kya hai?
**🔵:** Consumers > partitions?
**🟡:** Partition key kaise choose karoge?
**🔴:** Hot partition detect/mitigate production strategy design karo.

---

# 06 — REBALANCING & SCALING

```text
Before: C1→P0 P1, C2→P2 P3
C3 joins
After:  C1→P0, C2→P2, C3→P1 P3
```

Rebalancing hurts: ownership changes, processing pauses, latency spikes.

```text
Partitions=6, Consumers=3 → ~2/consumer; Consumers=6 → 1/consumer; >6 → idle
```

**Partition count is an architectural decision, not a casual tuning knob** — it can affect key-to-partition mapping and ordering assumptions.

### 🧪 Try It Yourself

Start two consumers in the same group, then run `kafka-consumer-groups.sh --describe --group demo-group` before and after starting a third — watch the ownership split change live.

### 💡 Extra Insight

Increasing partition count later changes which partition a given key hashes to for *new* messages, silently breaking "same key → same partition" ordering for anything produced afterward. Treat partition count as a Day-1 capacity decision.

### 🩹 Common Error & Fix

Symptom: "we added 3 more partitions and customer events are now out of order." New messages for that key may now hash to a different partition — there's no after-the-fact fix; plan partition count ahead of time.

### 🎤 Interview

**🟢:** Rebalance kab hota hai?
**🔵:** Consumers > partitions impact?
**🟡:** Partition count badhane se ordering kaise break hoti hai?
**🔴:** Zero-downtime partition scaling strategy design karo.

---

# 07 — CONSUMER OFFSETS & LAG

```text
lag = latest available offset − consumer committed/processed position
```

Auto commit (easy, less explicit) vs manual commit (explicit correctness control). At-least-once: `process → commit`; if commit fails after processing, duplicates possible.

Lag isn't automatically a failure: could be traffic spike, slow downstream API, hot partition, broker/network issue.

### 🧪 Try It Yourself

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group order-service
```

```text
GROUP          TOPIC   PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
order-service  orders  2          9200            9950            750
```

### 💡 Extra Insight

Lag in **message count** can mislead across topics with very different processing costs — pair it with **time-based lag** (age of oldest unprocessed message) for the real picture.

### 🩹 Common Error & Fix

`Consumer group not found / no active members` in `--describe` means zero running instances — expected right after stopping consumers, alarming otherwise.

### 🎤 Interview

**🟢:** Consumer lag kya hai?
**🔵:** Auto vs manual commit risk?
**🟡:** High lag dekh kar sabse pehle kya check karoge?
**🔴:** Lag-based autoscaling architecture design karo.

---

# 08 — WHY KAFKA IS FAST

Sequential I/O, batching, compression (gzip/snappy/lz4/zstd), OS page cache, zero-copy transfer, partition parallelism.

**Q: If Kafka is so fast, why is my consumer slow?** Kafka throughput ≠ end-to-end throughput — the slowest dependency is usually downstream (`Kafka → Consumer → DB/API → External service`).

### 🧪 Try It Yourself

```bash
kafka-producer-perf-test.sh --topic orders --num-records 1000000 --record-size 200 \
  --throughput -1 --producer-props bootstrap.servers=localhost:9092 acks=1 compression.type=lz4 linger.ms=20
```

Compare with `compression.type=none` — batching + compression's contribution made visible.

### 💡 Extra Insight

`linger.ms` is underrated: waiting a small amount of time to accumulate a bigger batch trades tiny per-message latency for large throughput/compression gains. Setting it to `0` feels "faster" per message but is often worse for overall system throughput.

### 🩹 Common Error & Fix

Symptom: "benchmark shows 500K msgs/sec but real consumer handles 5K/sec" — profile the actual processing step (DB write, API call) separately from `poll()`; the bottleneck is almost always downstream.

### 🎤 Interview

**🟢:** Kafka fast kyun hai?
**🔵:** Batching/compression trade-off?
**🟡:** Page cache Kafka performance mein kaise help karta hai?
**🔴:** End-to-end bottleneck isolation kaise karoge?

---

# 09 — OFFSET RESET & REPLAY

```text
Current: P0 → 9000
Reset:   P0 → 7000
Replay:  7000 → 9000
```

Dangerous mistake: replay without idempotency creates duplicate DB inserts. Safe replay: idempotent writes, unique keys, upsert, dedup.

### 🧪 Try It Yourself

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group order-service --topic orders --reset-offsets --to-datetime 2026-09-20T00:00:00.000 --dry-run
```

Always preview with `--dry-run` before `--execute`.

### 💡 Extra Insight

`--reset-offsets` only works while the group has **no active members** — stop every consumer instance first. This trips people up in production.

### 🩹 Common Error & Fix

```text
Assignments can only be reset if the group is inactive
```
Stop all consumer instances in the group, then reset, then restart them.

### 🎤 Interview

**🟢:** Replay kyun useful hai?
**🔵:** Earliest vs latest reset?
**🟡:** Replay ke time duplicates kaise avoid karoge?
**🔴:** Large-scale backfill/replay ka safe rollout plan design karo.

---

# 10 — JAVA CONSUMER CORE API

```text
create properties → KafkaConsumer → subscribe() → poll() → process records → commit
```

`poll()` is not just "give me messages" — group participation depends on polling correctly (`max.poll.interval.ms`, `max.poll.records`, `session.timeout.ms`, heartbeats). Static membership reduces unnecessary rebalances for stable consumers.

Commit after processing (better semantic control) vs commit before processing (risk of message loss on crash).

### 🧪 Try It Yourself

```java
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
...
for (ConsumerRecord<String,String> r : records) process(r);
consumer.commitSync(); // after the whole batch
```

Throw an exception mid-batch and restart — the entire batch reprocesses, including already-handled records, demonstrating at-least-once duplication.

### 💡 Extra Insight

`max.poll.interval.ms` decides whether your consumer gets kicked out of the group for taking too long between polls — if per-batch processing occasionally exceeds it, the consumer is marked dead and rebalanced away even though it's still alive, just slow. A very common cause of mystery rebalances.

### 🩹 Common Error & Fix

```text
CommitFailedException: Commit cannot be completed since the group has already rebalanced
```
Speed up processing, reduce `max.poll.records`, or deliberately raise the interval.

### 🎤 Interview

**🟢:** `poll()` kya karta hai?
**🔵:** Commit before vs after processing?
**🟡:** `max.poll.interval.ms` exceed hone par kya hota hai?
**🔴:** Static membership use case design karo.

---

# 11 — JAVA PRODUCER & IDEMPOTENCY

```text
Application → KafkaProducer → Serializer → Partitioner → Leader broker
```

Settings: `acks`, `retries`, `enable.idempotence`, `delivery.timeout.ms`, batching, compression, `linger.ms`. Idempotent producer prevents retry-caused duplicates via producer identity/sequence mechanisms.

Transactions coordinate multiple writes: `consume → process → produce → commit transaction`.

## 🔥 Exactly-once gotcha

Kafka transactions don't automatically make external DB/API side effects exactly-once — consider idempotency keys, transactional outbox, inbox/dedup.

### 🧪 Try It Yourself

```java
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
props.put(ProducerConfig.ACKS_CONFIG, "all");
```

Compare behavior with/without idempotence under simulated network flakiness — check for duplicate records on the broker.

### 💡 Extra Insight

Idempotent producer only guarantees no duplicates for a **single producer session** writing to a **single partition** — exactly-once across restarts/multiple partitions needs the transactional API too. People often conflate the two.

### 🩹 Common Error & Fix

```text
ProducerFencedException
```
Two producer instances used the same `transactional.id` concurrently (e.g., bad rolling-deploy overlap) — ensure only one instance holds a given transactional ID at a time.

### 🎤 Interview

**🟢:** Idempotent producer kya solve karta hai?
**🔵:** `acks=all` ka meaning?
**🟡:** Idempotence vs exactly-once farak?
**🔴:** Outbox/inbox pattern samjhao Kafka-to-external-API integration ke liye.

---

# 12 — REBALANCE STRATEGIES & CALLBACKS

Assignment strategies: range, round-robin, sticky, cooperative sticky. Cooperative sticky moves only what's necessary instead of revoking everything.

`onPartitionsRevoked` → flush/commit/close resources; `onPartitionsAssigned` → initialize/restore state.

### 🧪 Try It Yourself

```java
props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
    CooperativeStickyAssignor.class.getName());

consumer.subscribe(List.of("orders"), new ConsumerRebalanceListener() {
    public void onPartitionsRevoked(Collection<TopicPartition> p) { consumer.commitSync(currentOffsets); }
    public void onPartitionsAssigned(Collection<TopicPartition> p) { System.out.println("Now own: " + p); }
});
```

Compare eager vs cooperative sticky logs when a third consumer joins — far fewer partitions change hands with cooperative sticky.

### 💡 Extra Insight

Eager rebalance revokes **all** partitions from **all** consumers before reassigning — even ones that end up right back where they were. Cooperative sticky was designed specifically to fix this "stop the world" behavior.

### 🩹 Common Error & Fix

Symptom: "every restart pauses the whole pipeline for seconds" — classic eager-rebalance symptom; switch to `CooperativeStickyAssignor`.

### 🎤 Interview

**🟢:** Rebalance callback kyun zaroori hai?
**🔵:** onPartitionsRevoked mein kya karna chahiye?
**🟡:** Cooperative sticky better kyun hai?
**🔴:** Zero-downtime consumer deployment strategy design karo.

---

# 13 — MULTI-BROKER KRAFT CLUSTER

```text
Controller Quorum (C1, C2, C3) + Broker 1/2/3
```

Odd quorum sizes: `3 nodes → tolerate 1 failure`, `5 → tolerate 2` (`majority = floor(N/2)+1`). Separate listener concepts for CLIENT/CONTROLLER/INTER-BROKER. Security protocols: PLAINTEXT/SSL/SASL/SASL_SSL. `min.insync.replicas` + `acks=all` ensures durable writes.

### 🧪 Try It Yourself

```bash
kafka-metadata-quorum.sh --bootstrap-server localhost:9092 describe --status
```

`CurrentVoters` confirms exactly which nodes participate in the quorum.

### 💡 Extra Insight

`min.insync.replicas` only matters when the producer actually sets `acks=all` — setting the broker config alone without the producer setting silently defeats the durability guarantee.

### 🩹 Common Error & Fix

```text
NotEnoughReplicasException
```
ISR dropped below `min.insync.replicas` — investigate replica health, don't just lower the setting.

### 🎤 Interview

**🟢:** Quorum kyun odd number mein?
**🔵:** Replication factor 3 ka matlab?
**🟡:** `min.insync.replicas` + `acks=all` combined effect?
**🔴:** Multi-AZ KRaft cluster design karo.

---

# 14 — KAFKA CONNECT & CDC

```text
Source Connector: DB → Kafka
Sink Connector:   Kafka → DB/S3/Search
CDC: PostgreSQL → Debezium → Kafka → Consumers
```

CDC replaces polling (`SELECT ... WHERE updated_at > ?`) with transaction-log streaming. CDC ≠ magical database backup — still need source DB recovery, connector recovery, retention strategy, schema management.

### 🧪 Try It Yourself

```json
{"name":"orders-connector","config":{
  "connector.class":"io.debezium.connector.postgresql.PostgresConnector",
  "database.hostname":"postgres","database.dbname":"shop",
  "topic.prefix":"shop","table.include.list":"public.orders"}}
```

```bash
curl -X POST -H "Content-Type: application/json" --data @connector.json http://localhost:8083/connectors
```

Update a row directly in Postgres and watch a change-event appear on the Kafka topic within seconds.

### 💡 Extra Insight

Debezium's first message per table isn't an update — it's a full **snapshot** streamed as synthetic inserts before switching to live log-tailing. A large existing table means a real snapshot phase before the connector is "caught up."

### 🩹 Common Error & Fix

```text
replication slot "debezium" is already active
```
A previous connector instance still holds the slot — ensure it's fully stopped, or use a different slot name.

### 🎤 Interview

**🟢:** Kafka Connect kya solve karta hai?
**🔵:** Source vs sink connector?
**🟡:** CDC polling se better kyun hai?
**🔴:** Poora CDC pipeline design karo including schema evolution/failure recovery.

---

# 15 — LOG INTERNALS

## Mental model

A partition is not one giant file — it's a sequence of **segments**:

```text
Partition (log)
 ├── segment 00000000000000000000.log  (closed)
 ├── segment 00000000000000012345.log  (closed)
 └── segment 00000000000000024680.log  (active — currently being written)
```

Each segment has companion **index files**:

```text
.index       → offset → byte position in the .log file
.timeindex   → timestamp → offset (enables "seek by time")
```

Records also carry **headers** (arbitrary key-value metadata) and **timestamps** (either event time set by the producer, or log-append time set by the broker, depending on config).

Segments **roll** (a new active segment starts) based on size or time configuration, and old segments become eligible for retention-based deletion once they age out.

### 🧪 Try It Yourself

On a broker's data directory (if you have filesystem access to a local Docker Kafka), list a partition's actual files:

```bash
docker exec -it kafka ls -la /var/lib/kafka/data/orders-0/
```

Expected-shape output:

```text
00000000000000000000.index
00000000000000000000.log
00000000000000000000.timeindex
00000000000000012345.index
00000000000000012345.log
00000000000000012345.timeindex
```

Notice the filenames are literally the **starting offset** of that segment, zero-padded — this is Kafka's on-disk naming convention made visible.

### 💡 Extra Insight

The `.index` file doesn't map every single offset — it's a **sparse index** (an entry every N bytes by default), which is why Kafka does a quick binary search in the index followed by a short linear scan in the `.log` file to find an exact offset. This trade-off (index size vs. lookup speed) is a deliberate design choice, not an oversight.

### 🩹 Common Error & Fix

Symptom: disk usage keeps climbing even though retention is configured. Check whether the **active segment** simply hasn't rolled yet (segments only become eligible for deletion once they're closed and past their retention window) — `log.segment.bytes` / `log.roll.ms` control how often that happens.

### 🎤 Interview

**🟢:** Segment kya hai?
**🔵:** `.index` file kya karta hai?
**🟡:** Sparse index ka trade-off?
**🔴:** Retention delete timing ko segment rolling ke saath kaise design karoge for predictable disk usage?

---

# 16 — LOG COMPACTION & TOMBSTONES

## Retention vs Compaction

```text
Retention (delete):  keep records for X time/size, then delete old ones
Compaction (compact): keep only the LATEST record per key, forever
```

Compaction is used for "latest state" / changelog-style topics — e.g., a topic representing "current account balance per customer," where you only care about the newest value per key, not the full history.

## Tombstones

A record with a key and a **null value** signals "delete this key's state entirely":

```text
key=customer-101, value=null   → tombstone
```

After compaction runs, only the tombstone (and eventually not even that, after a grace period) remains for that key.

## Compaction is asynchronous

Compaction doesn't happen instantly on write — it runs periodically in the background. **This means the log can (and normally does) contain multiple records for the same key for a while**, even on a compacted topic.

### 🧪 Try It Yourself

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --create \
  --topic account-balance --partitions 1 --replication-factor 1 \
  --config cleanup.policy=compact

kafka-console-producer.sh --bootstrap-server localhost:9092 --topic account-balance \
  --property "parse.key=true" --property "key.separator=:"
> cust-1:{"balance":100}
> cust-1:{"balance":150}
> cust-1:{"balance":200}
```

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic account-balance \
  --from-beginning --property print.key=true
```

Immediately after producing, you'll likely still see all 3 records — compaction hasn't run yet. Wait, or trigger it, and re-consume: eventually only `cust-1:{"balance":200}` remains.

### 💡 Extra Insight

Compaction is *why* Kafka works well as the backing store for **KTables** in Kafka Streams — a compacted topic is essentially a durable, replayable representation of "current state per key," which is exactly what a changelog needs, without keeping unbounded full history.

### 🩹 Common Error & Fix

Symptom: "we deleted a key with a tombstone, but old consumers still show it as present." Old consumers reading from an offset before the tombstone will legitimately see the old value until they catch up — compaction and tombstones are eventually consistent from a consumer's point of view, not instantaneous.

### 🎤 Interview

**🟢:** Compaction kya hai?
**🔵:** Tombstone kya karta hai?
**🟡:** Compaction turant kyun nahi hoti?
**🔴:** Kafka Streams KTable ke context mein compaction ka role samjhao.

---

# 17 — KAFKA SECURITY

## AuthN vs AuthZ

```text
Authentication (AuthN) → "who are you?"      (TLS client certs, SASL/SCRAM, SASL/Kerberos, SASL/OAuth)
Authorization  (AuthZ) → "what can you do?"  (ACLs)
```

## TLS/SSL

Encrypts data in transit between clients and brokers (and between brokers). Also can be used for mutual authentication (mTLS) via client certificates.

## SASL mechanisms

```text
SCRAM      → username/password-based, salted challenge-response
Kerberos   → enterprise ticket-based auth, common in on-prem Hadoop-adjacent shops
OAuth      → token-based, integrates with modern identity providers
```

## ACLs

Fine-grained permission rules: which principal can do what operation on which resource.

```text
ALLOW User:order-service TO Read ON Topic:orders
ALLOW User:order-service TO Write ON Topic:order-results
DENY  User:order-service TO Delete ON Topic:orders
```

## Encryption at rest

Typically handled at the disk/filesystem/cloud-volume level, not by Kafka itself — an important distinction from "TLS in transit," which Kafka does handle directly.

### 🧪 Try It Yourself

Design exercise (safe to do without a live cluster): write the exact ACL rules a `payments-consumer` service account should have. Start from **zero permissions** and add only what's strictly needed (read on one topic, describe on its consumer group) — this is least-privilege thinking in practice, not theory.

### 💡 Extra Insight

A very common real-world security gap: teams enable TLS for encryption but leave `allow.everyone.if.no.acl.found=true` (or equivalent) set, meaning **anyone who can reach the broker network-wise can do anything**, because no ACLs were ever actually defined. Encryption in transit and access control are two separate controls — enabling one doesn't give you the other.

### 🩹 Common Error & Fix

```text
org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics
```
The client's principal has no ACL granting it access to that topic/operation — this is Kafka correctly enforcing authorization, not a bug; grant the specific ACL needed.

### 🎤 Interview

**🟢:** AuthN vs AuthZ?
**🔵:** SASL/SCRAM kya hai?
**🟡:** TLS encryption at rest bhi cover karta hai kya?
**🔴:** Multi-team Kafka cluster ke liye least-privilege ACL model design karo.

---

# 18 — QUOTAS & THROTTLING

## Why quotas?

Without them, one noisy team/service can saturate broker CPU, network, or request-handling capacity, degrading everyone else on a shared cluster.

```text
Producer quota → bytes/sec a client can produce
Consumer quota → bytes/sec a client can fetch
Request quota  → % of request-handler time a client can consume
```

## Noisy-neighbour problem

```text
Team A's misbehaving producer → floods broker → Team B's latency spikes
       ↓
Quotas cap Team A's impact without needing a separate cluster
```

## Multi-team governance

Quotas + ACLs + monitoring together let a shared multi-tenant Kafka cluster stay fair and secure, without every team needing its own dedicated cluster.

### 🧪 Try It Yourself

```bash
kafka-configs.sh --bootstrap-server localhost:9092 --alter \
  --add-config 'producer_byte_rate=1048576,consumer_byte_rate=2097152' \
  --entity-type clients --entity-name team-a-service
```

```bash
kafka-configs.sh --bootstrap-server localhost:9092 --describe --entity-type clients --entity-name team-a-service
```

Then run a producer perf test against this client ID and watch throughput cap at roughly the configured rate instead of climbing unbounded.

### 💡 Extra Insight

Throttling doesn't reject requests outright — the broker **delays responses** to bring the client's effective rate back under quota. This means a throttled client sees increased latency, not errors, which can be confusing when first debugging "why did our producer suddenly get slower with no error logs."

### 🩹 Common Error & Fix

Symptom: "producer requests are all succeeding but taking much longer than usual, no errors in logs." Check `kafka-configs.sh --describe` for quotas on that client/user — you may be hitting a throttle, not a network or broker health issue.

### 🎤 Interview

**🟢:** Quota kyun zaroori hai?
**🔵:** Noisy-neighbour problem kya hai?
**🟡:** Throttled client ko kaise pehchanoge (error vs latency)?
**🔴:** Multi-tenant Kafka platform ka governance model design karo (quotas + ACLs + monitoring).

---

# 19 — ADMINCLIENT & OPERATIONS

## What AdminClient does

A programmatic (and CLI-backed) API for cluster operations that would otherwise require manual CLI scripts:

```text
Topic create/describe/delete
Configuration management (per-topic, per-broker)
Consumer-group administration (describe, delete, reset)
Offset administration
Cluster metadata inspection
ACL / quota administration
Partition reassignment
```

## Why it matters operationally

Production automation (self-service topic creation portals, CI/CD-driven topic provisioning, automated ACL grants) is built on top of AdminClient, not on developers hand-running CLI scripts against production.

### 🧪 Try It Yourself

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic orders --partitions 6
kafka-configs.sh --bootstrap-server localhost:9092 --alter \
  --entity-type topics --entity-name orders --add-config retention.ms=604800000
kafka-configs.sh --bootstrap-server localhost:9092 --describe --entity-type topics --entity-name orders
```

Partition reassignment (moving replicas between brokers, e.g. for rebalancing disk usage):

```bash
kafka-reassign-partitions.sh --bootstrap-server localhost:9092 \
  --topics-to-move-json-file topics.json --broker-list "1,2,3" --generate
```

### 💡 Extra Insight

Partition reassignment is a genuinely heavy operation — it involves copying full partition data between brokers over the network, which competes with production traffic for disk and network bandwidth. Production tooling almost always throttles reassignment traffic explicitly (`--throttle` flag) rather than letting it run at full speed.

### 🩹 Common Error & Fix

Symptom: reassignment kicked off and production latency spiked cluster-wide. You likely didn't set a throttle — always pass `--throttle <bytes-per-sec>` on any reassignment touching a live production cluster.

### 🎤 Interview

**🟢:** AdminClient kya hai?
**🔵:** Partition count kaise badhaoge?
**🟡:** Partition reassignment risky kyun hai?
**🔴:** Self-service topic provisioning platform ka design karo AdminClient ke upar.

---

# 20 — PARTITION & BROKER SIZING

## Sizing inputs

```text
required throughput (msgs/sec, bytes/sec)
consumer parallelism needed
average message size
producer batching behavior
consumer processing speed (the real bottleneck, usually)
broker CPU / disk throughput / network throughput
replication traffic (RF multiplies write I/O and network)
retention (drives disk capacity)
future growth headroom
```

## 🔥 Gotcha

**More partitions are not automatically better.** Each partition adds:

```text
open file handles on brokers (multiple files per partition — .log/.index/.timeindex)
more metadata for controllers to track
longer rebalance times in large consumer groups
more replication traffic overhead
```

A topic with far more partitions than any realistic consumer group will ever use just adds operational overhead for no parallelism benefit.

### 🧪 Try It Yourself

Work through a concrete sizing exercise: target throughput = 50,000 msgs/sec, average message size = 1KB, single consumer instance can process 5,000 msgs/sec. Minimum partitions needed for full parallelism = `50,000 / 5,000 = 10` partitions (rounding up for headroom, maybe 12–15). Write out why going to 100 partitions for this workload would be *worse*, not better.

### 💡 Extra Insight

The single most common real-world sizing mistake isn't too few partitions — it's **way too many**, chosen "to be safe for the future," which then makes every controller operation (broker restart, leader election storms, metadata propagation) slower for no throughput benefit the workload will ever use.

### 🩹 Common Error & Fix

Symptom: broker restarts take unusually long, or controller failover is slow. Check total partition count across the cluster — clusters with tens of thousands of partitions per broker can see materially slower metadata operations; this is a documented operational ceiling to be aware of, not a bug.

### 🎤 Interview

**🟢:** Partition count decide karte waqt kya consider karoge?
**🔵:** Zyada partitions hamesha better kyun nahi hote?
**🟡:** Consumer processing speed sizing mein kyun important hai?
**🔴:** 500K events/sec ke liye partition/broker sizing exercise poora karo.

---

# 21 — MULTI-AZ / RACK AWARENESS

## The problem

```text
Bad:     RF=3 → AZ-A, AZ-A, AZ-A   (all replicas in one failure domain!)
Better:  RF=3 → AZ-A, AZ-B, AZ-C   (spread across failure domains)
```

If all 3 replicas of a partition happen to land in the same AZ/rack, that entire replication factor's fault tolerance evaporates the moment that one AZ has an issue — you paid for RF=3 but got the failure characteristics of RF=1.

## Rack awareness

Brokers are tagged with a `broker.rack` (or equivalent) identifier corresponding to their physical/logical failure domain. Kafka's replica placement logic uses this to spread replicas across racks/AZs rather than placing them wherever is convenient.

### 🧪 Try It Yourself

Design exercise: given 6 brokers across 3 AZs (2 brokers per AZ) and RF=3, sketch the replica placement Kafka *should* produce for a partition to survive any single AZ failure. Then sketch a placement that technically has RF=3 but would still fail if one AZ went down — the difference is exactly what `broker.rack` configuration prevents.

### 💡 Extra Insight

Rack awareness only helps if it's configured **before** partitions are created (or you deliberately re-run reassignment afterward) — Kafka doesn't automatically re-balance existing partition placements just because you added rack metadata later. This is a "set it up correctly from day one" concern, similar to partition count.

### 🩹 Common Error & Fix

Symptom: an AZ outage took down an unexpectedly large fraction of partitions' availability, even though RF=3 was configured everywhere. Check whether `broker.rack` was ever actually set — RF alone doesn't guarantee failure-domain spread without it.

### 🎤 Interview

**🟢:** Rack awareness kya solve karta hai?
**🔵:** RF=3 hone ke bawajood ek AZ fail se sab kyun fail ho sakta hai?
**🟡:** Rack awareness kab configure karni chahiye?
**🔴:** 3-AZ Kafka cluster design karo jo single-AZ failure ko fully tolerate kare.

---

# 22 — DISASTER RECOVERY & MULTI-CLUSTER

## RPO / RTO

```text
RPO (Recovery Point Objective) → how much data can we afford to lose?
RTO (Recovery Time Objective)  → how fast must we be back up?
```

These two numbers drive almost every DR architecture decision.

## DR patterns

```text
Active-passive: primary cluster serves traffic, secondary stays in sync, promoted on failure
Active-active:  both clusters serve traffic; requires careful handling of duplicate/conflicting writes
```

## MirrorMaker 2

A Kafka-to-Kafka replication tool, commonly used for:

```text
Disaster recovery (replicate topics to a standby region/cluster)
Migration (moving from one cluster to another with minimal downtime)
Data locality (bringing relevant topics closer to consumers in another region)
Selective topic replication (not everything needs to replicate everywhere)
```

## Concerns to design for

Consumer recovery after failover, duplicate event handling (replication can introduce at-least-once semantics across clusters), cross-region latency/cost, and **offset continuity** — offsets are not guaranteed identical across independently replicated clusters, so consumers may need offset-translation logic during failover.

### 🧪 Try It Yourself

Design exercise: for a payments topic with an RPO of "zero data loss" and an RTO of "under 5 minutes," sketch whether active-active or active-passive fits better, and write out what happens to in-flight (not-yet-committed) consumer offsets during a failover in your chosen model.

### 💡 Extra Insight

The offset-continuity problem is one of the most underestimated parts of Kafka DR: a consumer that failed over to a DR cluster **cannot simply reuse its old committed offsets**, because MirrorMaker-replicated topics get new offsets on the target cluster. Real DR designs need either offset translation tooling or a replay-and-deduplicate strategy on failover, not a naive "just point the consumer at the new cluster."

### 🩹 Common Error & Fix

Symptom: after a DR failover, consumers either reprocess huge amounts of duplicate data or skip data entirely. This is the offset-continuity gap above — verify your DR runbook explicitly addresses offset translation (many MirrorMaker 2 deployments use `__consumer_offsets` sync features designed for exactly this), rather than assuming a naive cutover works.

### 🎤 Interview

**🟢:** RPO/RTO kya hain?
**🔵:** Active-active vs active-passive?
**🟡:** MirrorMaker 2 ka use case?
**🔴:** Payments-grade zero-data-loss multi-region DR architecture design karo.

---

# 23 — TIERED STORAGE

## The idea

```text
Hot/recent data → local broker disk (fast, expensive, limited capacity)
Older data      → remote/object storage (S3-class, cheap, effectively unlimited, slower)
```

Instead of every broker needing enough local disk to hold your entire retention window, tiered storage lets Kafka offload older log segments to cheaper object storage while still serving them transparently on request.

## Trade-offs to study

```text
Local disk pressure relief (brokers need far less local disk for the same retention window)
Retrieval latency (reading tiered/remote data is slower than local disk)
Object-storage cost model (cheap storage, but data-transfer/API-call costs apply)
Recovery/rebuild time (a broker rebuild is faster since less data needs local replication)
```

### 🧪 Try It Yourself

Design exercise: you need 90 days of retention for a compliance-heavy topic, but only the last 24 hours are ever actually queried by consumers in normal operation. Sketch the storage-cost difference between "keep all 90 days on local broker disk across RF=3" versus "keep 24 hours local, tier the remaining 89 days to object storage" — this cost delta is the entire business case for tiered storage in one exercise.

### 💡 Extra Insight

Tiered storage changes the economics of retention *policy conversations* — teams that previously fought over "how long can we afford to keep this data locally" can often say "keep it for a year" once the bulk of that year lives in cheap object storage rather than expensive replicated broker disk.

### 🩹 Common Error & Fix

Symptom: consumers doing a full historical replay (e.g., rebuilding derived state from `--from-beginning`) see much higher latency than expected. Check whether they're reading mostly tiered (remote) data rather than local — replay performance against fully tiered history is a different capacity-planning conversation than "recent data lag," and dashboards/alerts should distinguish the two.

### 🎤 Interview

**🟢:** Tiered storage kya solve karta hai?
**🔵:** Hot vs cold data ka trade-off?
**🟡:** Replay performance tiered storage se kaise affect hoti hai?
**🔴:** Compliance-heavy long-retention topic ke liye cost-optimized storage architecture design karo.

---

# 24 — SCHEMA REGISTRY & EVENT CONTRACTS

## Why contracts matter

Kafka transports bytes — it has no opinion about what those bytes mean. Enterprise systems with independently deployed producers and consumers need an explicit **contract** so one team's change doesn't silently break another team's consumer.

## Formats

```text
Avro         → compact binary, strong schema evolution tooling, common in JVM/data-engineering shops
Protobuf     → compact binary, strong typing, common in polyglot/microservices shops
JSON Schema  → human-readable, easier debugging, larger payloads
```

## Compatibility modes (Schema Registry)

```text
BACKWARD → new schema can read data written with the OLD schema (safe to upgrade consumers first)
FORWARD  → old schema can read data written with the NEW schema (safe to upgrade producers first)
FULL     → both directions hold
NONE     → no compatibility checking (dangerous for shared topics)
```

## Schema evolution rules of thumb

Adding an optional field with a default = usually safe. Removing a required field, or changing a field's type, = usually breaking. Renaming a field without an alias = usually breaking.

### 🧪 Try It Yourself

Design exercise: your `OrderCreated` event currently has `{orderId, amount}`. You want to add a `currency` field. Write out the schema change under `BACKWARD` compatibility mode (hint: it needs a default value) and explain why a consumer that hasn't been redeployed yet still works fine after this producer-side change ships.

### 💡 Extra Insight

`BACKWARD` compatibility is the most common mode for a reason: it lets you **deploy consumers first**, then producers, without a synchronized "big bang" release — the new consumer code can already handle the new schema, and old producer traffic (old schema) still parses fine because the new schema is a superset with defaults. Getting the deployment *order* right for your chosen compatibility mode is as important as choosing the mode itself.

### 🩹 Common Error & Fix

```text
org.apache.kafka.common.errors.SerializationException: Error deserializing... Schema not found
```
Usually a schema wasn't registered before a producer started using it, or the Schema Registry URL config is wrong on one side — verify both producer and consumer point at the same registry and that the schema was actually registered (not just used locally).

### 🎤 Interview

**🟢:** Schema Registry kya solve karta hai?
**🔵:** Avro vs JSON Schema trade-off?
**🟡:** BACKWARD vs FORWARD compatibility?
**🔴:** Multi-team shared-topic schema governance model design karo (ownership, versioning, breaking-change process).

---

# 25 — RETRY, DLQ/DLT & POISON MESSAGES

## The pattern

```text
Main Topic
   ↓
Consumer
   ├── success → commit and move on
   └── failure → retry/backoff → if still failing → DLQ/DLT
```

## Transient vs permanent errors

```text
Transient (retry makes sense): downstream DB temporarily unavailable, network blip, rate limit
Permanent (retry is pointless): malformed/corrupt payload, schema validation failure, business-rule violation
```

## 🔥 Gotcha

**Don't retry permanent schema/validation errors forever.** A message that will never successfully process (a poison message) retried in an infinite loop can block an entire partition's progress for every message behind it, since ordering within a partition means the consumer can't skip ahead.

## Retry topic pattern

```text
orders → orders-retry-1 (after backoff) → orders-retry-2 (longer backoff) → orders-dlq
```

Bounded retry counts with increasing backoff, landing in a DLQ/DLT after exhausting retries, keeps the main topic's consumer unblocked.

### 🧪 Try It Yourself

Design exercise: sketch the exact header/metadata you'd attach to a message when routing it to a DLQ (original topic, original partition/offset, failure reason, retry count, first-failed timestamp) — this is what makes a DLQ actually *useful* for debugging and safe replay later, rather than just a graveyard of unexplained failures.

### 💡 Extra Insight

A DLQ without monitoring is worse than no DLQ at all — it silently absorbs failures that nobody looks at, creating a false sense of "the pipeline is healthy" while real data is quietly being dropped into a topic no one watches. **DLQ volume should always have an alert**, not just a topic.

### 🩹 Common Error & Fix

Symptom: consumer lag on the main topic spikes to enormous numbers and never recovers. Check whether a single poison message is stuck being retried in an infinite loop on one partition — every message behind it in that partition is blocked from being processed at all, which is a very different (and more urgent) problem than generic "slow consumer" lag.

### 🎤 Interview

**🟢:** DLQ kya hai?
**🔵:** Transient vs permanent error?
**🟡:** Poison message partition ko kaise block kar sakta hai?
**🔴:** Bounded retry + DLQ + replay ka production-grade design banao.

---

# 26 — KAFKA STREAMS

## Core building blocks

```text
KStream → an unbounded stream of individual records (like the raw topic)
KTable  → a continuously updated "current state per key" view (backed by a compacted changelog topic)
```

## Architecture

```text
Kafka → Kafka Streams App (your JVM process) → Kafka
```

Kafka Streams is a **library**, not a separate cluster — your application *is* the processing engine, using Kafka itself for input, output, and internal state (via changelog topics backing local state stores).

## Key concepts

```text
State stores    → local (often RocksDB-backed) storage for aggregation/join state, backed by a Kafka changelog topic for fault tolerance
Joins           → KStream-KStream, KStream-KTable, KTable-KTable, each with different semantics
Aggregations    → count, sum, reduce, etc., typically producing a KTable
Windowing       → tumbling, hopping, sliding windows for time-bounded aggregations
Repartitioning  → sometimes Streams must re-key and rewrite through an internal topic before a join/aggregation can proceed correctly
```

### 🧪 Try It Yourself

Conceptual code for a simple word-count-style aggregation (the "hello world" of stream processing, adapted to events):

```java
StreamsBuilder builder = new StreamsBuilder();
KStream<String, String> orders = builder.stream("orders");

KTable<String, Long> orderCountByCustomer = orders
    .groupBy((key, value) -> extractCustomerId(value))
    .count();

orderCountByCustomer.toStream().to("order-counts-by-customer");
```

Run it, produce a few orders for the same customer key, and consume `order-counts-by-customer` — watch the count update incrementally per key, live.

### 💡 Extra Insight

Every stateful operation (aggregation, join) in Kafka Streams creates an **internal changelog topic** automatically, even if you never explicitly created one — this is how Streams achieves fault tolerance for local state (a crashed instance can rebuild its RocksDB state store by replaying the changelog). This is invisible until you look at your cluster's topic list and wonder where a dozen extra `*-changelog` topics came from.

### 🩹 Common Error & Fix

```text
TopologyException: Invalid topology: ... needs to be re-partitioned
```
A join or aggregation needs data grouped by a different key than it currently is — Streams needs an explicit repartition step (often automatic, but sometimes requiring `.repartition()` in code) before the operation can proceed correctly.

### 🎤 Interview

**🟢:** KStream vs KTable?
**🔵:** State store kya hai?
**🟡:** Changelog topic kyun automatically banti hai?
**🔴:** Real-time customer order-count dashboard ka Kafka Streams architecture design karo.

---

# 27 — ADVANCED STREAM PROCESSING

## Three notions of time

```text
Event time      → when the event actually happened (embedded in the record, set by the producer)
Ingestion time   → when the broker received/appended the record
Processing time  → when the consumer/stream-processing app actually processes it
```

These three can differ significantly, especially with network delays, retries, or backfills — and **which one you use changes your windowing results**.

## Late events & watermarks

An event with an event-time timestamp *earlier* than data already processed (arriving late due to network delay, mobile offline sync, etc.) needs a policy: how late is "too late" to still include in a window's aggregation? **Watermarks** are the mechanism many stream-processing systems use to formalize "we're not going to wait any longer for late data for this window."

## Windowing types

```text
Tumbling → fixed-size, non-overlapping windows (e.g., every 5 minutes, no overlap)
Hopping  → fixed-size, overlapping windows (e.g., 5-minute windows every 1 minute)
Sliding  → windows defined relative to record pairs within a time gap (common for joins)
```

### 🧪 Try It Yourself

Design exercise: you're computing "orders per 5-minute window" for a fraud-detection system. A mobile app's order event was created at 10:00:00 (event time) but only reaches Kafka at 10:07:00 due to the phone being offline (ingestion/processing time). Write out what happens to this event under: (a) event-time windowing with a 2-minute allowed lateness, and (b) processing-time windowing. Which one gives the fraud system the "correct" answer, and why might the other one still be useful for a different use case (e.g., operational dashboards)?

### 💡 Extra Insight

Choosing processing-time windowing is often simpler to reason about and reproduce in tests, but it silently gives *different* answers depending on system load, restarts, and network hiccups — the same historical data reprocessed later can produce different window boundaries than it did the first time. Event-time windowing is more complex to implement correctly (needs watermarks/lateness policy) but gives deterministic, reproducible results regardless of when processing actually happens.

### 🩹 Common Error & Fix

Symptom: "we re-ran our nightly batch stream job over yesterday's data and got slightly different aggregation numbers than the live run produced." Classic processing-time-windowing artifact — switch to event-time windowing with an explicit lateness policy if reproducibility matters (common in financial/compliance reporting contexts).

### 🎤 Interview

**🟢:** Event time vs processing time?
**🔵:** Tumbling vs hopping window?
**🟡:** Late event ko kaise handle karoge?
**🔴:** Fraud-detection windowed aggregation pipeline design karo jo reproducible results de.

---

# 28 — MODERN CONSUMER / SHARE-GROUP AWARENESS

## Classic consumer groups (recap)

```text
One partition → owned by at most one consumer in a group at a time
```

This gives ordering-per-partition but means scaling beyond the partition count doesn't help, and a single slow message on a partition blocks everything behind it on that partition.

## Share groups (newer Kafka client API)

Newer Kafka versions expose a share-consumption model (`KafkaShareConsumer` / `ShareConsumer`) alongside the classic consumer group API — allowing **multiple consumers to cooperatively consume from the same partition**, more like a traditional competing-consumers queue, rather than strict one-partition-per-consumer ownership.

## When each model fits

```text
Classic consumer group → you need per-key/per-partition ORDERING (e.g., per-customer event sequencing)
Share group             → you need maximum parallelism/throughput and DON'T need strict per-partition ordering
                          (e.g., independent, order-agnostic work items)
```

### 🧪 Try It Yourself

Design exercise (conceptual, since share groups are a newer/evolving API — check current Kafka client documentation for exact availability in your version): take a workload you'd normally model as "one partition per worker" and ask whether the work items in that topic actually *need* to be processed in order relative to each other. If genuinely not (e.g., independent image-processing jobs, each self-contained), a share-group-style model could let you scale consumers past partition count without redesigning your topic's partitioning.

### 💡 Extra Insight

The existence of share groups doesn't replace classic consumer groups — it's an additional tool for a specific problem shape (queue-like, order-independent work) that classic consumer groups handle awkwardly (you'd otherwise need one partition per potential worker, which runs into the "more partitions isn't free" problem from Module 20).

### 🩹 Common Error & Fix

Symptom: "we over-provisioned partitions just so we could have more consumer instances than our natural key cardinality would suggest, purely for throughput." This is exactly the situation share groups are meant to address — worth evaluating as an alternative to partition-count inflation, depending on what your Kafka client/broker version actually supports.

### 🎤 Interview

**🟢:** Classic consumer group ki limitation kya hai jo share group solve karta hai?
**🔵:** Ordering requirement kaise decide karta hai kaunsa model use karna hai?
**🟡:** Share group workload ke liye partition design kaise badalta hai?
**🔴:** Order-agnostic high-throughput job queue ko Kafka par design karo, classic vs share-group trade-off ke saath.

---

# 29 — MODERN CONSUMER GROUP PROTOCOLS

## The coordination chain

```text
Membership → Coordination → Assignment → Processing → Failure → Reassignment
```

Every consumer group, regardless of assignor strategy, goes through this cycle: consumers join (membership), a group coordinator broker manages the process (coordination), partitions get handed out (assignment), consumers work (processing), and if a consumer fails to heartbeat in time or crashes, the cycle restarts (failure → reassignment).

## Protocol evolution

```text
Classic group protocol        → eager rebalancing, well-understood, but "stop the world" on every membership change
Cooperative rebalancing       → incremental reassignment (see Module 12)
Static membership             → stable identity across restarts, avoiding unnecessary rebalances entirely for planned restarts
Newer group-protocol work     → ongoing evolution aimed at faster, less disruptive rebalancing at scale
```

## Heartbeats & failure detection

```text
session.timeout.ms      → how long the coordinator waits without a heartbeat before considering a consumer dead
heartbeat.interval.ms   → how often the consumer sends a heartbeat (should be well below session.timeout.ms)
max.poll.interval.ms    → separately governs "is this consumer still actively processing," independent of heartbeats
```

### 🧪 Try It Yourself

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group order-service --state
```

Expected-shape output showing the group's coordination state:

```text
GROUP          COORDINATOR (ID)  ASSIGNMENT-STRATEGY   STATE           #MEMBERS
order-service  localhost:9092(1) cooperative-sticky    Stable          3
```

Kill one consumer process (simulate a crash) and re-run this command a few seconds later — watch `STATE` transition through something like `PreparingRebalance` → `CompletingRebalance` → `Stable` as the group recovers.

### 💡 Extra Insight

`heartbeat.interval.ms` and `max.poll.interval.ms` protect against two *different* failure modes: a truly dead/crashed process (caught by missing heartbeats, which run on a separate background thread) versus a technically-alive-but-stuck process that's still sending heartbeats but never calling `poll()` again (caught by `max.poll.interval.ms`). Confusing these two settings is a common source of "why didn't Kafka detect my hung consumer faster" debugging sessions.

### 🩹 Common Error & Fix

Symptom: a hung (deadlocked) consumer thread doesn't get evicted from the group for several minutes, even though it's clearly stuck. Check whether heartbeats are sent from a *separate* background thread that's still running fine, while the actual processing thread is deadlocked — in that case, only `max.poll.interval.ms` (not the heartbeat mechanism) will eventually catch it, and it may be configured too generously for how quickly you need detection.

### 🎤 Interview

**🟢:** Group coordination cycle ke steps kya hain?
**🔵:** `heartbeat.interval.ms` vs `session.timeout.ms`?
**🟡:** Heartbeat aur `max.poll.interval.ms` alag failure modes kyun catch karte hain?
**🔴:** Large-scale (100+ consumer) group ke liye fast, low-disruption failure-detection tuning design karo.

---

# 30 — PRODUCER INTERNALS

## The pipeline

```text
Application
 ↓
Serializer         (object → bytes)
 ↓
Partitioner        (decides which partition, based on key or default strategy)
 ↓
Accumulator/batches (records buffered per-partition, waiting to be sent)
 ↓
Sender             (background I/O thread, sends batches to brokers)
 ↓
Broker
```

## Key internals

```text
Batching       → records for the same partition accumulate into a batch before sending
linger.ms      → how long to wait for a batch to fill before sending anyway
compression    → applied per-batch, not per-record
retries        → automatic resend on retriable errors
acknowledgements (acks) → 0 (fire-and-forget), 1 (leader only), all (leader + ISR)
sequence numbers → used internally by idempotent producers to detect/suppress duplicate retries
delivery.timeout.ms → the outer bound on how long the producer will keep trying before giving up entirely
```

## Trade-off

More batching (higher `linger.ms`, larger `batch.size`) can improve throughput and compression ratio, but increases per-record latency — a classic throughput-vs-latency dial.

### 🧪 Try It Yourself

```java
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
```

Send a burst of 10,000 small records with `linger.ms=0` versus `linger.ms=20`, and compare both total time-to-send and average per-record latency — you should see `linger.ms=20` finish the whole batch faster overall, while individual early records technically waited a few extra milliseconds.

### 💡 Extra Insight

The accumulator batches **per partition**, not globally — a producer sending to 50 partitions is managing 50 independent batches concurrently, each governed by the same `linger.ms`/`batch.size` settings. A key implication: a producer sending to many partitions with low per-partition volume may never fill a batch to `batch.size` and will mostly be governed by the `linger.ms` timeout instead.

### 🩹 Common Error & Fix

```text
org.apache.kafka.common.errors.TimeoutException: Expiring ... record(s) ... due to timeout while requesting metadata
```
Often means the producer can't get fresh metadata for the target topic/partition (broker unreachable, topic doesn't exist, authorization failure) within `delivery.timeout.ms` — check basic connectivity/topic-existence/ACLs before assuming it's a performance-tuning problem.

### 🎤 Interview

**🟢:** Producer pipeline ke stages kya hain?
**🔵:** `linger.ms` kya karta hai?
**🟡:** Batching per-partition hone ka implication kya hai for many-partition topics?
**🔴:** High-throughput producer configuration design karo for a 50-partition topic with bursty traffic.

---

# 31 — BROKER / NETWORK REQUEST PATH

## Producer path

```text
Producer → Network → Broker request handling → Partition leader → Log append → Replication → Response
```

## Consumer path

```text
Consumer → Fetch request → Broker → Partition log → Response
```

## What to study at this layer

```text
Network bandwidth   → how much data can physically move between clients/brokers/replicas per second
Request latency     → time for a broker to handle produce/fetch requests (queueing + processing + I/O)
Disk throughput     → sequential write speed for appends, read speed for fetches not in page cache
Page cache          → how much of the "hot" data is servable from memory vs requiring actual disk reads
Replication          → follower fetch requests add to a broker's total request load, on top of client traffic
Compression/message size → smaller, well-compressed messages reduce load at every stage of this path
```

### 🧪 Try It Yourself

```bash
docker stats kafka-broker-1
```

While running a producer perf test, watch CPU and network I/O climb — then also check:

```bash
kafka-run-class.sh kafka.tools.JmxTool --jmx-url service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi \
  --object-name kafka.network:type=RequestMetrics,name=TotalTimeMs,request=Produce
```

(Exact JMX tooling/metric names vary by version — the point of the exercise is connecting a real load test to real request-latency metrics, not memorizing one specific command.)

### 💡 Extra Insight

A broker's request-handling threads are a **shared, finite resource pool** — a burst of expensive fetch requests (e.g., many consumers doing a cold historical replay that misses page cache and hits disk) can slow down produce-request latency for completely unrelated topics on the same broker, because they're competing for the same thread pool and disk I/O capacity. This is why "one topic's traffic pattern can degrade another topic's latency" is a real production phenomenon, not a myth.

### 🩹 Common Error & Fix

Symptom: produce latency degrades cluster-wide whenever a particular batch job runs (e.g., a nightly analytics replay). Check whether that job is doing a large historical read that's missing page cache and hitting disk hard — consider isolating such jobs to specific brokers/partitions, throttling their fetch rate, or scheduling them during lower-traffic windows.

### 🎤 Interview

**🟢:** Producer aur consumer ka broker request path kya hai?
**🔵:** Page cache request latency ko kaise affect karta hai?
**🟡:** Replication traffic normal client traffic ke saath kaise compete karta hai?
**🔴:** Ek analytics replay job ko production traffic se isolate karne ka broker-level design banao.

---

# 32 — EXACTLY-ONCE DEEP DIVE

## Five separate concepts, often conflated

```text
1. Producer idempotency        → no duplicates from THIS producer's own retries, single partition, single session
2. Kafka transactions          → atomic multi-partition writes, all-or-nothing commit
3. Read-committed isolation    → consumers can choose to only see committed transactional writes, not in-flight/aborted ones
4. Kafka-to-Kafka exactly-once → combining 1+2+3 for a consume-process-produce pipeline entirely within Kafka
5. External side-effect correctness → a SEPARATE problem Kafka's transactions cannot solve by themselves
```

## 🔥 Critical gotcha

```text
Kafka transaction ≠ global distributed transaction
```

A Kafka transaction can atomically commit writes **across Kafka partitions/topics**. It has no mechanism to atomically roll back a side effect in an external system (a REST API call, a non-Kafka database write) that happened as part of "processing" a record. For those, you still need idempotency keys, a transactional outbox, an inbox/dedup table, or destination-side transactions that are coordinated at the application level, not by Kafka.

### 🧪 Try It Yourself

Design exercise: sketch a consume-process-produce pipeline reading from `orders`, calling a non-Kafka payment API, and producing to `payment-results`. Identify exactly which part of this pipeline Kafka's transactional API *can* make exactly-once (the Kafka read + Kafka write), and which part (the payment API call) needs its own idempotency mechanism (e.g., an idempotency key derived from the order ID, which the payment API itself must honor).

### 💡 Extra Insight

"Exactly-once" in Kafka's own marketing/documentation specifically refers to the Kafka-to-Kafka case (concepts 1–4 above) — this is a precise, well-defined, and genuinely achievable guarantee *within Kafka*. The moment any external system enters the picture (concept 5), "exactly-once" reverts to being an application-design problem that Kafka's transactional API does not, and cannot, solve for you. Recognizing this boundary is one of the highest-signal things a Kafka architect can articulate correctly in an interview.

### 🩹 Common Error & Fix

Symptom: "we enabled Kafka transactions and idempotent producers, but our downstream payment API still shows occasional double-charges." This confirms the gotcha above — Kafka's exactly-once guarantee never covered the payment API call; add an idempotency key to that specific external call, independent of any Kafka configuration.

### 🎤 Interview

**🟢:** Idempotent producer vs transaction — same cheez hai kya?
**🔵:** Read-committed isolation kya karta hai?
**🟡:** Kafka-to-Kafka exactly-once achievable hai kya?
**🔴:** Ek payment-processing pipeline mein Kafka ke exactly-once guarantee ki exact boundary identify karo aur external correctness ka design karo.

---

# 33 — UPGRADES & COMPATIBILITY

## What needs to stay compatible

```text
Broker ↔ Client compatibility  → newer brokers generally support older clients (and vice versa, within a supported range)
Protocol/feature compatibility → newer protocol features may be gated behind explicit version bumps
Connector compatibility        → Kafka Connect plugins pinned to specific Connect/Kafka API versions
Serializer compatibility       → Avro/Protobuf/JSON Schema libraries need to stay in sync with Schema Registry versions
Schema compatibility           → separate from library compatibility — see Module 24
```

## Rolling upgrades

The standard pattern for upgrading a live Kafka cluster without downtime: upgrade brokers one at a time (or in small batches), verifying cluster health (ISR, under-replicated partitions) between each step, before finalizing the new "inter-broker protocol version" that unlocks new features cluster-wide.

## Rollback planning

Before starting any upgrade, know exactly how you'd roll back — including whether the **inter-broker protocol version** has already been bumped (which can make rollback harder or impossible once features depending on the new protocol are in use).

### 🧪 Try It Yourself

Design exercise: write out a rolling-upgrade runbook for a 5-broker cluster, including: the health check to run between each broker upgrade (what specific metric/command confirms "safe to proceed to the next broker"), and the exact point in the process after which rollback becomes significantly harder (hint: usually right after bumping the inter-broker protocol version).

### 💡 Extra Insight

A very common upgrade mistake is bumping the inter-broker protocol version (to "finish" the upgrade and unlock new features) **immediately** after all brokers are on the new binary, without a soak period. If a subtle issue surfaces after the protocol bump, rolling back becomes much harder — experienced operators deliberately wait (days, sometimes longer) at "all brokers upgraded, protocol version NOT yet bumped" before finalizing, specifically to preserve an easy rollback path.

### 🩹 Common Error & Fix

Symptom: after an upgrade, a specific Kafka Connect connector or client library starts throwing serialization/protocol errors it didn't before. Check compatibility matrices for that specific connector/client version against the new broker version before assuming it's a Kafka bug — connector/client lag behind broker upgrades is a very common real-world friction point.

### 🎤 Interview

**🟢:** Rolling upgrade kya hai?
**🔵:** Inter-broker protocol version kya control karta hai?
**🟡:** Rollback protocol bump ke baad kyun harder ho jata hai?
**🔴:** 5-broker production cluster ka safe upgrade + rollback runbook design karo.

---

# 34 — TESTING & FAILURE INJECTION

## What to actually test

```text
Happy path: producer → Kafka → consumer, end to end
Consumer crash mid-processing → does reprocessing behave as expected (duplicates? gaps?)
Broker failure → does leader election happen cleanly, do clients recover without manual intervention?
Leader change (deliberate, not failure) → does the producer/consumer handle it transparently?
Network delay/partition → does the system degrade gracefully or fail catastrophically?
Duplicate delivery → does downstream idempotency actually work as designed, or only in theory?
Replay → does replaying a known range produce the expected (deduplicated) end state?
Lag spike → do alerts fire, does auto-scaling (if any) kick in appropriately?
Schema incompatibility → does a bad producer deploy get caught before or after breaking consumers?
Load/throughput → does the system hold up at 2x, 5x, 10x expected peak load?
```

## What to measure while testing

```text
Throughput, p95/p99 latency (not just average — averages hide the worst-case experience)
CPU, memory, disk, network utilization under load
Consumer lag behavior under injected failure
```

### 🧪 Try It Yourself

Pick just one failure-injection scenario and actually run it end-to-end this week: kill a broker mid-load-test (`docker stop <broker-container>` in a local multi-broker Compose setup) while a producer perf test and a real consumer are both running, and observe: did the producer's requests briefly fail/retry and recover? Did consumer lag spike and then recover? Did you need to intervene manually, or did the system self-heal?

### 💡 Extra Insight

The most valuable failure-injection tests are the ones that validate **assumptions your team has never actually verified**, not the ones that confirm things you already know work. If nobody on the team has ever actually watched a real broker die under real production-shaped load, "we have RF=3 so we're fine" is a belief, not a tested fact — the gap between the two is exactly what chaos/failure-injection testing exists to close.

### 🩹 Common Error & Fix

Symptom: a failure-injection test "passes" in staging but the same failure in production caused a real incident. Check whether staging's data volume, partition count, and traffic shape actually resemble production — many failure modes (rebalance storms, replication lag under load) only appear at realistic scale, and a lightly-loaded staging cluster can hide them entirely.

### 🎤 Interview

**🟢:** Failure injection testing kyun zaroori hai?
**🔵:** p95/p99 latency average se zyada important kyun hai?
**🟡:** Staging mein pass, production mein fail — kyun ho sakta hai?
**🔴:** Ek production-grade chaos-testing program design karo Kafka cluster ke liye.

---

# 35 — ADVANCED OBSERVABILITY

## Full metric taxonomy

### Broker

```text
CPU, memory
Disk usage, disk latency
Network throughput
Request latency (produce, fetch, metadata)
Under-replicated partitions (health signal #1)
Offline partitions (health signal #2 — worse than under-replicated)
```

### Producer

```text
Retries, errors
Request latency
Record send rate
Batch efficiency (average batch size — low values suggest linger.ms/batch.size tuning opportunity)
```

### Consumer

```text
Lag (both message-count and time-based — see Module 07)
Processing latency
Rebalance rate (frequent rebalances are themselves a health signal, not just background noise)
Commit failures
```

### Business-level

```text
Event age (time since the event was produced, at point of business action — the truest end-to-end latency metric)
End-to-end latency
Failed processing count
DLQ count (see Module 25 — must be alerted, not just collected)
Duplicate rate
Schema failures
```

### 🧪 Try It Yourself

Build (even just on paper, or as a real dashboard if you have Grafana/Prometheus available) a single "Kafka pipeline health" dashboard using exactly one metric from each category above — broker, producer, consumer, business. Force yourself to pick the *single most diagnostic* metric per category rather than including everything, which is the actual skill of good observability design (signal over noise).

### 💡 Extra Insight

**Under-replicated partitions** and **offline partitions** are two different severities of the same underlying problem, and conflating them in an alert is a common mistake: under-replicated means "still available, but fault tolerance is currently degraded" (urgent, not necessarily emergency); offline means "this partition currently cannot serve reads or writes at all" (emergency). They deserve different alert severities and different runbooks.

### 🩹 Common Error & Fix

Symptom: "our dashboard shows everything green, but customers are reporting missing/delayed data." Check whether you're monitoring only Kafka-internal metrics (lag, replication health) without any **business-level** metric (event age at point of business action, DLQ count) — a technically healthy Kafka cluster can still be sitting behind a broken or silently-failing consumer application.

### 🎤 Interview

**🟢:** Under-replicated vs offline partitions?
**🔵:** Batch efficiency metric kya batata hai?
**🟡:** Business-level metrics Kafka-internal metrics se alag kyun zaroori hain?
**🔴:** End-to-end observability architecture design karo jo broker se business impact tak trace kare.

---

# 36 — ENTERPRISE EVENT GOVERNANCE

## What needs to be defined, org-wide

```text
Topic naming convention        → e.g., <domain>.<entity>.<event-type> (orders.order.created)
Event naming convention        → consistent tense/structure across teams (past-tense for "this happened")
Ownership                      → which team owns this topic — who do you page when it breaks?
Schema ownership                → who approves schema changes, and under what compatibility mode?
Retention policy                 → per-topic, driven by business/compliance need, not a single cluster-wide default
PII classification              → which topics carry personal data, and what controls apply as a result?
ACL ownership                    → who can grant/request access, and through what process?
SLA/SLO                          → what latency/availability does this event stream promise consumers?
Replay policy                    → who is allowed to trigger a replay, and under what approval process?
Deprecation policy               → how is a topic formally retired, and how long do consumers get to migrate off it?
Consumer ownership                → for shared topics, who is accountable for each consumer's health?
```

## Why this matters at scale

Without these agreed upfront, a growing Kafka platform accumulates undocumented topics with unclear ownership, inconsistent naming that makes discovery painful, and schema changes that break consumers nobody remembered existed — governance debt that gets exponentially more expensive to pay down later.

### 🧪 Try It Yourself

Draft an actual one-page "event contract template" your organization could require for every new topic — at minimum: topic name, owning team, schema location/format, compatibility mode, retention period, PII classification, and a Slack channel/on-call rotation for issues. Having this artifact exist (even in a lightweight form) is the single highest-leverage governance step most growing Kafka platforms are missing.

### 💡 Extra Insight

Governance that's purely a wiki page nobody reads doesn't work — the organizations that do this well encode as much of it as possible into **tooling** (a topic-creation portal that requires filling in ownership/PII/retention fields before provisioning; CI checks that reject schema changes violating the configured compatibility mode) rather than relying on documentation and goodwill alone.

### 🩹 Common Error & Fix

Symptom: "we want to deprecate this old topic but we're not sure who's still consuming it, or if it's safe." This is exactly the failure mode consumer-ownership and deprecation-policy governance prevents — if you're in this position already, the practical fix is enabling broker-level tracking of active consumer groups per topic and treating "unknown consumers found" as a blocker to deprecation, not a green light.

### 🎤 Interview

**🟢:** Event governance kyun zaroori hai?
**🔵:** Topic naming convention ka value kya hai?
**🟡:** Deprecation policy kaise design karoge for a shared topic with unknown consumers?
**🔴:** Enterprise-wide Kafka governance framework design karo (ownership, schema, PII, SLA, deprecation).

---

# 37 — PRODUCTION FAILURE SCENARIOS

## Practice these end-to-end chains, not just definitions

### Broker failure

```text
broker failure → leader election → replica promoted → clients refresh metadata → traffic resumes
```

Practice question: what's different if the broker that failed held the **controller** role vs. a broker that only held partition leadership?

### Consumer failure

```text
consumer failure → coordinator detects (missed heartbeat or poll interval) → rebalance → partitions reassigned → possible duplicates on resume
```

Practice question: how does your consumer's commit strategy (Module 07/10) determine exactly how many duplicates are possible here?

### Producer timeout

```text
producer timeout → ambiguous acknowledgement (did the write actually land?) → retry → idempotency prevents duplicate write
```

Practice question: what happens in this exact scenario if idempotent producer is *not* enabled?

### Disk full

```text
disk full → broker can't append new records → cleanup/capacity/recovery needed
```

Practice question: does a disk-full broker fail immediately and visibly, or does it degrade in a way that's harder to detect quickly?

### Hot partition

```text
hot partition → one partition/consumer overloaded → key-distribution redesign needed
```

Practice question: can you fix this without changing partition count (i.e., purely by fixing key design), or does it sometimes genuinely require both?

### Poison event

```text
poison event → retry/backoff exhausted → DLQ → root cause fixed → corrected replay from DLQ
```

Practice question: what has to be true about your replay tooling for this to be safe (see Module 09's dangerous-mistake warning)?

### 🧪 Try It Yourself

Pick the scenario your team is *weakest* on (be honest) and actually run it against a local multi-broker cluster this week, exactly as described in Module 34's failure-injection approach — reading about a failure chain and having lived through it once are very different levels of readiness.

### 💡 Extra Insight

The single best predictor of how well a team handles a real Kafka incident isn't how much Kafka theory they know — it's **whether they've already answered these "practice questions" once, in a low-stakes setting, before a real incident forces them to answer under pressure.** This is the entire justification for treating Modules 34 and 37 as mandatory, not optional, for any team running Kafka in production.

### 🩹 Common Error & Fix

Symptom: an incident retro reveals the team's mental model of "what happens during X failure" was simply wrong (e.g., assumed idempotent producer was enabled when it wasn't). This is why failure-scenario walkthroughs need to include **checking actual current configuration**, not just reasoning from "what should be configured" — assumptions drift from reality over time as configs change.

### 🎤 Interview

**🟢:** In sab scenarios mein se ek explain karo end-to-end.
**🔵:** Consumer failure ke baad duplicates kaise depend karte hain commit strategy par?
**🟡:** Disk full failure detect karna kyun tricky ho sakta hai?
**🔴:** Ek complete incident-response runbook design karo jo in saari scenarios ko cover kare.

---

# 🧠 MASTER KAFKA FLOW

```text
                 ┌───────────────┐
                 │   Producer    │
                 └───────┬───────┘
                         │
                         ▼
                 ┌───────────────┐
                 │    Topic      │
                 └───────┬───────┘
                         │
             ┌───────────┼───────────┐
             ▼           ▼           ▼
            P0          P1          P2
             │           │           │
             └─────── Kafka Cluster ─┘
                         │
                  Consumer Group
                 ┌───────┼───────┐
                 ▼       ▼       ▼
                C1      C2      C3
                         │
                         ▼
                       DB/API
```

---

# 📖 GLOSSARY (Expanded)

| Term | Plain-language meaning |
|---|---|
| **Broker** | A single Kafka server that stores partitions and serves clients. |
| **Topic** | A named, logical stream of events. |
| **Partition** | An ordered, append-only shard of a topic — the unit of parallelism and ordering. |
| **Offset** | A record's position within one specific partition. |
| **Segment** | A single file (plus index files) making up a portion of a partition's log. |
| **Consumer group** | A set of consumers sharing the work of reading a topic; each partition goes to one member at a time. |
| **Share group** | A newer consumption model allowing multiple consumers to cooperatively read the same partition, competing-consumers style. |
| **ISR (In-Sync Replicas)** | Replicas caught up enough with the leader to be eligible for election on failure. |
| **KRaft** | Kafka's built-in metadata quorum architecture, replacing ZooKeeper. |
| **Rebalance** | Redistribution of partition ownership across a consumer group's members. |
| **Cooperative sticky assignor** | A rebalance strategy that moves only the partitions that must move. |
| **Lag** | How far behind a consumer's position is from the latest available offset. |
| **Idempotent producer** | A producer mode preventing its own retries from creating duplicate records. |
| **Transactional producer** | A producer mode allowing atomic multi-partition writes with all-or-nothing commit semantics. |
| **CDC (Change Data Capture)** | Streaming database changes as events, instead of polling. |
| **Kafka Connect** | A framework of source/sink connectors moving data between Kafka and external systems. |
| **Outbox pattern** | Writing a business row and its event in one DB transaction, then relaying the event to Kafka. |
| **Schema Registry** | A service enforcing compatibility rules for event schemas (Avro/Protobuf/JSON Schema). |
| **Compaction** | Retention mode keeping only the latest record per key, forever. |
| **Tombstone** | A record with a null value signaling "delete this key's state." |
| **ACL** | A rule defining which principal can perform which operation on which resource. |
| **Quota** | A rate limit on a client's produce/consume/request throughput. |
| **AdminClient** | The API/tooling layer for cluster operations (topics, configs, ACLs, reassignment). |
| **Rack awareness** | Replica-placement logic that spreads replicas across failure domains (AZs/racks). |
| **RPO / RTO** | Recovery Point/Time Objective — how much data loss and downtime is acceptable in a DR event. |
| **MirrorMaker 2** | A Kafka-to-Kafka replication tool used for DR, migration, and data locality. |
| **Tiered storage** | Offloading older log segments to cheap remote/object storage. |
| **KStream / KTable** | Kafka Streams abstractions for an unbounded record stream vs. a current-state-per-key view. |
| **Watermark** | A stream-processing mechanism formalizing "how late is too late" for out-of-order events. |
| **DLQ / DLT** | Dead Letter Queue/Topic — where permanently-failing messages land after exhausting retries. |

---

# 🩺 COMMON ERROR MESSAGES REFERENCE (Expanded)

| Error message (shortened) | Likely cause | Typical fix |
|---|---|---|
| `TimeoutException: Topic ... not present in metadata` | Wrong `advertised.listeners` | Fix advertised listener to a client-reachable address |
| `NOT_LEADER_OR_FOLLOWER` | Stale metadata after leader election | Usually transient; client refreshes and retries |
| `NotEnoughReplicasException` | ISR below `min.insync.replicas` | Investigate replica health, don't just lower the setting |
| `CommitFailedException: group has already rebalanced` | Processing exceeded `max.poll.interval.ms` | Speed up processing or raise the interval deliberately |
| `ProducerFencedException` | Two producers shared one `transactional.id` | Coordinate rolling deploys so only one instance holds it |
| `Assignments can only be reset if the group is inactive` | Reset attempted with live consumers | Stop all consumers first, then reset |
| `replication slot "debezium" is already active` | Previous CDC connector still holds the slot | Stop the old connector, or use a different slot name |
| `TopicAuthorizationException` | No ACL granting the client access | Grant the specific ACL needed |
| `TopologyException: needs to be re-partitioned` | Kafka Streams join/aggregation needs a different key grouping | Add explicit `.repartition()` or let Streams auto-repartition |
| `SerializationException: Schema not found` | Schema not registered, or wrong Schema Registry URL | Verify both sides point at the same registry; confirm registration |
| `TimeoutException: Expiring record(s) due to timeout while requesting metadata` | Producer can't get metadata (broker unreachable, topic missing, ACL issue) | Check connectivity/topic existence/ACLs first |

---

# ⚡ COMMAND REFERENCE BY TASK (Expanded)

```bash
# ---- Topics ----
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic orders --partitions 3 --replication-factor 1
kafka-topics.sh --bootstrap-server localhost:9092 --list
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic orders
kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic orders --partitions 6

# ---- Produce / Consume ----
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic orders
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders --from-beginning
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders --group order-service

# ---- Keyed produce/consume ----
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic orders \
  --property "parse.key=true" --property "key.separator=:"
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders \
  --property print.key=true --property key.separator=" -> "

# ---- Consumer groups & lag ----
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group order-service
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group order-service --state

# ---- Offset reset / replay ----
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group order-service --topic orders --reset-offsets --to-earliest --dry-run
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group order-service --topic orders --reset-offsets --to-datetime 2026-09-20T00:00:00.000 --execute

# ---- Cluster / quorum ----
kafka-metadata-quorum.sh --bootstrap-server localhost:9092 describe --status

# ---- Configs (retention, compaction, etc.) ----
kafka-configs.sh --bootstrap-server localhost:9092 --alter \
  --entity-type topics --entity-name orders --add-config retention.ms=604800000
kafka-configs.sh --bootstrap-server localhost:9092 --describe --entity-type topics --entity-name orders

# ---- Quotas ----
kafka-configs.sh --bootstrap-server localhost:9092 --alter \
  --add-config 'producer_byte_rate=1048576,consumer_byte_rate=2097152' \
  --entity-type clients --entity-name team-a-service

# ---- Partition reassignment ----
kafka-reassign-partitions.sh --bootstrap-server localhost:9092 \
  --topics-to-move-json-file topics.json --broker-list "1,2,3" --generate

# ---- Performance testing ----
kafka-producer-perf-test.sh --topic orders --num-records 1000000 --record-size 200 \
  --throughput -1 --producer-props bootstrap.servers=localhost:9092 acks=1

# ---- Kafka Connect ----
curl -X POST -H "Content-Type: application/json" --data @connector.json http://localhost:8083/connectors
curl http://localhost:8083/connectors/orders-connector/status
curl -X DELETE http://localhost:8083/connectors/orders-connector
```

---

# 🔥 MASTER INTERVIEW LADDER

*(Modules 00–14's ladder retained in full below; modules 15–37 add architect-track depth covered inline within each module above.)*

## 🟢 BEGINNER

1. What is Kafka?
2. Why do we need Kafka?
3. Kafka vs traditional queue?
4. What is a topic?
5. What is a partition?
6. What is an offset?
7. What is a broker?
8. What is a producer?
9. What is a consumer?
10. What is a consumer group?
11. What is replication?
12. Where does Kafka guarantee ordering?
13. What is retention?
14. What is consumer lag?
15. What is KRaft?

## 🔵 JUNIOR / DEVELOPER

1. Why are partitions needed?
2. What happens if consumers > partitions?
3. What happens if partitions > consumers?
4. How does a key affect partitioning?
5. What is `__consumer_offsets`?
6. Auto commit vs manual commit?
7. What causes consumer rebalance?
8. What is ISR?
9. What does `acks=all` mean?
10. Why use idempotent producer?
11. What is `advertised.listeners`?
12. Why can Docker Kafka bootstrap successfully but later fail?
13. How do you replay Kafka messages?
14. What is Kafka Connect?
15. Source connector vs sink connector?

## 🟡 SENIOR

1. How would you choose partition count?
2. How do you preserve ordering for customer events?
3. How do you diagnose high consumer lag?
4. Why can increasing consumers fail to improve throughput?
5. How do hot partitions happen?
6. How does consumer rebalance affect latency?
7. How would you design retry handling?
8. At-most-once vs at-least-once vs exactly-once?
9. Why doesn't Kafka exactly-once automatically solve external DB duplication?
10. What is idempotent producer?
11. What is the role of ISR?
12. How does KRaft differ from ZooKeeper architecture?
13. How do you safely increase partitions?
14. How would you design replay?
15. How would you design CDC from PostgreSQL to Kafka?
16. What is log compaction and when would you use it? *(new)*
17. What's the difference between BACKWARD and FORWARD schema compatibility? *(new)*
18. How do you design a bounded-retry-then-DLQ pipeline? *(new)*

## 🟠 LEAD

1. Design Kafka for 500K events/sec.
2. How would you size partitions?
3. How do you choose replication factor?
4. How do you choose `min.insync.replicas`?
5. What metrics would you put on the production dashboard?
6. How would you handle a consumer that is 2 hours behind?
7. How do you handle poison messages?
8. How would you design retry topics/DLQ?
9. How would you avoid hot partitions?
10. How would you roll out a schema change?
11. How would you design multi-AZ Kafka?
12. What happens when one AZ fails?
13. How would you migrate ZooKeeper Kafka to KRaft?
14. How would you secure Kafka?
15. How would you control Kafka cost?
16. How would you design a multi-tenant quota/ACL governance model? *(new)*
17. How would you plan a rolling upgrade with a safe rollback point? *(new)*

## 🔴 ARCHITECT

### Scenario 1 — Enterprise Event Backbone

```text
Salesforce → MuleSoft / Integration → Kafka
   ├── Analytics
   ├── ERP
   ├── Notifications
   ├── Data Lake
   └── AI
```

Questions: What are event contracts? Who owns schemas? How do you version events? What is the replay strategy? How do you prevent duplicate side effects? What are retention requirements? What are RPO/RTO? How do you isolate teams?

### Scenario 2 — 1M events/sec

Reason about: partition count, broker count, disk throughput, network throughput, replication traffic, producer batching, compression, consumer parallelism, downstream capacity.

### Scenario 3 — Multi-region

Questions: active-active or active-passive? Replication strategy? Duplicate events? Ordering? Failover? Data residency? Disaster recovery? Offset continuity?

### Scenario 4 — Exactly-once payments pipeline *(new)*

Identify precisely where Kafka's exactly-once guarantee ends and application-level correctness (idempotency keys, outbox) must begin, for a pipeline that calls an external payment API.

### Scenario 5 — Multi-tenant platform governance *(new)*

Design the combined quota + ACL + schema-governance + deprecation-policy framework for a shared Kafka platform serving 10+ independent teams.

---

# ⚠️ TOP ERRORS & GOTCHAS (Expanded)

1. **Global ordering assumption** — ordering is per-partition, not per-topic.
2. **More consumers = more throughput** — not necessarily; partition count limits parallelism.
3. **Consumed means deleted** — false; retention controls Kafka record lifetime.
4. **Exactly-once everywhere** — false; external side effects need their own correctness mechanism.
5. **Bad partition key** — creates hot partitions.
6. **Wrong advertised listener** — very common Docker/local-cluster failure.
7. **Ignoring lag** — lag is a symptom; find the bottleneck.
8. **Increasing partitions casually** — changes partitioning behavior and ordering assumptions.
9. **Treating Kafka as a database** — it's a durable event log, not a relational query engine.
10. **No idempotency** — retries + replay + crashes can produce duplicates.
11. **`min.insync.replicas` without `acks=all`** — durability guarantee only kicks in with `acks=all`.
12. **Trusting message-count lag alone** — pair with time-based lag.
13. **Compaction is instant** *(new)* — it's asynchronous; multiple versions of a key can coexist temporarily.
14. **More partitions are always better** *(new)* — adds controller/metadata/rebalance overhead for no benefit past real parallelism needs.
15. **RF=3 automatically means AZ-fault-tolerant** *(new)* — only true with rack/AZ-aware replica placement configured.
16. **DLQ without alerting is a safety net** *(new)* — an unmonitored DLQ silently absorbs real failures.
17. **Retrying permanent errors indefinitely** *(new)* — blocks the entire partition behind the poison message.
18. **Encryption in transit = access control** *(new)* — TLS alone doesn't grant authorization; ACLs are a separate control.

---

# 🎯 DELIVERY SEMANTICS

## At-most-once

```text
commit → process
```

Possible loss, lower duplicate risk.

## At-least-once

```text
process → commit
```

Possible duplicates, much safer for avoiding loss.

## Exactly-once

Requires carefully designed transactional/idempotent processing. For Kafka-to-Kafka:

```text
consume → transaction → produce → commit
```

For external systems (`Kafka → DB/API`), you need destination-side correctness mechanisms too (see Module 32).

---

# 🧩 KAFKA + OUTBOX

```text
Application
 ├── DB transaction
 │     ├── Business row
 │     └── Outbox event
 └── Commit
          ↓
      CDC / Relay
          ↓
        Kafka
```

Outbox makes the business transaction and event creation atomic in the same database transaction, avoiding dual-write inconsistency.

---

# 🧩 KAFKA + CDC

```text
PostgreSQL → WAL / transaction log → Debezium → Kafka → Consumers
```

---

# 🧩 KAFKA + AI

```text
Applications → Kafka → Feature / Event Processing → AI / ML Systems → Predictions / Actions → Kafka
```

AI does not replace Kafka — Kafka provides the event backbone around AI systems.

---

# 🧩 KAFKA + CLOUD

```text
Self-managed Kafka   vs   Managed Kafka
```

Self-managed: maximum control, more operational responsibility. Managed: less platform maintenance, still requires application-level Kafka expertise.

---

# 💰 COST / PERFORMANCE THINKING

```text
Storage + Replication + Network + Broker compute + Retention + Cross-region traffic
```

```text
1 TB logical data, RF=3 ≈ 3 TB raw replicated storage
```

Tiered storage (Module 23) can materially change this equation for older data.

---

# 📊 PRODUCTION MONITORING

See Module 35 for the full expanded metric taxonomy (broker/producer/consumer/business-level).

---

# 🏗️ ARCHITECT DECISION TREE

```text
Need synchronous request/response?
        │
        ├── YES → REST/gRPC may fit
        │
        └── NO
             ↓
Need durable asynchronous events?
             │
             ├── YES → Kafka candidate
             │
             └── NO → simpler mechanism may fit
```

Then:

```text
Need ordering? → Define ordering key → Choose partition key → Choose partition count
 → Choose replication → Choose retention/compaction → Choose consumer model (classic vs share group)
 → Define failure/replay strategy → Define schema governance → Define security (AuthN/AuthZ/quotas)
 → Define observability → Define multi-AZ/DR posture
```

---

# 🧪 REAL-WORLD END-TO-END EXAMPLE

## E-commerce order

```text
Customer → Order Service → Kafka: order-events → Partition by orderId → Replicated Kafka Cluster
   ↓
Consumer Groups
   ├── Payment
   ├── Inventory
   ├── Shipping
   ├── Notification
   ├── Analytics
   └── Data Lake
```

If Analytics fails, Payment/Inventory/Shipping continue unaffected — Analytics catches up later. This is the key decoupling value.

## Scenario — Poison Message Stalls a Consumer

```text
Consumer crashes repeatedly on the same message → is it deserialization failure or business-logic exception?
 → Route to DLT after N retries instead of blocking the partition forever
 → Alert on DLT volume → Fix root cause → optionally replay from the DLT
```

## Scenario — Schema Change Breaks Downstream Consumers

```text
Producer adds a required field without a default → old consumers fail to deserialize
 → Root cause: incompatible schema evolution
 → Fix: Schema Registry with BACKWARD compatibility rejecting breaking changes at publish time
```

## Scenario — Exactly-Once Payment Double-Charge *(new)*

```text
Kafka transactions enabled → payment API still shows duplicate charges
 → Root cause: Kafka's exactly-once never covered the external API call (Module 32)
 → Fix: idempotency key on the payment API call itself, independent of Kafka config
```

## Scenario — DR Failover Reprocesses Everything *(new)*

```text
Failover to MirrorMaker 2 DR cluster → consumers reprocess massive duplicate volume
 → Root cause: offset continuity gap — DR cluster offsets aren't the same as primary's
 → Fix: offset-translation tooling or explicit replay-and-dedupe strategy in the DR runbook
```

---

# 🏆 BEST-PRACTICE CHECKLIST (Expanded)

## Application

- [ ] Choose meaningful event names
- [ ] Define event schema
- [ ] Define partition key
- [ ] Design idempotency
- [ ] Handle retries
- [ ] Handle poison messages (bounded retry + DLQ)
- [ ] Monitor lag (message-count AND time-based)
- [ ] Define replay procedure

## Kafka

- [ ] Correct partition count (not "as many as possible")
- [ ] Appropriate replication factor
- [ ] Correct `min.insync.replicas` + `acks=all` together
- [ ] Security enabled (AuthN + AuthZ, not just encryption)
- [ ] TLS/SASL where required
- [ ] ACLs, least privilege
- [ ] Quotas for multi-tenant fairness
- [ ] Capacity planning
- [ ] Rack/AZ-aware multi-AZ design
- [ ] Monitoring (broker/producer/consumer/business layers)
- [ ] Disaster recovery with offset-continuity plan

## Data

- [ ] Schema evolution policy + compatibility mode
- [ ] Retention vs compaction policy per topic
- [ ] PII classification
- [ ] Encryption
- [ ] Replay policy
- [ ] Data ownership + deprecation policy

---

# 🎓 FINAL MENTAL MODEL

```text
EVENT
 ↓
PRODUCER
 ↓
SERIALIZATION / SCHEMA
 ↓
PARTITION KEY
 ↓
TOPIC / PARTITION
 ↓
LEADER + REPLICAS / ISR
 ↓
RETENTION / COMPACTION
 ↓
CONSUMER GROUP / SHARE MODEL
 ↓
OFFSET / COMMIT
 ↓
PROCESSING
 ↓
RETRY / DLQ / IDEMPOTENCY
 ↓
LAG / OBSERVABILITY
 ↓
STREAMS / CONNECT / CDC
 ↓
SECURITY / QUOTAS / GOVERNANCE
 ↓
MULTI-AZ / DR / MULTI-CLUSTER
 ↓
ENTERPRISE EVENT PLATFORM
```

---

# 🚀 INTERVIEW 30-SECOND ANSWER

> **Apache Kafka is a distributed event streaming platform based on partitioned and replicated logs. Producers publish records to topics, topics are divided into partitions for scalability and per-partition ordering, and consumer groups allow multiple consumers to process partitions in parallel. Kafka retains records independently of consumer progress, which enables replay, and can compact topics to keep only the latest value per key. Replication and ISR provide fault tolerance, while modern Kafka uses KRaft for metadata management. In production, the important architecture decisions are partitioning, ordering, replication, retention/compaction, delivery semantics, idempotency, schema evolution, security, quotas, observability, multi-AZ resilience, and disaster recovery — and knowing precisely where Kafka's own guarantees end and application-level correctness must begin.**

---

# 📌 CURRENT SCENE

For new Kafka learning, focus first on:

```text
KRaft, Topics, Partitions, Consumer Groups, Offsets, Lag,
Replication / ISR, Producer Idempotency, Transactions,
Schema Evolution, Kafka Connect, CDC, Security, Observability
```

Then progress into the architect track: compaction, quotas, sizing, multi-AZ/DR, tiered storage, Streams, and enterprise governance (Modules 15–37).

Treat ZooKeeper primarily as **legacy/transition knowledge**, not as the default architecture for a new Kafka deployment.

---

# 🧾 SOURCE ROADMAP COVERAGE

This master follows the supplied repository roadmap from:

```text
00-kafka-with-salesforce
01-why-kafka-and-what-is-kafka
02-kafka-fundamentals
03-local-setup
04-cli-produce-consume
05-consumer-groups-and-partitions
06-rebalancing-and-scaling-scenarios
07-consumer-offsets-and-lag
08-why-kafka-is-fast
09-offset-reset-and-replay
10-java-consumer-core-api
11-java-producer-and-idempotency
12-rebalance-strategies-and-callbacks
13-kafka-cluster-setup
14-kafka-connect-and-cdc
```

plus the architect-track extension (Modules 15–37) covering log internals, compaction, security, quotas, AdminClient, sizing, rack awareness, DR/MirrorMaker 2, tiered storage, Schema Registry, DLQ patterns, Kafka Streams, advanced stream processing, modern consumer/share-group protocols, producer/broker internals, exactly-once semantics, upgrades, testing, observability, enterprise governance, and production failure scenarios.
