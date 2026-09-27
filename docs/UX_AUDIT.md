# Frontend UX & Product Audit

## Executive Summary

This document presents a comprehensive, factual audit of the frontend user experience, product workflows, accessibility, responsiveness, and architecture for the **Campus Booking System (Office Hours Booking System)**.

The frontend is currently a single-page React application built with Vite. It features a clean, minimalist visual style, but contains severe product and interaction defects—most notably a **lockout preventing students from joining the waitlist on full slots**, **zero confirmation dialogs on destructive actions** (booking cancellation, slot cancellation, account deactivation), **unhandled session expiration traps (HTTP 401)**, and **a monolithic 131-line / 22.6KB `main.jsx` file** bundling all components, utilities, and views into one unrouted file.

The audit was conducted without modifying source code.

---

## Current Frontend Architecture

- **Build Tooling**: Vite 8.2.0, `@vitejs/plugin-react` (placed in `dependencies` instead of `devDependencies`).
- **Dependencies**: React 19 (`react`, `react-dom`), `bootstrap` (imported in `main.jsx` line 1: `bootstrap/dist/css/bootstrap.min.css`).
- **Routing**: **None**. No client-side routing library (`react-router-dom` is not installed). Navigation is purely internal React component state (`tab` string stored in `App.jsx`).
- **State Management**: Local component state (`useState`, `useCallback`, `useMemo`). No global store or context; session is read from and written directly to `localStorage` under key `ohs.session`.
- **API Client**: Simple native `fetch` wrapper function `api(path, options)` in `main.jsx` lines 11–20.
- **Styling Architecture**: Dual CSS loading:
  - Bootstrap 5 CSS imported first (`bootstrap/dist/css/bootstrap.min.css` ~237KB compiled).
  - Custom CSS imported second (`frontend/src/styles.css` ~8.2KB).
  - The application relies almost entirely on custom CSS classes (`.button`, `.field`, `.card`, `.slot-row`, `.auth-card`), using Bootstrap only for incidental utility classes like `.ms-3` and `.table-responsive`.
- **File Structure**:
  - `frontend/index.html` (359 bytes)
  - `frontend/src/main.jsx` (22,667 bytes, 131 lines — contains 15 components)
  - `frontend/src/styles.css` (8,199 bytes, 10 lines)

---

## Student UX Audit

### 1. Registration & Sign In
- **Flow**: User toggles between "Sign in" and "Register" via tab buttons.
- **Role Selection**: Selecting `STUDENT` displays conditional fields for `Roll number` and `Year of study`.
- **Post-Registration Behavior**: Submitting registration does not sign the student in; it switches mode to `"login"`, pre-fills the email and password, and requires a second manual click on "Sign in".
- **Form Feedback**: No inline field-level validation; errors from the API are displayed as a raw text string below the inputs.
- **Password Input**: Standard `<input type="password">` with no show/hide visibility toggle.

### 2. Discover Professor & View Availability
- **Query / API**: Calls `GET /api/professors?page=0&size=50`.
- **Omission of Search**: Backend supports searching professors by department and name (`GET /api/professors?name=...&department=...`), but the frontend renders only an unsearchable `<select>` dropdown.
- **Pagination Missing**: Hardcoded to page 0, size 50. If the institution has more than 50 faculty members, subsequent professors are unreachable.
- **Selection Change**: Changing the dropdown immediately triggers `GET /api/professors/{id}/slots`.

### 3. Slot Selection & The Waitlist Lockout Bug (P0 Defect)
- **Slot Display**: Slots are rendered as a vertical list of buttons (`.slot-row`).
- **Critical Defect**: In `main.jsx` line 68:
  ```jsx
  <button className={`slot-row ${slot.status !== "OPEN" ? "disabled" : ""} ...`}
          disabled={slot.status !== "OPEN"}
          onClick={() => setSelected(slot)}>
  ```
  When a slot becomes `FULL`, `disabled` is set to `true`. Because disabled buttons cannot receive click events, **students are completely prevented from selecting a full slot**.
