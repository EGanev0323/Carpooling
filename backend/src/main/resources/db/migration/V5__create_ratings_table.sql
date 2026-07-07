CREATE TABLE ratings (
    id         BIGSERIAL   PRIMARY KEY,
    trip_id    BIGINT      NOT NULL REFERENCES trips(id),
    rater_id   BIGINT      NOT NULL REFERENCES users(id),
    rated_id   BIGINT      NOT NULL REFERENCES users(id),
    score      SMALLINT    NOT NULL,
    comment    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ratings_score             CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT uq_ratings_trip_rater_rated   UNIQUE (trip_id, rater_id, rated_id)
);
