# Carpooling — Project Context

**Един ред за проекта:** карпулинг платформа за българския пазар, която
свързва шофьори с пътници по маршрути между градове; сигурна, бърза и с
модерен UI.

## Стек (не отклонявай без разрешение)

### Frontend
- Vue 3.5+ (Composition API, `<script setup>`)
- Vite 5+
- Pinia (state)
- Vue Router 4
- PrimeVue 4 (компоненти) + PrimeIcons
- vue-i18n 10 (bg / en)
- Leaflet + `@vue-leaflet/vue-leaflet` (карти, OpenStreetMap tiles)
- `@stomp/stompjs` + `sockjs-client` (WebSocket чат)
- axios (HTTP)
- vitest + @vue/test-utils (тестове)

### Backend
- Java 21
- Spring Boot 3.5
- Spring Web, Spring Data JPA, Spring Security, Spring WebSocket
- PostgreSQL 17
- Flyway (миграции)
- Lombok, MapStruct
- Bucket4j (rate limiting)
- `com.auth0:java-jwt` за JWT
- Testcontainers + JUnit 5 (тестове)
- OpenAPI / springdoc (документация)

### Инфраструктура
- Docker Compose (postgres, backend, frontend, nginx reverse proxy)
- Nginx за static + reverse proxy към backend
- GitHub Actions (build + test на всеки push)

## Правила за код

- **Java**: package-by-feature (не layered) —
  `bg.tu_sofia.carpooling.rides`, `.auth`, `.chat`, `.ratings`, `.users`.
- **Vue**: `src/features/{rides,auth,chat,ratings,profile}/` +
  `src/shared/` за общото.
- **Именуване**: всичко на английски (класове, файлове, DB колони, API).
  UI лейбъли — двуезично през `vue-i18n`.
- **Коментари**: минимални, само за нетривиални решения. JSDoc върху
  композабъли/store actions.
- **Без магически числа** — константи във `constants.ts` / `Constants.java`.
- **DTO ≠ Entity** — винаги MapStruct за конвертиране.
- **Никакво `any` в TypeScript**. Никакво `Object` в Java, където може
  конкретен тип.

## Правила за git

- Conventional commits на английски:
  `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `security`.
- Един логически change = един commit.
- Всеки commit трябва да оставя проекта в компилиращо се състояние.
- Клонове: `main` (стабилен), feature branches `feat/xxx`.

## Правила за сигурност (задължителни, без изключения)

Виж `docs/SECURITY.md` за подробности. Кратко:

1. **Никога** пароли в plaintext / логове / комити. Argon2id за нови акаунти.
2. **JWT** в `HttpOnly; Secure; SameSite=Lax` cookies, **не** в localStorage.
3. **CSRF** защита за всички mutating endpoints (Spring Security default).
4. **CORS** — whitelist само на конкретния frontend origin.
5. **Rate limiting** на login, register, chat send, ride search.
6. **Cloudflare Turnstile** на login **и** register (не само register!).
7. **Bean Validation** на всички REST входове. Никаква doverie към клиента.
8. **Параметризирани заявки** — JPA / Query methods; никаква стрингова конкатенация.
9. **Аудит лог** за: login, logout, ride create/cancel, chat message,
   rating submit, admin action.
10. **Security headers**: HSTS, X-Content-Type-Options, X-Frame-Options,
    Content-Security-Policy, Referrer-Policy — конфигурирани в nginx + Spring.

## Език на комуникация

- **С мен (в чата)**: български.
- **Всичко останало** (код, комити, PR-и, UI ID-та, DB имена, лог съобщения):
  английски.
- **UI (потребителят вижда)**: двуезично bg/en, default `bg`.

## Стил на общуване

Кратко, плътно, без излишни любезности. Ако нещо е неясно — питай веднъж
с конкретен въпрос, не с общо „нещо друго?“. Ако трябва да предположиш —
предположи, обяви го, продължи.

## Забранени практики

- Магически URL-и в код (винаги през env / config).
- `System.out.println` / `console.log` в production код (SLF4J / pino).
- Копиране на снипети от Stack Overflow без разбиране.
- Google Maps (изисква API ключ + billing) — използваме Leaflet + OSM.
- Firebase / външни BaaS — самостоятелен self-hosted стек.
- Секрети в `application.yml` — само в `.env` (не се комитва).

## Референции

- Функционален обхват — `docs/SPEC.md`
- Как е структурирано — `docs/ARCHITECTURE.md`
- Заплахи и защити — `docs/SECURITY.md`
- Визуален дизайн — `docs/UI_DESIGN.md`
- ER модел — `docs/DATA_MODEL.md`
- API — `docs/API.md`
- Демо данни — `docs/SEED_DATA.md`
- Roadmap — `docs/TASKS.md`
