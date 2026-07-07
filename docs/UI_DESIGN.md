# UI дизайн

## 1. Дизайн теза

**„Пътят като линия, а не като карта“** — визуалната идентичност е базирана
на едно графично решение: **тънки полилинии, които свързват две точки**.
Всеки маршрут в списъка се обозначава от малка линия между два кръга (origin
и destination), която повтаря формата на реалния маршрут на голямата карта.

Това е сигнатурата на приложението — не hero image, не goofy илюстрация на
кола, а **самата геометрия на пътуването**.

## 2. Палитра

Отклоняваме се от типичните „warm cream + terracotta“ и „black + acid green“
на AI-генерирани дизайни. Избираме асфалт + линии от пътна маркировка като
метафора:

| Име | Hex | Употреба |
|---|---|---|
| `--ink` | `#0E1116` | основен текст, фонове на header в light mode |
| `--asphalt` | `#1B1F26` | фон на dark mode (default) |
| `--lane` | `#F5F1E8` | фон на light mode, текст в dark mode |
| `--mark` | `#F2C641` | акцент — жълто от пътна маркировка, само за primary CTA и активни състояния |
| `--route` | `#3F7CAC` | цвят на полилинии, links, focus outlines |
| `--warn` | `#D95D39` | грешки, отмени, negative rating |
| `--muted` | `#6B7280` | secondary text, disabled |
| `--rule` | `#2A2F38` (dark) / `#E5E1D8` (light) | hairlines, dividers |

**Default тема: dark**. Light mode е първокласен, не заден.

## 3. Типография

Не posh-serif + humanist sans (default AI look). Избираме:

- **Display**: **`Bespoke Serif`** (или fallback `Fraunces` от Google Fonts)
  — само за големите числа (свободни места, цена, рейтинг) и hero заглавия.
  Wide weight axis, opsz axis активен.
- **Body**: **`Inter`** — четлив, добре хвърля в i18n bg/en.
- **Mono**: **`JetBrains Mono`** — само за код в debug view, ride ID в admin.

Скала (rem):

```
--fs-display-xl:  4.5rem   (72px) — hero
--fs-display-l:   3.0rem   (48px) — числа в карти
--fs-h1:          2.25rem  (36px)
--fs-h2:          1.5rem   (24px)
--fs-h3:          1.25rem  (20px)
--fs-body:        1.0rem   (16px)
--fs-small:       0.875rem (14px)
--fs-caption:     0.75rem  (12px) — timestamp, meta
```

Line height: 1.15 за display, 1.5 за body. Letter-spacing: -0.02em за display,
0 за body, 0.06em uppercase за eyebrows.

## 4. Spacing и grid

- Базова единица: 4px.
- Стъпки: 4, 8, 12, 16, 24, 32, 48, 64, 96.
- Content max-width: 1200px, gutter 24px на desktop, 16px на mobile.
- Карти: min-height 160px, padding 24px.

## 5. Компоненти (PrimeVue тема)

### 5.1 Buttons

- **Primary**: жълт (`--mark`) фон, черен текст, radius 8px, weight 600.
  Hover: -8% brightness. Focus: 2px outline `--route`.
- **Secondary**: transparent, 1px border `--rule`, текст `--lane` / `--ink`.
- **Ghost**: без border, само текст `--route`.
- **Destructive**: `--warn` фон, бял текст.
- Height: 44px (touch-friendly), 36px за compact варианти.

### 5.2 RideCard (най-важният компонент)

```
┌─────────────────────────────────────────────────┐
│ 08:30  ● ─────────────────────  ●  11:45        │
│        София (Централна ЖП)   Пловдив (Мол)      │
│                                                  │
│  ⭐ 4.9 · Иван Г. · Skoda Octavia · 3 мест     │
│                                                  │
│  35 лв.                             [Резервирай] │
└─────────────────────────────────────────────────┘
```

