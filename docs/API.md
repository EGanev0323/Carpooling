# API

Всички REST endpoint-и са под `/api/v1`. WebSocket: `/ws`.

## Общи конвенции

- **Auth**: HttpOnly cookie `AUTH_TOKEN` + CSRF header `X-XSRF-TOKEN` за
  mutating методи (POST/PUT/PATCH/DELETE).
- **Content-Type**: `application/json; charset=utf-8` за request и response.
- **Error format**: RFC 7807 problem+json.
- **Pagination**: query params `page` (0-based), `size` (default 20, max 100),
  `sort` (`field,asc` или `field,desc`). Response носи `page`, `size`,
  `totalElements`, `totalPages`.
- **Идемпотентност**: mutating endpoint-и приемат optional header
  `Idempotency-Key`; повторните заявки връщат кеширания резултат до 24h.
- **Rate limits**: описани в `docs/SECURITY.md` и в конкретните секции.

## 1. Auth (`/api/v1/auth`)

### POST `/register`
```json
{
  "email": "ivan@example.com",
  "password": "sekurna-parola-123!",
  "firstName": "Иван",
  "lastName": "Георгиев",
  "phone": "+359888123456",
  "turnstileToken": "..."
}
```
201 → празен body, изпраща се email с verify link.
409 → email вече съществува (стандартизирано съобщение — не издава
дали email съществува, отговорът е винаги 201/429/400 по друг критерий).
Rate: 3/час/IP.

### POST `/verify-email`
```json
{ "token": "..." }
```
200 → празен body, auto-login (сет-ва cookies).

### POST `/login`
```json
{
  "email": "ivan@example.com",
  "password": "sekurna-parola-123!",
  "turnstileToken": "..."
}
```
200 → cookies set; body: `{ "user": { ... } }`.
401 → празен body.
429 → `Retry-After` header.
Rate: 5/15min/(IP+email).

### POST `/refresh`
Не носи body. Чете `REFRESH_TOKEN` cookie. Ротира.
200 → нови cookies.

### POST `/logout`
204 → изчиства cookies, отбелязва refresh като revoked, blacklist-ва JTI.

### GET `/me`
Връща текущия потребител + роли.

## 2. Users (`/api/v1/users`)

### GET `/{publicId}`
Публичен профил (`firstName`, `lastName`, `avatar`, `bio`, `avgRating`,
`ridesCompleted`, `memberSince`).

### PATCH `/me`
Обновяване на собствен профил.
```json
{ "firstName": "...", "lastName": "...", "bio": "...", "phone": "..." }
```