- **Consequence**: The system's central feature—joining the waitlist—is entirely blocked by the UI. Students can only ever join the waitlist if a race condition fills the slot between slot load time and booking confirmation.

### 4. Booking Confirmation & Reason for Visit
- **Sidebar Presentation**: Selecting an open slot opens a sticky right-hand card displaying the slot date, time, and professor name.
- **Reason for Visit**: Contains `<textarea value={reason} ... placeholder="What would you like to discuss?">`.
  - Directly underneath, an explicit helper text states:
    `"This note stays in your browser; the current backend booking API accepts only a slot ID."`
  - When clicking "Confirm booking", `POST /api/bookings` sends only `{ slotId: selected.slotId }`. The note is cleared upon confirmation without being transmitted or saved.
- **Submission Feedback**: The button changes text to `"Booking..."`. On completion, a toast displays `"Booking confirmed."` or `"You are waitlisted at position {N}."`

### 5. My Bookings & Waitlist
- **Confirmed Bookings Section**:
  - Displays table of bookings: Professor, Date/Time, Status Badge (`BOOKED`, `CANCELLED`), and Action.
  - Action is a red text link button: `"Cancel"`.
  - **No Confirmation Dialog**: Clicking `"Cancel"` instantly calls `DELETE /api/bookings/{id}`. There is no confirmation prompt, no modal, and no undo. An accidental touch or click immediately releases the student's seat to the next waitlisted student.
- **Waitlist Section**:
  - Displays table: Slot, Position, Status Badge, Action (`Leave`).
  - **Missing Slot Context**: The "Slot" column renders only `"Slot #{entry.slotId}"`. It does not show the Professor's name, the Date, or the Time. The student cannot tell what office hour they are queued for without remembering raw database slot IDs.
  - **No Confirmation Dialog**: Clicking `"Leave"` immediately invokes `DELETE /api/bookings/waitlist/{id}` with no confirmation prompt.

### 6. Student Profile
- **Fields**: Roll number, Year of study.
- **Update**: Submits `PUT /api/students/me`. Displays a toast on success.
- **Initial Load State**: While fetching, the form inputs are blank rather than displaying a loading placeholder or disabled state.

---

## Professor UX Audit

### 1. View Schedule & Slots
- **Endpoint**: `GET /api/professors/slots/me`.
- **Table Columns**: Date, Time, Capacity (`bookedCount / capacity`), Status Badge (`OPEN`, `FULL`, `CANCELLED`), and actions (`Roster`, `Edit`, `Cancel`).
- **Slot Status Indicators**: Distinguishes `OPEN` (green), `FULL` (red), `CANCELLED` (red).

### 2. View Slot Roster
- **Action**: Clicking `"Roster"` fetches `GET /api/professors/slots/{id}/bookings` and opens a card below the schedule table.
- **Content**: Shows two columns: Confirmed Bookings (Student name and roll number) and Waitlist (Queue position and student name).
- **UX Defect**: Once opened, the roster card has no close (`×` or "Dismiss") button. It remains permanently rendered at the bottom of the page until another roster is selected.

### 3. Edit Slot
- **Action**: Clicking `"Edit"` populates an "Edit slot #{id}" form below the table with Date, Capacity, Start time, End time.
- **Validation Defect**: If a slot already has active bookings (`bookedCount > 0`), the backend rules prohibit altering Date, Start Time, or End Time. However, the frontend leaves all date/time fields enabled and editable. Submitting changes to date/time results in an unhandled 409 Conflict error toast (`"Slot has active bookings - only capacity may be changed, not date/time"`) rather than proactively disabling the immutable fields in the UI.

### 4. Cancel Slot (P0 Destructive Action)
- **Action**: Clicking `"Cancel"` immediately invokes `DELETE /api/professors/slots/{id}`.
- **Consequence**: There is **no confirmation modal or warning**. Cancelling a slot cascades cancellation to all enrolled students and waitlisted students. A single misclick permanently invalidates all student bookings for that meeting window.

