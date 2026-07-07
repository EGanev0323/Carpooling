# Roadmap по фази

Фазите не са водопадни срокове — те са **приоритетна подредба**. Всяка
фаза трябва да оставя проекта в използваемо, компилиращо се, тествано
състояние.

Приоритети:
- **P0** — блокиращи, без тях няма продукт.
- **P1** — задължителни за MVP.
- **P2** — качество и полишинг.
- **P3** — стрейч цели.

---

## Фаза 0 · Анализ и подготовка

**Цел**: разбираме какво имаме и планираме първия commit.

- [ ] `git log`, `tree`, четене на съществуващи pom/package файлове
- [ ] Кратък доклад към потребителя (30 реда max)
- [ ] Съгласуване на промени преди пренаписване
- [ ] Създаване на `feat/foundation` branch

**Deliverable**: доклад в чата, план за Фаза 1.

---

## Фаза 1 · Foundation (P0)

**Цел**: пуска се `docker compose up` и виждам празно, но живо приложение.

- [ ] Backend skeleton: Spring Boot 3.5, Java 21, Maven multi-module
      или single-module (single е достатъчно)
- [ ] PostgreSQL 17 в docker-compose, health check
- [ ] Flyway `V1__init.sql` — минимум `users`, `roles`, `cities`, `audit_events`
- [ ] Spring Security config — JWT filter, CSRF, CORS (whitelist)
- [ ] Auth endpoints: register, login, refresh, logout, me (виж `docs/API.md`)
- [ ] Argon2id password encoder
- [ ] Cloudflare Turnstile валидация (server-side)
- [ ] Rate limiting с Bucket4j на login/register
- [ ] Frontend skeleton: Vite + Vue 3 + Pinia + Router + PrimeVue + i18n
- [ ] Login / Register страници (functional, не perfect)
- [ ] Axios instance с cookie credentials + CSRF interceptor
- [ ] Nginx service в docker-compose с reverse proxy + static
- [ ] `.env.example`, `.gitignore` покрива секретите
- [ ] `README.md` пренаписан: клониране → пуск за 2 минути
- [ ] CI: GitHub Actions build + test (без deploy засега)
- [ ] Първи commit с всичко компилиращо се, README актуален

**Deliverable**: `docker compose up` → можеш да се регистрираш и логнеш.

---

## Фаза 2 · Core функционалност (P0)

**Цел**: пълен happy path — шофьор публикува, пътник резервира.

- [ ] `V2__rides_bookings.sql` миграция
- [ ] Cars CRUD endpoints + UI (моите коли, добави кола)
- [ ] Ride publish endpoint + form UI с валидации
- [ ] Ride search endpoint с филтри + list + pagination
- [ ] Search results UI: карти + placeholder за карта (Leaflet идва във Фаза 3)
- [ ] Ride detail страница
- [ ] Booking create endpoint с optimistic locking
- [ ] Booking status transitions (pending → confirmed → completed/cancelled)
- [ ] My rides / My bookings страници
- [ ] Аудит събития за create/cancel/confirm
- [ ] Seed данните (виж `docs/SEED_DATA.md`) работят в dev профил
- [ ] Unit тестове за matching logic и booking service
- [ ] Integration тестове (Testcontainers) за rides + bookings контролерите

**Deliverable**: happy path работи end-to-end с реалистични данни.

---

## Фаза 3 · Карта и real-time чат (P1)

**Цел**: визуалната „уаа“ фаза — приложението изглежда живо и модерно.

- [ ] Leaflet интеграция във фронтенда
- [ ] Компонент `MapView` с полилинии на всички search резултати
- [ ] `fitBounds` на видимите ride-ове
- [ ] Custom SVG маркери (не default pin)
- [ ] Popup карти при hover на маркер
- [ ] `route_polyline` генерация в backend
      (може с прост `great-circle` line между origin и destination — не
      реален път по OSRM, освен ако имаш време)
