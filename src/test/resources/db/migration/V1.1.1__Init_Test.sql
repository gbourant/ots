CREATE SEQUENCE BASE_ENTITY_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE BASE_ENTITY
(
    ID         int8        NOT NULL,
    NAME       varchar(42) NOT NULL,
    INSTANT    timestamp   NOT NULL,
    CREATED_AT timestamp   NOT NULL,
    UPDATED_AT timestamp,
    VERSION    int8        NOT NULL
);