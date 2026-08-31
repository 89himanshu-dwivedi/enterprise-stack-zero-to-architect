package com.suel.kafkaplayground.day02;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Day 02 consumer - commits each record individually and subscribes to several
 * topics with a pattern, so order-events and order-returns land in one place.
 */
public class Day02KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(Day02KafkaConsumer.class);

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "1");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            // order-events, order-returns, and anything else starting with "order"
            consumer.subscribe(Pattern.compile("order.*"));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                records.forEach(record -> {
                    log.info("topic={} partition={} offset={} key={} value={}",
                            record.topic(), record.partition(), record.offset(),
                            record.key(), record.value());

                    // offsets are per partition, so an acknowledgement is expressed per partition
                    TopicPartition topicPartition =
                            new TopicPartition(record.topic(), record.partition());

                    // commit the offset we want to read NEXT, hence the + 1
                    OffsetAndMetadata nextOffset =
                            new OffsetAndMetadata(record.offset() + 1);

                    consumer.commitSync(Map.of(topicPartition, nextOffset));
                    log.info("acknowledged {} up to offset {}", topicPartition, nextOffset.offset());
                });
            }
        } catch (Exception e) {
            log.error("consumer failed", e);
        }
    }
}
