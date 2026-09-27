# Campus Booking System — Authoritative Design System

## 1. Visual Character & Philosophy

### Product Personality
The Campus Booking System is an academic scheduling platform operating at university standards. The interface follows the **"Operate"** mode (per `impeccable` & `frontend-design`): task-focused, calm, scannable, and robust. It prioritizes clarity, informational density, and high-contrast legibility over decorative excess.

### What Makes It Feel Premium
- **Restrained Depth**: Surfaces are distinguished through subtle background luminosity steps (`#090d16` → `#0f172a` → `#1e293b`), crisp single-pixel borders (`rgba(255, 255, 255, 0.08)`), and soft layered shadows rather than heavy blurs or noisy textures.
- **Intentional Typography**: High-readability sans-serif stack (`Inter`, system fonts) with tight, geometric headings, balanced line-heights, and tabular numbers for all dates, times, and capacity counts.
- **Precise Information Density**: Compact without feeling cramped. Data is organized into clean visual rows, grouped chips, and structured tables.
- **State Transparency**: Slots, bookings, and waitlists have explicit, unambiguous visual cues (badges, subtle fills, clear helper copy) so students and professors never guess their status.
- **Strict Anti-Cliché Rules**:
  - No neon cyan/magenta gradients.
  - No floating blurred ambient blobs.
  - No random nested cards-in-cards without structural purpose.
  - No un-dismissible blocking overlays.
  - No raw emoji used as interface icons; all iconography uses vector Lucide icons.

---

## 2. Color System & Design Tokens

The color system uses CSS custom properties defined on `:root` and scoped for a sleek, dark-slate academic SaaS theme with high WCAG 2.1 AA/AAA contrast ratios.

```css
:root {
  /* Surfaces & Backgrounds */
  --bg-app: #080c14;               /* Deepest canvas background */
  --bg-surface: #0f172a;           /* Standard card & container surface */
  --bg-surface-elevated: #162032;  /* Modals, popovers, active selection */
  --bg-surface-hover: #1e293b;     /* Hover state on list rows / cards */
  --bg-surface-subtle: #0d1424;    /* Input backgrounds, inset wells */

  /* Text & Foreground */
  --text-primary: #f8fafc;         /* Main headings, active titles (4.5:1+ contrast) */
  --text-secondary: #94a3b8;       /* Subtext, descriptions, labels */
  --text-muted: #64748b;           /* Metadata, timestamps, helper text */
  --text-disabled: #475569;        /* Disabled button & input labels */

  /* Primary Brand (Sleek Slate Blue) */
  --primary: #3b82f6;              /* Action buttons, active tab indicators */
  --primary-hover: #2563eb;        /* Hover state for primary actions */
  --primary-focus: rgba(59, 130, 246, 0.35); /* Focus ring */
  --primary-foreground: #ffffff;   /* Text on primary buttons */

  /* Secondary / Ghost */
  --secondary-bg: #1e293b;
  --secondary-hover: #334155;
  --secondary-text: #e2e8f0;

  /* Accent (Subtle Indigo/Violet) */
  --accent: #6366f1;
  --accent-subtle: rgba(99, 102, 241, 0.12);
  --accent-border: rgba(99, 102, 241, 0.3);

  /* Semantic Feedback & Product States */
  /* Available / Success (Green) */
  --success: #10b981;
  --success-bg: rgba(16, 185, 129, 0.1);
  --success-border: rgba(16, 185, 129, 0.25);
  --success-text: #34d399;

  /* Warning / Waitlist (Amber) */
  --warning: #f59e0b;
  --warning-bg: rgba(245, 158, 11, 0.1);
  --warning-border: rgba(245, 158, 11, 0.3);
  --warning-text: #fbbf24;

  /* Danger / Cancelled / Error (Red / Rose) */
  --danger: #ef4444;
  --danger-hover: #dc2626;
  --danger-bg: rgba(239, 68, 68, 0.1);
  --danger-border: rgba(239, 68, 68, 0.3);
  --danger-text: #f87171;

  /* Info / Inactive (Sky) */
  --info: #0284c7;
  --info-bg: rgba(2, 132, 199, 0.1);
  --info-border: rgba(2, 132, 199, 0.25);
  --info-text: #38bdf8;

  /* Borders & Dividers */
  --border-subtle: rgba(255, 255, 255, 0.07);
  --border-default: rgba(255, 255, 255, 0.12);
  --border-strong: rgba(255, 255, 255, 0.2);
  --border-focus: #3b82f6;

  /* Focus Indicator */
  --focus-ring: 0 0 0 2px var(--bg-app), 0 0 0 4px var(--primary);
}
```

