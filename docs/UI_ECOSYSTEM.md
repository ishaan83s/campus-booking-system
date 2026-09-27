# Frontend UI Ecosystem Research

## Executive Summary

This research evaluation establishes the architectural strategy and component ecosystem for rehabilitating the **Campus Booking System (Office Hours Booking System)** frontend.

The current frontend is a single-file React 19 application (`main.jsx`, 22.6KB) paired with custom CSS and a legacy, mostly unused 237KB Bootstrap 5 dependency. The core user workflows—student slot discovery/booking, waitlist queuing, professor schedule/roster management, and admin oversight—demand a professional, accessible, responsive, and maintainable academic SaaS interface.

Rather than assembling an uncurated bundle of trendy UI packages that add cognitive overhead and React 19 compatibility hazards, this report evaluates candidates against strict criteria: React 19 compatibility, accessibility (WCAG 2.1 AA), bundle weight, licensing, and domain fit for office-hour scheduling.

### Key Strategic Decisions
1. **Core UI Primitives**: Adopt **Radix UI Primitives** (via copy-in / `shadcn/ui` style ownership) with standard Vanilla CSS or Tailwind CSS utility tokens, completely replacing Bootstrap.
2. **Routing**: Adopt **React Router v7 / v6** (`react-router-dom`) to provide declarative URL routing, deep linking, and browser back/forward navigation.
3. **Scheduling Model**: Adopt a **Calendar Date-Picker (React DayPicker v9) + Grouped Time-Slot Chip Grid** (the Cal.com / Calendly pattern) rather than a heavy drag-and-drop scheduler (e.g. FullCalendar). This perfectly matches discrete capacity-based office hour windows and excels on mobile screens.
4. **Tables**: Maintain **Lightweight Semantic HTML Table Components** for simple tabular views, with **TanStack Table v8** reserved only if client-side multi-column sorting, pagination, and filtering become required.
5. **Forms & Validation**: Adopt **React Hook Form + Zod** for robust client-side validation without rerender penalties.
6. **Notifications**: Adopt **Sonner** for toast notifications (auto-dismissing, queueable, accessible).
7. **Icons**: Adopt **Lucide React** for clean, consistent iconography.
8. **Bootstrap**: **Fully remove** Bootstrap 5. It contributes ~237KB of CSS bloat while only 2 minor utility classes are used.

---

## Current Frontend Constraints

1. **Runtime Environment**:
   - **React Version**: `19.2.8` (latest React release).
   - **Bundler**: Vite 8.2.0 (`@vitejs/plugin-react`).
   - Any library that relies on deprecated React internals (`findDOMNode`, legacy context) or has strict peer dependencies capping at `react@^18.0.0` will fail or require `--legacy-peer-deps`.
2. **Domain Architecture**:
   - The scheduling model is discrete time intervals (`slotDate`, `startTime`, `endTime`, `capacity`, `bookedCount`, `status`).
   - It is not an arbitrary event timeline (like Google Calendar or Outlook); meetings are strictly bound to professor-created slot windows.
3. **Current Assets & Dependencies**:
   - `bootstrap: latest` (~237KB CSS loaded on every page).
   - Custom `styles.css` (8.2KB) handles 98% of actual component styling.
4. **Maintenance Constraint**:
   - Bounded academic project. The codebase must remain transparent, easily inspectable, and resilient to long-term dependency bitrot.

---

## Core UI / Primitive Options

