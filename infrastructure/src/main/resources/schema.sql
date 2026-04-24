DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS plans;
DROP TABLE IF EXISTS owners;

CREATE TABLE owners (
    id                UUID PRIMARY KEY,
    nit               VARCHAR(20)  NOT NULL UNIQUE,
    name              VARCHAR(100) NOT NULL,
    email             VARCHAR(150) NOT NULL UNIQUE,
    phone             VARCHAR(20),
    business_name     VARCHAR(200),
    grace_period_days INT          NOT NULL DEFAULT 0,
    password_hash     VARCHAR(100) NOT NULL
);

CREATE TABLE clients (
    id       UUID PRIMARY KEY,
    owner_id UUID         NOT NULL REFERENCES owners(id),
    cedula   VARCHAR(20)  NOT NULL,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL,
    phone    VARCHAR(20),
    status   VARCHAR(20)  NOT NULL
);

CREATE TABLE plans (
    id             UUID           PRIMARY KEY,
    owner_id       UUID           NOT NULL REFERENCES owners(id),
    name           VARCHAR(100)   NOT NULL,
    description    TEXT,
    price_amount   NUMERIC(12, 2) NOT NULL,
    price_currency VARCHAR(10)    NOT NULL,
    duration_days  INT            NOT NULL,
    status         VARCHAR(20)    NOT NULL
);

CREATE TABLE subscriptions (
    id             UUID           PRIMARY KEY,
    client_id      UUID           NOT NULL REFERENCES clients(id),
    plan_id        UUID           NOT NULL REFERENCES plans(id),
    start_date     DATE           NOT NULL,
    end_date       DATE           NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    price_amount   NUMERIC(12, 2) NOT NULL,
    price_currency VARCHAR(10)    NOT NULL
);

CREATE TABLE payments (
    id              UUID           PRIMARY KEY,
    subscription_id UUID           NOT NULL REFERENCES subscriptions(id),
    amount          NUMERIC(12, 2) NOT NULL,
    currency        VARCHAR(10)    NOT NULL,
    paid_at         TIMESTAMP      NOT NULL,
    registered_by   UUID           NOT NULL
);
