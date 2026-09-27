# Rehabilitation Baseline Document: Campus Booking System

**Date:** September 27, 2026  
**Active Branch:** `develop`  
**Git HEAD:** `3513db4` (Updated Readme)  
**Status:** Pre-Rehabilitation Architectural & Codebase Audit  

---

## 1. Executive Summary

This document establishes the official technical baseline for rehabilitating the **Campus Booking System (Office Hours Booking System)**. The system is designed to facilitate scheduling of office hours between students and faculty, handle capacity-based slot booking and waitlist promotion, and provide administrative oversight.

The audit was performed on branch `develop` without modifying application code. Both the frontend and backend were inspected, built, and evaluated against design specs, API contracts, entity schemas, and operational assumptions.

---

## 2. Current Architecture

The system is organized as a decoupled two-tier client-server web application:

```
[ Client Browser ]
        │
   (HTTPS / HTTP)
   Vite + React SPA (Port 5173)
        │  Authorization: Bearer <JWT>
        ▼
[ Spring Boot REST API ] (Port 8080)
   ├── Security Filter Chain (Stateless JWT Auth)
   ├── Controllers (Thin HTTP / Validation layer)
   ├── Service Layer (Business logic, Transactions)
   └── Spring Data JPA / Hibernate Layer
        │
   (JDBC / MySQL 8)
        ▼
[ Relational Database (MySQL 8 / H2 in-memory test) ]
```

### Module / Package Structure
- **Backend (`/backend`)**:
  - `backend.common`: Base entity, global user entity, core enums (`Role`, `BookingStatus`, `SlotStatus`, `WaitlistStatus`), user repository.
  - `backend.security`: JWT filter, token provider, `UserPrincipal`, `CustomUserDetailsService`, `SecurityConfig`.
  - `backend.auth`: Authentication controllers, DTOs, service (registration, login, `/me`).
  - `backend.student`: Student profile controller, DTOs, repository, service.
  - `backend.professor`: Professor profiles and slots management, DTOs, repository, services (`ProfessorService`, `SlotService`).
  - `backend.booking`: Slot booking management, pessimistic locking, history, cancellation, waitlist dispatch.
  - `backend.waitlist`: Waitlist queue management, FIFO position assignment, promotion on booking cancellation, resequencing.
  - `backend.admin`: Administrative overview reporting, user activation/deactivation, force booking cancellation.
  - `backend.notification`: Decoupled notification service abstraction with SLF4J console logger implementation.
  - `backend.config`: CORS configuration (`CorsConfig`), stub configs (`OpenApiConfig`, `PasswordEncoderConfig`).
  - `backend.exception`: Centralized exception classes and `@RestControllerAdvice` error handler.
- **Frontend (`/frontend`)**:
  - `index.html`: Single-page entry point loading Vite bundle.
  - `src/main.jsx`: Monolithic component containing all views, forms, authentication state, and API client.
  - `src/styles.css`: Custom utility and component stylesheet combined with Bootstrap CSS import.
- **Documentation & Tools (`/docs`, `/postman`)**:
  - `docs/Postman_API_Testing_Guide.md`: Endpoint guide and usage instructions.
  - Empty stub files (`docs/architecture.md`, `docs/api-design.md`, `docs/database-schema.md`, `docs/meeting-notes.md`, `postman/BookingSystem.postman_collection.json`).

---

## 3. Frontend Stack

| Dimension | Current Implementation | Notes / Observations |
| :--- | :--- | :--- |
| **Framework** | React 19 (`react`, `react-dom`) | Single-file architecture in `src/main.jsx` |
| **Build Tool** | Vite 8.2 (`@vitejs/plugin-react`) | Fast HMR and bundle compilation |
| **Styling** | Custom CSS + Bootstrap 5 (`bootstrap/dist/css/bootstrap.min.css`) | Bootstrap imported but minimally utilized; custom CSS handles 95% of layout |
| **Routing** | None (Component state tabs: `book`, `bookings`, `profile`, `schedule`, `create`, `overview`, `users`) | Page refreshes reset state back to default tab; no browser history or deep linking |
| **State Management**| Local React component state (`useState`, `useCallback`, `useMemo`) | State stored in `localStorage` under `ohs.session` |
| **HTTP Client** | Native `fetch` wrapper (`api()` function) | Handles token headers and basic error unwrapping |
| **Testing** | None (0 test files, no test runner) | No unit tests, component tests, or E2E tests |
| **Production Build** | `npm run build` generates 210 KB JS + 237 KB CSS | Builds cleanly without syntax errors |

