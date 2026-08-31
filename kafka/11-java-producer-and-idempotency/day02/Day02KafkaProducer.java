package com.suel.kafkaplayground.day02;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

/**
 * Day 02 producer - the console producer, written in Java.
 * Note there is no group.id: consumer groups are a consumer-side concept.
 */
public class Day02KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(Day02KafkaProducer.class);

    private static final String TOPIC = "order-events";

    public static void main(String[] args) throws InterruptedException {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // producers only serialize; deserializing is the consumer's problem
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            for (int i = 0; i < 100; i++) {
                String key = Integer.toString(i);
                String value = "order-" + i;

                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);

                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("failed to publish key={}", key, exception);
                        return;
                    }
                    log.info("produced correlationId={} partition={} offset={}",
                            key, metadata.partition(), metadata.offset());
                });

                // only so the flow is visible while demoing
                Thread.sleep(Duration.ofMillis(100));
            }

            // send() is async and batched - without this, buffered records die with the producer
            producer.flush();
        }
    }
}
