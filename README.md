# Carpooling — Система за споделено пътуване

Карпулинг платформа за българския пазар. Шофьори публикуват маршрути между градове, пътници резервират места, комуникират в реално време и се оценяват взаимно.

## Стек

| Слой | Технология |
|---|---|
| Frontend | Vue 3.5 + Vite + Pinia + PrimeVue 4 + vue-i18n |
| Backend | Spring Boot 3.5 + Java 21 + Spring Security (JWT) |
| База данни | PostgreSQL 17 + Flyway миграции |
| Карта | Leaflet + OpenStreetMap (без API ключ) |
| Чат | STOMP над WebSocket (SockJS fallback) |
| Инфраструктура | Docker Compose + Nginx |

## Бързо стартиране (2 минути)

### Изисквания
- Docker Desktop (или Docker Engine + Compose)
- Git

### Стъпки

```bash
# 1. Клонирай
git clone https://github.com/EGanev0323/Carpooling.git
cd Carpooling

# 2. Конфигурирай (копирай .env.example → .env, промени само JWT_SECRET)
cp .env.example .env

# 3. Генерирай JWT secret (минимум 64 знака)
# Linux/Mac:
openssl rand -hex 64
# Windows PowerShell:
[System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64) | ForEach-Object { $_.ToString("x2") } | Out-String -NoNewline

# Постави генерирания secret в .env:
# JWT_SECRET=<генерираното_стойност>

# 4. Стартирай
docker compose up --build

# Приложението е достъпно на http://localhost
```

### Тест потребители (seed данни)

| Роля | Email | Парола |
|---|---|---|
| Шофьор | ivan.petrov@example.com | Parola123! |
| Шофьор | maria.georgieva@example.com | Parola123! |
| Пътник | georgi.ivanov@example.com | Parola123! |
| Пътник | elena.kostadinova@example.com | Parola123! |
| Пътник | petar.nikolov@example.com | Parola123! |

> Seed данните се зареждат автоматично при `SEED_ENABLED=true` (default в .env.example).

## Локална разработка (без Docker)

### Backend

```bash
cd backend

# Стартирай само PostgreSQL
docker compose up postgres -d

# Стартирай backend
./mvnw spring-boot:run
```

Backend е на: http://localhost:8080  
Swagger UI: http://localhost:8080/api/swagger-ui.html

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend е на: http://localhost:5173 (proxy към backend:8080)

## Тестове

```bash
# Backend тестове
cd backend && ./mvnw verify

# Frontend тестове
cd frontend && npm run test:unit
```

## Архитектура

```
Browser → Nginx :80
              ├── /api/** → Spring Boot :8080
              ├── /ws     → Spring Boot :8080 (WebSocket)
              └── /*      → Vue SPA (static)

Spring Boot → PostgreSQL :5432 (само в inter-container мрежата)
```

Виж `docs/ARCHITECTURE.md` за пълна документация.

## Сигурност

- JWT в `HttpOnly; Secure; SameSite=Lax` cookies (не в localStorage)
- Argon2id за пароли (нови акаунти)
- CSRF защита (cookie-based)
- Rate limiting с Bucket4j (login 5/15мин, register 3/час)
- Всички security headers по SECURITY.md §8

Виж `docs/SECURITY.md` за пълен анализ.

## Документация

| Файл | Съдържание |
|---|---|
| `docs/SPEC.md` | Функционална спецификация |
| `docs/ARCHITECTURE.md` | Архитектура |
| `docs/SECURITY.md` | STRIDE анализ и мерки |
| `docs/DATA_MODEL.md` | ER модел |
| `docs/API.md` | REST и WebSocket endpoints |
| `docs/UI_DESIGN.md` | Дизайн система |
| `docs/SEED_DATA.md` | Demo данни |
| `docs/TASKS.md` | Roadmap |