---

## 4. Backend Stack

| Dimension | Current Implementation | Notes / Observations |
| :--- | :--- | :--- |
| **Language / Runtime** | Java 17 (tested on OpenJDK 17.0.18) | Compiles cleanly with Maven 3.9+ |
| **Framework** | Spring Boot (Starter Parent 4.1.0/Spring 6 + Jakarta EE) | Modern Jakarta namespaces (`jakarta.persistence.*`, `jakarta.validation.*`) |
| **Security** | Spring Security 6 (`@EnableMethodSecurity`) + JJWT 0.12.6 | HMAC-SHA256 stateless tokens |
| **Data Access** | Spring Data JPA + Hibernate ORM 7.4.1 | Repositories with JPQL and derived query methods |
| **Database Driver** | MySQL Connector/J (`com.mysql:mysql-connector-j`) | H2 database used for test scope |
| **Utilities** | Project Lombok | Used for `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor` |
| **Build Tooling** | Maven Wrapper (`./mvnw`) + `pom.xml` | **Broken wrapper**: `.mvn/wrapper/` is missing; system `mvn` required |
| **Testing** | JUnit 5 + Spring Boot Test Starter | Exactly 1 smoke test (`contextLoads()`) |

---

## 5. Database & Entity Architecture

### Relational Schema (Hibernate-Managed / DDL)

1. **`users`** (`backend.common.entity.User` extends `BaseEntity`):
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `created_at`: `DATETIME NOT NULL`
   - `updated_at`: `DATETIME NOT NULL`
   - `full_name`: `VARCHAR(150) NOT NULL`
   - `email`: `VARCHAR(150) NOT NULL UNIQUE`
   - `password_hash`: `VARCHAR(255) NOT NULL`
   - `is_active`: `VARCHAR(255) NOT NULL` ⚠️ *(CRITICAL MAPPING BUG: this column stores `Role`, see Section 11)*
   - `active`: `BOOLEAN NOT NULL` *(stores actual boolean active status)*

2. **`student_profiles`** (`backend.student.model.StudentProfile` extends `BaseEntity`):
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `user_id`: `BIGINT NOT NULL UNIQUE` (FK -> `users.id`)
   - `roll_no`: `VARCHAR(50) NOT NULL UNIQUE`
   - `year_of_study`: `INT` (Valid range 1-8)

3. **`professor_profiles`** (`backend.professor.model.ProfessorProfile` extends `BaseEntity`):
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `user_id`: `BIGINT NOT NULL UNIQUE` (FK -> `users.id`)
   - `department`: `VARCHAR(100) NOT NULL`
   - `office_location`: `VARCHAR(150)`
   - `bio`: `VARCHAR(1000)`

4. **`slots`** (`backend.professor.model.Slot` extends `BaseEntity`):
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `professor_id`: `BIGINT NOT NULL` (FK -> `users.id`)
   - `slot_date`: `DATE NOT NULL`
   - `start_time`: `TIME NOT NULL`
   - `end_time`: `TIME NOT NULL`
   - `capacity`: `INT NOT NULL` (min 1)
   - `booked_count`: `INT NOT NULL` (default 0)
   - `status`: `VARCHAR(20) NOT NULL` (`OPEN`, `FULL`, `CANCELLED`, `COMPLETED`)

5. **`bookings`** (`backend.booking.model.Booking` extends `BaseEntity`):
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `student_id`: `BIGINT NOT NULL` (FK -> `users.id`)
   - `slot_id`: `BIGINT NOT NULL` (FK -> `slots.id`)
   - `status`: `VARCHAR(20) NOT NULL` (`BOOKED`, `CANCELLED`, `COMPLETED`)
   - `booked_at`: `DATETIME NOT NULL`
   - `cancelled_at`: `DATETIME`

6. **`waitlist_entries`** (`backend.waitlist.model.WaitlistEntry` extends `BaseEntity`):
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `student_id`: `BIGINT NOT NULL` (FK -> `users.id`)
   - `slot_id`: `BIGINT NOT NULL` (FK -> `slots.id`)
   - `position`: `INT NOT NULL` (1-indexed FIFO)
   - `status`: `VARCHAR(20) NOT NULL` (`WAITING`, `PROMOTED`, `CANCELLED`)
   - `promoted_at`: `DATETIME`
   - Unique Constraint: `uq_waitlist_student_slot` (`student_id`, `slot_id`)

