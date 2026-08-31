/**
 * Direct publish to a Confluent REST Proxy - the only way Apex can reach Kafka
 * without an event bus in between.
 *
 * Queueable because a trigger cannot call out after DML, and ONE request for the
 * whole batch because a transaction only gets 100 callouts.
 *
 * Requires a Named Credential called Kafka_REST_Proxy.
 */
public with sharing class KafkaRestProxyPublisher implements Queueable, Database.AllowsCallouts {

    private static final Integer MAX_RECORDS_PER_CALL = 500;

    private final String topic;
    private final List<KafkaRecord> records;

    public KafkaRestProxyPublisher(String topic, List<KafkaRecord> records) {
        this.topic = topic;
        this.records = records;
    }

    public void execute(QueueableContext context) {

        // chunk so a huge trigger context cannot blow the heap or the payload size
        for (Integer start = 0; start < records.size(); start += MAX_RECORDS_PER_CALL) {
            Integer stop = Math.min(start + MAX_RECORDS_PER_CALL, records.size());
            send(new List<KafkaRecord>(records).subList(start, stop));
        }
    }

    private void send(List<KafkaRecord> chunk) {

        HttpRequest request = new HttpRequest();
        request.setEndpoint('callout:Kafka_REST_Proxy/topics/' + topic);
        request.setMethod('POST');
        request.setHeader('Content-Type', 'application/vnd.kafka.json.v2+json');
        request.setHeader('Accept', 'application/vnd.kafka.v2+json');
        request.setTimeout(30000);
        request.setBody(JSON.serialize(new Payload(chunk)));

        HttpResponse response = new Http().send(request);

        if (response.getStatusCode() >= 300) {
            // let the platform retry the Queueable rather than losing the batch
            throw new CalloutException('REST Proxy returned ' + response.getStatusCode()
                + ': ' + response.getBody());
        }
    }

    /** key becomes the Kafka partition key - same key, same partition, ordering preserved. */
    public class KafkaRecord {
        public String key;
        public Object value;

        public KafkaRecord(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    private class Payload {
        public List<KafkaRecord> records;
        Payload(List<KafkaRecord> records) {
            this.records = records;
        }
    }
}