---

## 3. Typography

### Font Stack
```css
font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
```
For numeric data (times, dates, counts, IDs), `font-variant-numeric: tabular-nums` is strictly enforced to prevent jitter during live updates or in structured columns.

### Scale & Hierarchy
| Level | Font Size | Line Height | Weight | Tracking | Usage |
|---|---|---|---|---|---|
| **Display / H1** | `1.75rem` (28px) | `2.25rem` (36px) | 700 (Bold) | `-0.02em` | Main dashboard titles, portal header |
| **Section / H2** | `1.25rem` (20px) | `1.75rem` (28px) | 600 (Semibold) | `-0.015em` | Card section titles, modal headings |
| **Subheading / H3**| `1.0rem` (16px) | `1.5rem` (24px) | 600 (Semibold) | `-0.01em` | Group headers, slot day headers |
| **Body (Default)** | `0.9375rem` (15px)| `1.4rem` (22px) | 400 (Regular) | `0` | Standard body text, descriptions |
| **Labels / Controls**| `0.875rem` (14px)| `1.25rem` (20px) | 500 (Medium) | `0` | Form labels, button text, table headers |
| **Caption / Meta** | `0.75rem` (12px) | `1.0rem` (16px) | 400 / 500 | `0.01em` | Badges, timestamps, helper notes |

---

## 4. Spacing Scale & Layout Grid

Spacing is strictly based on a 4px/8px modular scale:
- `--space-1`: `4px` (`0.25rem`) — micro padding, icon gaps
- `--space-2`: `8px` (`0.5rem`) — button icon spacing, badge padding
- `--space-3`: `12px` (`0.75rem`) — compact input padding, chip gaps
- `--space-4`: `16px` (`1.0rem`) — standard card interior padding, form field gap
- `--space-5`: `20px` (`1.25rem`) — card header padding, drawer gutters
- `--space-6`: `24px` (`1.5rem`) — section spacing, dialog padding
- `--space-8`: `32px` (`2.0rem`) — major layout gutters
- `--space-10`: `40px` (`2.5rem`) — top-level dashboard vertical flow

### Section Spacing
- Dashboard max-width: `1200px` centered with auto margins and `16px` (mobile) to `32px` (desktop) horizontal padding.
- Grid layouts: standard 2-column or 3-column with `16px` to `24px` column gaps.

---

## 5. Shape & Elevation

- **Border Radius**:
  - Tags / Badges: `4px` (`0.25rem`)
  - Inputs / Buttons / Selects: `6px` (`0.375rem`)
  - Cards / Tables / Modals: `10px` (`0.625rem`)
  - Floating Toasts / Chips: `8px` (`0.5rem`)
  - Pill Badges: `9999px`
- **Shadows**:
  - `shadow-sm`: `0 1px 2px 0 rgba(0, 0, 0, 0.4)`
  - `shadow-md`: `0 4px 6px -1px rgba(0, 0, 0, 0.5), 0 2px 4px -2px rgba(0, 0, 0, 0.4)`
  - `shadow-lg`: `0 10px 15px -3px rgba(0, 0, 0, 0.6), 0 4px 6px -4px rgba(0, 0, 0, 0.5)`
  - `shadow-dialog`: `0 25px 50px -12px rgba(0, 0, 0, 0.85)`

---

