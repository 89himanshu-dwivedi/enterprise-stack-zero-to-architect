package com.suel.kafkaplayground.day01;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * Day 01 - a Kafka consumer with nothing but the core client API.
 * A plain main() on purpose: no Spring here, so every property is visible.
 */
public class Day01KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(Day01KafkaConsumer.class);

    private static final String TOPIC = "order-events";

    public static void main(String[] args) {

        Properties props = new Properties();

        // comma-separate several for a real cluster: localhost:9092,localhost:9093,localhost:9094
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "play-group");

        // the broker only ever hands us bytes - these say what to turn them back into
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // we acknowledge by hand, after the work is done
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // only applies when the group has no committed offset yet
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // static membership - a restart is recognised as the same member, so no 45s rebalance wait
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "1");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(TOPIC));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                records.forEach(record -> log.info(
                        "partition={} offset={} key={} value={}",
                        record.partition(), record.offset(), record.key(), record.value()));

                // poll() times out every second and often returns nothing at all
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        } catch (Exception e) {
            log.error("consumer failed", e);
        } finally {
            consumer.close();
        }
    }
}