### 5. Create Slot
- **Flow**: Dedicated tab "Add a slot".
- **Inputs**: Date (`<input type="date">`), Start time (`<input type="time">`), End time (`<input type="time">`), Capacity (`<input type="number" min="1">`).
- **Date Constraints**: The date input lacks a `min` attribute set to today, allowing users to submit past dates in the browser.
- **Post-Submission**: Clears the form and shows a `"New slot created."` toast, but does not navigate back to the schedule view; the professor must manually click the "My schedule" tab to inspect the created slot.

---

## Admin UX Audit

### 1. Overview & Statistics
- **Endpoint**: `GET /api/admin/reports`.
- **Metrics Display**: Renders 6 cards: Total users, Professors, Students, Active bookings, Waitlisted, Utilization percentage.
- **Visual Presentation**: Plain card grid with large numbers.

### 2. Force-Cancel Booking (Blind Input Defect)
- **UI Form**: Contains an input: `<input type="number" placeholder="Booking ID" required />` and button `"Cancel booking"`.
- **Defect**: The admin dashboard has **no booking list, table, or search view**. The admin is expected to know an arbitrary integer primary key from the backend database.
- **No Confirmation**: Submitting the form calls `DELETE /api/admin/bookings/{id}` immediately with no confirmation, no display of student name, professor name, or meeting time.

### 3. User Management
- **Endpoint**: `GET /api/admin/users`.
- **Table**: Name, Email, Role badge, Access badge (`ACTIVE` / `INACTIVE`), Action link (`Deactivate` / `Activate`).
- **No Confirmation**: Clicking `"Deactivate"` instantly deactivates the user's account with no prompt.
- **Scalability Defect**: The table loads all users in a single unpaginated list with no search box, no role filter, and no sorting controls.

---

## Cross-Cutting UX Problems

### 1. Accidental Destructive Actions (No Confirmation Anywhere)
Every single destructive action in the application triggers immediately on click:
- Student booking cancellation
- Student waitlist leave
- Professor slot cancellation
- Admin user deactivation
- Admin booking force-cancellation

### 2. Browser History, Navigation, and URL Deep Linking
- Because the app uses internal state (`tab`) rather than URL paths:
  - URLs cannot be bookmarked (e.g. `http://localhost:5173/` is the only URL).
  - Browser Back and Forward buttons do not navigate between tabs; clicking "Back" navigates away from the website entirely.
  - Refreshing the browser (`F5`) completely resets the view to the default tab (`book` for student, `schedule` for professor, `overview` for admin).

### 3. Session Expiration Handling (P0 Defect)
- When the JWT token expires (backend default 24h), backend endpoints return HTTP 401 Unauthorized.
- In `main.jsx`, `api()` throws `new Error(body?.message ?? "Request failed (401)")`.
- The error is caught by the view and displayed as an error toast.
- **The session is never cleared from `localStorage`**, and the user is not logged out or returned to the login screen. The user remains stuck in the dashboard where every subsequent action fails with repeated 401 toasts until they manually refresh the page.

### 4. Toast Notifications
- Renders via `<Notice notice={notice} onDismiss={() => setNotice(null)} />` at the bottom-right corner.
- **No Auto-Dismiss**: Toasts never expire or fade out automatically. They stay permanently on screen until the user manually targets and clicks the small `×` dismiss button.
- **No Toast Queue**: Any new notification immediately overwrites the existing one.

---

## Accessibility Audit

1. **Color Contrast Failures**:
   - Muted text (`.muted`), helper text (`.helper`), and placeholder text use `#94a3b8` on `#fafafa` background.
   - Contrast ratio: **2.38:1**, severely failing WCAG 2.1 AA (minimum requirement: 4.5:1 for normal text).
   - Professor role badge uses `#0369a1` on `#f0f9ff`, contrast ratio: ~3.8:1 (fails 4.5:1).
