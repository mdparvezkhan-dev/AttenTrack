# AttenTrack: tomar UI + backend (ki ki change hoyeche)

## Tomar UI te ja change korechi
**Shudhu 2 ta jaygay tomar UI file chhuyechi:**
1. `res/values/colors.xml` ar `res/values-night/colors.xml`: tomar palette boshano (niche mapping).
2. `res/layout/activity_main.xml`: container ta 409x729dp fixed size chhilo (real phone e screen bhorto na) ar bottom navigation chhilo na.
   Ekhon container screen bhore, ar `BottomNavigationView` add kora (`bottomNav`). `app:navGraph` baad dewa hoyeche karon
   login kora thakle dashboard, na hole login diye shuru korte graph code theke set hoy. Baki shob id/attribute ager moto.
3. `res/layout/fragment_change_password2.xml`: `xmlns:app` declaration chhilo na (build error dito). Shudhu oi ek line add.

**Notun file (tomar kono file er jaygay na):**
- Layout: `fragment_take_attendance`, `fragment_history`, `fragment_report` ar `item_*` (14 ta). Tomar project e ei screen/row gulo chhilo na,
  tai ager design theke niyechhi, ar ei gulo tomar palette/style nijei niye ney.
- `res/menu/bottom_menu_student.xml` (student er bottom tab). Teacher er `bottom_menu.xml` tomar-i, untouched.
- `res/drawable/bg_badge.xml`, `ic_search.xml`.

**Tomar baki shob UI untouched:** 15 ta fragment layout, `main_nav`, themes/styles, drawables, strings, `bottom_menu.xml`, `BarChartView.kt`.

## Palette mapping
| Tomar color | Kothay |
|---|---|
| `--color-1` #555555 | Main text |
| `--color-2` #0D6EFD | Button, active tab, link, primary action |
| `--color-3` #5C0931 | Header (shob screen er upor) |
| `--color-4` #F4F6F9 | Screen background |
| `--color-5` #828486 | Secondary text, caption, divider (transparent version) |
| `--color-6` #82195E | Badge / icon-circle accent (tint #F3E3EC background er sathe) |

`colors.xml` e `color_1 ... color_6` naame palette ache, baki role (brand_primary, header_bg, ...) oi gulo ke point kore. Rong bodlate shudhu ei 6 ta line bodlaw.
Palette er baire: white (card, header text), present/late/absent er green/amber/red, ar dark mode er ujjol version.

## Backend (notun)
`data/` (Retrofit, Gson, repositories, DataStore session, encrypted saved login), `ui/` (loading/error/empty state, shared ViewModel, chart wiring),
`MainActivity` (single activity + bottom nav), `LoginFragment`, shob Fragment er data/click logic, Settings (Theme Light/Dark/System, Change password, Log out).

## Server address
Tomar UI te "Server settings" nai, tai address fixed: `gradle.properties` er `ATTENDANCE_API_URL` (default `https://mdparvezkhan.com/attendance/api/`).
Onno domain/folder hole ei ek line bodlao (shesh e `/api/`).

## Behaviour (ager moto)
Login kore thakle logout porjonto login; "Remember my ID and password"; splash nai; forced password change nai; teacher dashboard + attendance + history + report; student dashboard + course detail.
