import { LightningElement, track } from 'lwc';
import { subscribe, unsubscribe, onError } from 'lightning/empApi';

const CHANNEL = '/event/Order_Event__e';
const MAX_ROWS = 20;

export default class KafkaEventMonitor extends LightningElement {

    @track events = [];
    subscription = null;
    connected = false;

    connectedCallback() {
        // -1 replays only new events; -2 replays everything still retained
        subscribe(CHANNEL, -1, (message) => this.handleEvent(message)).then((response) => {
            this.subscription = response;
            this.connected = true;
        });

        onError((error) => {
            this.connected = false;
            // eslint-disable-next-line no-console
            console.error('empApi error', JSON.stringify(error));
        });
    }

    disconnectedCallback() {
        if (this.subscription) {
            unsubscribe(this.subscription);
            this.subscription = null;
        }
    }

    handleEvent(message) {
        const payload = message.data.payload;

        this.events = [
            {
                eventId: payload.Event_Id__c,
                orderId: payload.Order_Id__c,
                status: payload.Status__c,
                amount: payload.Amount__c,
                receivedAt: new Date().toLocaleTimeString()
            },
            ...this.events
        ].slice(0, MAX_ROWS);
    }

    get hasEvents() {
        return this.events.length > 0;
    }

    get connectionLabel() {
        return this.connected ? 'Streaming' : 'Disconnected';
    }

    get connectionClass() {
        return this.connected ? 'slds-theme_success' : 'slds-theme_error';
    }
}
