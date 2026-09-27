---
name: react-composition-patterns
description: "Architectural guidance for React component composition, headless primitives, compound components, avoiding prop explosion, and building scalable UI hierarchies."
---

# React Composition Patterns

## Overview
Guidelines for building clean, maintainable, composable React component systems. Prevents prop drilling, giant config objects, and boolean prop proliferation by favoring composition, inversion of control, and headless primitives.

## Core Principles

### 1. Inversion of Control over Boolean Props
Instead of adding boolean flags for every minor layout variation (`isHeaderCompact`, `hasBadge`, `showFooter`, `withWarningBanner`), expose compound slots or accept `children`.
```tsx
// Anti-Pattern: Prop explosion
<Card
  title="Office Hours"
  badgeText="Open"
  showFooter={true}
  footerButtonText="Book"
  isCompact={true}
  onFooterClick={handleBook}
/>

// Composition Pattern: Composable Slots
<Card size="sm">
  <CardHeader>
    <CardTitle>Office Hours</CardTitle>
    <Badge variant="success">Open</Badge>
  </CardHeader>
  <CardContent>...</CardContent>
  <CardFooter>
    <Button onClick={handleBook}>Book</Button>
  </CardFooter>
</Card>
```

### 2. Compound Components with Context
For stateful sets of cooperating components (Tabs, Accordions, Dialogs, Selects), share state via an internal Context rather than passing handlers and active indexes through multiple layers.
- Root manages or delegates state (`<Tabs value={tab} onValueChange={setTab}>`)
- Triggers trigger state changes (`<TabsTrigger value="schedule">`)
- Panels render conditionally (`<TabsContent value="schedule">`)

### 3. Headless Primitives + Owned Styles (The shadcn Pattern)
- Separate accessibility/state mechanics from visual presentation.
- Use Radix UI or native semantic HTML for the primitive foundation.
- Wrap primitives in a consistent local component API (`src/components/ui/`) that applies design tokens.
- Allow `className` merging and prop forwarding (`...props`) using standard HTML attributes.

### 4. Slot & AsChild Pattern
Allow components to merge their behavior and styles into a custom child element without rendering redundant DOM wrappers.
- E.g. Radix `Slot` or accepting `asChild` so `<Button asChild><Link to="/book">Book</Link></Button>` renders a clean `<a>` with button styling and keyboard support.

### 5. Co-location of Role & Feature Logic
- Generic primitives live in `components/ui/` (Button, Dialog, Card, Input, Badge).
- Application-level shared elements live in `components/common/` (Header, PageHeading, EmptyState).
- Role-specific workflows live in `features/<role>/` (e.g. `features/student/StudentBookingFlow.jsx`).
- Avoid premature global abstraction: if a component is only used within the Professor schedule, keep it in `features/professor/`.

### 6. Controlled vs Uncontrolled Forms
- Use uncontrolled inputs with `react-hook-form` for complex forms to avoid rerendering entire dashboards on every character input.
- Keep validation schemas declarative (Zod) outside component render bodies.
