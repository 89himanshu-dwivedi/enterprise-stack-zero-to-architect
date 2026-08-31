/**
 * Minimal Salesforce -> Kafka bridge.
 *
 * Subscribes to a Platform Event over the Salesforce Pub/Sub API (gRPC) and
 * produces each event to a Kafka topic, keyed by Order Id so all events for one
 * order keep their order - the partition key rule from part 05.
 *
 *   npm install kafkajs @salesforce/core jsforce avro-js @grpc/grpc-js
 *   node index.js
 */

const { Kafka } = require('kafkajs');
const fs = require('fs');

const TOPIC = process.env.KAFKA_TOPIC || 'sfdc.order-events';
const CHANNEL = process.env.SF_CHANNEL || '/event/Order_Event__e';
const REPLAY_FILE = process.env.REPLAY_FILE || './replay-id';

const kafka = new Kafka({
    clientId: 'sfdc-bridge',
    brokers: (process.env.KAFKA_BROKERS || 'localhost:8081,localhost:8082,localhost:8083').split(',')
});

const producer = kafka.producer({ idempotent: true });

async function main() {
    await producer.connect();

    const client = await connectPubSub();          // gRPC channel to api.pubsub.salesforce.com:7443
    const replayId = loadReplayId();

    const stream = client.Subscribe();

    stream.on('data', async (response) => {
        for (const event of response.events) {
            const payload = decode(event);         // Avro decode using the schema id on the event

            await producer.send({
                topic: TOPIC,
                messages: [{
                    key: payload.Order_Id__c,      // same order -> same partition -> ordered
                    value: JSON.stringify(payload),
                    headers: { 'event-id': payload.Event_Id__c }   // the consumer dedupes on this
                }]
            });
        }

        // only checkpoint after the produce succeeded - process first, then acknowledge
        saveReplayId(response.latestReplayId);

        // Pub/Sub API is pull-based too: ask for the next batch
        stream.write({ topicName: CHANNEL, numRequested: 100 });
    });

    stream.on('error', (error) => {
        console.error('pub/sub stream failed', error);
        process.exit(1);
    });

    stream.write({
        topicName: CHANNEL,
        numRequested: 100,
        replayPreset: replayId ? 'CUSTOM' : 'LATEST',
        replayId
    });
}

function loadReplayId() {
    return fs.existsSync(REPLAY_FILE) ? fs.readFileSync(REPLAY_FILE) : null;
}

function saveReplayId(replayId) {
    fs.writeFileSync(REPLAY_FILE, replayId);
}

// connectPubSub() and decode() are left to the official pub-sub-api samples -
// they handle the OAuth headers and the Avro schema cache.

main().catch((error) => {
    console.error(error);
    process.exit(1);
});