## 6. Components Specification

### 1. Buttons (`Button`)
- **Variants**:
  - `primary`: Blue background (`--primary`), white text. Used for main action per panel (e.g. "Book Slot", "Save Slot").
  - `secondary`: Subtle dark background (`--secondary-bg`), border `--border-default`. Used for secondary workflows ("Filter", "Cancel").
  - `danger`: Red border and text or solid red for irreversible actions ("Cancel Booking", "Deactivate User").
  - `ghost`: Transparent background, hover highlight. Used for icon buttons and table row quick-actions.
- **Sizes**:
  - `sm`: Height `32px`, font `13px`, padding `0 10px`.
  - `md`: Height `40px`, font `14px`, padding `0 16px`. (Standard, meets touch minimum).
  - `lg`: Height `48px`, font `15px`, padding `0 24px`.
- **States**: Default, Hover (subtle brightness shift), Active (pressed scale 0.98), Focus-visible (explicit outline ring), Disabled (`opacity: 0.5`, pointer-events none).

### 2. Form Inputs (`Input`, `Select`, `Textarea`)
- Background: `--bg-surface-subtle`
- Border: `1px solid var(--border-default)`
- Focus: `border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-focus);`
- Always accompanied by `<label htmlFor="...">` with medium weight.
- Error state: `border-color: var(--danger)`, with inline `<span role="alert">` rendered immediately below.

### 3. Cards (`Card`)
- Compound architecture: `<Card>`, `<CardHeader>`, `<CardTitle>`, `<CardDescription>`, `<CardContent>`, `<CardFooter>`.
- Background: `--bg-surface`, border: `1px solid var(--border-subtle)`.
- Hoverable cards (e.g. professor directory card, slot chip): smooth transition on border color (`--border-strong`) and subtle lift (`translateY(-1px)`).

### 4. Status Badges (`Badge`)
- Semantic styling using dedicated background and border tokens:
  - `AVAILABLE` / `CONFIRMED`: Green (`--success-bg`, `--success-text`, `--success-border`)
  - `WAITLIST` / `PENDING`: Amber (`--warning-bg`, `--warning-text`, `--warning-border`)
  - `CANCELLED` / `INACTIVE`: Red/Muted (`--danger-bg`, `--danger-text`, `--danger-border`)
  - `ROLE`: Slate blue / violet (`--accent-subtle`, `--accent`)
- Format: Inline-flex, items center, gap 4px, font-size `12px`, font-weight 500, border radius `4px`.

### 5. Tables (`Table`)
- Semantic HTML (`<table>`, `<thead>`, `<tbody>`, `<tr>`, `<th>`, `<td>`).
- Header: Text muted, uppercase tracking, font size 12px, border-bottom `1px solid var(--border-default)`.
- Rows: Hover background `--bg-surface-hover`, vertical padding `12px`, tabular numeric alignment for dates and metrics.
- Mobile: Wrapped in smooth horizontal overflow with scroll indicators or card transformation for narrow viewports.

### 6. Modal Dialogs (`Dialog`)
- Built on accessible Radix UI primitive or owned dialog abstraction with focus-trapping, `aria-modal="true"`, and `Escape` key listeners.
- Backdrop: Deep dimmed blur `rgba(0, 0, 0, 0.7)`.
- Content: Centered, max-width `480px` to `560px`, elevated surface `--bg-surface-elevated`, border `--border-strong`, shadow `--shadow-dialog`.
- Clear header with title, description, body, and action footer.

### 7. Feedback & Toasts (`Sonner`)
- Stacked non-blocking notifications in the bottom-right corner.
- Semantic variants for success, error, warning, and info.
- Auto-dismisses in 4000ms with manual close action and pause-on-hover.

### 8. Loading & Empty States
- Skeletons: Subtle pulse animation using CSS variables (`background: linear-gradient(...)`).
- Empty states: Dedicated illustration or Lucide icon (e.g. `CalendarX`, `UserX`), clear heading ("No slots scheduled for this date"), and an optional call-to-action ("Pick another date" or "Create a slot").