### POST `/me/avatar`
Multipart. Валидация: max 5MB, image/*, magic bytes проверка.

### POST `/me/password`
```json
{ "currentPassword": "...", "newPassword": "..." }
```
Invalidates всички refresh tokens освен текущия.

## 3. Cars (`/api/v1/cars`)

### GET `/mine`
Списък от коли на текущия потребител.

### POST ``
Създава кола. При първата успешно създадена — user получава роля `DRIVER`.
```json
{
  "make": "Skoda", "model": "Octavia", "year": 2019,
  "plate": "CB1234XY", "color": "silver", "seats": 5,
  "amenities": { "ac": true, "pets": false, "smoke": false, "luggage": "medium" }
}
```

### PATCH `/{id}`, DELETE `/{id}`
Стандартни. DELETE е soft delete; ако колата има активни маршрути → 409.

## 4. Rides (`/api/v1/rides`)

### GET `/search`
Query: `originSlug`, `destinationSlug`, `date` (YYYY-MM-DD, ± 1 ден),
`minSeats`, `amenities` (comma-separated), `sort` (`departureAt` default,
`price`, `rating`).
Response: paginated list от ride summaries.
Rate: 60/min/user (30/min anonymous).

### GET `/{publicId}`
Детайли на маршрут + shipper профил + свободни места.

### POST ``
Auth: `DRIVER` роля.
```json
{
  "carId": 42,
  "originCitySlug": "sofia",
  "originDetail": "ЦЖС София",
  "destinationCitySlug": "plovdiv",
  "destinationDetail": "Мол Пловдив Плаза",
  "departureAt": "2026-07-15T08:30:00+03:00",
  "seatsOffered": 3,
  "pricePerSeat": 25.00,
  "comment": "Без пушене, без домашни любимци"
}
```
201 → `{ "publicId": "...", "detail": "..." }`.
Rate: 10/час/user.

### PATCH `/{publicId}`
Само от шофьора. Забранено ако има confirmed bookings и промяната засяга
време/цена/маршрут.

### DELETE `/{publicId}`
= cancel. Освобождава всички bookings, изпраща нотификации, audit event.

### GET `/mine`
Ride-овете на текущия потребител като шофьор + като пътник (query param `role`).

## 5. Bookings (`/api/v1/bookings`)

### POST ``
```json
{ "ridePublicId": "...", "seats": 2, "notes": "..." }
```
201 → booking с status PENDING.
409 → недостатъчно места (тяло с текуща наличност).

### GET `/mine`
Собствените резервации.

### POST `/{publicId}/confirm`
Само шофьорът. → CONFIRMED.

### POST `/{publicId}/reject`
Само шофьорът. Body: `{ "reason": "..." }` (audit only, не се показва на
пътника).

### POST `/{publicId}/cancel`
Пътник или шофьор. При <12h преди тръгване → mark as NO_SHOW за пътника.

## 6. Chat (`/api/v1/chat`)

### GET `/channels`
Всички канали на текущия потребител + last message preview.

### GET `/channels/{channelPublicId}/messages`
Paginated (default 30, най-новите). `before=<messageId>` за history.

### POST `/channels/{channelPublicId}/read`
Маркира до последното съобщение като прочетено.

**Изпращане на съобщение**: не през REST, а през WebSocket (виж § 8).

## 7. Ratings (`/api/v1/ratings`)

### GET `/user/{publicId}`
Paginated списък от рейтинги за потребителя (последните 50 по default).

### POST ``
```json
{ "bookingPublicId": "...", "stars": 5, "comment": "Много точен и приятен шофьор." }
```
Валидни само след `departureAt + estimated_duration + 2h`.
Направлението (DRIVER→PASSENGER или обратно) се определя от service-а
базирано на `currentUser` и booking-а.

## 8. WebSocket (`/ws`)

Handshake носи cookies (JWT). Ако cookie липсва → 401 close.

### Client → Server: `/app/chat.send`
```json
{ "channelPublicId": "...", "body": "Здравейте, ще имате ли място за багаж?" }
```
Rate limit: 10/min/channel.

### Server → Client: `/topic/chat.{channelPublicId}`
```json
{
  "id": 123,
  "channelPublicId": "...",
  "senderPublicId": "...",
  "body": "...",
  "createdAt": "2026-07-15T09:12:33+03:00"
}
```

### Server → Client: `/user/queue/errors`
Персонални грешки: rate limit, validation, forbidden.

## 9. Admin (`/api/v1/admin`)

Всичко изисква роля `ADMIN`.

### GET `/users` — paginated filter (status, role, from/to date, q).
### POST `/users/{publicId}/block` — блокира; body: `{ "reason": "..." }`.
### POST `/users/{publicId}/unblock`.
### POST `/rides/{publicId}/force-cancel`.
### GET `/audit-events` — paginated, филтри по `eventType`, `userId`, дата.
### GET `/stats/overview` — брой активни ride-ове, резервации по градове,
top 10 шофьори по рейтинг.

## 10. Geo (`/api/v1/geo`)

### GET `/cities`
Списък от активни градове (за dropdown). Cache 24h на клиента.

## 11. Telemetry (`/api/v1/telemetry`)

### POST `/errors`
Frontend изпраща неигнорирани грешки. Rate limit: 20/min/user.
Body:
```json
{
  "level": "error",
  "message": "...",
  "stack": "...",
  "url": "...",
  "userAgent": "..."
}
```

## 12. OpenAPI

Пълната документация се генерира от кода през `springdoc-openapi` на:
- `/v3/api-docs` (JSON)
- `/swagger-ui.html` (само в `dev` профил)
