CREATE TABLE bookings (
    id           BIGSERIAL   PRIMARY KEY,
    trip_id      BIGINT      NOT NULL REFERENCES trips(id),
    passenger_id BIGINT      NOT NULL REFERENCES users(id),
    seats_booked SMALLINT    NOT NULL DEFAULT 1,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message      TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_bookings_status        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_bookings_seats         CHECK (seats_booked >= 1),
    CONSTRAINT uq_bookings_trip_passenger UNIQUE (trip_id, passenger_id)
);