2. **Accessible Names on Action Buttons**:
   - Table action buttons are rendered as plain text strings (`"Cancel"`, `"Edit"`, `"Roster"`, `"Leave"`) without row context. Screen readers read "Cancel, button", "Cancel, button" repeatedly with no indication of which booking or slot is targeted.
3. **Form Field Labelling**:
   - Form controls are wrapped in `<label className="field"><span>{label}</span><input /></label>`. While functionally associated in most browsers, explicit `id` and `htmlFor` pairings are missing.
   - Errors (`.form-error`) are not linked to inputs via `aria-describedby` or `aria-invalid`.
4. **Dialogs & Modals**:
   - Edit forms and roster displays are injected inline into the document flow rather than using accessible modal dialog patterns (`role="dialog"`, `aria-modal="true"`, focus trapping, escape key dismiss).
5. **Screen Reader Live Regions**:
   - Toast notification uses `role="status"`, which is acceptable, but does not use `aria-live="polite"`.

---

## Responsive / Mobile Audit

1. **Mobile Breakpoint**: Set at `@media (max-width: 800px)` in `styles.css`.
2. **Hidden Navigation Elements**:
   - On screens `<= 800px`, the user's name and role badge in the header are hidden (`display: none`), leaving only a 30px circular avatar with their first initial.
3. **Stacked Sidebar Defect on Student Booking**:
   - Desktop layout uses a 2-column grid: slots on the left, sticky confirmation card on the right.
   - On mobile (`<= 800px`), `.side-card` drops below the entire list of slots.
   - When a student taps an available slot at the top of their screen, the selection is visually recorded, but the confirmation card and "Confirm booking" button are positioned at the bottom of the page, below all other slots. If there are 10+ slots, the action is completely off-screen with no visual cue to scroll down.
4. **Table Horizontal Overflow**:
   - Tables use `.table-responsive { overflow-x: auto; }`. On small mobile screens (375px–414px), tables require awkward two-dimensional panning to view actions in the rightmost column.

---

## State Handling Audit

### Loading
- **Forms**: Buttons show `"Please wait..."` or `"Booking..."` when `saving` / `loading` is true.
- **Data Views**: Initial loading states are completely missing. When navigating to "My bookings", "My schedule", or "Users", tables are blank until the network request finishes. There are no skeleton loaders, shimmer effects, or spinner indicators.

### Empty
- Empty states are provided via `<Empty text="..." />`:
  - Student: `"No future slots are available for this professor."`, `"You have no bookings yet."`, `"You are not waiting for any slots."`
  - Professor: `"No upcoming slots. Add one to start accepting bookings."`, `"No bookings."`, `"No waitlist entries."`