### Concurrency & Locking Strategy
- **Pessimistic Locking**: `SlotRepository.findByIdForUpdate(slotId)` uses `@Lock(LockModeType.PESSIMISTIC_WRITE)`. This prevents race conditions during slot booking, waitlist entry generation, and automatic waitlist promotion.

---

## 6. Authentication & Roles

### Authentication Mechanism
- **JWT (Stateless Bearer Tokens)**:
  - Generated on `POST /api/auth/login`.
  - Signed with HMAC-SHA key from `app.jwt.secret` (minimum 32 bytes).
  - Default expiration: 86,400 seconds (24 hours).
  - Handled by `JwtAuthenticationFilter` on each HTTP request before `UsernamePasswordAuthenticationFilter`.
  - Password hashing: `BCryptPasswordEncoder` (strength 10).

### Role-Based Access Control (RBAC)
Three distinct roles defined in enum `Role`:
1. **`STUDENT`**:
   - Can register through public auth endpoint with roll number and year of study.
   - Can search professors and view available slots.
   - Can create bookings and be waitlisted.
   - Can view personal booking history and waitlist status.
   - Can cancel personal bookings and leave waitlists.
   - Can update personal student profile.
2. **`PROFESSOR`**:
   - Can register through public auth endpoint with department name.
   - Can create, edit, and soft-cancel personal availability slots.
   - Can view student booking roster and waitlist for owned slots.
   - Cannot self-register with student profile or book student slots.
3. **`ADMIN`**:
   - Cannot self-register (attempting `POST /api/auth/register` with role `ADMIN` returns `403 Forbidden`).
   - Must be seeded directly in database.
   - Can view system metrics and reports (`/api/admin/reports`).
   - Can view all users, activate/deactivate user accounts.
   - Can force-cancel any booking in the system.

---

## 7. API Surface Specification

| Endpoint | Method | Role / Auth | Description | Status Codes |
| :--- | :--- | :--- | :--- | :--- |
| `/api/auth/register` | `POST` | Public | Register new STUDENT or PROFESSOR account | 201, 400, 403, 409 |
| `/api/auth/login` | `POST` | Public | Authenticate user and return JWT + user info | 200, 400, 401, 403 |
| `/api/auth/me` | `GET` | Authenticated | Retrieve current user profile | 200, 401, 404 |
| `/api/students/me` | `GET` | `STUDENT` | Retrieve student profile (roll number, year) | 200, 401, 403, 404 |
| `/api/students/me` | `PUT` | `STUDENT` | Update student profile | 200, 400, 401, 403, 409 |
| `/api/professors` | `GET` | Authenticated | Search professors with pagination and name/department filters | 200, 401 |
| `/api/professors/{id}/slots` | `GET` | Authenticated | View upcoming bookable slots (`OPEN`, `FULL`) for a professor | 200, 401, 404 |
| `/api/professors/slots/me` | `GET` | `PROFESSOR` | View all upcoming slots owned by logged-in professor | 200, 401, 403 |
| `/api/professors/slots` | `POST` | `PROFESSOR` | Create a new availability slot | 201, 400, 401, 403, 409 |
| `/api/professors/slots/{id}` | `PUT` | `PROFESSOR` | Update slot date, times, or capacity (restricted if booked) | 200, 400, 401, 403, 404, 409 |
| `/api/professors/slots/{id}` | `DELETE`| `PROFESSOR` | Soft-cancel slot (status set to `CANCELLED`) | 204, 401, 403, 404 |
| `/api/professors/slots/{id}/bookings` | `GET` | `PROFESSOR`, `ADMIN` | View student bookings roster and waitlist for slot | 200, 401, 403, 404 |
| `/api/bookings` | `POST` | `STUDENT` | Book slot (or join waitlist if full) | 201 (Booked), 202 (Waitlisted), 400, 401, 403, 404, 409 |
| `/api/bookings/me` | `GET` | `STUDENT` | View personal booking history and active waitlist entries | 200, 401, 403 |
| `/api/bookings/{id}` | `DELETE`| `STUDENT`, `ADMIN` | Cancel confirmed booking and promote next waitlisted student | 204, 401, 403, 404, 409 |
| `/api/bookings/waitlist/{id}`| `DELETE`| `STUDENT` | Leave waitlist queue and resequence remaining entries | 204, 401, 403, 404, 409 |
| `/api/admin/users` | `GET` | `ADMIN` | List all registered users | 200, 401, 403 |
| `/api/admin/users/{id}/deactivate` | `PATCH` | `ADMIN` | Deactivate user account | 204, 401, 403, 404 |
| `/api/admin/users/{id}/activate` | `PATCH` | `ADMIN` | Reactivate user account | 204, 401, 403, 404 |
| `/api/admin/reports` | `GET` | `ADMIN` | Aggregate metrics (users, bookings, waitlists, utilization) | 200, 401, 403 |
| `/api/admin/bookings/{id}` | `DELETE`| `ADMIN` | Force-cancel booking with waitlist promotion | 204, 401, 403, 404, 409 |

