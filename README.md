# Community Library Management System

> **🚀 Built entirely by me with the help of AI** — this project was designed, coded and brought to life personally with assistance from AI tools (code generation, debugging, and feature guidance) from start to finish.

A full-featured, production-ready **web-based Library Management System** built with **Java 21**, **Spring Boot 3**, **Thymeleaf**, and **Spring Security**. It manages the complete book lifecycle — catalog, circulation, reservations, fines, ratings and reports — through two separate role-based portals: **Member (User)** and **Staff (Admin / Librarian)**.

> Zero external setup required. Download, run, and the app creates & seeds a file-based H2 database automatically with a realistic catalog of **103 books across 7 categories**, sample authors, publishers, reviews and demo accounts.

---

## Table of Contents

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Screens & Modules](#screens--modules)
4. [Project Structure](#project-structure)
5. [Quick Start](#quick-start)
6. [Demo Accounts](#demo-accounts)
7. [Configuration](#configuration)
8. [Database & H2 Console](#database--h2-console)
9. [Deployment](#deployment)
10. [API Integrations](#api-integrations)
11. [Formatting & Reports](#formatting--reports)
12. [Troubleshooting](#troubleshooting)
13. [Roadmap Ideas](#roadmap-ideas)

---

## Features

### Member Portal (Users)
- **Smart Home & Discovery** — a landing page that shuffles the world's best-selling books into a fresh order on every visit, plus a member dashboard with overdue warnings and unpaid fine totals.
- **Search** — search the catalog by title, author, ISBN, category or keyword.
- **Shopping Cart** — add up to **3 books**, review, then check out to borrow them all at once.
- **Borrowing** — one-click borrow with configurable loan periods; each book keeps a separate physical copy count.
- **Self-Service Returns** — members return borrowed books themselves (no staff visit needed).
- **Reservations** — reserve out-of-stock titles; staff fulfill reservations automatically from the circulation desk.
- **Online Fine Payments** — view pending fines, pay online via **Cash / Card / Online / Other** with a receipt reference.
- **Book Ratings & Reviews** — real internet ratings (average score + review count) pulled from **OpenLibrary**, plus member-written reviews and 1–5 star ratings.
- **Notifications & Feedback** — an in-app notification inbox, event notifications, and a feedback form.
- **Account Features** — profile editing, password change, forgot-password with token-based reset, member registration, and activity tracking.

### Staff Portal (Admin / Librarian)
- **Dashboard** — KPIs, counts, charts and recent activity.
- **Catalog Management** — full **CRUD** for **books, categories, authors and publishers**, with **CSV import/export** and **PDF export**.
- **Sync Ratings from the Internet** — one button re-fetches live OpenLibrary ratings for every book.
- **Circulation** — issue books, process returns, manage reservations, issue history, and overdue tracking.
- **Fines** — automatic late-return fines (per-day rate), manual fines, and a **Record Payment** section.
- **Member Management** — manage users & membership status; staff self-registration and user account management.
- **Reports** — issued books, returns, fines, inventory, daily and monthly reports, all with **CSV & PDF export**.
- **Administration** — active session management, configurable system settings, one-click **backup & restore**, feedback inbox, and the **H2 console**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 |
| **Backend** | Spring Boot 3.3, Spring MVC, Spring Data JPA, Hibernate |
| **Security** | Spring Security 6 (two login chains, role-based access, BCrypt, session concurrency control) |
| **Templating** | Thymeleaf + Thymeleaf Spring Security extras |
| **Frontend** | Bootstrap 5.3, Chart.js, Bootstrap Icons, custom CSS |
| **Database** | H2 (file-based, `./data/library`) |
| **External API** | OpenLibrary REST API (live book ratings) |
| **Export** | OpenPDF (PDF), Apache Commons CSV |
| **Build** | Maven 3.9+ |
| **Deploy** | Container-ready (`Dockerfile.vercel`) → Vercel |

---

## Screens & Modules

| Area | Templates | What you can do |
|---|---|---|
| **Auth** | `auth/*` | Portal selection, member/staff login, register, forgot/reset password, change password, access-denied |
| **Catalog** | `books/*`, `categories/*`, `authors/*`, `publishers/*` | Browse, search, details with ratings, CRUD, CSV import, cover images |
| **Member self-service** | `my/*` | Cart (max 3 books), my borrowed books, self-return, my fines & payments |
| **Circulation** | `circulation/*`, `issues/*`, `returns/*`, `reservations/*` | Issue, return, reserve, history, overdue tracking |
| **Fines** | `fines/*` | Auto/manual fines, payment records |
| **Reports** | `reports/*` | 6 report types with CSV/PDF export |
| **Admin** | `admin/*` | Users, staff, settings, sessions, backup/restore, feedback inbox, H2 console |
| **Social** | `reviews/*`, `feedback.html`, `notifications/*` | Ratings, reviews, feedback, notifications |

---

## Project Structure

```
LIB/
├── pom.xml                                  # Maven build (Spring Boot 3.3.5)
├── Dockerfile.vercel                        # Container build for Vercel
├── h.html                                   # Standalone HTML prototype
├── README.md
└── src/main/
    ├── java/com/library/
    │   ├── LibraryApplication.java          # Entry point (@SpringBootApplication, @EnableScheduling)
    │   ├── config/
    │   │   ├── SecurityConfig.java          # Two SecurityFilterChain beans (staff + app)
    │   │   ├── DataSeeder.java              # Seeds settings, users, 103 books, reviews
    │   │   ├── RatingSyncRunner.java        # Startup/background OpenLibrary sync
    │   │   └── GlobalModelAdvice.java       # Shared model attributes for templates
    │   ├── controller/                       # 26 controllers (Home, Cart, MyBooks, Fines,
    │   │                                    #   Circulation, Reports, Admin, Auth, ...)
    │   ├── service/                          # Business logic (Cart, Circulation, Reports,
    │   │                                    #   RatingSync, Csv, Pdf, Backup, Settings, ...)
    │   ├── repository/                       # 14 Spring Data JPA repositories
    │   ├── model/                            # 23 JPA entities + enums
    │   └── security/                         # UserDetails, auth success handler, security utils
    └── resources/
        ├── application.properties            # DB, H2 console, port, logging
        ├── static/                           # CSS + ~103 book cover images
        └── templates/                        # Thymeleaf pages (fragments/layout.html shared shell)
```

**Key domain entities** (`model/`): `User`, `Book`, `BookIssue`, `Fine`, `Reservation`, `CartItem`, `Review`, `Notification`, `Message`, `Category`, `Author`, `Publisher`, `Setting`, `Activity` — with enums for `Role`, `FineStatus`, `IssueStatus`, `ReservationStatus`, `MembershipStatus`, `MessageType/Status`, `NotificationType`, `PaymentMethod`.

---

## Quick Start

**Prerequisites:** Java 21+ and Maven 3.9+.

```bash
java -version    # must be 21+
mvn -version     # must be 3.9+
```

Missing Java? `brew install openjdk@21`

### Run in 3 steps

```bash
cd /Users/macbook/Movies/LIB

# 1. Build
mvn clean install

# 2. Run the application
mvn spring-boot:run
```

Wait for `Started LibraryApplication in X seconds`, then open:

```bash
open http://localhost:8080    # or type it in Google Chrome
```

> The database is created automatically on first run and seeded with **103 books / 7 categories** (12–16 copies each), sample authors, publishers, reviews and ratings.

---

## Demo Accounts

| Portal | Username | Password | Role |
|---|---|---|---|
| **Staff** | `admin` | `admin123` | Administrator |
| **Staff** | `librarian` | `lib123` | Librarian |
| **User** | `alice` | `alice123` | Member |
| **User** | `bob` | `bob123` | Member |
| **User** | `carol` | `carol123` | Member |

Start at `http://localhost:8080`, pick a portal and sign in:
- **User Portal** → `alice / alice123` to browse, borrow, return, reserve and pay fines.
- **Staff Portal** → `admin / admin123` for full catalog, circulation, fines, reports and admin.

Security model: members sign in through the **member login chain**; staff through the **admin login chain**. Route-level rules protect `/admin/**` (ADMIN only) and catalog/circulation/report endpoints (ADMIN + LIBRARIAN), with an `access-denied` page for everything else.

---

## Configuration

Everything lives in `src/main/resources/application.properties`:

```properties
# Database — override with env var for production/ephemeral storage
spring.datasource.url=${LIBRARY_DB_URL:jdbc:h2:file:./data/library;AUTO_SERVER=TRUE}

# Port — Vercel overrides this via PORT env var
server.port=${PORT:8080}

# H2 console (dev tooling, included by design)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Key runtime settings you can change from the **staff Settings page** (stored in the `Setting` entity):

| Setting | Default | Purpose |
|---|---|---|
| `libraryName` | Community Library | Branding shown in UI |
| `loanPeriodDays` | 14 | How long a book can be borrowed |
| `maxBooksPerMember` | 5 | Borrowing limit per member |
| `finePerDay` | 1.0 | Daily late-return fine (currency units) |
| `contactEmail` / `contactPhone` / `address` | — | Footer & contact info |

---

## Database & H2 Console

The app uses a **file-based H2 database** at `./data/library` — no install, no config.

Access the console:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/library`
- Username: `sa` — Password: *(blank)*

Reset to a clean seed anytime: `rm -rf data && mvn spring-boot:run`

---

## Deployment

### Vercel (Container)

Vercel runs the app as a container from `Dockerfile.vercel` (multi-stage Maven build → JRE 21 run image). Push this project to a Git repo, import it on Vercel, and it detects the Dockerfile. Vercel injects a `PORT` env var that Spring reads via `server.port=${PORT:8080}`.

```bash
git init && git add . && git commit -m "Initial commit"
```

> ⚠️ Vercel's filesystem is **ephemeral** — the H2 file DB resets per instance (DataSeeder repopulates the base catalog + accounts). Point at a durable store via `LIBRARY_DB_URL`, e.g. `LIBRARY_DB_URL=jdbc:h2:file:/tmp/library/data`.

### Classic JAR

```bash
mvn -q -DskipTests package
java -jar target/library-management-1.0.0.jar
```

---

## API Integrations

- **OpenLibrary REST API** — used for *live internet book ratings*. `RatingSyncService` (plus the startup `RatingSyncRunner` and a staff "Sync Ratings from the Internet" button) fetches the average rating + review count for each ISBN. If the API is slow/flaky, retry via the staff Books page or restart the app.

---

## Formatting & Reports

| Export | Library | Used for |
|---|---|---|
| **CSV** | Apache Commons CSV | Import & export books; export all report types |
| **PDF** | OpenPDF | Book catalogs and report exports |

Staff reports available: **Issued**, **Returned**, **Fines**, **Inventory**, **Daily**, **Monthly**.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| **Port 8080 in use** | `pkill -f "spring-boot:run"`, then run again |
| **Invalid username / password** | Use the right portal — members via **User Portal**, staff via **Staff Portal** |
| **Data looks wrong / want clean start** | Delete `data/` and restart — the app reseeds automatically |
| **No internet ratings** | OpenLibrary is occasionally slow — use **Sync Ratings from the Internet** on the staff Books page or restart |
| **Running in IntelliJ / VS Code** | Open the folder as a Maven project and run `LibraryApplication` |

---

## Roadmap Ideas

- Move to a real relational DB (PostgreSQL/MySQL) for multi-instance production.
- External payments provider (Stripe) instead of recorded payments.
- Email/SMS notifications for due dates and reservations.
- Due-date reminder scheduling (the app already enables `@EnableScheduling`).
- Pagination + advanced faceted search across the catalog.

---

## Acknowledgment

This project was **built from scratch by me (a solo developer) with the help of AI**. AI was used as an assistant throughout the whole journey — helping generate boilerplate, architect the domain models, debug issues, and polish features — while all direction, testing and final decisions were mine.

*Built with Java 21 + Spring Boot 3.3.5 — Community Library Management System.*