# Сигурност

Този документ е задължителен. Всяко PR-искане, което го нарушава, не се
приема — независимо от бизнес натиска. Написан е с уроците от
предишните проекти (AI Recruiter — Turnstile само на register, JWT в
localStorage, missing @CacheEvict, unbuilt Docker frontend).

## 1. STRIDE анализ

| Заплаха | Пример за Carpooling | Мярка |
|---|---|---|
| **S**poofing | Логин с чужд имейл | Argon2id хешове, Turnstile на login **и** register, 5/15 rate limit |
| **T**ampering | Промяна на цена в POST от клиент | Bean Validation + server-side authoritative price |
| **R**epudiation | Шофьор отрича, че е отменил маршрут | Append-only `audit_events` таблица с hash-chain |
| **I**nformation Disclosure | Списък с телефони през enum на ID | UUID за публични референции, phone маскиран в списък |
| **D**oS | Скрипт създава 10 000 fake rides | Bucket4j: 10 create/hour на потребител |
| **E**levation of Privilege | Пътник промени `role=ADMIN` в JSON | `@JsonProperty(access=READ_ONLY)` + сървърна валидация на роли |

## 2. Аутентикация

### 2.1 Съхранение на пароли
- **Нови акаунти**: Argon2id (`memory=64MB, iterations=3, parallelism=4`).
- **Legacy** (ако мигрираш): BCrypt cost=12; при първи логин re-hash с Argon2id.
- **Никога** MD5, SHA-1, SHA-256 без salt.

### 2.2 JWT токени
- **Access token**: 15 минути, HS512, secret ≥ 64 знака от `.env`.
- **Refresh token**: 14 дни, ротира при използване (`rotate on use`),
  съхранение в `refresh_tokens` таблица с `revoked_at`.
- **Съхранение в браузър**: **само** `HttpOnly; Secure; SameSite=Lax` cookies.
  Никога `localStorage` или `sessionStorage`.
- **Claims**: `sub` (user id), `roles`, `iat`, `exp`, `jti` (за revocation).
- **Blacklist**: `jti` на разлогнати токени в Caffeine cache до `exp`.

### 2.3 Turnstile
- И на `/api/v1/auth/register`, и на `/api/v1/auth/login`.
- Валидация server-side чрез `https://challenges.cloudflare.com/turnstile/v0/siteverify`.
- Ако Turnstile е недостъпен → отхвърли заявката (fail-closed), не се допуска
  bypass „временно“.

### 2.4 Login attempts
- Bucket4j token bucket: 5 attempts / 15 min / IP + email комбинация.
- След 5 неуспешни: HTTP 429 с `Retry-After` header.
- Логни всяко login събитие в `audit_events` (успех и неуспех).

## 3. Оторизация

- Role-based: `USER`, `DRIVER`, `ADMIN`. Ролите не се стакват имплицитно —
  `DRIVER` **не** носи `USER` автоматично; user има set от роли.
- Method-level: `@PreAuthorize("hasRole('ADMIN')")` на админ endpoint-и.
- Resource-level: собственик проверка в service, не в controller
  (`ride.driver.id == currentUser.id`).
- **Никаква** авторизация на клиента. UI скриване на бутони е UX, не сигурност.

## 4. Входна валидация

- `@Valid` на всеки `@RequestBody`.
- Constraints:
  - `@Email`, `@Pattern` за телефон,
  - `@Size(min=12)` за пароли,
  - `@DecimalMin`/`@DecimalMax` за цени,
  - `@Future` за `departureAt`.
- **Whitelist**, не blacklist. Ако полето не е в DTO — игнорирай го.
- Sanitize свободния текст (chat, ride comment, review): HTML escape в UI
  (Vue го прави по default с `{{ }}`, никакво `v-html` без DOMPurify).

## 5. Защита от инжекции

### 5.1 SQL
- Само JPA / Query methods / JPQL с параметри.
- **Никакво** `String.format` в заявки.
- **Никакво** динамично `ORDER BY` без whitelist.

### 5.2 XSS
- Vue templates по default escape-ват.
- `v-html` е забранено освен през composable `useSanitizedHtml` (DOMPurify).
- HTTP отговори: `Content-Type: application/json; charset=utf-8`.

### 5.3 CSRF
- Spring Security default с `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
- Axios interceptor чете `XSRF-TOKEN` cookie и го прикачва като `X-XSRF-TOKEN`.

### 5.4 SSRF
- Turnstile е единственият external call от backend. URL-ът е константа.
- Ако някога се добавят external calls (аватар upload от URL и т.н.) —
  whitelist от домейни.

## 6. Съобщения (Chat)

- Мaксимум 500 знака (validated).
- HTML escape при съхранение и извеждане.
- Rate limit: 10 съобщения/минута/канал.
- Съхранени 90 дни; след това move в `chat_messages_archive` cron job.
- Забранена реч (prof filter): не се прави в MVP, но полета в audit са готови
  за flag-ване.

## 7. Файлове (аватар, снимка на кола)

- Max 5MB.
- MIME whitelist: `image/jpeg`, `image/png`, `image/webp`.
- **Magic bytes check** (не само MIME) — библиотека `Apache Tika`.
- Ре-енкодирай през `ImageIO` за да разкараш EXIF + evtl. embedded payload.
- Съхранение: на файлова система в `/data/uploads/` (mounted volume), с
  генерирано UUID име. Никакви оригинални имена от клиента.

## 8. HTTP security headers (nginx + Spring)

```
Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(self), camera=(), microphone=()
Content-Security-Policy: default-src 'self';
                         script-src 'self' https://challenges.cloudflare.com;
                         frame-src https://challenges.cloudflare.com;
                         img-src 'self' data: https://*.tile.openstreetmap.org;
                         style-src 'self' 'unsafe-inline';
                         connect-src 'self' wss://carpooling.example ...;
                         font-src 'self' data:;
                         object-src 'none';
                         base-uri 'self';
                         form-action 'self';
