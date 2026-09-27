# Frontend Agent Skills Stack

## Overview

To execute the master frontend upgrade with precision, consistency, and professional design intelligence, six specialized agent skills have been researched, verified, and installed into both the global Antigravity configuration (`~/.gemini/config/skills/`) and the project workspace (`.agents/skills/`).

These skills guide the design system, architectural decomposition, visual refinement, interaction design, accessibility audits, and React performance.

---

## Skill Inventory & Verification

| Skill Name | Official Source | Primary Purpose | Status | Antigravity Scope |
|---|---|---|---|---|
| **UI/UX Pro Max** | `nextlevelbuilder/ui-ux-pro-max-skill` | Design system intelligence, style selection, typography scales, semantic palettes, UX patterns | Installed & Verified | Workspace & Global |
| **Impeccable** | `pbakaus/impeccable` | Visual refinement, hierarchy, spacing, polish, anti-pattern detection, production-grade aesthetics | Installed & Verified | Workspace & Global |
| **Frontend Design** | `anthropics/skills` (`frontend-design`) | Distinctive UI composition, avoiding generic AI templates, intentional aesthetic direction | Installed & Verified | Workspace & Global |
| **Web Interface Guidelines** | `vercel-labs/agent-skills` + `web-interface-guidelines` | Accessibility, focus management, keyboard support, form interaction, responsive design | Installed & Verified | Workspace & Global |
| **React Best Practices** | `vercel-labs/agent-skills` (`react-best-practices`) | Eliminating waterfalls, bundle optimization, re-render avoidance, efficient data fetching | Installed & Verified | Workspace & Global |
| **React Composition Patterns** | Builtin architectural patterns | Reusable component architecture, slot patterns, compound components, avoiding prop explosion | Installed & Verified | Workspace & Global |

---

## Detailed Skill Profiles & Usage Plan

### 1. UI/UX Pro Max (`ui-ux-pro-max`)
- **Source**: `https://github.com/nextlevelbuilder/ui-ux-pro-max-skill`
- **Purpose**: Provides structured guidance on design tokens, spacing scales (4px/8px grid), typography hierarchies, semantic color pairings (neutral slate, deep navy, subtle accents), and domain-specific UX heuristics.
- **How It Will Be Used**:
  - Defining the authoritative design system tokens in `docs/DESIGN_SYSTEM.md`.
  - Guiding status badge colors (available, booked, waitlist, cancelled) with guaranteed contrast ratios (≥ 4.5:1).
  - Determining touch target minimums (44×44px) and layout breakpoints.
- **Conflicts / Overlap**: Minor overlap with Impeccable on spacing and hierarchy. Resolved by using UI/UX Pro Max for token definition and Impeccable for fine visual review and polish.
- **Reason for Keeping**: Most comprehensive repository of design rules, token scales, and UX priority lists.

### 2. Impeccable (`impeccable`)
- **Source**: `https://github.com/pbakaus/impeccable` (created by Paul Bakaus)
- **Purpose**: Acts as an opinionated design director. Enforces the "Operate" mode for SaaS dashboards (clarity, scanability, and task completion outranking decorative expression). Detects and flags AI tells (e.g., nested cards, floating blobs, excessive gradients).
- **How It Will Be Used**:
  - Reviewing component layouts (Student slot list, Professor schedule list, Admin user table).
  - Eliminating visual clutter and redundant card wrappers.
  - Ensuring the design feels like a restrained, elite academic SaaS platform.
- **Conflicts / Overlap**: Shares "anti-slop" goals with Anthropic frontend-design. Resolved by using Impeccable for operational UI polish and Anthropic for overall visual personality.
- **Reason for Keeping**: Explicit focus on operational SaaS interfaces and zero-tolerance for decorative fluff.

### 3. Frontend Design (`frontend-design`)
- **Source**: `https://github.com/anthropics/skills`
- **Purpose**: Directs visual identity and personality. Prevents default templated aesthetics (e.g. generic purple gradients, warm cream landing page clichés).
- **How It Will Be Used**:
  - Setting the typography and layout rhythm for the app shell and authentication screens.
  - Ensuring deliberate, restrained styling: crisp typography, subtle borders, high information density, calm slate/navy background.
- **Conflicts / Overlap**: Focuses on visual identity; complements technical performance skills.
- **Reason for Keeping**: Provides high-level aesthetic guardrails that keep the application grounded in its academic scheduling reality.

### 4. Vercel Web Interface Guidelines (`web-design-guidelines`)
- **Source**: `https://github.com/vercel-labs/agent-skills` + `web-interface-guidelines`
- **Purpose**: High-signal checklist for accessibility, interaction design, form handling, and focus states.
- **How It Will Be Used**:
  - Enforcing visible focus rings (`:focus-visible`).
  - Ensuring accessible form labels, clickable labels (`htmlFor`), and proper input types.
  - Verifying modal dialog accessibility (focus trapping, Escape to dismiss, `aria-modal`).
  - Validating destructive action confirmations (cancelling slots/bookings, deactivating users).
- **Conflicts / Overlap**: Focuses exclusively on interface ergonomics and accessibility standards. Zero conflict.
- **Reason for Keeping**: Indispensable quality gate before marking any frontend work complete.

### 5. Vercel React Best Practices (`react-best-practices`)
- **Source**: `https://github.com/vercel-labs/agent-skills`
- **Purpose**: Performance guidelines from Vercel Engineering: eliminating request waterfalls, re-render optimization, bundle size management, and clean data flow.
- **How It Will Be Used**:
  - Structuring API calls in Student, Professor, and Admin features to prevent waterfalls.
  - Keeping form state uncontrolled (React Hook Form) to avoid typing latency.
  - Preventing unnecessary re-renders of large slot and booking lists.
- **Conflicts / Overlap**: Purely technical runtime guidance; complements visual skills.
- **Reason for Keeping**: Ensures the application is not only visually beautiful but snappy and lightweight.

### 6. React Composition Patterns (`react-composition-patterns`)
- **Source**: Industry standard architecture (headless primitives + compound components)
- **Purpose**: Guides the refactoring of the monolithic `main.jsx` into modular, composable primitives in `src/components/ui/` and `src/features/`.
- **How It Will Be Used**:
  - Breaking down giant prop-heavy functions into composable compound components (`Card`, `CardHeader`, `CardContent`, `CardFooter`, `Dialog`, `DialogTrigger`, `DialogContent`).
  - Inverting control so feature components can assemble layouts without fighting rigid parent abstractions.
- **Conflicts / Overlap**: Complements React Best Practices on the component structure side.
- **Reason for Keeping**: Eliminates prop explosion and maintains long-term code maintainability.
