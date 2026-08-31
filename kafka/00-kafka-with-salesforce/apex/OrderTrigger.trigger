trigger OrderTrigger on Order__c (after insert, after update) {
    if (Trigger.isAfter) {
        OrderEventPublisher.publish(Trigger.new);
    }
}
