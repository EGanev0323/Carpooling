# Модел на данните

## 1. ER преглед (текстова диаграма)

```
users ──┬─< user_roles >── roles
        ├─< cars
        ├─< rides (as driver)
        ├─< bookings (as passenger)
        ├─< ratings_given / ratings_received
        ├─< chat_messages (as sender)
        ├─< refresh_tokens
        └─< audit_events

rides ──┬─< bookings
        ├─── origin_city, destination_city (cities)
        ├─< ride_stops (междинни спирки)
        └─< chat_channels

cities ── (справочна таблица)

chat_channels ──< chat_messages
                └── ride, driver_user, passenger_user

audit_events (append-only, hash-chained)
```

## 2. Таблици

### `users`

| Колона | Тип | Констрейнти |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `public_id` | UUID | UNIQUE, NOT NULL, DEFAULT `gen_random_uuid()` |
| `email` | CITEXT | UNIQUE (partial index WHERE deleted_at IS NULL) |
| `email_verified_at` | TIMESTAMPTZ | NULL |
| `password_hash` | VARCHAR(255) | NOT NULL (Argon2id encoded string) |
| `first_name` | VARCHAR(64) | NOT NULL |
| `last_name` | VARCHAR(64) | NOT NULL |
| `phone` | VARCHAR(20) | NOT NULL, format `^(\+359|0)[0-9]{8,9}$` |
| `avatar_url` | VARCHAR(512) | NULL |
| `bio` | VARCHAR(500) | NULL |
| `status` | VARCHAR(16) | `ACTIVE` / `BLOCKED`, DEFAULT `ACTIVE` |
| `created_at` | TIMESTAMPTZ | DEFAULT NOW() |
| `updated_at` | TIMESTAMPTZ | DEFAULT NOW() |
| `deleted_at` | TIMESTAMPTZ | NULL |

Индекси: `email` (unique partial), `public_id` (unique), `status`.

### `roles` и `user_roles`

