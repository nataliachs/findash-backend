# FinDash — Financial Aggregator & Ledger Platform

A scalable, full-stack personal finance platform inspired by Mint and YNAB. FinDash aggregates linked accounts, tracks spending, manages bills and loans, and maintains a real-time double-entry ledger — all backed by a microservices architecture on AWS.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [API Reference](#api-reference)
- [Environment Variables](#environment-variables)
- [Roadmap](#roadmap)

---

## Overview

FinDash is a portfolio project that mirrors real-world fintech infrastructure. It is built with a microservices architecture where each financial domain (users, wallets, ledger, billing) is owned by an independent service with its own isolated database.

The frontend is an Angular 17 SPA that communicates with all backend services via RESTful APIs, secured using JWT authentication.

---

## Tech Stack

### Frontend
| Technology | Purpose |
|---|---|
| Angular 17+ | SPA framework (Standalone Components) |
| Angular Signals | Reactive local state management |
| Tailwind CSS | Utility-first styling |
| TypeScript | Type-safe development |
| RxJS | HTTP and async handling |

### Backend
| Technology | Purpose |
|---|---|
| Java 17 | Primary language |
| Spring Boot 3.2 | Microservices framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | ORM and database abstraction |
| Hibernate | JPA implementation |
| JWT (JJWT 0.12.6) | Stateless token-based auth |
| BCrypt | Password hashing |
| Gradle (Groovy) | Build tool |

### Data & Infrastructure
| Technology | Purpose |
|---|---|
| PostgreSQL 16 | Primary relational database (one per service) |
| AWS ECS (Fargate) | Container compute (planned) |
| AWS RDS | Managed PostgreSQL in production (planned) |
| AWS SNS / SQS | Async messaging — Saga pattern (planned) |
| AWS API Gateway | Single entry point for all services (planned) |
| AWS EventBridge | Scheduled billing cron jobs (planned) |
| Docker | Containerization (planned) |

---

## Architecture

```
Angular SPA (port 4200)
         │
         │  REST HTTP + JWT
         ▼
API Gateway / ALB
         │
         ├──▶ User Service    (port 8080) ──▶ findash_users   DB
         ├──▶ Wallet Service  (port 8081) ──▶ findash_wallet  DB
         ├──▶ Ledger Service  (port 8082) ──▶ findash_ledger  DB
         └──▶ Billing Service (port 8083) ──▶ findash_billing DB
                    │
                    │  Async events (SNS/SQS)
                    ▼
         BillPaidEvent ──▶ Ledger Service (record transaction)
                      ──▶ Billing Service (mark bill paid)
```

### Communication Patterns
- **Synchronous** — REST HTTP calls between Angular and services, and between services when an immediate response is required (e.g. balance check before transfer)
- **Asynchronous** — AWS SNS/SQS events for non-blocking operations (e.g. recording ledger entries after a payment)

---

## Features

### Authentication
- User registration with BCrypt password hashing
- JWT-based login with 24-hour token expiry
- Route guard protecting all authenticated routes
- HTTP interceptor auto-attaching JWT to every request
- Client-side form validation (email format, password strength, username rules)
- Error banners for invalid credentials and duplicate accounts

### Dashboard
- Total Net Worth computed dynamically from all account balances minus total debt
- Linked Accounts ribbon with horizontal scroll — shows institution, masked account number, and balance
- Upcoming Bills widget with one-click payment recording
- Active Loans tracker with visual progress bars showing paid vs remaining
- Global Ledger with CREDIT, DEBIT, and TRANSFER transaction types
- Record Transfer modal implementing double-entry bookkeeping
- Real-time balance updates after every transaction

---

## Project Structure

```
findash/                          ← root
├── findash-frontend/             ← Angular SPA
│   └── src/app/
│       ├── auth/
│       │   └── auth.component.ts
│       ├── dashboard/
│       │   └── dashboard.component.ts
│       ├── guards/
│       │   └── auth.guard.ts
│       ├── interceptors/
│       │   └── auth.interceptor.ts
│       ├── services/
│       │   └── auth.service.ts
│       ├── app.config.ts
│       └── app.routes.ts
│
└── user-service/                 ← Spring Boot microservice
    └── src/main/java/com/findash/userservice/
        ├── controller/
        │   ├── AuthController.java
        │   └── UserController.java
        ├── dto/
        │   ├── LoginRequest.java
        │   ├── RegisterRequest.java
        │   └── UserResponse.java
        ├── model/
        │   └── User.java
        ├── repository/
        │   └── UserRepository.java
        ├── security/
        │   ├── JwtUtil.java
        │   └── SecurityConfig.java
        └── service/
            ├── AuthService.java
            └── UserService.java
```

---

## Getting Started

### Prerequisites

Make sure you have the following installed:

| Tool | Version | Download |
|---|---|---|
| Node.js | 18+ | [nodejs.org](https://nodejs.org) |
| Angular CLI | 17+ | `npm install -g @angular/cli@17` |
| Java JDK | 17 | [adoptium.net](https://adoptium.net) |
| PostgreSQL | 16 | [postgresql.org](https://postgresql.org/download) |
| IntelliJ IDEA | Any | [jetbrains.com](https://jetbrains.com/idea) |

---

### Backend Setup

**1. Clone the repository and open `user-service` in IntelliJ**

**2. Set up PostgreSQL**

Connect to PostgreSQL and run:

```sql
CREATE DATABASE findash_users;
CREATE USER findash_user WITH PASSWORD 'findash123';
GRANT ALL PRIVILEGES ON DATABASE findash_users TO findash_user;
\c findash_users
GRANT ALL ON SCHEMA public TO findash_user;
```

**3. Configure `application.properties`**

Open `src/main/resources/application.properties`:

```properties
spring.application.name=user-service
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/findash_users
spring.datasource.username=findash_user
spring.datasource.password=findash123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

app.jwt.secret=your-secret-key-minimum-32-characters-long
app.jwt.expiration=86400000
```

**4. Run the service**

Click the green Run button in IntelliJ or press `Shift + F10`.

You should see:
```
Started UserServiceApplication in X seconds
Tomcat started on port 8080
```

**5. Test the API with Postman**

Register a new user:
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "fullName": "Alex Johnson",
  "username": "alexj",
  "email": "alex@test.com",
  "password": "Secret123!"
}
```

Login:
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "alex@test.com",
  "password": "Secret123!"
}
```

Get current user (requires JWT):
```
GET http://localhost:8080/api/users/me
Authorization: Bearer <your_token_here>
```

---

### Frontend Setup

**1. Navigate to the frontend directory**

```bash
cd findash-frontend
```

**2. Install dependencies**

```bash
npm install
```

**3. Install Tailwind CSS**

```bash
npm install -D tailwindcss@3 postcss autoprefixer
npx tailwindcss init
```

Update `tailwind.config.js`:
```js
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: { extend: {} },
  plugins: []
}
```

Add to `src/styles.css`:
```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

**4. Run the development server**

```bash
ng serve
```

Open [http://localhost:4200](http://localhost:4200) in your browser.

---

## API Reference

### User Service (port 8080)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Register a new user |
| POST | `/api/auth/login` | None | Login and receive JWT |
| GET | `/api/users/me` | JWT | Get current user profile |

### Wallet Service (port 8081) — planned

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/accounts` | JWT | Get all linked accounts |
| POST | `/api/accounts` | JWT | Add a new account |
| DELETE | `/api/accounts/:id` | JWT | Remove an account |
| POST | `/api/accounts/transfer` | JWT | Transfer between accounts |

### Ledger Service (port 8082) — planned

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/transactions` | JWT | Get all transactions |
| POST | `/api/transactions` | JWT | Record a transaction |

### Billing Service (port 8083) — planned

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/bills` | JWT | Get all upcoming bills |
| POST | `/api/bills` | JWT | Add a new bill |
| POST | `/api/bills/:id/pay` | JWT | Record a bill payment |

---

## Environment Variables

Never commit sensitive values to version control. For local development use `application.properties`. For production use environment variables or AWS Secrets Manager.

| Variable | Description |
|---|---|
| `spring.datasource.url` | PostgreSQL connection URL |
| `spring.datasource.username` | Database username |
| `spring.datasource.password` | Database password |
| `app.jwt.secret` | JWT signing secret (min 32 characters) |
| `app.jwt.expiration` | Token expiry in milliseconds (86400000 = 24h) |

---

## Roadmap

```
Phase 1 — Auth (complete)
  ✅ User registration and login
  ✅ JWT token generation and validation
  ✅ Angular route guard
  ✅ HTTP interceptor
  ✅ Dashboard UI with mock data

Phase 2 — Wallet Service (in progress)
  ⬜ Account management API
  ⬜ Balance tracking
  ⬜ Wire Angular dashboard to real accounts

Phase 3 — Ledger Service
  ⬜ Transaction recording API
  ⬜ CREDIT, DEBIT, TRANSFER types
  ⬜ Idempotency keys

Phase 4 — Billing Service
  ⬜ Bill management API
  ⬜ Scheduled payments via EventBridge
  ⬜ Saga pattern with SNS/SQS

Phase 5 — Production Readiness
  ⬜ Docker containers for all services
  ⬜ AWS ECS deployment
  ⬜ AWS RDS for production databases
  ⬜ CI/CD with GitHub Actions
  ⬜ AWS CloudWatch monitoring
  ⬜ Email verification and password reset
```

---

## Author

Built as a portfolio project demonstrating microservices architecture, JWT authentication, and modern full-stack development practices aligned with production fintech systems.
