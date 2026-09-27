# Backend Correctness & Domain Audit

## Executive Summary

During Step 1 of the Campus Booking System rehabilitation, an in-depth audit of the backend domain logic, JPA entity mappings, concurrency safeguards, and API contracts was conducted. Several critical domain defects were confirmed and fixed with minimal, targeted changes:
1. **Critical JPA Column Mapping Defect in `User.java`**: `@Column(name = "is_active")` was placed on `Role role` instead of boolean `active`.
2. **Incomplete Slot Overlap Detection in `SlotRepository` / `SlotServiceImpl`**: Only identical start times were rejected, allowing overlapping slot intervals (such as 10:00–11:00 vs 10:30–11:30 or 09:30–10:30).
3. **Orphaned Bookings & Waitlist Entries on Slot Cancellation**: Cancelling a slot previously left associated bookings in `BOOKED` status and waitlist entries in `WAITING` status, creating ghost active reservations.
4. **Premature Same-Day Slot Rejection**: `@Future` on `SlotRequest.slotDate` rejected slots created for later today.
5. **Broken Maven Wrapper**: Missing `.mvn/wrapper/maven-wrapper.properties` prevented `./mvnw` execution.
6. **Obsolete Ghost Files**: Nine 0-byte unreferenced files in redundant package structures were safely removed.
7. **Rigid CORS Origin Configuration**: Hardcoded localhost origin was made configurable via property/environment variable.
8. **Waitlist State Guard on Cancelled Slots**: Guarded `joinWaitlist` and `promoteNext` against execution on cancelled or completed slots.

All 20 backend unit/integration regression tests pass against the H2 database, `./mvnw` functions cleanly, and the frontend production build compiles with zero errors.

---

## Current Repository State

- **Branch**: `develop` (verified, no branch merges or history rewrites).
- **Working Tree**: Targeted corrections applied to 5 backend source files, 9 empty ghost files removed, Maven wrapper restored, and 1 comprehensive regression test suite added.
- **Backend Build**: Clean compilation with JDK 17 and Maven 3.9.14 (`./mvnw`).
- **Frontend Build**: Clean production build (`npm run build` using Vite 8.2.0, 16 modules transformed).

---

## Verified Issues

### 1. User JPA Column Mapping Defect
- **Severity**: Critical (Data Integrity / Authentication & Authorization Hazard)
- **File/Location**: `backend/src/main/java/backend/common/entity/User.java` (lines 19–25)
- **Observed Behavior**: The `@Column(name = "is_active", nullable = false)` annotation was attached to `private Role role` rather than `private boolean active = true`. The `active` boolean field lacked an explicit column name annotation.
- **Root Cause**: Annotation placement typo during entity creation. In databases or schemas where `is_active` is expected to be boolean, Hibernate mapped enum string values (`STUDENT`, `PROFESSOR`, `ADMIN`) into `is_active` and created a separate `active` boolean column.
- **Fix Applied**: Adjusted column mappings so `role` maps to `@Column(name = "role", nullable = false)` and `active` maps to `@Column(name = "is_active", nullable = false)`.
- **Regression Test**: `DomainCorrectnessTests$UserColumnMappingTests`:
  - `roleFieldStoresEnumCorrectly()`: Verifies enum string round-trips correctly and is not mapped to `is_active`.
  - `deactivatedUserPreservesRole()`: Verifies deactivating an account does not corrupt role enum persistence.
  - `allRolesCoexist()`: Verifies STUDENT, PROFESSOR, and ADMIN persist cleanly alongside active/inactive states.

### 2. Incomplete Slot Overlap Detection
- **Severity**: High (Overbooking / Scheduling Conflict Defect)
- **File/Location**:
  - `backend/src/main/java/backend/professor/repository/SlotRepository.java`
  - `backend/src/main/java/backend/professor/service/SlotServiceImpl.java` (methods `createSlot` and `updateSlot`)