```sql
CREATE TABLE roles (
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(32) UNIQUE NOT NULL  -- 'USER', 'DRIVER', 'ADMIN'
);

CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id SMALLINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### `cities`

Справочна таблица, заредена от seed данни.

| Колона | Тип | |
|---|---|---|
| `id` | SERIAL | PK |
| `slug` | VARCHAR(64) | UNIQUE (`sofia`, `plovdiv`, `varna`) |
| `name_bg` | VARCHAR(128) | NOT NULL |
| `name_en` | VARCHAR(128) | NOT NULL |
| `region_bg` | VARCHAR(128) | NULL |
| `latitude` | DECIMAL(9,6) | NOT NULL |
| `longitude` | DECIMAL(9,6) | NOT NULL |
| `is_active` | BOOLEAN | DEFAULT TRUE |

### `cars`

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `owner_id` | BIGINT | FK → users, NOT NULL |
| `make` | VARCHAR(64) | NOT NULL |
| `model` | VARCHAR(64) | NOT NULL |
| `year` | SMALLINT | CHECK (year BETWEEN 1990 AND 2100) |
| `plate` | VARCHAR(16) | NOT NULL (не се показва публично) |
| `color` | VARCHAR(32) | |
| `seats` | SMALLINT | CHECK (seats BETWEEN 2 AND 8) |
| `photo_url` | VARCHAR(512) | NOT NULL |
| `amenities` | JSONB | `{ "ac": true, "pets": false, "smoke": false, "luggage": "medium" }` |
| `created_at` | TIMESTAMPTZ | DEFAULT NOW() |
| `deleted_at` | TIMESTAMPTZ | NULL |

### `rides`

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `public_id` | UUID | UNIQUE, NOT NULL |
| `driver_id` | BIGINT | FK → users |
| `car_id` | BIGINT | FK → cars |
| `origin_city_id` | INT | FK → cities |
| `destination_city_id` | INT | FK → cities |
| `origin_detail` | VARCHAR(255) | точна точка на тръгване, свободен текст |
| `destination_detail` | VARCHAR(255) | |
| `departure_at` | TIMESTAMPTZ | NOT NULL, CHECK > NOW() at create |
| `estimated_arrival_at` | TIMESTAMPTZ | NULL |
| `seats_offered` | SMALLINT | CHECK ≥ 1 |
| `price_per_seat` | DECIMAL(6,2) | CHECK ≥ 0 |
| `currency` | CHAR(3) | DEFAULT `BGN` |
| `distance_km` | DECIMAL(6,1) | NULL, изчислен |
| `comment` | VARCHAR(500) | NULL |
| `status` | VARCHAR(16) | `ACTIVE` / `COMPLETED` / `CANCELLED` |
| `route_polyline` | TEXT | encoded polyline за карта |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

Индекси: `(origin_city_id, destination_city_id, departure_at)`,
`(driver_id, status)`, `public_id`.

### `ride_stops` (междинни спирки, optional)

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `ride_id` | BIGINT | FK → rides |
| `city_id` | INT | FK → cities |
| `order_index` | SMALLINT | 1, 2, 3 ... |
| `expected_at` | TIMESTAMPTZ | NULL |

### `bookings`

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `public_id` | UUID | UNIQUE, NOT NULL |
| `ride_id` | BIGINT | FK → rides |
| `passenger_id` | BIGINT | FK → users |
| `seats` | SMALLINT | CHECK ≥ 1 |
| `total_price` | DECIMAL(7,2) | seats × ride.price_per_seat, snapshotнато |
| `status` | VARCHAR(16) | `PENDING` / `CONFIRMED` / `REJECTED` / `CANCELLED` / `NO_SHOW` |
| `notes` | VARCHAR(500) | NULL |
| `created_at`, `updated_at` | TIMESTAMPTZ | |
| `confirmed_at`, `cancelled_at` | TIMESTAMPTZ | NULL |

Индекси: `(ride_id, status)`, `(passenger_id, status)`, `public_id`.

Unique: **един pending/confirmed booking на пътник на ride**
(partial unique index `(ride_id, passenger_id) WHERE status IN ('PENDING', 'CONFIRMED')`).

### `chat_channels`

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `public_id` | UUID | UNIQUE, NOT NULL |
| `ride_id` | BIGINT | FK → rides |
| `driver_id` | BIGINT | FK → users |
| `passenger_id` | BIGINT | FK → users |
| `created_at` | TIMESTAMPTZ | |
| `last_message_at` | TIMESTAMPTZ | NULL, updated on new message |

Unique: `(ride_id, driver_id, passenger_id)`.

### `chat_messages`

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `channel_id` | BIGINT | FK → chat_channels |
| `sender_id` | BIGINT | FK → users |
| `body` | VARCHAR(500) | NOT NULL |
| `created_at` | TIMESTAMPTZ | |
| `read_at` | TIMESTAMPTZ | NULL |

Индекс: `(channel_id, created_at DESC)`.

### `ratings`

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `booking_id` | BIGINT | FK → bookings |
| `rater_id` | BIGINT | FK → users (кой оценява) |
| `ratee_id` | BIGINT | FK → users (кого оценяват) |
| `direction` | VARCHAR(16) | `DRIVER_TO_PASSENGER` / `PASSENGER_TO_DRIVER` |
| `stars` | SMALLINT | CHECK BETWEEN 1 AND 5 |
| `comment` | VARCHAR(500) | NULL |
| `created_at` | TIMESTAMPTZ | |

Unique: `(booking_id, direction)` — по един рейтинг на посока на booking.

### `refresh_tokens`

| Колона | Тип | |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `user_id` | BIGINT | FK → users |
| `token_hash` | CHAR(64) | SHA-256 на самия token (в клиента остава суровият) |
| `issued_at` | TIMESTAMPTZ | |
| `expires_at` | TIMESTAMPTZ | |
| `revoked_at` | TIMESTAMPTZ | NULL |
| `replaced_by_id` | BIGINT | FK → refresh_tokens, NULL (за rotation trace) |
| `ip` | INET | NULL |
| `user_agent` | VARCHAR(255) | NULL |

Индекс: `token_hash` (unique), `(user_id, revoked_at)`.

### `audit_events`

Виж `docs/SECURITY.md § 10` за структурата и hash-chain логиката.

## 3. Изведени полета / views

### `v_user_ratings` (materialized view, refresh cron 1h)

```sql
CREATE MATERIALIZED VIEW v_user_ratings AS
SELECT
    ratee_id AS user_id,
    direction,
    COUNT(*) AS ratings_count,
    ROUND(AVG(stars)::numeric, 2) AS avg_stars,
    ROUND(AVG(stars) FILTER (WHERE created_at > NOW() - INTERVAL '90 days'), 2) AS avg_stars_90d
FROM ratings
GROUP BY ratee_id, direction;
```

### `v_ride_available_seats` (regular view)

```sql
CREATE VIEW v_ride_available_seats AS
SELECT
    r.id AS ride_id,
    r.seats_offered - COALESCE(SUM(b.seats) FILTER (WHERE b.status IN ('PENDING', 'CONFIRMED')), 0) AS available
FROM rides r
LEFT JOIN bookings b ON b.ride_id = r.id
GROUP BY r.id;
```

Използва се в search response; кеширане не се препоръчва (данните са
чувствителни към последователност).

## 4. Правила за консистентност

- **Optimistic locking** на `bookings` (JPA `@Version`) — иначе възможно е
  двама пътници да резервират последното място едновременно.
- **CHECK constraint** на `bookings.seats ≤ v_ride_available_seats.available`
  се проверява в service layer (не в DB, защото зависи от view).
- **Cascade delete** е забранен на всякакви финансови / audit таблици.
- `users.deleted_at` — soft delete. Всички queries добавят `WHERE deleted_at IS NULL`.
- **Timezones**: всичко в UTC в DB (`TIMESTAMPTZ`), конвертиране към
  `Europe/Sofia` в UI.

## 5. Миграции (Flyway)

- `V1__init.sql` — създава всички таблици + roles.
- `V2__seed_cities.sql` — 30-те областни центъра + няколко ключови града.
- `V3__seed_demo_users.sql` — само за `dev` профил, изпълнява се условно
  чрез `flyway.placeholders` или чрез `db.migration.seed=true` flag.

Всяка миграция е идемпотентна. `V*__desc.sql` е форматът; никакви `V2.1`
или дупки в номерата.
