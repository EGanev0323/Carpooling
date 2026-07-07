CREATE TABLE users (
    id                 BIGSERIAL    PRIMARY KEY,
    username           VARCHAR(50)  NOT NULL UNIQUE,
    email              VARCHAR(100) NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    first_name         VARCHAR(50)  NOT NULL,
    last_name          VARCHAR(50)  NOT NULL,
    phone              VARCHAR(20),
    avatar_url         VARCHAR(500),
    is_driver          BOOLEAN      NOT NULL DEFAULT FALSE,
    is_passenger       BOOLEAN      NOT NULL DEFAULT TRUE,
    avg_rating         NUMERIC(3,2) NOT NULL DEFAULT 0.00,
    rating_count       INT          NOT NULL DEFAULT 0,
    trips_as_driver    INT          NOT NULL DEFAULT 0,
    trips_as_passenger INT          NOT NULL DEFAULT 0,
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