---

## 8. Existing User Workflows

### Flow 1: Student Registration and Booking
1. User enters full name, college email, password, roll number, and year of study.
2. System creates `User` and `StudentProfile` in a single transaction.
3. User logs in, receives JWT, and enters student dashboard.
4. User selects professor from dropdown; frontend requests `/api/professors/{id}/slots`.
5. Available slots render with capacity count.
6. User clicks slot and confirms booking (`POST /api/bookings`).
7. If space exists, receives `201 Created` with booking confirmation. If full, receives `202 Accepted` with waitlist position.
8. Confirmed booking appears in "My bookings"; waitlisted item appears in "Waitlist" table.

### Flow 2: Professor Slot Lifecycle
1. Professor registers with department name and logs in.
2. Under "Add a slot", selects date, start time, end time, and capacity (`POST /api/professors/slots`).
3. Under "My schedule", professor views all scheduled slots.
4. Clicking "Roster" shows confirmed students (name + roll number) and waitlisted students with positions.
5. Slot can be edited (capacity increased/decreased, date changed if unbooked) or cancelled.

### Flow 3: Waitlist Promotion Lifecycle
1. Slot with capacity 1 is booked by Student A (`bookedCount = 1`, status `FULL`).
2. Student B requests booking on same slot; system creates `WaitlistEntry` at position 1.
3. Student A cancels their booking (`DELETE /api/bookings/{id}`).
4. System decrements `bookedCount`, finds Student B at position 1, creates confirmed `Booking` for Student B, increments `bookedCount`, sets `WaitlistEntry` status to `PROMOTED`, and logs notification.
5. Student B's dashboard now reflects confirmed booking.

### Flow 4: Administrative Management
1. Seeded admin logs in with credentials.
2. Admin views aggregate stats (user breakdown, active bookings, waitlists, slot utilization percentage).
3. Admin views user roster and can toggle activation status (preventing deactivated users from logging in).
4. Admin can force-cancel problematic bookings by ID.

---

## 9. Existing Tests & Test Infrastructure

| Component | Test File | Test Cases | Execution Result |
| :--- | :--- | :--- | :--- |
| **Backend** | `backend.BackendApplicationTests` | 1 test: `contextLoads()` | **Passes** (5.35s, H2 in-memory DB) |
| **Frontend** | None | 0 tests | N/A |
| **Integration** | None | 0 tests | N/A |
| **Security / Auth** | None | 0 tests | N/A |
| **Postman Collection** | `postman/BookingSystem.postman_collection.json` | 0 requests (0-byte file) | N/A |

### Test Gap Analysis
- There are no tests verifying:
  - Registration validation and duplicate email/roll number constraints.
  - Authentication success/failure and token expiration.
  - Concurrency/race conditions during slot booking.
  - Automatic waitlist promotion on booking cancellation.
  - Role-based authorization on restricted endpoints.
  - Slot overlap detection.
  - Frontend component rendering, user interactions, or API error handling.

---

## 10. Deployment Assumptions & Environment

- **Local Hostname / Ports**:
  - Backend runs on `http://localhost:8080`.
  - Frontend runs on `http://localhost:5173` (Vite dev server default).
- **Environment Variables**:
  - `DB_URL`: default `jdbc:mysql://localhost:3306/office_hours_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
  - `DB_USERNAME`: default `root`
  - `DB_PASSWORD`: default empty string
  - `APP_JWT_SECRET`: default `change-this-development-secret-key-to-at-least-32-bytes`
  - `APP_JWT_EXPIRATION_SECONDS`: default `86400`
  - `VITE_API_URL`: default `http://localhost:8080`