- The empty states are plain text paragraphs; they lack actionable prompts (e.g. no "Add a slot" button inside the professor's empty schedule card).

### Error
- Network or HTTP errors are caught in `.catch()` handlers and forwarded to `notify(e.message, "error")`.
- If an API request fails on initial load, the view falls back to displaying the empty state as if no data exists, while displaying an error toast at the bottom of the screen. There is no in-page error card or "Retry" button.

### Success
- Form submissions display green success toasts (`"Booking confirmed."`, `"New slot created."`, `"Profile updated."`).

### Session Expiration
- Handled only on initial page reload (in `App.useEffect`, `api("/api/auth/me")` catches error and removes `SESSION_KEY`).
- Fails completely during active use: an expired token generates a 401 error toast but leaves the session active in memory and in `localStorage`.

---

## Visual Design Problems

1. **Typography & Contrast**:
   - Relies on Google Fonts `Inter`, but font weights and color choices create washed-out secondary text (`#94a3b8`).
   - Badge font size of `10px` is illegibly small on high-density displays.
2. **Layout & Density**:
   - Large empty gaps on widescreen displays (`app-shell` max-width 1180px with centered margin).
   - Inconsistent vertical spacing between section headers and card containers.
3. **Form Elements**:
   - Input controls use basic 1px border with minimal hover and active focus states.
   - Date and time pickers rely entirely on OS-native pickers with no custom styling or calendar widgets.
4. **Cards and Hierarchy**:
   - Flat white cards on off-white background (`#fafafa`) provide weak visual separation on low-contrast monitors.

---

## Frontend Architecture Debt

1. **Monolithic `main.jsx`**:
   - Entire application (15 distinct components, API logic, state, and styles) is written in a single file.
   - Lines are concatenated with multiple statements per line, severely hindering code review, testing, and maintainability.
2. **Unnecessary Bootstrap Dependency**:
   - `package.json` includes `bootstrap: "latest"`.
   - `main.jsx` imports `bootstrap.min.css` (~237KB).
   - Only 2 Bootstrap utility classes are referenced (`.ms-3`, `.table-responsive`), while the application uses its own custom CSS for cards, buttons, badges, forms, and headers. This adds unnecessary bundle weight.
3. **Missing Routing Layer**:
   - Tab switching is handled by `const [tab, setTab] = useState()`. No URLs exist for bookmarking or sharing views.
4. **Scattered API Invocations**:
   - Every view component makes direct `fetch` calls through the generic `api()` helper rather than utilizing centralized API client modules or service functions.
5. **No Form Validation Architecture**:
   - Forms use ad-hoc object state (`useState({ rollNo: '', yearOfStudy: '' })`) with manual string-to-number casting on submit.

---

## P0 Issues (Broken / Data-Loss / Severe Usability Flaw)

1. **Waitlist Selection Lockout**:
   - **Location**: `main.jsx` line 68 (`StudentBooking`).
   - **Problem**: Full slots have `disabled={slot.status !== "OPEN"}`, preventing students from selecting full slots and making it impossible to join the waitlist.
2. **Zero Confirmation on Destructive Actions**:
   - **Location**: `StudentBookings` (Cancel booking, Leave waitlist), `ProfessorSchedule` (Cancel slot), `AdminUsers` (Deactivate user), `AdminOverview` (Force cancel).
   - **Problem**: Instant execution on click with no confirmation dialog or undo option.
3. **Session Expiration Lockout Trap (HTTP 401)**:
   - **Location**: `main.jsx` lines 11–20 (`api()` function).
   - **Problem**: Expired JWT tokens trigger error toasts without clearing `localStorage` or resetting session state, leaving the user trapped on a non-functional screen.

---

## P1 Issues (Major Product / UX Problems)

4. **Blind Admin Force-Cancellation**:
   - **Location**: `AdminOverview` (`main.jsx` line 105).
   - **Problem**: Requires typing an arbitrary numeric booking ID with no list or search interface showing what is being cancelled.
5. **Waitlist Entries Missing Crucial Details**:
   - **Location**: `StudentBookings` (`main.jsx` line 77).
   - **Problem**: Displays only `"Slot #ID"` and queue position, omitting professor name, date, and meeting time.
6. **No URL Routes / Broken Browser History**:
   - **Location**: `App` navigation (`main.jsx` lines 121–127).
   - **Problem**: No URLs exist for views; page refresh resets to default tab; browser back button exits the app.
7. **Professor Search / Filter Omission**:
   - **Location**: `StudentBooking` (`main.jsx` line 68).
   - **Problem**: Displays an unsearchable dropdown limited to the first 50 professors, ignoring backend search endpoints.
8. **Toast Notifications Never Auto-Dismiss**:
   - **Location**: `Notice` component (`main.jsx` line 24).
   - **Problem**: Alerts stay permanently on screen until the user manually clicks the dismiss cross.
9. **Mobile Slot Confirmation Off-Screen**:
   - **Location**: `StudentBooking` on mobile screen `<= 800px`.
   - **Problem**: Sidebar drops below the slot list, forcing the confirmation card out of view when a slot is tapped.

---

## P2 Issues (Meaningful Quality Issues)

10. **Missing Loading States & Skeletons**:
    - Data tables and dashboard statistics render blank while waiting for network responses.
11. **WCAG AA Color Contrast Failures**:
    - Secondary/muted text (`#94a3b8` on `#fafafa`) has contrast 2.38:1 (fails 4.5:1 minimum).
12. **Professor Edit Slot Validation Trap**:
    - Date and time inputs remain enabled on slots with active bookings, failing only on backend submit.
13. **Monolithic Architecture**:
    - Entire frontend resides in one 131-line / 22.6KB file.
14. **Unclosable Professor Roster**:
    - Once opened, the roster card cannot be dismissed or hidden.
15. **Redundant 237KB Bootstrap Dependency**:
    - Full Bootstrap CSS loaded for only 2 minor utility classes.

---

## P3 Issues (Cosmetic / Polish Issues)

16. **No Password Visibility Toggle**:
    - Auth forms lack an eye icon to inspect entered passwords.
17. **No Auto-Login After Registration**:
    - Registering switches mode to login and requires a second click.
18. **Sub-11px Badges**:
    - Status badges use 10px text, difficult to read on small screens.
19. **Date Input Lacks Min Attribute**:
    - Professor slot creation form does not set `min` attribute to current date.

---

## What Is Already Good

1. **Cohesive Visual Aesthetics**:
   - Clean, modern, distraction-free aesthetic with subtle slate-and-navy styling.
2. **Clear Role Separation**:
   - Distinct headers, navigation tabs, and badge indicators for Student, Professor, and Admin.
3. **Pessimistic Concurrency Awareness**:
   - Feedback message correctly distinguishes between confirmed booking and waitlist position.
4. **Honest Helper Text**:
   - Transparently alerts students that "Reason for visit" is a local client-side note rather than making false claims.
5. **Accurate Status Badging**:
   - Color-coded badges for `OPEN`, `FULL`, `CANCELLED`, `BOOKED`, `WAITING`, `ACTIVE`, `INACTIVE`.

---

## What Must NOT Be Changed

1. **Backend API Contracts**:
   - Endpoints (`/api/auth/*`, `/api/professors/*`, `/api/bookings/*`, `/api/students/*`, `/api/admin/*`) and payload shapes must not be altered during frontend improvements.
2. **Pessimistic Locking & Waitlist Workflow**:
   - The two-step booking response contract (HTTP 201 for confirmed booking, HTTP 202 for waitlisting) must remain intact.
3. **Core Color Scheme & Typography Direction**:
   - The dark-accented slate palette (`#0f172a`, `#64748b`, `#f8fafc`) and Inter typography should be preserved and refined rather than replaced.

---

## Recommended Work Order

1. **Phase 1: Critical Bug Fixes (P0)**
   - Fix waitlist selection lockout by enabling click on `FULL` slots to trigger waitlisting.
   - Add confirmation modals for all destructive actions (cancel booking, cancel slot, leave waitlist, deactivate user).
   - Implement automatic 401 interception to clean expired session and redirect to login.
2. **Phase 2: Product & Information Architecture (P1)**
   - Introduce URL routing (React Router) for deep linking and back/forward navigation.
   - Add professor search/filter by name and department in student booking.
   - Enhance student waitlist view to show professor name, date, and meeting time.
   - Provide auto-dismissing toast notifications.
   - Improve mobile layout for slot selection and confirmation.
3. **Phase 3: Componentization & Architecture (P2)**
   - Deconstruct monolithic `main.jsx` into modular components, pages, hooks, and API services.
   - Remove redundant Bootstrap dependency and standardize on custom lightweight CSS tokens.
   - Add loading skeleton states for tables and cards.
   - Fix WCAG AA contrast ratios for secondary and muted text.
4. **Phase 4: Polish & Refinement (P3)**
   - Add password visibility toggle.
   - Add auto-login on registration.
   - Enhance date/time picker inputs and status badges.
