-- Runs once, on first startup of the orders-db container.

CREATE TABLE IF NOT EXISTS orders (
    id            BIGSERIAL PRIMARY KEY,
    order_number  VARCHAR(64)   NOT NULL UNIQUE,
    customer_id   BIGINT        NOT NULL,
    product       VARCHAR(255)  NOT NULL,
    quantity      INT           NOT NULL,
    amount        NUMERIC(12,2) NOT NULL,
    status        VARCHAR(32)   NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders (customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status      ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at  ON orders (created_at);

-- ---------------------------------------------------------------------------
-- CDC plumbing. Debezium needs its own login with the REPLICATION attribute,
-- otherwise it cannot open a replication slot on the WAL.
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'debezium') THEN
        CREATE ROLE debezium WITH LOGIN REPLICATION PASSWORD 'debezium';
    END IF;
END
$$;

GRANT CONNECT ON DATABASE orders TO debezium;
GRANT USAGE ON SCHEMA public TO debezium;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO debezium;

-- Postgres logical replication is a publication / subscription model:
-- the source publishes, and whoever wants the stream subscribes by name.
-- publication.name in the connector config must match this exactly.
DROP PUBLICATION IF EXISTS dbz_publication;
CREATE PUBLICATION dbz_publication FOR TABLE orders;

-- REPLICA IDENTITY FULL puts the old row values in the WAL too, so an UPDATE
-- event carries a "before" image and a DELETE carries more than just the key.
ALTER TABLE orders REPLICA IDENTITY FULL;