- **Database Initialization**:
  - `spring.jpa.hibernate.ddl-auto=update` is configured in `application.properties`.
  - Schema is automatically updated by Hibernate upon application boot.

---

## 11. Obvious Bugs & Defects

### Bug 1: Catastrophic JPA Column Mapping Swap in `User.java`
In `backend/src/main/java/backend/common/entity/User.java`:
```java
    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;
```
- The annotation `@Column(name = "is_active", nullable = false)` is placed on the `Role role` field, while the boolean `active` field has no name specified (defaulting to column `active`).
- In MySQL, this creates a column named `is_active` containing string values (`"STUDENT"`, `"PROFESSOR"`, `"ADMIN"`).
- Any standard database query or external tooling expecting `is_active` to be a boolean will fail or produce incorrect results.

### Bug 2: Broken Maven Wrapper (`./mvnw`)
Executing `./mvnw test-compile` fails immediately:
```
./mvnw: line 117: ./.mvn/wrapper/maven-wrapper.properties: No such file or directory
cannot read distributionUrl property in ./.mvn/wrapper/maven-wrapper.properties
```
- The directory `.mvn/wrapper/` was omitted from git or lost during setup. Only developers with a pre-installed system Maven (`mvn`) can currently compile or run the backend.

### Bug 3: Strict Hardcoded CORS Origin
In `backend/src/main/java/backend/config/CorsConfig.java`:
```java
configuration.setAllowedOrigins(List.of("http://localhost:5173"));
```
- If the Vite frontend is started when port 5173 is occupied, Vite binds to port 5174. Any requests from 5174 (or staging/preview hosts) are immediately rejected by Spring Security CORS with `403 Forbidden`.

### Bug 4: Inadequate Slot Overlap Validation
In `backend/src/main/java/backend/professor/service/SlotServiceImpl.java`:
- The method `slotRepository.existsByProfessorIdAndSlotDateAndStartTime(professorId, date, startTime)` only checks for an exact match on `startTime`.
- If a professor creates a slot from 10:00 to 11:00, and attempts to create another slot from 10:30 to 11:30, the system permits it, resulting in overlapping slots.

### Bug 5: Orphan Bookings on Slot Cancellation
When a professor calls `DELETE /api/professors/slots/{slotId}` (`cancelSlot` in `SlotServiceImpl.java`):
- The slot status changes to `CANCELLED`.
- However, existing confirmed bookings and waitlist entries for that slot are **not updated or cancelled**, and affected students receive no notification. Students still see the booking as `BOOKED` in their history.

### Bug 6: Strict `@Future` Date Constraint Rejects "Today"
In `backend/src/main/java/backend/professor/dto/SlotRequest.java`:
```java
@NotNull(message = "slotDate is required")
@Future(message = "slotDate must be in the future")
private LocalDate slotDate;
```
- `@Future` rejects `LocalDate.now()`. A professor cannot schedule office hours for later on the current day.
- Furthermore, if a professor attempts to update capacity on the day of the slot (`PUT /api/professors/slots/{id}`), the request fails Bean Validation because the slot date is now "today".

### Bug 7: Disconnected "Reason for Visit" in UI
In `frontend/src/main.jsx`:
- The booking modal prompts the student for an optional "Reason for visit".
- The UI contains the helper text: *"This note stays in your browser; the current backend booking API accepts only a slot ID."*
- `BookingRequest.java` and entity `Booking.java` have no `reason` field, so student input is silently discarded.

### Bug 8: Missing Professor Profile Management
- `StudentController` provides `GET /api/students/me` and `PUT /api/students/me`.
- `ProfessorController` has no corresponding `/me` profile endpoints. Professors cannot view or edit their department, office location, or bio after initial registration.

---

## 12. Technical Debt & Code Smells

1. **Dead & 0-Byte Ghost Files**:
   - `docs/architecture.md` (0 bytes)
   - `docs/api-design.md` (0 bytes)
   - `docs/database-schema.md` (0 bytes)
   - `docs/meeting-notes.md` (0 bytes)
   - `postman/BookingSystem.postman_collection.json` (0 bytes)
   - `backend/src/main/java/backend/config/OpenApiConfig.java` (0 bytes)
   - `backend/src/main/java/backend/config/PasswordEncoderConfig.java` (0 bytes)
   - `backend/src/main/java/backend/common/util/DateTimeUtil.java` (0 bytes)
   - `backend/src/main/java/backend/professor/mapper/SlotMapper.java` (0 bytes)
   - `backend/src/main/java/backend/common/exception/*` (5 files, 0 bytes; duplicated by `backend/exception/*`)