- **Observed Behavior**: The system only verified `existsByProfessorIdAndSlotDateAndStartTime`, which merely checked for identical start times. Overlapping intervals such as 10:00–11:00 and 10:30–11:30 or 09:30–10:30 were accepted without conflict.
- **Root Cause**: The repository query lacked time-range interval intersection logic (`newStart < existingEnd AND existingStart < newEnd`).
- **Fix Applied**: Added `existsOverlappingSlot` JPQL query in `SlotRepository` that checks for interval intersection while excluding `CANCELLED` slots and optionally excluding the current slot ID (for slot updates). Updated `createSlot` and `updateSlot` in `SlotServiceImpl` to invoke this check.
- **Regression Test**: `DomainCorrectnessTests$SlotOverlapTests` covering all required test vectors:
  - Test A: Existing 10:00–11:00 vs New 10:30–11:30 (partial overlap end) -> Rejected (`SlotOverlapException`).
  - Test B: Existing 10:00–11:00 vs New 09:30–10:30 (partial overlap start) -> Rejected (`SlotOverlapException`).
  - Test C: Existing 10:00–11:00 vs New 10:30–11:00 (subset overlap) -> Rejected (`SlotOverlapException`).
  - Test D: Existing 10:00–11:00 vs New 09:00–12:00 (superset overlap) -> Rejected (`SlotOverlapException`).
  - Test E: Existing 10:00–11:00 vs New 09:00–10:00 and 11:00–12:00 (adjacent slots) -> Allowed.
  - Different Date: Same time on different date -> Allowed.

### 3. Slot Cancellation Consistency (Orphaned Bookings & Waitlist)
- **Severity**: High (Domain Consistency / User Confusion)
- **File/Location**: `backend/src/main/java/backend/professor/service/SlotServiceImpl.java` (method `cancelSlot`)
- **Observed Behavior**: When a professor cancelled an office hours slot, the slot status was changed to `CANCELLED`, but existing bookings remained in `BOOKED` status and waitlist entries remained in `WAITING` status. Students retained active bookings for slots that were cancelled.
- **Root Cause**: `cancelSlot` only mutated the `Slot` entity and did not cascade state changes to active bookings or waitlist entries.
- **Fix Applied**: In `cancelSlot`, transitioned all active bookings (`BookingStatus.BOOKED`) for that slot to `BookingStatus.CANCELLED` with a timestamp, and transitioned all waiting entries (`WaitlistStatus.WAITING`) to `WaitlistStatus.CANCELLED`. Preserved historical rows (soft cancel).
- **Regression Test**: `DomainCorrectnessTests$SlotCancellationTests`:
  - `cancelSlotCascadesToBookings()`: Confirms active bookings transition to `CANCELLED` when slot is cancelled.
  - `cancelSlotCascadesToWaitlist()`: Confirms waitlist entries transition to `CANCELLED`.

### 4. Same-Day Slot Validation Rejection
- **Severity**: Medium (Functional Restriction)
- **File/Location**: `backend/src/main/java/backend/professor/dto/SlotRequest.java` (field `slotDate`)
- **Observed Behavior**: Creating or modifying a slot for later today was rejected with a validation error: `slotDate must be in the future`.
- **Root Cause**: `@Future` on `LocalDate` validates that `slotDate.isAfter(LocalDate.now())`, which strictly rejects `LocalDate.now()`.
- **Fix Applied**: Replaced `@Future` with `@FutureOrPresent(message = "slotDate must be today or in the future")`. Past date booking is already rejected at service layer in `BookingServiceImpl` via `slot.getSlotDate().isBefore(LocalDate.now())`.
- **Regression Test**: `DomainCorrectnessTests$SameDaySlotTests`:
  - `todaySlotAllowed()`: Confirms slots scheduled for today are accepted.
  - `futureDateSlotAllowed()`: Confirms future date slots continue to be accepted.

### 5. Missing Maven Wrapper Properties
- **Severity**: Medium (Build Reproducibility / Developer Ergonomics)
- **File/Location**: `backend/.mvn/wrapper/maven-wrapper.properties`
- **Observed Behavior**: Executing `./mvnw` failed with an error that `maven-wrapper.properties` was missing.
- **Root Cause**: The `.mvn/wrapper` directory had never been committed to git.
- **Fix Applied**: Created `.mvn/wrapper/maven-wrapper.properties` configured for Maven 3.9.14 (matching developer toolchain). Verified `./mvnw` now works standalone.
- **Regression Test**: Verified via `./mvnw -v` and `./mvnw test`.

