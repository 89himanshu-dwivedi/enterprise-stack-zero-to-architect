/**
 * Publishes one Platform Event per order. No callout here on purpose - the
 * bridge subscribes over the Pub/Sub API and produces to Kafka itself.
 */
public with sharing class OrderEventPublisher {

    public static void publish(List<Order__c> orders) {

        List<Order_Event__e> events = new List<Order_Event__e>();

        for (Order__c order : orders) {
            events.add(new Order_Event__e(
                // the consumer dedupes on this - see part 11
                Event_Id__c  = newEventId(),
                Order_Id__c  = order.Id,
                Account_Id__c = order.Account__c,
                Status__c    = order.Status__c,
                Amount__c    = order.Amount__c,
                Occurred_At__c = System.now()
            ));
        }

        List<Database.SaveResult> results = EventBus.publish(events);

        for (Integer i = 0; i < results.size(); i++) {
            if (!results[i].isSuccess()) {
                for (Database.Error error : results[i].getErrors()) {
                    System.debug(LoggingLevel.ERROR,
                        'Order_Event__e publish failed for ' + orders[i].Id + ': ' + error.getMessage());
                }
            }
        }
    }

    /** Apex has no UUID type, so build one from a random AES key. */
    private static String newEventId() {
        String hex = EncodingUtil.convertToHex(Crypto.generateAesKey(128));
        return hex.substring(0, 8) + '-' + hex.substring(8, 12) + '-' + hex.substring(12, 16)
             + '-' + hex.substring(16, 20) + '-' + hex.substring(20, 32);
    }
}
