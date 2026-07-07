ALTER TABLE users ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO users (username, email, password_hash, first_name, last_name, is_driver, is_passenger, is_admin, avg_rating, rating_count, trips_as_driver, trips_as_passenger)
VALUES ('admin', 'admin@carpooling.bg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Админ', 'Системен', false, false, true, 0.00, 0, 0, 0)
ON CONFLICT (username) DO NOTHING;