### 6. Empty / Ghost Files Cluttering Package Structure
- **Severity**: Low (Maintenance / Confusion)
- **File/Location**: 9 zero-byte files in `backend/src/main/java/`
- **Observed Behavior**: Zero-byte placeholder files existed across redundant packages:
  - `backend/config/OpenApiConfig.java`
  - `backend/config/PasswordEncoderConfig.java`
  - `backend/common/util/DateTimeUtil.java`
  - `backend/professor/mapper/SlotMapper.java`
  - `backend/common/exception/ResourceNotFoundException.java`
  - `backend/common/exception/ConflictException.java`
  - `backend/common/exception/GlobalExceptionHandler.java`
  - `backend/common/exception/ForbiddenOperationException.java`
  - `backend/common/exception/ErrorResponse.java`
- **Root Cause**: Abandoned scaffolding; active implementations exist in `backend/exception/` and `backend/security/SecurityConfig.java`. None were referenced or imported.
- **Fix Applied**: Safely deleted the 9 unreferenced zero-byte files and emptied obsolete directories.
- **Regression Test**: Full compilation (`./mvnw test-compile`) verified zero missing references.

### 7. Hardcoded CORS Configuration
- **Severity**: Low (Deployment / Port Flexibility Defect)
- **File/Location**: `backend/src/main/java/backend/config/CorsConfig.java`
- **Observed Behavior**: Allowed origin was hardcoded to `http://localhost:5173`.
- **Root Cause**: Missing environment/property interpolation.
- **Fix Applied**: Configured `@Value("${app.cors.allowed-origins:http://localhost:5173}")` to allow comma-separated origins via environment or configuration without changing default security.

---

## Findings Not Confirmed

### Reason for Visit "Bug"
- **Finding**: Baseline noted that the frontend collects a "Reason for visit" that is not accepted by the backend API.
- **Verification**: The frontend UI in `frontend/src/main.jsx` explicitly contains an honest user notice directly below the input:
  `"This note stays in your browser; the current backend booking API accepts only a slot ID."`
- **Conclusion**: This is an intentional UI placeholder / deferred feature rather than a silent defect or misleading claim. In accordance with the operating instructions ("Do NOT redesign the frontend" and "prefer documenting it as deferred functionality"), no schema or frontend changes were made.

---

## Additional Bugs Discovered

### Cancelled Slot Waitlist Promotion Hazard
- **Severity**: Medium (Domain Logic Flaw)
- **File/Location**: `backend/src/main/java/backend/waitlist/service/WaitlistServiceImpl.java` (methods `joinWaitlist` and `promoteNext`)
- **Observed Behavior**: `promoteNext` only checked `if (slot.getBookedCount() >= slot.getCapacity()) return;`. It did not check whether `slot.getStatus() == SlotStatus.CANCELLED || slot.getStatus() == SlotStatus.COMPLETED`. Similarly, `joinWaitlist` did not guard against cancelled slots if invoked directly.
- **Root Cause**: Missing slot lifecycle status checks in `WaitlistServiceImpl`.
- **Fix Applied**:
  - In `promoteNext`: Added `if (slot.getStatus() == SlotStatus.CANCELLED || slot.getStatus() == SlotStatus.COMPLETED || slot.getBookedCount() >= slot.getCapacity()) return;`.
  - In `joinWaitlist`: Added `if (slot.getStatus() == SlotStatus.CANCELLED || slot.getStatus() == SlotStatus.COMPLETED) throw new ConflictException("This slot is no longer accepting waitlist entries");`.
- **Regression Test**:
  - `promoteNextOnCancelledSlotDoesNothing()`: Confirms no promotions or bookings occur on a cancelled slot.
  - `joinWaitlistOnCancelledSlotRejected()`: Confirms joining waitlist on a cancelled slot is rejected with `ConflictException`.

---

## Deferred Technical Debt

1. **In-Memory Aggregation in `AdminServiceImpl.getGlobalReport()`**:
   - `AdminServiceImpl` calls `users.findAll().stream()`, `slots.findAll().stream()`, and `bookings.findAll().stream()` to compute global statistics in memory.
   - For small/medium datasets, this produces correct results. For larger production datasets, this should be refactored into JPQL aggregate queries (`COUNT`, `SUM`). Left unchanged per the rule: "Do not perform a broad performance refactor unless necessary."