```

`unsafe-inline` в `style-src` е компромис заради PrimeVue; ако рефакторираш
към scoped styles — премахни го.

## 9. CORS

- **Whitelist само на конкретния origin** (не `*`).
- `Access-Control-Allow-Credentials: true` (иначе HttpOnly cookies не работят).
- Позволени методи: `GET, POST, PUT, PATCH, DELETE, OPTIONS`.
- Позволени headers: `Content-Type, X-XSRF-TOKEN, Authorization`.

## 10. Аудит лог

Append-only таблица `audit_events`:

| Колона | Тип | Забележка |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `user_id` | BIGINT NULL | NULL за анонимни събития (напр. failed login) |
| `event_type` | VARCHAR(64) | enum |
| `entity_type` | VARCHAR(64) | напр. `RIDE`, `BOOKING` |
| `entity_id` | BIGINT NULL | |
| `metadata` | JSONB | детайли (стар/нов статус, IP, UA) |
| `created_at` | TIMESTAMPTZ | |
| `prev_hash` | CHAR(64) | SHA-256 на предишния ред |
| `hash` | CHAR(64) | SHA-256 на този ред + prev_hash |

Хеш-веригата се пресмята в `@PrePersist`. Никакви UPDATE/DELETE на таблицата
(revoked на DB ниво: `REVOKE UPDATE, DELETE ON audit_events FROM app_user`).

Събития за логване:
- `AUTH_LOGIN_SUCCESS`, `AUTH_LOGIN_FAILURE`, `AUTH_LOGOUT`
- `AUTH_REGISTER`, `AUTH_EMAIL_VERIFIED`
- `RIDE_CREATED`, `RIDE_CANCELLED`, `RIDE_COMPLETED`
- `BOOKING_CREATED`, `BOOKING_CONFIRMED`, `BOOKING_REJECTED`, `BOOKING_CANCELLED`
- `RATING_SUBMITTED`
- `ADMIN_USER_BLOCKED`, `ADMIN_RIDE_FORCE_CANCELLED`
- `CHAT_FLAGGED` (за бъдещи модерации)

## 11. Криптиране на PII в покой

- **Телефони**: не шифровани в MVP (търсими); маскирани при извеждане на трети
  страни (`+3598***34`).
- **Emails**: не шифровани; salt-ватият hash се пази в отделна колона
  `email_lookup_hash` за rate limit по email.
- **Пароли**: Argon2id (не reversible).
- **JWT secret, DB password**: само в `.env`.

## 12. Deployment сигурност

- HTTPS задължителен в prod (Let's Encrypt чрез Certbot).
- Auto-renew на сертификатите.
- Nginx конфигурация:
  - `ssl_protocols TLSv1.2 TLSv1.3;`
  - `ssl_ciphers` по Mozilla intermediate profile.
  - `ssl_prefer_server_ciphers on;`
  - OCSP stapling.
- Docker:
  - Backend контейнер работи като non-root user.
  - `read_only: true` на файлова система, tmpfs за `/tmp`.
  - `no-new-privileges: true`.
- PostgreSQL:
  - Порт не е експозиран навън, само в inter-container мрежата.
  - `pg_hba.conf` — `md5` (или `scram-sha-256`) authentication.
  - Backup: `pg_dump` cron, шифровани файлове с `age` на remote storage.

## 13. Известни ограничения (обяви ги в защита на дипломна работа)

- MVP не поддържа истински плащания → PCI DSS не се прилага.
- MVP е single-instance (in-memory rate limit, Caffeine cache) → не мащабира
  хоризонтално без Redis + централизиран JWT blacklist.
- Няма 2FA (planned за фаза 2).
- Chat съобщенията не са E2E шифровани — server може да ги чете (за модерация).
- Няма формален DPO / GDPR представител; политиката покрива техническите
  задължения (export, delete, retention), но не и организационните.

## 14. Контролен списък преди release

- [ ] Всички зависимости минават `npm audit` / `mvn dependency-check`
      без CRITICAL / HIGH.
- [ ] `.env` е в `.gitignore` и няма история с секрети (`git log -p | grep`).
- [ ] Turnstile е конфигуриран и работи на login **и** register.
- [ ] JWT е в HttpOnly cookie (провери в DevTools → Application → Cookies).
- [ ] CSRF cookie се сетва при първата GET заявка.
- [ ] Rate limit на login е тестван (curl in loop → 429 след 5).
- [ ] Security headers са налични (проверка с `curl -I` или Mozilla Observatory).
- [ ] Аудит лог се пълни при login/booking/rating.
- [ ] Docker контейнерите не работят като root (`docker exec ... whoami`).
- [ ] SQL е параметризиран (grep-ни `String.format` в repo пакетите).
- [ ] Playwright happy path e2e minava в CI.
