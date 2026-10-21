# Campus Booking System

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Live Demo](https://img.shields.io/badge/Live_Demo-Online-success.svg)](https://officehour-roan.vercel.app/)

A full-stack office-hours booking platform for a college campus. Students can find and reserve time with professors, professors can publish and manage availability, and administrators can oversee users and booking activity.

## Live application

[Open the Campus Booking System](https://officehour-roan.vercel.app/)

## Features

- **Role-based accounts** for students, professors, and administrators, with JWT authentication.
- **Office-hour scheduling:** professors create time slots with a capacity; students browse and book available slots.
- **Booking management:** students can review and cancel bookings; professors can manage their schedules and see bookings.
- **Waitlists:** students can join full slots and are promoted when a place becomes available.
- **Conflict checks** to prevent overlapping slots and overbooking.
- **Administration:** view system-wide reports, manage accounts, and cancel bookings when needed.
- **Responsive React interface** with role-specific pages for students, professors, and administrators.

## Technology

| Area | Stack |
| --- | --- |
| Frontend | React, React Router, Vite, JavaScript, CSS |
| Backend | Java 17, Spring Boot, Spring Security, Spring Data JPA, Hibernate |
| Database | MySQL 8 |
| Authentication | JWT bearer tokens |
| API testing | Postman collection and guide in `postman/` and `docs/` |

## Repository layout

```text
backend/    Spring Boot REST API, domain logic, persistence, and tests
frontend/   React single-page application
postman/    Postman collection
docs/       API guide, design notes, and engineering documentation
```

## Run locally

### Requirements

- Java 17 or later
- MySQL 8
- Node.js and pnpm

Create a MySQL database for the application:

```sql
CREATE DATABASE office_hours_booking;
```

### Start the backend

From the repository root, set your database credentials and a private JWT secret in your shell. Keep real credentials out of source control.

```bash
export DB_URL='jdbc:mysql://localhost:3306/office_hours_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='your-password'
export APP_JWT_SECRET='replace-with-a-random-secret-at-least-32-bytes-long'

cd backend
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080` by default. The backend reads `PORT` if you need a different port. Hibernate updates the local schema by default; configure the database before starting the application.

### Start the frontend

In another terminal, from the repository root:

```bash
cd frontend
pnpm install
pnpm dev
```

The Vite development server prints the local URL when it starts. The frontend uses `http://localhost:8080` for the API by default. To point it at another API URL, create `frontend/.env.local`:

```dotenv
VITE_API_URL=http://localhost:8080
```

Restart Vite after changing environment variables.

### Accounts

Students and professors can register through the application. Administrator accounts cannot self-register; an administrator must be provisioned in the database for local use. See the [Postman API testing guide](docs/Postman_API_Testing_Guide.md) for the authentication flow and endpoint examples.

## Checks

Run the backend test suite:

```bash
cd backend
./mvnw test
```

Create a production frontend build:

```bash
cd frontend
pnpm install
pnpm build
```

## Documentation

### API and backend

- [Postman API testing guide](docs/Postman_API_Testing_Guide.md) — authentication workflow, request examples, and response formats.
- [Backend correctness audit](docs/BACKEND_CORRECTNESS_AUDIT.md) — domain rules, persistence behavior, and backend checks.
- [Final engineering review](docs/FINAL_ENGINEERING_REVIEW.md) — overall architecture, engineering review, and follow-up notes.

### Product, design, and frontend

- [Design system](docs/DESIGN_SYSTEM.md) — visual tokens and interface conventions.
- [UX audit](docs/UX_AUDIT.md) — user experience review and findings.
- [UI ecosystem](docs/UI_ECOSYSTEM.md) — interface structure and component overview.
- [Frontend upgrade notes](docs/FRONTEND_UPGRADE.md) — frontend modernization notes.
- [Frontend code review](docs/FRONTEND_CODE_REVIEW.md) — implementation review and findings.
- [Frontend fixes](docs/FRONTEND_FIXES.md) — recorded frontend corrections.
- [Visual QA report](docs/VISUAL_QA.md) — visual inspection notes.
- [Final frontend QA report](docs/FINAL_FRONTEND_QA.md) — final frontend quality review.
- [Rehabilitation baseline](docs/REHABILITATION_BASELINE.md) — project baseline and improvement plan.

The repository also has placeholder files for [API design](docs/api-design.md), [architecture](docs/architecture.md), [database schema](docs/database-schema.md), and [meeting notes](docs/meeting-notes.md); those files are currently empty.

## Project team

| Name | Program |
| --- | --- |
| Ishaan Chavan | Computer Engineering |
| Vivaan Chauhan | Computer Engineering |
| Hritik Chauhan | Computer Engineering |
| Krish Choudhary | Computer Engineering |

## License

This project is licensed under the [MIT License](LICENSE).