2. **Audit Logging & Async Notification Delivery**:
   - `ConsoleNotificationServiceImpl` logs notifications to stdout. A future phase should introduce proper persistent notification records or email/webhook dispatchers.
3. **Database Migration Tooling**:
   - The application relies on Hibernate `ddl-auto: update`. Flyway or Liquibase migrations should be introduced in a future operations/deployment phase.
4. **Backend Booking "Reason for Visit" Persistence**:
   - Adding a `reason` column to `bookings` and updating `BookingRequest` / `BookingResponse` should be scheduled for an explicit API enhancement phase when frontend designs are finalized.

---

## API / Domain Behavior Preserved

The following foundational patterns and contracts were verified and preserved intact:
1. **Pessimistic Locking Concurrency Guard**:
   - `SlotRepository.findByIdForUpdate` with `@Lock(LockModeType.PESSIMISTIC_WRITE)` is preserved to prevent race conditions during concurrent bookings.
2. **Booking & Waitlist Invariants**:
   - Capacity boundary: Slot flips from `OPEN` to `FULL` when booked count reaches capacity.
   - Waitlist transition: When a slot is `FULL`, `POST /api/bookings` returns HTTP 202 ACCEPTED with `WaitlistEntryResponse`.
   - Single active booking per slot per student: Duplicate booking attempts are rejected with HTTP 409 CONFLICT.
   - FIFO Waitlist promotion: When an active booking is cancelled, the highest priority waiting student (`position = 1`) is promoted to `BOOKED` and positions are resequenced.
3. **Role-Based Access Control**:
   - `@PreAuthorize` guards on all endpoints (`STUDENT`, `PROFESSOR`, `ADMIN`).
   - Slot ownership enforcement: Professors can only modify or cancel their own slots (`loadOwnedSlot`).
   - Booking ownership enforcement: Students can only cancel their own bookings (Admins can force cancel).
4. **API Response Shapes**:
   - All DTO shapes (`SlotResponse`, `BookingResponse`, `WaitlistEntryResponse`, `GlobalReportResponse`, `ProfessorResponse`) remain unchanged.

---

## Test Results

### 1. Backend Compilation
- **Command**: `./mvnw test-compile`
- **Result**: BUILD SUCCESS (0 errors, 73 source files compiled cleanly with Java 17).

### 2. Backend Test Suite
- **Command**: `./mvnw test`
- **Result**: BUILD SUCCESS
  - `BackendApplicationTests.contextLoads`: PASSED
  - `DomainCorrectnessTests`: 20 tests PASSED, 0 failures, 0 errors, 0 skipped.

### 3. Regression Tests Breakdown
- `DomainCorrectnessTests$UserColumnMappingTests` (3 tests):
  - `roleFieldStoresEnumCorrectly`: PASSED
  - `deactivatedUserPreservesRole`: PASSED
  - `allRolesCoexist`: PASSED
- `DomainCorrectnessTests$SlotOverlapTests` (7 tests):
  - `overlappingEndOverlap` (Case A): PASSED
  - `overlappingStartOverlap` (Case B): PASSED
  - `subsetOverlap` (Case C): PASSED
  - `supersetOverlap` (Case D): PASSED
  - `adjacentSlotsAllowed` (Case E): PASSED
  - `adjacentAfterSlotAllowed`: PASSED
  - `sameDateDifferentDay`: PASSED
- `DomainCorrectnessTests$SlotCancellationTests` (4 tests):
  - `cancelSlotCascadesToBookings`: PASSED
  - `cancelSlotCascadesToWaitlist`: PASSED
  - `promoteNextOnCancelledSlotDoesNothing`: PASSED
  - `joinWaitlistOnCancelledSlotRejected`: PASSED
- `DomainCorrectnessTests$SameDaySlotTests` (2 tests):
  - `todaySlotAllowed`: PASSED
  - `futureDateSlotAllowed`: PASSED
- `DomainCorrectnessTests$BookingSafetyTests` (3 tests):
  - `bookingFillsSlotAndWaitlists`: PASSED
  - `duplicateBookingRejected`: PASSED
  - `waitlistPromotionAfterCancellation`: PASSED

### 4. Frontend Production Build
- **Command**: `npm run build` (in `frontend/`)
- **Result**: SUCCESS (built in 126ms, Vite v8.2.0, 16 modules transformed, zero warnings/errors).
