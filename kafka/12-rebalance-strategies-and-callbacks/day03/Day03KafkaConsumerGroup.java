package com.suel.kafkaplayground.day03;

/**
 * Three separate main methods so the IDE can launch three JVMs - three real
 * members of the same consumer group, each with its own group.instance.id.
 */
public class Day03KafkaConsumerGroup {

    public static class Consumer1 {
        public static void main(String[] args) {
            Day03KafkaConsumer.start("1");
        }
    }

    public static class Consumer2 {
        public static void main(String[] args) {
            Day03KafkaConsumer.start("2");
        }
    }

    public static class Consumer3 {
        public static void main(String[] args) {
            Day03KafkaConsumer.start("3");
        }
    }
}
