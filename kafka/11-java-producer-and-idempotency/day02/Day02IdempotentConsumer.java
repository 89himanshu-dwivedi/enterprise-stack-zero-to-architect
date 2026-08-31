package com.suel.kafkaplayground.day02;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Day 02 idempotent consumer - the shape you actually want in production.
 * Kafka guarantees at-least-once, so the same record can arrive twice.
 * Guarding against that is the consumer application's job; there is no broker setting for it.
 */
public class Day02IdempotentConsumer {

    private static final Logger log = LoggerFactory.getLogger(Day02IdempotentConsumer.class);

    private static final String EVENT_ID_HEADER = "event-id";

    /** Stands in for a processed_events table with a unique index on event_id. */
    private static final Set<String> processedEventIds = new HashSet<>();

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "card-processing-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "1");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of("card-events"));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    String eventId = eventIdOf(record);

                    if (processedEventIds.contains(eventId)) {
                        log.warn("duplicate eventId={} - acknowledging without processing", eventId);
                        acknowledge(consumer, record);
                        continue;
                    }

                    charge(record);

                    // record the id BEFORE acknowledging - a crash in between just replays the record
                    processedEventIds.add(eventId);
                    acknowledge(consumer, record);
                }
            }
        } catch (Exception e) {
            log.error("consumer failed", e);
        }
    }

    /** Prefers the producer's unique event id; falls back to the record's own coordinates. */
    private static String eventIdOf(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(EVENT_ID_HEADER);
        if (header != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return record.topic() + "-" + record.partition() + "-" + record.offset();
    }

    private static void charge(ConsumerRecord<String, String> record) {
        log.info("processing value={}", record.value());
    }

    private static void acknowledge(KafkaConsumer<String, String> consumer,
                                    ConsumerRecord<String, String> record) {
        TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
        consumer.commitSync(Map.of(topicPartition, new OffsetAndMetadata(record.offset() + 1)));
    }
}
