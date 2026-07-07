-- V1__init.sql
-- Full initial schema for Carpooling TU Sofia

-- Extensions
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- ROLES
-- ============================================================
CREATE TABLE roles (
    id   SMALLSERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    public_id         UUID        NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    email             CITEXT      NOT NULL,
    email_verified_at TIMESTAMPTZ,
    password_hash     VARCHAR(255) NOT NULL,
    first_name        VARCHAR(64) NOT NULL,
    last_name         VARCHAR(64) NOT NULL,
    phone             VARCHAR(20) NOT NULL,
    avatar_url        VARCHAR(512),
    bio               VARCHAR(500),
    status            VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE', 'BLOCKED')),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ
);

-- Partial unique index: email must be unique among non-deleted users
CREATE UNIQUE INDEX uq_users_email_active ON users(email) WHERE deleted_at IS NULL;

-- ============================================================
-- USER_ROLES (join table)
-- ============================================================
CREATE TABLE user_roles (
    user_id BIGINT   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id SMALLINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- CITIES
-- ============================================================
CREATE TABLE cities (
    id        SERIAL PRIMARY KEY,
    slug      VARCHAR(64)  NOT NULL UNIQUE,
    name_bg   VARCHAR(128) NOT NULL,
    name_en   VARCHAR(128) NOT NULL,
    region_bg VARCHAR(128),
    latitude  NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- CARS (vehicles)
-- ============================================================
CREATE TABLE cars (
    id           BIGSERIAL PRIMARY KEY,
    owner_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    make         VARCHAR(64) NOT NULL,
    model        VARCHAR(64) NOT NULL,
    year         SMALLINT    NOT NULL,
    color        VARCHAR(32),
    license_plate VARCHAR(20) NOT NULL,
    seats        SMALLINT    NOT NULL CHECK (seats BETWEEN 1 AND 8),
    amenities    JSONB        NOT NULL DEFAULT '{}',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- RIDES
-- ============================================================
CREATE TABLE rides (
    id                  BIGSERIAL PRIMARY KEY,
    public_id           UUID        NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    driver_id           BIGINT      NOT NULL REFERENCES users(id),
    car_id              BIGINT      NOT NULL REFERENCES cars(id),
    origin_city_id      INTEGER     NOT NULL REFERENCES cities(id),
    destination_city_id INTEGER     NOT NULL REFERENCES cities(id),
    departure_at        TIMESTAMPTZ NOT NULL,
    arrival_at_estimate TIMESTAMPTZ,
    total_seats         SMALLINT    NOT NULL CHECK (total_seats BETWEEN 1 AND 8),
    price_per_seat      NUMERIC(8, 2) NOT NULL CHECK (price_per_seat >= 0),
    route_polyline      TEXT,
    description         VARCHAR(500),
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- RIDE_STOPS
-- ============================================================
CREATE TABLE ride_stops (
    id          BIGSERIAL PRIMARY KEY,
    ride_id     BIGINT      NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    city_id     INTEGER     NOT NULL REFERENCES cities(id),
    stop_order  SMALLINT    NOT NULL,
    arrive_at   TIMESTAMPTZ,
    UNIQUE (ride_id, stop_order)
);

-- ============================================================
-- BOOKINGS
-- ============================================================
CREATE TABLE bookings (
    id           BIGSERIAL PRIMARY KEY,
    public_id    UUID        NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    ride_id      BIGINT      NOT NULL REFERENCES rides(id),
    passenger_id BIGINT      NOT NULL REFERENCES users(id),
    seats        SMALLINT    NOT NULL DEFAULT 1 CHECK (seats >= 1),
    status       VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED', 'NO_SHOW')),
    message      VARCHAR(500),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Prevent duplicate active bookings for same passenger on same ride
CREATE UNIQUE INDEX uq_bookings_active
    ON bookings(ride_id, passenger_id)
    WHERE status IN ('PENDING', 'CONFIRMED');

-- ============================================================
-- CHAT_CHANNELS
-- ============================================================
CREATE TABLE chat_channels (
    id           BIGSERIAL PRIMARY KEY,
    ride_id      BIGINT NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    driver_id    BIGINT NOT NULL REFERENCES users(id),
    passenger_id BIGINT NOT NULL REFERENCES users(id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chat_channels UNIQUE (ride_id, driver_id, passenger_id)
);

-- ============================================================
-- CHAT_MESSAGES
-- ============================================================
CREATE TABLE chat_messages (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT       NOT NULL REFERENCES chat_channels(id) ON DELETE CASCADE,
    sender_id  BIGINT       NOT NULL REFERENCES users(id),
    content    VARCHAR(2000) NOT NULL,
    read_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- RATINGS
-- ============================================================
CREATE TABLE ratings (
    id         BIGSERIAL PRIMARY KEY,
    booking_id BIGINT      NOT NULL REFERENCES bookings(id),
    rater_id   BIGINT      NOT NULL REFERENCES users(id),
    rated_id   BIGINT      NOT NULL REFERENCES users(id),
    score      SMALLINT    NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment    VARCHAR(500),
    direction  VARCHAR(32) NOT NULL
                   CHECK (direction IN ('DRIVER_TO_PASSENGER', 'PASSENGER_TO_DRIVER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ratings UNIQUE (booking_id, direction)
);

-- ============================================================
-- REFRESH_TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      CHAR(64)    NOT NULL UNIQUE,
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked_at      TIMESTAMPTZ,
    replaced_by_id  BIGINT      REFERENCES refresh_tokens(id),
    ip              VARCHAR(45),
    user_agent      VARCHAR(255)
);

-- ============================================================
-- AUDIT_EVENTS
-- ============================================================
CREATE TABLE audit_events (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    event_type  VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64),
    entity_id   BIGINT,
    metadata    JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    prev_hash   CHAR(64),
    hash        CHAR(64)
);

-- ============================================================
-- EMAIL_VERIFICATION_TOKENS
-- ============================================================
CREATE TABLE email_verification_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64)  NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- VIEW: v_ride_available_seats
-- ============================================================
CREATE VIEW v_ride_available_seats AS
SELECT
    r.id                                                      AS ride_id,
    r.public_id,
    r.total_seats,
    COALESCE(SUM(b.seats) FILTER (WHERE b.status IN ('PENDING', 'CONFIRMED')), 0) AS booked_seats,
    r.total_seats - COALESCE(SUM(b.seats) FILTER (WHERE b.status IN ('PENDING', 'CONFIRMED')), 0) AS available_seats
FROM rides r
LEFT JOIN bookings b ON b.ride_id = r.id
GROUP BY r.id, r.public_id, r.total_seats;

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_rides_route_departure
    ON rides(origin_city_id, destination_city_id, departure_at);

CREATE INDEX idx_rides_driver_status
    ON rides(driver_id, status);

CREATE INDEX idx_bookings_ride_status
    ON bookings(ride_id, status);

CREATE INDEX idx_bookings_passenger_status
    ON bookings(passenger_id, status);

CREATE INDEX idx_chat_messages_channel_created
    ON chat_messages(channel_id, created_at DESC);

CREATE INDEX idx_audit_events_user_created
    ON audit_events(user_id, created_at DESC);

-- refresh_tokens indexes
CREATE INDEX idx_refresh_tokens_token_hash
    ON refresh_tokens(token_hash);

CREATE INDEX idx_refresh_tokens_user_revoked
    ON refresh_tokens(user_id, revoked_at);
