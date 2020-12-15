CREATE TABLE IF NOT EXISTS EVENT_ENTITY (
    ID SERIAL PRIMARY KEY,
    AGGREGATE_ID VARCHAR(255),
    TYPE VARCHAR(255),
    SEQUENCE_NUMBER BIGINT,
    CREATED_AT TIMESTAMP,
    DOMAIN_EVENT VARCHAR(4096)
);
CREATE INDEX IF NOT EXISTS AGGREGATE_ID_INDEX ON EVENT_ENTITY (AGGREGATE_ID);
CREATE INDEX IF NOT EXISTS CREATED_AT_INDEX ON EVENT_ENTITY (CREATED_AT);
CREATE INDEX IF NOT EXISTS SEQUENCE_NUMBER_INDEX ON EVENT_ENTITY (SEQUENCE_NUMBER);
