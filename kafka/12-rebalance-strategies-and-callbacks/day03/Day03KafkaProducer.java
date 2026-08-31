package com.suel.kafkaplayground.day03;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

/**
 * Day 03 producer - 10,000 records rather than 100, so there is steady traffic
 * to watch while consumers are started and stopped.
 */
public class Day03KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(Day03KafkaProducer.class);

    private static final String TOPIC = "order-events";
    private static final int RECORD_COUNT = 10_000;

    public static void main(String[] args) throws InterruptedException {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            for (int i = 0; i < RECORD_COUNT; i++) {
                String key = Integer.toString(i);

                producer.send(new ProducerRecord<>(TOPIC, key, "order-" + i), (metadata, exception) -> {
                    if (exception != null) {
                        log.error("failed to publish key={}", key, exception);
                        return;
                    }
                    log.info("produced correlationId={} partition={} offset={}",
                            key, metadata.partition(), metadata.offset());
                });

                Thread.sleep(Duration.ofMillis(50));
            }

            producer.flush();
        }
    }
}