- [ ] Backend: WebSocket config (STOMP endpoint `/ws`, security integration)
- [ ] Chat channels — auto-create при booking CONFIRMED
- [ ] Chat messages endpoint (history)
- [ ] Chat UI компонент: списък от канали, отваряне на разговор,
      изпращане на съобщение, indicator „пише...“
- [ ] Rate limiting на chat send (10/min/channel)
- [ ] E2E test с Playwright: register → search → book → confirm → chat

**Deliverable**: демо-ready карта + чат.

---

## Фаза 4 · Рейтинги и профил (P1)

- [ ] Ratings API (submit + list)
- [ ] Materialized view `v_user_ratings` + refresh cron
- [ ] Profile страница с рейтинги, история, коли
- [ ] Rating submission modal (accessible след ride completion)
- [ ] Показване на avg star + брой отзиви на всяка ride card
- [ ] Rating защити — не може да оценяваш преди `departureAt + 2h`

**Deliverable**: пълен цикъл на доверие.

---

## Фаза 5 · Админ панел (P2)

- [ ] Admin dashboard страница с protection (role guard)
- [ ] Списък на потребители с блокиране/деблокиране
- [ ] Аудит лог браузър с филтри
- [ ] Статистика: активни ride-ове, top шофьори, резервации по градове
      (charts през `Chart.js` или `Apache ECharts`)
- [ ] Force-cancel на ride

**Deliverable**: демонстрационен админ panel за защитата.

---

## Фаза 6 · Полишинг (P2)

- [ ] Целия UI според `docs/UI_DESIGN.md`
- [ ] Dark / Light mode toggle с prefers-color-scheme
- [ ] Empty states и error states по спецификацията
- [ ] Accessibility одит (Axe, WAVE)
- [ ] Keyboard navigation тест на всички страници
- [ ] Skip-to-content линк
- [ ] Loading skeletons вместо spinners
- [ ] Toast нотификации с aria-live
- [ ] i18n покритие 100% (никакви hardcoded стрингове)
- [ ] Responsive down to 375px
- [ ] Favicon + Open Graph meta
- [ ] Lighthouse score ≥ 90 на всички категории

**Deliverable**: приложение, което не се срамува от продуктов design review.

---

## Фаза 7 · Security hardening (P1)

- [ ] Целия контролен списък от `docs/SECURITY.md § 14` е зелен
- [ ] `npm audit` и `mvn dependency-check` — 0 HIGH/CRITICAL
- [ ] Mozilla Observatory score ≥ A-
- [ ] Security headers verified чрез `curl -I`
- [ ] Non-root containers
- [ ] Docker `read_only: true` + tmpfs за writable paths
- [ ] Prod профил без seed
- [ ] Playwright тест: неавторизиран потребител не може да дърне защитен endpoint
- [ ] Playwright тест: rate limit на login връща 429 след 5 опита

**Deliverable**: сигурност, която оценителят на защита не може да оспори.

---

## Фаза 8 · Стрейч цели (P3)

Не работи по нищо от тук, докато P0-P2 не е завършено.

- [ ] PWA (Service Worker + offline shell)
- [ ] Push нотификации (browser API)
- [ ] Междинни спирки в маршрут + матчинг по частичен маршрут
- [ ] Реални маршрути през OSRM за accurate polylines
- [ ] Auto-suggest на цена базиран на средно за маршрута
- [ ] Grafana + Prometheus dashboards
- [ ] i18n добавен език (например немски — свързва се с FDIBA контекста)

---

## Общи правила

- **Никаква фаза не се пропуска.** Ако P2 не е готов, P3 не се започва.
- **Commit-и след всяка задача.** Ако си на нещо голямо — WIP commit е OK,
  но не смесвай featureи.
- **Тестовете растат заедно с кода.** Не оставяй coverage за „накрая“.
- **Питай, ако си несигурен**, не измисляй тихо. Един въпрос сега спасява
  час рефакторинг после.