2. **In-Memory Streaming of Entire Database in Admin Reports**:
   In `AdminServiceImpl.getGlobalReport()`:
   ```java
   users.findAll().stream().filter(...)
   bookings.findAll().stream().filter(...)
   slots.findAll().stream().mapToLong(...)
   ```
   Loads every record into JVM memory rather than utilizing SQL `COUNT()` and aggregate queries.

3. **Monolithic Frontend Architecture**:
   - Entire frontend resides in a single 131-line minified-style file (`frontend/src/main.jsx`).
   - No component folder structure, no custom hooks, no modular CSS, and no type safety.

4. **Missing Production-Grade Git Ignore**:
   - `.gitignore` does not ignore `frontend/dist/` or `frontend/node_modules/`.

---

## 13. UI / UX Problems

1. **Primitive State-Based Navigation**:
   - Tabs are switched via React state (`tab === 'bookings' ? ...`).
   - Refreshing the browser page resets the view back to the default tab.
   - URLs do not update, preventing bookmarking, direct sharing, or browser back/forward navigation.

2. **Crude Slot Selection Grid**:
   - Available slots are displayed as a vertical list of buttons without calendar view, day grouping, or time-of-day filtering.
   - Professors with dozens of slots will overwhelm the view.

3. **Lack of Real-Time Feedback or Polling**:
   - Waitlist positions and slot availability do not update unless the user manually triggers a full component refresh.

4. **Form Validation UX**:
   - Backend errors are rendered as raw strings in a bottom floating toast notification (`Notice` component) rather than contextual inline form field indicators.

5. **Aesthetic Baseline**:
   - Basic corporate-minimalist aesthetic using standard Bootstrap reset styles and simple bordered cards. Does not match modern, dynamic web application standards.

---

## 14. Things That Must NOT Be Changed

To preserve system stability and team alignment during rehabilitation:
1. **Branch Hygiene**: Work strictly on `develop`. Never push or merge directly to `main`.
2. **REST API Contract Stability**: The existing request/response JSON payload shapes defined in `docs/Postman_API_Testing_Guide.md` must not be broken arbitrarily.
3. **Pessimistic Concurrency Model**: The `PESSIMISTIC_WRITE` locking on `SlotRepository.findByIdForUpdate()` must remain intact to prevent double-booking.
4. **Role & Permission Guardrails**: The `@PreAuthorize` security model distinguishing `STUDENT`, `PROFESSOR`, and `ADMIN` must remain enforced.
5. **Waitlist FIFO Promotion Logic**: The automated promotion sequence upon booking cancellation must continue to honor earliest position order.
6. **Stateless JWT Architecture**: Do not convert the application to stateful session cookies; maintain Bearer token authentication.

---

## 15. Recommended Order of Work

```
Phase 1: Tooling & Stability Foundation
  ├── Fix Maven wrapper (.mvn/wrapper/maven-wrapper.properties)
  ├── Clean up 0-byte ghost files and duplicate exception packages
  ├── Update .gitignore (frontend/dist, frontend/node_modules)
  └── Establish automated test suites (Backend integration tests + Frontend tests)

Phase 2: Core Data & Domain Remediation
  ├── Correct User.java JPA column mappings (@Column(name = "role") vs @Column(name = "is_active"))
  ├── Implement comprehensive slot time overlap check in SlotService
  ├── Handle cascade / notifications on slot cancellation (cancel orphan bookings)
  ├── Fix SlotRequest validation (@FutureOrPresent) to permit today's slots
  └── Add Professor profile viewing/editing endpoints

Phase 3: Backend Performance & Architecture Hardening
  ├── Refactor AdminServiceImpl reporting to use database aggregate queries
  ├── Configurable CORS origins via environment variables
  ├── Optional: Add optional visit reason to Booking entity and DTOs
  └── Complete OpenAPI/Swagger documentation

Phase 4: Frontend Modernization & Architecture Overhaul
  ├── Modularize frontend into feature directories (auth, student, professor, admin, components, api)
  ├── Introduce proper routing (React Router) for persistent URLs and deep linking
  ├── Upgrade UI design system (modern typography, responsive calendar/grid view, interactive states)
  └── Connect Professor Profile management & inline validation
```
