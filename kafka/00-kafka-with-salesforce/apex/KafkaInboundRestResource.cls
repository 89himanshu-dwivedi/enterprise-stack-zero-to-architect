/**
 * Where the bridge lands Kafka records that need custom mapping.
 * POST /services/apexrest/kafka/payments
 *
 * Upserts on an External Id so a Kafka redelivery cannot create a duplicate -
 * the at-least-once problem from part 11, solved by the database.
 */
@RestResource(urlMapping='/kafka/payments/*')
global with sharing class KafkaInboundRestResource {

    private static final Integer MAX_BATCH = 200;

    @HttpPost
    global static Response post() {

        RestRequest request = RestContext.request;

        List<PaymentMessage> messages =
            (List<PaymentMessage>) JSON.deserialize(request.requestBody.toString(), List<PaymentMessage>.class);

        if (messages.size() > MAX_BATCH) {
            RestContext.response.statusCode = 413;
            return new Response(false, 'batch larger than ' + MAX_BATCH);
        }

        List<Payment__c> payments = new List<Payment__c>();
        for (PaymentMessage message : messages) {
            payments.add(new Payment__c(
                Kafka_Event_Id__c = message.eventId,   // External Id, unique
                Order__r          = new Order__c(External_Order_Id__c = message.orderId),
                Amount__c         = message.amount,
                Status__c         = message.status,
                Paid_At__c        = message.paidAt
            ));
        }

        Database.UpsertResult[] results =
            Database.upsert(payments, Payment__c.Kafka_Event_Id__c, false);

        List<String> failures = new List<String>();
        for (Integer i = 0; i < results.size(); i++) {
            if (!results[i].isSuccess()) {
                failures.add(messages[i].eventId + ': ' + results[i].getErrors()[0].getMessage());
            }
        }

        if (!failures.isEmpty()) {
            // a 4xx/5xx tells the bridge not to commit the offset, so Kafka replays the batch
            RestContext.response.statusCode = 207;
            return new Response(false, String.join(failures, ' | '));
        }

        return new Response(true, 'accepted ' + payments.size());
    }

    global class PaymentMessage {
        global String eventId;
        global String orderId;
        global Decimal amount;
        global String status;
        global Datetime paidAt;
    }

    global class Response {
        global Boolean success;
        global String message;
        Response(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
