-- Runs once, on first startup of the analytics-db container.
-- No replication settings here: nothing streams *out* of this database.

CREATE TABLE IF NOT EXISTS order_events (
    id            BIGINT        PRIMARY KEY,
    order_number  VARCHAR(64),
    customer_id   BIGINT,
    product       VARCHAR(255),
    quantity      INT,
    amount        NUMERIC(12,2),
    status        VARCHAR(32),
    ingested_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_events_customer_id ON order_events (customer_id);
CREATE INDEX IF NOT EXISTS idx_order_events_status      ON order_events (status);
