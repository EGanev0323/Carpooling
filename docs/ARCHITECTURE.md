# Архитектура

## 1. Диаграма (текстова)

```
                         ┌─────────────┐
                         │   Browser   │
                         └──────┬──────┘
                                │ HTTPS
                         ┌──────▼──────┐
                         │    Nginx    │  reverse proxy, static, security headers
                         └──┬───────┬──┘
                            │       │
              /assets/*     │       │  /api/**, /ws/**
             (SPA build)    │       │
                            │       ▼
                            │   ┌────────────────┐
                            │   │  Spring Boot   │
                            │   │  (Java 21)     │
                            │   └───────┬────────┘
                            │           │ JDBC + Flyway
                            │           ▼
                            │   ┌────────────────┐
                            │   │ PostgreSQL 17  │
                            │   └────────────────┘
                            ▼
                       Vue 3 SPA
                     (Vite dist/)
```

Всичко е контейнеризирано в `docker-compose.yml`. Nginx е единствената
експозирана услуга; backend и DB са само в inter-container мрежата.

## 2. Модули (package-by-feature)

### Backend (`bg.tu_sofia.carpooling.*`)

```
├── auth/           регистрация, логин, JWT, refresh, Turnstile
├── users/          профили, промяна на парола, upload на avatar
├── cars/           регистриране и списък на автомобили на потребител
├── rides/          създаване, търсене, детайли на маршрути; матчинг алгоритъм
├── bookings/       резервации, статуси, отмяна
├── chat/           WebSocket канали, съобщения, история
├── ratings/        двупосочни рейтинги + агрегация
├── audit/          неизменяем аудит лог
├── admin/          админ endpoints
├── geo/            градове, кеширани разстояния (без external API)
├── config/         Spring config, security config, CORS, WebSocket config
└── common/         DTO base, exception handler, validation, MapStruct base
```

Всеки модул има вътрешна структура: `api/` (controllers, DTOs),
`domain/` (entities), `service/`, `repository/`.

### Frontend (`src/`)

```
├── main.ts                bootstrap, PrimeVue, i18n, router
├── App.vue                layout root
├── router/                маршрути + guards
├── stores/                Pinia (auth, ui, notifications)
├── features/
│   ├── auth/              login, register, verify email
│   ├── rides/             search, list, detail, create, my rides
│   ├── bookings/          my bookings, cancel
│   ├── chat/              chat window, message list, STOMP клиент
│   ├── ratings/           submit form, list в profile
│   ├── profile/           my profile, cars, avatar
│   └── admin/             admin dashboard
├── shared/
│   ├── api/               axios instance + interceptors
│   ├── components/        Button, Modal, RideCard, MapView, ...
│   ├── composables/       useAuth, useCsrf, useToast, useDebounce
│   ├── utils/             date, currency, validators
│   └── i18n/              bg.json, en.json
└── assets/                fonts, images, icons
```

## 3. Комуникация между слоеве

### HTTP (REST)

- Всички REST endpoint-и са под `/api/v1/**`.
- Формат: JSON, UTF-8. Дати: ISO 8601 с offset (`2026-07-15T08:30:00+03:00`).
- Грешки: RFC 7807 (`application/problem+json`) с `type`, `title`, `status`,
  `detail`, `traceId`.
- Аутентикация: HttpOnly cookie `AUTH_TOKEN` (JWT, 15 мин.) + `REFRESH_TOKEN`
  (14 дни, ротира при използване).
- CSRF: `X-XSRF-TOKEN` header + `XSRF-TOKEN` cookie (Spring Security default).

### WebSocket

- Endpoint: `/ws` (SockJS-съвместим fallback).
- Протокол: STOMP.
- Auth при handshake: JWT от cookie (Spring Security integration).
- Топологии:
  - `/topic/chat.{channelId}` — broadcast към участниците в канал
  - `/user/queue/errors` — грешки, специфични за потребител
  - `/app/chat.send` — client → server за изпращане на съобщение

## 4. База данни

### Стратегия

- Единна релационна база (PostgreSQL 17).
- Миграции с Flyway; винаги напред, никакви `undo` в production.
- Naming: `snake_case`, множествено число за таблици (`rides`, `users`).
- PK: `BIGSERIAL` (`id`).
- Времеви колони: `created_at`, `updated_at` (`TIMESTAMPTZ`).
- Soft delete: `deleted_at TIMESTAMPTZ NULL` където има смисъл (users, cars).

### Индекси (задължителни)

- `rides (origin_city_id, destination_city_id, departure_at)` — за search.
- `bookings (ride_id, status)` — за seat count.
- `chat_messages (channel_id, created_at DESC)` — за pagination.
- `audit_events (user_id, created_at DESC)` — за admin филтри.

Виж пълния модел в `docs/DATA_MODEL.md`.

## 5. Кеширане

- **Frontend**: SWR-подобен pattern чрез Pinia + TTL за градове (24h),
  профили (5min), детайли на ride (30s).
- **Backend**: Caffeine cache (in-memory) за:
  - Списъка с градове (`geo.cities`, TTL безкраен, инвалидация ръчно)
  - Средни рейтинги на потребители (`ratings.avg`, TTL 15min)
- **Никакъв Redis в MVP** — Caffeine е достатъчен за single-instance деплой.

## 6. Deploy

### Docker Compose (development и single-node production)

```yaml
services:
  postgres:  postgres:17-alpine
  backend:   локален build от ./backend/Dockerfile
  frontend:  локален build от ./frontend/Dockerfile (multi-stage → dist/)
  nginx:     nginx:1.27-alpine, mount на dist/ + nginx.conf
```

### Профили

- `dev` — hot reload, verbose logs, self-signed certs, seed данни зареждат
  автоматично при startup.
- `prod` — минимални логове (INFO), no seed, actuator само на internal port.

### Секрети

- `.env` файл (git-ignored), примерен `.env.example` в репото.
- Задължителни променливи:
  `DB_PASSWORD`, `JWT_SECRET` (мин. 64 знака), `TURNSTILE_SECRET`,
  `SMTP_HOST`, `SMTP_USER`, `SMTP_PASSWORD`, `CORS_ALLOWED_ORIGIN`.

## 7. Наблюдаемост

- **Логове**: SLF4J + Logback, JSON формат в prod, чове стандарта в dev.
  Всеки лог носи `traceId` (генериран в филтър при вход).
- **Метрики**: Spring Actuator + Micrometer, endpoint `/actuator/prometheus`
  (само internal).
- **Health**: `/actuator/health` (публичен, но без detail).
- **Frontend logging**: собствен composable `useLogger` — само warn/error
  се пращат към backend endpoint `/api/v1/telemetry/errors`.

## 8. Тестова стратегия

| Слой | Инструмент | Покритие |
|---|---|---|
| Backend unit | JUnit 5 + Mockito | services, mappers, validators |
| Backend integration | JUnit 5 + Testcontainers (Postgres) | controllers + DB |
| Backend security | @WithMockUser, MockMvc | auth flows, CSRF, роли |
| Frontend unit | Vitest + @vue/test-utils | composables, stores, компоненти |
| E2E | Playwright | happy paths: register → book → chat → rate |

Coverage минимум: 70% на backend, 60% на frontend. Няма мерло за
generated-code (MapStruct, миграции).

## 9. CI/CD (GitHub Actions)

- `pull_request` → build + test + lint + докер image build (без push).
- `push` в `main` → същото + push на image в GHCR + deploy на staging (ако е конфигуриран).
- Секретите се четат от GitHub Secrets.
