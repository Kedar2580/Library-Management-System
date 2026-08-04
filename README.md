



# Community Library Management System

A full-featured web-based Library Management System built with **Spring Boot**, **Thymeleaf**, and **Spring Security**. Originally a JavaFX desktop app, now converted to a modern web application with separate **User** and **Staff** portals.

---

## Features

### User Portal (Members)
- Browse and search the catalog (by title, author, ISBN, category)
- **Borrow Books** with one-click borrow + reserve out-of-stock titles
- View borrowed books, due dates, reservations and fines
- Personal dashboard with overdue warnings and unpaid fine total
- Book reviews, ratings, feedback, notifications, forgot-password flow

### Staff Portal (Admin / Librarian)
- Catalog management: books, categories, authors, publishers (CRUD + CSV import)
- Circulation: issue, return, reserve, issue history, overdue tracking
- **Fines**: automatic late-return fines, manual fines, and a **Record Payment** section (Cash/Card/Online with receipt reference)
- Members management, staff self-registration, user management
- Reports (issued / returned / fines / inventory / daily / monthly) with **CSV & PDF export**
- **Active Sessions** — see who is logged in right now
- System settings, backup & restore, feedback inbox, H2 console

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring MVC, Spring Data JPA |
| Security | Spring Security 6 (role-based, two login chains, BCrypt) |
| Frontend | Thymeleaf, Bootstrap 5.3, Chart.js, Bootstrap Icons |
| Database | H2 (file-based, `./data/library`) |
| Export | OpenPDF, Apache Commons CSV |
| Build | Maven |

---

## Prerequisites

- **Java 21** (JDK) — check with `java -version`
- **Maven 3.9+** — check with `mvn -version`

Verify on macOS:

```bash
java -version    # must be 21+
mvn -version     # must be 3.9+
```

If Java is not installed: `brew install openjdk@21`

---

## Deploy to Vercel

Vercel can run this Spring Boot app as a **Docker** deployment (the root `Dockerfile` builds and runs the packaged jar). There is no JVM on Vercel's Node runtime, so the app ships as a container via the `Dockerfile`.

### 1. Push the project to a Git repository

The app must live in a Git repo (GitHub / GitLab / Bitbucket) so Vercel can import it.

```bash
git init
git add .
git commit -m "Initial commit"
```

> `.gitignore` already excludes `target/`, `data/` (the local H2 database), `backups/`, etc.

### 2. Import the repo in Vercel

1. Go to **vercel.com** → **New Project** → import your repository.
2. Vercel auto-detects the `Dockerfile` (deployment type **Docker**).
3. The container listens on the `PORT` env var that Vercel injects (Spring reads `${PORT:8080}`).

### 3. (Optional) Persistent database

Vercel's filesystem is **ephemeral** — an H2 file DB resets per instance/warmup, though `DataSeeder` repopulates initial books, categories, and accounts on a fresh DB. For durable data, set a project environment variable pointing at a managed database or an instance-specific store, for example:

```
LIBRARY_DB_URL=jdbc:h2:file:/tmp/library/data
```

The default (local development) URL is `jdbc:h2:file:./data/library;AUTO_SERVER=TRUE`.

### 4. Deploy

Vercel builds the image from the `Dockerfile` and serves it on a Vercel URL. Default logins after first boot: `admin/admin123`, `librarian/lib123`, `alice/alice123`, `bob/bob123`, `carol/carol123`.

### Build locally into a jar (same as the Docker build)

```bash
mvn -q -DskipTests package
java -jar target/library-management-1.0.0.jar
```

---

## How to Open the Project From the Start

### 1. Clone / open the project

```bash
cd /Users/macbook/Movies/LIB
```

> You can also open this folder directly in IntelliJ IDEA or VS Code. IntelliJ will import it as a Maven project automatically.

### 2. Build the project

```bash
mvn clean install
```

### 3. Run the application

```bash
mvn spring-boot:run
```

You should see:

```
Started LibraryApplication in X seconds
```

### 4. Open in your browser

Go to **http://localhost:8080** — or run `open http://localhost:8080` on macOS.

The database is created automatically on first run and seeded with 103 books in 7 categories.

---

## Login Accounts

| Portal | Username | Password | Role |
|---|---|---|---|
| **Staff** | `admin` | `admin123` | Administrator |
| **Staff** | `librarian` | `lib123` | Librarian |
| **User** | `alice` | `alice123` | Member |
| **User** | `bob` | `bob123` | Member |
| **User** | `carol` | `carol123` | Member |

Start at http://localhost:8080 and choose a portal:

- **User Portal** → sign in with `alice / alice123` to browse and borrow books
- **Staff Portal** → sign in with `admin / admin123` to manage the library

---

## Running in an IDE

**IntelliJ IDEA**
1. `File → Open` → select the `LIB` folder → click **Trust Project** / **Open as Project**
2. Wait for Maven to import dependencies
3. Run `LibraryApplication` (green ▶ next to the class in `src/main/java/com/library/`)

**VS Code**
1. `File → Open Folder` → select the `LIB` folder
2. Install the **Extension Pack for Java** and **Spring Boot Extension Pack**
3. Run the main class or press `F5`

---

## Project Structure

```
src/main/java/com/library/
├── LibraryApplication.java      # entry point
├── config/                      # security, data seeder, global model advice
├── controller/                  # Spring MVC controllers
├── model/                       # JPA entities (Book, User, BookIssue, Fine, ...)
├── repository/                  # Spring Data repositories
├── security/                    # user details, auth success handler
└── service/                     # business logic (circulation, reports, PDF, ...)
src/main/resources/
├── templates/                   # Thymeleaf HTML pages (auth, books, fines, admin, ...)
├── static/css/style.css
└── application.properties
```

---

## Useful Commands

| Action | Command |
|---|---|
| Run the app | `mvn spring-boot:run` |
| Stop the app | `Ctrl+C` (or `pkill -f "com.library.LibraryApplication"`) |
| Compile only | `mvn compile` |
| Fresh start (reset DB) | `rm -rf data && mvn spring-boot:run` |
| H2 database console | http://localhost:8080/h2-console |
| H2 connection | JDBC URL `jdbc:h2:file:./data/library`, User `sa`, empty password |

---

## Troubleshooting

- **Port 8080 already in use** → stop the old instance: `pkill -f "com.library.LibraryApplication"` then run again.
- **"Invalid username or password"** → use the correct portal. A user account must sign in through the **User Portal**, staff through the **Staff Portal**.
- **Data looks wrong / want a clean start** → delete the `data/` folder and restart; it reseeds automatically.
