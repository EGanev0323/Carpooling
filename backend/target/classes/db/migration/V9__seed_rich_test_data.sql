-- Additional test users (password = "password" for all)
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, is_driver, is_passenger, avg_rating, rating_count, trips_as_driver, trips_as_passenger)
VALUES
  ('maria_georgieva', 'maria@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Мария', 'Георгиева', '+359 878 111 222', true, true, 4.80, 15, 12, 3),
  ('petar_ivanov',    'petar@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Петър', 'Иванов',    '+359 888 333 444', true, false, 4.60, 8,  8,  0),
  ('elena_todorova',  'elena@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Елена', 'Тодорова',  '+359 899 555 666', false, true, 4.90, 5,  0,  5),
  ('georgi_dimitrov', 'georgi@example.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Георги','Димитров',  '+359 877 777 888', true, true, 4.30, 6,  5,  2)
ON CONFLICT (username) DO NOTHING;

-- Additional cars
INSERT INTO cars (owner_id, make, model, year, color, license_plate, total_seats)
SELECT id, 'BMW', '5 Series', 2021, 'Черен', 'СА 1234 АА', 4
FROM users WHERE username = 'maria_georgieva'
ON CONFLICT (license_plate) DO NOTHING;

INSERT INTO cars (owner_id, make, model, year, color, license_plate, total_seats)
SELECT id, 'Volkswagen', 'Golf', 2019, 'Бял', 'СА 5678 ВВ', 3
FROM users WHERE username = 'petar_ivanov'
ON CONFLICT (license_plate) DO NOTHING;

INSERT INTO cars (owner_id, make, model, year, color, license_plate, total_seats)
SELECT id, 'Toyota', 'Corolla', 2022, 'Сив', 'РВ 9999 СС', 3
FROM users WHERE username = 'georgi_dimitrov'
ON CONFLICT (license_plate) DO NOTHING;

-- Trips (future dates)
INSERT INTO trips (driver_id, car_id, origin_city, origin_address, destination_city, destination_address, departure_time, estimated_arrival, price_per_seat, total_seats, available_seats, status, description, smoking_allowed, pets_allowed)
SELECT
  u.id,
  c.id,
  'София', 'бул. Витоша 1', 'Пловдив', 'пл. Централен', NOW() + INTERVAL '1 day' + INTERVAL '8 hours', NOW() + INTERVAL '1 day' + INTERVAL '10 hours',
  12.00, 3, 3, 'SCHEDULED', 'Удобно пътуване, спиране при необходимост.', false, false
FROM users u JOIN cars c ON c.owner_id = u.id AND c.license_plate = 'СА 1234 АА'
WHERE u.username = 'maria_georgieva';

INSERT INTO trips (driver_id, car_id, origin_city, origin_address, destination_city, destination_address, departure_time, estimated_arrival, price_per_seat, total_seats, available_seats, status, description, smoking_allowed, pets_allowed)
SELECT
  u.id,
  c.id,
  'Пловдив', 'бул. Марица 5', 'Варна', 'ул. Черно море 10', NOW() + INTERVAL '2 days' + INTERVAL '7 hours', NOW() + INTERVAL '2 days' + INTERVAL '11 hours',
  25.00, 3, 2, 'SCHEDULED', 'Директно, без спирания. Климатик, зарядно.', false, false
FROM users u JOIN cars c ON c.owner_id = u.id AND c.license_plate = 'СА 5678 ВВ'
WHERE u.username = 'petar_ivanov';

INSERT INTO trips (driver_id, car_id, origin_city, origin_address, destination_city, destination_address, departure_time, estimated_arrival, price_per_seat, total_seats, available_seats, status, description, smoking_allowed, pets_allowed)
SELECT
  u.id,
  c.id,
  'Варна', 'пл. Независимост', 'София', 'НДК', NOW() + INTERVAL '3 days' + INTERVAL '6 hours', NOW() + INTERVAL '3 days' + INTERVAL '11 hours',
  22.00, 3, 3, 'SCHEDULED', 'Спираме в Шумен ако има желаещи.', false, true
FROM users u JOIN cars c ON c.owner_id = u.id AND c.license_plate = 'РВ 9999 СС'
WHERE u.username = 'georgi_dimitrov';

INSERT INTO trips (driver_id, car_id, origin_city, origin_address, destination_city, destination_address, departure_time, estimated_arrival, price_per_seat, total_seats, available_seats, status, description, smoking_allowed, pets_allowed)
SELECT
  u.id,
  c.id,
  'София', 'Студентски град', 'Бургас', 'Автогара Юг', NOW() + INTERVAL '4 days' + INTERVAL '9 hours', NOW() + INTERVAL '4 days' + INTERVAL '13 hours',
  18.00, 4, 4, 'SCHEDULED', 'Музика по желание, спиране на Тракия при нужда.', false, false
FROM users u JOIN cars c ON c.owner_id = u.id AND c.license_plate = 'СА 1234 АА'
WHERE u.username = 'maria_georgieva';

INSERT INTO trips (driver_id, car_id, origin_city, origin_address, destination_city, destination_address, departure_time, estimated_arrival, price_per_seat, total_seats, available_seats, status, description, smoking_allowed, pets_allowed)
SELECT
  u.id,
  c.id,
  'София', 'Летище Терминал 2', 'Велико Търново', 'пл. Майка България', NOW() + INTERVAL '5 days' + INTERVAL '14 hours', NOW() + INTERVAL '5 days' + INTERVAL '17 hours',
  15.00, 3, 2, 'SCHEDULED', NULL, false, false
FROM users u JOIN cars c ON c.owner_id = u.id AND c.license_plate = 'СА 5678 ВВ'
WHERE u.username = 'petar_ivanov';

-- A completed trip for ratings testing
INSERT INTO trips (driver_id, car_id, origin_city, destination_city, departure_time, estimated_arrival, price_per_seat, total_seats, available_seats, status, smoking_allowed, pets_allowed)
SELECT
  u.id,
  c.id,
  'София', 'Пловдив', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '2 hours',
  10.00, 3, 1, 'COMPLETED', false, false
FROM users u JOIN cars c ON c.owner_id = u.id AND c.license_plate = 'РВ 9999 СС'
WHERE u.username = 'georgi_dimitrov';
