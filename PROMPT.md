# Промт за Claude Code (пейстни като първо съобщение)

---

Работиш върху проекта `Carpooling` (https://github.com/EGanev0323/Carpooling.git).
Целта е да го превърнеш в **production-ready карпулинг платформа** за
българския пазар със:

- **Vue 3 (Composition API) + Vite + Pinia + PrimeVue** — фронтенд
- **Spring Boot 3.5 + Java 21 + PostgreSQL 17 + Flyway** — бекенд
- **Docker Compose** — за локална разработка и деплой
- **Leaflet + OpenStreetMap** — карта с маршрути (без API ключ)
- **STOMP над WebSocket** — real-time чат
- **JWT в HttpOnly cookies + BCrypt + Argon2id (нови акаунти)** — auth

## Задължителна документация

Прочети внимателно преди да напишеш и един ред код:

1. `CLAUDE.md` — общи правила и tone-of-voice
2. `docs/SPEC.md` — какво прави приложението
3. `docs/ARCHITECTURE.md` — как е структурирано
4. `docs/SECURITY.md` — заплахи и защити (STRIDE)
5. `docs/UI_DESIGN.md` — палитра, типография, компоненти
6. `docs/DATA_MODEL.md` — entities и връзки
7. `docs/API.md` — REST и WebSocket endpoints
8. `docs/SEED_DATA.md` — демо данни за визуализация
9. `docs/TASKS.md` — roadmap по фази

Ако между този промт и някой от `docs/*` има противоречие — `docs/` печели.

## Фаза 0 — Анализ (не пиши код)

Преди всичко друго:

1. Изпълни `git status`, `git log --oneline -20`, `tree -L 3 -I 'node_modules|target|dist'`.
2. Прочети всеки съществуващ `package.json`, `pom.xml`, `application.yml`,
   `docker-compose.yml`, ако има такива.
3. Направи кратък **доклад** (макс. 30 реда) с:
   - Какво има в момента в репото (стек, модули, схема)
   - Кое ще запазиш as-is
   - Кое ще пренапишеш
   - Кое ще добавиш от нулата
   - Оценка за време по фазите от `docs/TASKS.md`
4. **Изчакай одобрение**, преди да продължиш към Фаза 1.

## Работен режим

- **Комити често**, conventional commits на английски
  (`feat(auth): add JWT HttpOnly cookie strategy`).
- **Никога не пиши код, който компрометира сигурност** — дори „временно“.
  Ако си изкушен да го направиш, спри и ме питай.
- **Тестове** — минимум unit + integration за auth, ride matching, chat.
  Testcontainers за PostgreSQL.
- **Комуникирай на български** в чата, но код, коментари, commit messages,
  UI лейбъли (двуезично bg/en с vue-i18n) — както е указано в `docs/`.
- **Не измисляй библиотеки** — придържай се към стека в `CLAUDE.md`.
  Ако ти трябва нещо извън него — питай.

## Определение за „готово“ (Definition of Done)

Проектът е готов, когато:

- [ ] `docker compose up` вдига всичко от нулата (postgres, backend, frontend,
      nginx) и seed данните са налични
- [ ] Тест потребител (виж `docs/SEED_DATA.md`) може: да се регистрира,
      логне, създаде маршрут, резервира място, чатне с шофьор, оцени го
- [ ] Картата показва всички активни маршрути с полилинии
- [ ] `docs/SECURITY.md` § контролен списък е зелен на 100%
- [ ] `README.md` е пренаписан с точни стъпки за клониране → пуск за 2 минути
- [ ] `npm run build` и `mvn verify` минават без грешки и warnings

Започни с **Фаза 0 — Анализ**.
