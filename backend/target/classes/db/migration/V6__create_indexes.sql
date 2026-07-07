-- trips search
CREATE INDEX idx_trips_origin_city      ON trips(origin_city);
CREATE INDEX idx_trips_destination_city ON trips(destination_city);
CREATE INDEX idx_trips_departure_time   ON trips(departure_time);
CREATE INDEX idx_trips_status           ON trips(status);
CREATE INDEX idx_trips_driver_id        ON trips(driver_id);

-- bookings
CREATE INDEX idx_bookings_trip_id       ON bookings(trip_id);
CREATE INDEX idx_bookings_passenger_id  ON bookings(passenger_id);
CREATE INDEX idx_bookings_status        ON bookings(status);

-- ratings
CREATE INDEX idx_ratings_rated_id       ON ratings(rated_id);
CREATE INDEX idx_ratings_trip_id        ON ratings(trip_id);

-- cars
CREATE INDEX idx_cars_owner_id          ON cars(owner_id);
