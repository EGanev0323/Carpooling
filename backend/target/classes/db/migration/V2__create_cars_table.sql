CREATE TABLE cars (
    id            BIGSERIAL   PRIMARY KEY,
    owner_id      BIGINT      NOT NULL REFERENCES users(id),
    make          VARCHAR(50) NOT NULL,
    model         VARCHAR(50) NOT NULL,
    year          SMALLINT    NOT NULL,
    color         VARCHAR(30) NOT NULL,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    total_seats   SMALLINT    NOT NULL,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