### 1. Radix UI Primitives
- **Project**: Radix Primitives
- **GitHub**: [`radix-ui/primitives`](https://github.com/radix-ui/primitives)
- **Purpose**: Unstyled, accessible UI primitive components (Dialog, Popover, Dropdown Menu, Tooltip, Select, Tabs).
- **License**: MIT
- **Maintenance**: Extremely active; maintained by WorkOS. Official React 19 support published.
- **Accessibility**: World-class WAI-ARIA compliance built-in; automated focus trapping, keyboard navigation, screen reader announcements.
- **Customization**: 100% headless. Zero default styling. Fully styled with custom CSS or utility classes.
- **Strengths**: Solves hardest accessibility problems (modals, popovers, select dropdowns); zero style opinion; modular per-component packages.
- **Weaknesses**: Requires writing CSS for every primitive; not an out-of-the-box pre-styled kit.
- **Relevance**: Ideal for modal dialogs (confirmations), dropdown menus, tabs, and accessible tooltips.
- **Recommendation**: **USE** (either directly via `@radix-ui/react-*` or via shadcn/ui pattern).

### 2. Base UI
- **Project**: Base UI (v1 rewrite)
- **GitHub**: [`mui/base-ui`](https://github.com/mui/base-ui)
- **Purpose**: Next-generation unstyled component library by the MUI team.
- **License**: MIT
- **Maintenance**: Active development, but currently in preview / alpha rewrite.
- **Accessibility**: High standard WAI-ARIA compliance.
- **Customization**: Unstyled, CSS-agnostic.
- **Strengths**: Modern API design, CSS transitions support.
- **Weaknesses**: Still evolving towards a stable 1.0 release; community ecosystem and documentation are less mature than Radix UI.
- **Relevance**: Strong future candidate, but introduces stability risk today compared to Radix UI.
- **Recommendation**: **REFERENCE** (monitor, but avoid in immediate rehabilitation).

### 3. shadcn/ui Pattern
- **Project**: shadcn/ui
- **GitHub**: [`shadcn-ui/ui`](https://github.com/shadcn-ui/ui)
- **Purpose**: Component distribution architecture where accessible primitives (Radix UI) are copied directly into the project codebase rather than installed as an opaque npm library.
- **License**: MIT
- **Maintenance**: The most widely adopted React UI architecture in modern web development.
- **Accessibility**: Inherits Radix UI WAI-ARIA compliance.
- **Customization**: Absolute. Code lives in `src/components/ui/` and is fully owned by the developer.
- **Strengths**: Zero black-box dependency lockdown; full ownership; excellent design defaults; easily adapted to custom CSS or Tailwind tokens.
- **Weaknesses**: Conventionally expects Tailwind CSS for token styling.
- **Relevance**: High. Copy-in ownership eliminates third-party component wrapper bloat.
- **Recommendation**: **USE** (adopt the component-ownership pattern for dialogs, badges, buttons, and form inputs).

### 4. Headless UI
- **Project**: Headless UI
- **GitHub**: [`tailwindlabs/headlessui`](https://github.com/tailwindlabs/headlessui)
- **Purpose**: Unstyled accessible components by Tailwind Labs.
- **License**: MIT
- **Maintenance**: Maintained, but slower feature additions compared to Radix UI.
- **Accessibility**: Good WAI-ARIA support.
- **Customization**: Unstyled.
- **Strengths**: Tight pairing with Tailwind CSS.
- **Weaknesses**: Narrower component selection than Radix (e.g. fewer layout and scheduling primitives).
- **Recommendation**: **AVOID** (Radix UI provides a richer primitive set).

### 5. React Aria Components
- **Project**: React Aria
- **GitHub**: [`adobe/react-spectrum`](https://github.com/adobe/react-spectrum)
- **Purpose**: Adobe's headless component library and accessibility hooks.
- **License**: Apache-2.0
- **Maintenance**: Very high enterprise backing.
- **Accessibility**: Exceptional (military-grade accessibility).
- **Customization**: Completely headless.
- **Strengths**: Unmatched internationalization (i18n), date/time arithmetic, and mobile gesture support.
- **Weaknesses**: Steeper learning curve and higher API complexity than Radix UI.
- **Recommendation**: **REFERENCE** (admirable date/time concepts, but higher complexity than needed).

---

## Routing Options

### 1. React Router v7 / v6 (`react-router-dom`)
- **GitHub**: [`remix-run/react-router`](https://github.com/remix-run/react-router)
- **License**: MIT
- **Status**: Standard de facto routing library for React. React Router v7 supports traditional SPA client routing seamlessly without requiring full-stack SSR.
- **Strengths**:
  - Declarative nested routes (`/student/book`, `/student/bookings`, `/professor/schedule`, `/admin/users`).
  - Restores browser Back/Forward navigation.
  - Enables deep linking and shareable URLs.
  - Protected route wrappers (`<RequireAuth role="PROFESSOR">`).
  - Active tab highlighting derived from current URL path.
- **Recommendation**: **USE**.

### 2. TanStack Router
- **GitHub**: [`tanstack/router`](https://github.com/tanstack/router)
- **License**: MIT
- **Status**: Type-safe routing library gaining rapid traction.
- **Strengths**: 100% end-to-end type safety, search params validation.
- **Weaknesses**: Code-generation CLI or strict file-based routing overhead; excessive ceremony for a single-developer or small academic scheduling application.
- **Recommendation**: **AVOID** (React Router provides the ideal balance of simplicity and capability).

---

## Table Options

### 1. Semantic HTML Tables with Clean React Abstraction (Current + Refined)
- **Architecture**: Native `<table>`, `<thead>`, `<tbody>`, `<tr>`, `<th>`, `<td>` structured into reusable components (`Table`, `TableRow`, `TableCell`, `TableEmpty`).
- **Complexity**: Zero dependencies, 0 KB bundle weight.
- **Accessibility**: Native HTML tables provide out-of-the-box screen reader navigation (`role="table"`, row/col headers).
- **Suitability**: Perfect for our existing data sizes (5–30 bookings per student, 10–50 slots per professor).
- **Recommendation**: **USE** as the primary table approach.

### 2. TanStack Table v8 (`@tanstack/react-table`)
- **GitHub**: [`tanstack/table`](https://github.com/tanstack/table)
- **License**: MIT
- **Architecture**: Headless table logic engine (sorting, multi-filtering, pagination, row selection).
- **Strengths**: 100% headless, zero styles, handles massive datasets smoothly.
- **Weaknesses**: 15 KB gzipped dependency; requires writing significant boilerplate rendering code.
- **Suitability**: Only needed if Admin user management or Professor slot lists grow into thousands of records requiring client-side multi-column sorting and filtering.
- **Recommendation**: **DEFER / USE ONLY IF NEEDED** (reserve for future high-volume admin views).

### 3. AG Grid / MUI DataGrid
- **Recommendation**: **AVOID**. Commercial licensing hurdles, massive bundle size (>200 KB), heavy visual opinion that conflicts with lightweight SaaS design.

---

## Calendar / Scheduling Options

A critical domain question for the Campus Booking System is how students browse availability and how professors view and manage their schedule.

### Detailed Comparison of Scheduling Options

| Project | Architecture | Date/Time Capabilities | Views Supported | Accessibility | Customization | Bundle / Complexity | License | Project Relevance | Recommendation |
|---|---|---|---|---|---|---|---|---|---|
| **React DayPicker v9** | Unstyled / headless calendar primitive | Single date, date range, multiple dates, disable past/custom dates | Monthly grid, multiple months | Exceptional (WAI-ARIA date grid, keyboard arrows) | Total (CSS classes or custom components) | ~12 KB / Low | MIT | **Ideal for Student slot browsing & Professor slot creation** | **USE** |
| **Calendly / Cal.com Pattern** (DayPicker + Time Slot Chips) | Date Picker left + Grouped time slots right | Date selection filtering slot list; capacity badges; waitlist triggers | Day selection + Time list; grouped chronological cards | High (Native buttons + accessible calendar) | Total control over badge and slot states | Minimal (~12 KB total) | MIT (custom) | **Matches office-hours workflow exactly on desktop & mobile** | **USE (Primary Pattern)** |
| **Schedule-X** | Modern TS calendar suite | Events, recurring rules, drag-and-drop | Day, Week, Month, Agenda | Good | Themeable via CSS | ~45 KB / Medium | MIT | Good for visual timeline, but overkill for fixed office hours | **REFERENCE** |
| **FullCalendar** | Large multi-view scheduler engine | Complete event calendar, timeline, resources | Month, Week, Day, List, Timeline, Resource | Moderate (dense DOM) | Heavy CSS overrides required | ~150 KB+ / High | MIT (core) / Commercial (scheduler) | React 19 compatibility friction; excessive for 30-min office hours | **AVOID** |
| **React-Big-Calendar** | Classic React calendar | Events rendering on week/day grid | Month, Week, Work Week, Day, Agenda | Outdated ARIA; table-based | Heavy overrides; depends on moment/date-fns | ~60 KB / Medium | MIT | Maintenance is sluggish; styling is difficult to modernize | **AVOID** |

### Domain Evaluation: Why the "Date Picker + Slot Chip Grid" Wins
1. **Discrete Capacity Windows**: Office hours are not free-form calendar events; they are discrete bookable windows with finite capacity (`bookedCount/capacity`) and queueing (`Waitlist`). A traditional Google Calendar grid crams multiple 15-minute slots into narrow columns where text like "2 spaces left - Dr. Smith" is truncated.
2. **Mobile Ergonomics**: Full-week calendar grids fail on mobile (375px screens) because 7 columns cannot render legible time slots without horizontal scrolling. In contrast, selecting a date on a compact monthly picker and viewing clean, tappable slot rows beneath works flawlessly on mobile screens.
3. **Waitlist Clarity**: Displaying slots as list cards allows clear badges (`OPEN`, `FULL / WAITLIST`), capacity indicators (`2 spaces left`), and clear action buttons (`Confirm booking` vs `Join waitlist`).

---

## Form / Dialog Options

### 1. Form Validation: React Hook Form + Zod
- **Projects**: [`react-hook-form/react-hook-form`](https://github.com/react-hook-form/react-hook-form), [`colinhacks/zod`](https://github.com/colinhacks/zod)
- **License**: MIT
- **Purpose**: High-performance, uncontrolled form state management with declarative schema validation.
- **Why It Matters**:
  - Eliminates unnecessary component re-renders on every keystroke.
  - Zod schemas match backend DTO constraints (`rollNo` max 50, `password` min 8, `capacity` min 1, time order validation `endTime > startTime`).
  - Native integration with standard inputs or headless components.
- **Recommendation**: **USE**.

### 2. Dialogs / Modals: Radix UI Dialog Primitive
- **Project**: `@radix-ui/react-dialog`
- **Purpose**: Accessible modal overlay for destructive action confirmations, slot editing, and roster inspect.
- **Features**: Automatic focus trapping, returns focus on close, dismiss on `Escape` or backdrop click, `aria-modal="true"`.
- **Recommendation**: **USE**.

---

## Notification Options

### 1. Sonner
- **Project**: Sonner
- **GitHub**: [`emilkowalski/sonner`](https://github.com/emilkowalski/sonner)
- **License**: MIT
- **Features**: Highly polished, accessible, stacked toasts with smooth micro-animations, swipe-to-dismiss, automatic timeout, and action buttons. React 19 verified.
- **Bundle**: Tiny (~3.5 KB).
- **Comparison to current `Notice`**: Replaces the single static bottom-right banner with an elegant, non-blocking toast stack.
- **Recommendation**: **USE**.

### 2. React Hot Toast
- **Status**: Less actively updated for React 19 than Sonner; lacks stacked expansion.
- **Recommendation**: **AVOID** (Sonner is superior in 2026).

---

## Motion Options

### 1. Motion (formerly Framer Motion)
- **Project**: Motion
- **GitHub**: [`motiondivision/motion`](https://github.com/motiondivision/motion)
- **License**: MIT
- **Assessment**: The industry gold standard for React motion. React 19 compatible.
- **Recommendation for Campus Booking**:
  - **Do NOT** use for flashy decorative animations.
  - **USE SPARINGLY** for functional transitions:
    - Dialog scale-in / fade-in
    - Toast stack entry / exit
    - Tab content cross-fades
    - Slot list height transitions when filtering by date
- **Recommendation**: **USE (Strictly bounded to functional UI feedback)**.

### 2. Magic UI / Aceternity UI
- **GitHub**: [`magicuidesign/magicui`](https://github.com/magicuidesign/magicui), [`aceternity/ui`](https://github.com/aceternity/ui)
- **Assessment**: Excellent visual design repositories for marketing landing pages, glowing borders, and particle effects.
- **Relevance**: Inappropriate for an academic SaaS scheduling dashboard. Adding neon borders, spotlight effects, or 3D cards introduces visual noise and distracts from core scheduling tasks.
- **Recommendation**: **REFERENCE ONLY** for subtle typography/card styling inspiration; do not import visual effect components.

---

## Icon Options

### 1. Lucide React
- **Project**: Lucide
- **GitHub**: [`lucide-react`](https://github.com/lucide-icons/lucide)
- **License**: ISC
- **Features**: Over 1,400 clean, consistent vector icons designed on a 24x24 grid. Tree-shakeable, lightweight SVGs. Full React 19 support.
- **Relevance**: Essential for calendar icons, clock icons, user avatars, status indicators (check, alert, x), and chevron arrows.
- **Recommendation**: **USE**.

### 2. Tabler Icons / Heroicons
- Good alternatives, but Lucide has superior ecosystem consistency and tighter integration with modern component libraries.

---

## Bootstrap Assessment

### Current Repository State
- `frontend/package.json` lists `"bootstrap": "latest"`.
- `frontend/src/main.jsx` line 1 imports `"bootstrap/dist/css/bootstrap.min.css"`.
- Bundle analysis shows `index-*.css` is **238.72 KB**, of which over **95% is unused Bootstrap CSS**.
- **Actual Bootstrap usage**:
  - Exactly 1 utility class in JSX: `.ms-3` (`main.jsx` line 92).
  - Exactly 1 utility class in Table wrapper: `.table-responsive` (`main.jsx` line 117).
  - All buttons (`.button`, `.button-dark`, `.button-danger`, `.button-waitlist`), cards (`.card`, `.side-card`, `.stat-card`), forms (`.field`, `.form-stack`), badges (`.badge-soft`), and layout grids (`.page-grid`, `.two-column`, `.stat-grid`) are defined in `styles.css`.

### Recommendation
**Completely remove Bootstrap 5**.
- Replace `.ms-3` with standard flex/gap or a simple margin token.
- Replace `.table-responsive` with the custom `.table-responsive { overflow-x: auto; }` rule already present in `styles.css`.
- Uninstall `bootstrap` from `package.json`.
- **Immediate Benefit**: Reduces CSS bundle size from ~238 KB to under 15 KB (a **94% reduction in stylesheet payload**), eliminating CSS specificity conflicts and layout quirks.

---

## Recommended Frontend Stack

We recommend exactly **ONE coherent, bounded, and modern frontend stack**:

```
├── Framework & Build:
│   ├── React 19 (existing)
│   ├── Vite 8 (existing)
│   └── React Router v7 (client-side routing & deep linking)
│
├── UI Architecture:
│   ├── Headless Primitives: Radix UI (@radix-ui/react-dialog, @radix-ui/react-tabs, @radix-ui/react-select)
│   ├── Component Ownership: shadcn/ui pattern (modular components in src/components/ui/)
│   └── Styling: Modern CSS Tokens / Clean Vanilla CSS (Bootstrap eliminated)
│
├── Specialized Modules:
│   ├── Scheduling / Calendar: React DayPicker v9 + Time Slot Chip Grid (Calendly / Cal.com pattern)
│   ├── Forms & Validation: React Hook Form + Zod
│   ├── Toasts / Notifications: Sonner
│   ├── Icons: Lucide React
│   └── Micro-Interactions: Motion (bounded strictly to dialogs & toasts)
```

---

## What We Should NOT Install

| Library | Reason for Rejection |
|---|---|
| **FullCalendar / React-Big-Calendar** | Massive bundle overhead (>150KB); poor mobile responsiveness; unsuited for discrete capacity/waitlist slots. |
| **Bootstrap 5** | Bloats CSS bundle by 230KB for only 2 utility classes; conflicts with modern design tokens. |
| **Tailwind UI / Material UI / Ant Design** | Heavy runtime/style opinions; destroys existing cohesive minimalist brand identity. |
| **Magic UI / Aceternity UI Packages** | Overly flashy landing page effects unsuited for an academic portal; adds GPU overhead. |
| **TanStack Router** | Unnecessary code-gen and routing complexity for a small SaaS application. |
| **Redux / Zustand / MobX** | Overkill; session and booking data are server-driven and easily managed via React state/context. |
| **AG Grid / Enterprise DataGrids** | Commercial licensing; massive payload; unneeded for 10–50 table rows. |

---

## Migration Strategy

To avoid destabilizing the working application, the ecosystem should be introduced across clear, sequential steps:

1. **Step 5: Dependency Cleanup & Routing Foundation**
   - Remove `bootstrap` and purge unused CSS.
   - Install `react-router-dom` and define clean routes (`/login`, `/student/book`, `/student/bookings`, `/professor/schedule`, `/admin/users`).
   - Move from monolithic `main.jsx` into structured folder layout (`src/pages/`, `src/components/`, `src/services/`).

2. **Step 6: Accessible Primitives & Shared Components**
   - Introduce Radix UI Dialog (replacing manual `ConfirmDialog`).
   - Introduce `Sonner` (replacing manual `Notice`).
   - Introduce `Lucide React` icons for clear visual hierarchy.

3. **Step 7: Scheduling Experience Upgrade**
   - Integrate `React DayPicker v9` in `StudentBooking` to allow browsing availability by calendar day.
   - Pair with chronological time-slot chips with capacity and waitlist status indicators.
   - Ensure seamless mobile responsiveness where selecting a date filters slots immediately above the confirmation drawer.

4. **Step 8: Forms & Administrative Polish**
   - Integrate React Hook Form + Zod on `CreateSlot` (with date >= today and endTime > startTime validation) and `AuthScreen`.
   - Add search and role filtering on `AdminUsers`.

---

## Final Decision

- **Core Framework**: React 19 + Vite 8.
- **Routing**: React Router (`react-router-dom`).
- **Design System Architecture**: Copy-in component ownership (shadcn/ui pattern) powered by **Radix UI Primitives** and clean modern CSS tokens.
- **Scheduling**: **React DayPicker v9 + Time Slot Chip Grid** (the Cal.com / Calendly pattern).
- **Forms & Validation**: **React Hook Form + Zod**.
- **Feedback & Details**: **Sonner** (toasts) + **Lucide React** (icons).
- **Bootstrap**: **Purged completely**.
- **Backend Impact**: **Zero backend API changes required**. All proposed frontend components interface directly with the existing REST contracts.