---

## 7. Product State Visual Matrix

| Domain State | Badge Label | Background Token | Border Token | Text Token | Icon |
|---|---|---|---|---|---|
| **Slot: Open** | `Available` | `var(--success-bg)` | `var(--success-border)` | `var(--success-text)` | `CheckCircle` |
| **Slot: Full** | `Full (Waitlist)` | `var(--warning-bg)` | `var(--warning-border)` | `var(--warning-text)` | `Clock` |
| **Slot: Selected** | `Selected` | `rgba(59, 130, 246, 0.15)` | `var(--primary)` | `var(--primary)` | `Check` |
| **Slot: Cancelled**| `Cancelled` | `var(--danger-bg)` | `var(--danger-border)` | `var(--danger-text)` | `XCircle` |
| **Booking: Confirmed**| `Confirmed` | `var(--success-bg)` | `var(--success-border)` | `var(--success-text)` | `CheckCircle2` |
| **Booking: Waitlist** | `Waitlisted #N` | `var(--warning-bg)` | `var(--warning-border)` | `var(--warning-text)` | `ListOrdered` |
| **User: Active** | `Active` | `var(--success-bg)` | `var(--success-border)` | `var(--success-text)` | `ShieldCheck` |
| **User: Inactive** | `Suspended` | `var(--danger-bg)` | `var(--danger-border)` | `var(--danger-text)` | `ShieldAlert` |

---

## 8. Responsive Design & Breakpoints

- **Mobile (`< 640px`)**:
  - Single-column stacked layouts.
  - Full-width buttons and form inputs.
  - Date Picker displayed full-width above time-slot chips.
  - Tables convert to scrollable containers with clear visual boundary cues or responsive card views.
  - Top navigation collapses into a clean mobile header with a mobile drawer/dropdown for role switching and logout.
  - Touch targets guaranteed at minimum `44px × 44px`.
- **Tablet (`640px – 1024px`)**:
  - Two-column layouts for Discovery & Booking (calendar left, slots right).
  - Metrics cards display 2 per row.
  - Compact table rows with essential columns preserved.
- **Desktop (`> 1024px`)**:
  - Full multi-column views: Discovery sidebar + Date Picker + Time Slot Grid.
  - Admin metrics in 4-column KPI grid.
  - Tables render with all metadata columns and row actions visible.

---

## 9. Accessibility (WCAG 2.1 AA)

- **Color Contrast**: All primary text maintains ≥ 7:1 against `--bg-surface`, and secondary/badge text maintains ≥ 4.5:1.
- **Keyboard Navigation**:
  - Full logical tab order across every interactive element.
  - Accessible focus rings using `:focus-visible` that never blend into the background.
  - Modals trap focus and return focus to the trigger on dismiss.
- **Form Association**:
  - Every `<input>`, `<select>`, and `<textarea>` has an explicit `<label htmlFor="...">`.
  - Validation errors have `id="..."` and are linked via `aria-describedby` with `role="alert"`.
- **Screen Reader Clarity**:
  - Icon-only buttons have descriptive `aria-label` attributes.
  - Decorative icons include `aria-hidden="true"`.
  - Dynamic status changes (e.g. booking confirmation) announced via `aria-live="polite"`.
- **Motion Reduction**:
  - When `prefers-reduced-motion: reduce` is active, all transitions and animations are collapsed to `0.001ms`.

---

## 10. Motion Principles

- **Functional Only**: Motion answers user action (opening a dialog, dismissing a toast, selecting a slot) and never exists purely for decoration.
- **Durations**:
  - Fast micro-interactions (hover, button press): `150ms ease-out`
  - Panel & Dialog entries: `200ms ease-out`
  - Expand / Collapse transitions: `250ms cubic-bezier(0.16, 1, 0.3, 1)`
- **Transforms**: Limited to `opacity` and `transform` (`translateY`, `scale`) for 60fps compositor performance without layout thrashing.
