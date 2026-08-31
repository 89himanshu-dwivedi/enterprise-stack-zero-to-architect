package com.suel.kafkaplayground.day03;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Day 03 consumer - built to be started several times so partition rebalancing
 * can be watched live. main() became start(instanceId) for exactly that reason.
 */
public class Day03KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(Day03KafkaConsumer.class);

    private static final String TOPIC = "order-events";

    public static void start(String instanceId) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, instanceId);

        // without this, every join revokes every partition from every consumer
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                CooperativeStickyAssignor.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            consumer.subscribe(List.of(TOPIC), new ConsumerRebalanceListener() {

                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    log.info("[{}] partitions revoked {}", instanceId, partitions);
                    consumer.commitSync();
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    log.info("[{}] partitions assigned {}", instanceId, partitions);

                    // re-read the last record of each partition to warm up in-memory state
                    for (TopicPartition partition : partitions) {
                        long current = consumer.position(partition);
                        long seekTo = Math.max(0, current - 1);
                        consumer.seek(partition, seekTo);
                        log.info("[{}] seeking {} to offset {}", instanceId, partition, seekTo);
                    }
                }
            });

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                records.forEach(record -> log.info("[{}] partition={} offset={} value={}",
                        instanceId, record.partition(), record.offset(), record.value()));

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        } catch (Exception e) {
            // a commit for a partition you no longer own throws here mid-rebalance
            log.error("[{}] consumer stopped", instanceId, e);
        }
    }
}