- Линията между двата кръга е `--route`, 2px, дискретен gradient към muted.
- Времената са в `Bespoke Serif`, 32px.
- Цената в `Bespoke Serif`, 36px, дясно подравнена.
- Hover: elevation z1 (subtle shadow) + border-color става `--route`.

### 5.3 MapView

- Leaflet с `CartoDB Voyager` tiles (по-неутрален от default OSM).
- Полилинии в `--route`, weight 4, opacity 0.85.
- Маркери: собствен SVG (не default pin), кръг с бял border 2px.
- Popup: `RideCard`-mini версия.
- Attribution: минимална, в подсказка при hover, не постоянно на екрана.

### 5.4 Chat

- Message bubbles: 12px radius, max-width 70%.
- Собствени съобщения: `--route` фон, бял текст, дясно.
- Чужди съобщения: `--rule` фон, `--lane` текст, ляво.
- Timestamp под всяко: 10px, `--muted`.
- Индикатор „пише...“: три точки animation.
- Empty state: „Още няма съобщения. Напишете нещо, за да започнете.“ +
  малка илюстрация на 2 балона.

### 5.5 Форми

- Label над input-а, не placeholder-as-label.
- Плаваща валидационна грешка отдолу, `--warn`, 12px.
- Success състояние: `--route` тик отдясно.
- Password input: показвай сила (zxcvbn) като 4 стъпки цветна лента.

## 6. Motion

Всичко през `transition: <prop> 200ms cubic-bezier(0.4, 0, 0.2, 1);`.

Специални moment-и:
- **Page load**: content fade-in stagger по 40ms.
- **Ride list update**: нови карти slide-in отгоре (25ms per item).
- **Search results**: карта пан-зoomва към bounding box на резултатите
  (Leaflet `fitBounds`, 400ms animate).

`prefers-reduced-motion: reduce` → всички transitions стават 0ms, animations
се disable-ват.

## 7. Иконки

- **PrimeIcons** за стандартни (menu, close, chevron).
- **Lucide** (через `lucide-vue-next`) за всичко останало — по-модерен look.
- Никакви Font Awesome, никакви emoji-та като функционални икони.

## 8. Тъмна / светла тема

Toggle в header. Съхранява се в `localStorage` (`ui.theme`).
Auto-detect от `prefers-color-scheme` при първо посещение.
CSS variables се сменят на `:root[data-theme="light"]`.

## 9. Accessibility (WCAG 2.1 AA)

- Контраст ≥ 4.5:1 за body, ≥ 3:1 за large text.
- Всеки interactive елемент е focusable с 2px `--route` outline.
- Skip-to-content линк в header.
- `aria-live="polite"` за toast нотификации.
- Модалите trap-ват фокуса; ESC затваря.
- Форми: label винаги свързан с `for`/`id`.
- Иконки-бутони имат `aria-label`.

## 10. Empty / error states

Не „Oops! Something went wrong 😢“. По-конкретно:

| Ситуация | Съобщение |
|---|---|
| Няма маршрути за филтъра | „Няма маршрути за София → Пловдив на 15 юли. Опитайте съседен ден или премахнете филтри.“ |
| Failed търсене (мрежа) | „Не успяхме да заредим маршрутите. Опитайте отново.“ + [Опитай пак] бутон |
| Няма резервации | „Все още нямате резервации. Разгледайте маршрутите →“ (линк към search) |
| Празен чат | „Още няма съобщения. Напишете нещо, за да започнете.“ |

Тонът: активен глас, спокоен, без възклицания и извинения.

## 11. Микрокопие (гласът на приложението)

- **Регистрирай се**, не „Създай акаунт“.
- **Публикувай маршрут**, не „Направи маршрут“.
- **Резервирай място**, не „Book seat“.
- **Оцени пътуването**, не „Rate this ride“.
- Grеших-съобщения: „Паролата трябва да е поне 12 знака.“ — не „Password
  must contain at least 12 characters.“ (в bg локала).
