-- Seed test users
-- password for both accounts: "password"
-- bcrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (username, email, password_hash, first_name, last_name, phone, is_driver, is_passenger)
VALUES
    ('ivan_driver',
     'ivan.driver@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Ivan',
     'Petrov',
     '+359881234567',
     TRUE,
     FALSE),

    ('maria_passenger',
     'maria.passenger@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Maria',
     'Georgieva',
     '+359889876543',
     FALSE,
     TRUE);

-- Seed test car (owned by the driver)
INSERT INTO cars (owner_id, make, model, year, color, license_plate, total_seats)
VALUES (
    (SELECT id FROM users WHERE username = 'ivan_driver'),
    'Volkswagen',
    'Golf',
    2019,
    'Silver',
    'CB1234AB',
    4
);

-- Seed test trip (driven by ivan_driver, using his car)
INSERT INTO trips (driver_id, car_id, origin_city, origin_address, destination_city, destination_address,
                   departure_time, estimated_arrival, price_per_seat, total_seats, available_seats, status,
                   description)
VALUES (
    (SELECT id FROM users WHERE username = 'ivan_driver'),
    (SELECT id FROM cars WHERE license_plate = 'CB1234AB'),
    'Sofia',
    'bul. Vitosha 10',
    'Plovdiv',
    'ul. Ivan Vazov 5',
    NOW() + INTERVAL '1 day',
    NOW() + INTERVAL '1 day 2 hours',
    15.00,
    4,
    3,
    'SCHEDULED',
    'Direct trip, no stops. Music allowed.'
);

-- Seed test booking (maria_passenger books 1 seat on the trip)
INSERT INTO bookings (trip_id, passenger_id, seats_booked, status, message)
VALUES (
    (SELECT id FROM trips WHERE origin_city = 'Sofia' AND destination_city = 'Plovdiv' ORDER BY created_at DESC LIMIT 1),
    (SELECT id FROM users WHERE username = 'maria_passenger'),
    1,
    'PENDING',
    'Zdraveite, molya rezervirajte mi myasto.'
);
