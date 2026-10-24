# HabitFlow — Complete Improvement & Bug Fix Plan

A comprehensive architectural upgrade to transform HabitFlow into a polished, high-contrast monochrome habit tracker. Features a unified black-and-white design system, dynamic analytics driven strictly by stored user data, a fully navigable interactive calendar with per-date habit completion checklists, centralized category management, expanded categorized Material icon picker, and an enhanced Add/Edit form with duration presets and custom reminder scheduling.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed for this implementation:
> - **Monochrome Palette**: High-contrast black, white, and neutral grays across all screens, charts, gauges, and dialogs.
> - **Icon Picker**: Clean, categorized monochrome Material Icons library covering Fitness, Health, Productivity, Learning, Mindfulness, and Lifestyle.
> - **Calendar Interaction**: Month/year navigation with an interactive habit checklist rendered directly below the calendar for any selected date.
> - **Reset All Data**: One clear, confirmed reset action that wipes all habits, history, and streaks back to a fresh blank slate with standard default categories.

---

## 1. Overview & Core Concept

- **Refined Experience**: A distraction-free, minimalist monochrome aesthetic that emphasizes typography, contrast, and tactile feedback.
- **Data Integrity**: 100% dynamic calculations across streaks, completion rates, weekly progress, monthly heatmaps, and category statistics with zero hardcoded fallbacks or simulated numbers.
- **Daily Continuity**: Preserves the responsive Today dashboard while expanding the Habits and Calendar sections with full CRUD capabilities.

---

## 2. User Experience & Visual Design

### Visual Identity & Monochrome Theme

- **Palette**:
  - *Primary / Accent*: Pure Black (`#000000`) in light mode / Pure White (`#FFFFFF`) in dark mode
  - *Background*: Clean Off-White (`#F9F9F9` / `#FAFAFA`)
  - *Card Surface*: Pure White (`#FFFFFF`) with crisp 1.dp neutral borders (`#E4E4E7`)
  - *Muted & Secondary*: Neutral Grays (`#71717A` for subtitles/secondary text, `#A1A1AA` for captions, `#E4E4E7` for dividers)
  - *Selection & Chips*: Filled Black with White text for selected states; White with gray border for unselected states.
- **Charts & Gauges**:
  - Heatmap: 4 monochromatic levels (Level 0: `#F4F4F5`, Level 1: `#D4D4D8`, Level 2: `#71717A`, Level 3: `#18181B`).
  - Bar Chart & Donut: Solid black progress arcs/bars against subtle gray track backgrounds.

### Screen & Feature Upgrades

1. **Today Dashboard**:
   - Preserves current workflow while updating all styling to the crisp monochrome theme.
   - Dynamic gauge reflecting today's real habit completion ratio (0/0 when empty with friendly zero-state).

2. **Progress Screen**:
   - Removes the redundant Habit Categories section.
   - 100% Dynamic calculations:
     - Donut completion rate calculated strictly from real logs.
     - Heatmap rendered from actual logged timestamps.
     - Weekly bar chart reflecting real completions per day.
     - Streak counter dynamically traversing consecutive active dates with zero hardcoded fallbacks.

3. **Habit Detail & Navigable Calendar**:
   - Header with month/year navigation arrows (`<`, `>`) and dropdown picker.
   - Month boundaries, leap years, and dates accurately calculated.
   - Any date is selectable: displays an interactive habit completion checklist for that date directly beneath the calendar.
   - Users can mark habits as completed or missed for past or current dates.

4. **Habits Section (Full CRUD & Category Management)**:
   - Category management bar with "+ Add Category" button and custom category creation dialog.
   - Full habit cards with edit and delete actions (with confirmation dialog).
   - Instant search/filter by category.

5. **Profile & Unified Reset Flow**:
   - Replaces all old reset buttons with a single **"Reset All Data"** card.
   - Confirmation dialog explaining that all habits, progress, streaks, and custom categories will be cleared to a fresh blank slate.
   - Single-tap atomic reset that updates the database and refreshes all UI states immediately.

6. **Add/Edit Habit Form Enhancements**:
   - **Text Readability**: Fixed high-contrast input text, labels, and placeholders.
   - **Smart Duration Suggestions**: Quick-select preset chips (5m, 10m, 15m, 20m, 30m, 45m, 60m) + context suggestions based on habit name + custom text field.
   - **Expanded Icon Picker**: Modal/grid organized into Fitness, Health, Productivity, Learning, Mindfulness, Lifestyle.
   - **Reminder Time Picker**: Interactive time selection dialog with persistent AlarmManager rescheduling.

---

## 3. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    HabitFlow Architecture                   │
│                                                             │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐  │
│  │ TodayScreen  │ │ProgressScreen│ │ Habits & Categories │  │
│  └──────┬───────┘ └──────┬───────┘ └──────────┬──────────┘  │
│         │                │                    │             │
│  ┌──────▼────────────────▼────────────────────▼──────────┐  │
│  │                    HabitViewModel                     │  │
│  │   - todayHabitsState / progressStatsState (Dynamic)   │  │
│  │   - selectedCalendarDate / dateHabitsState            │  │
│  │   - categoriesState / customCategories               │  │
│  │   - resetAllData() [Atomic Database Purge]            │  │
│  └───────────────────────┬───────────────────────────────┘  │
│                          │                                  │
│  ┌───────────────────────▼───────────────────────────────┐  │
│  │                    HabitRepository                    │  │
│  │   - Dynamic Streak & Completion Rate algorithms       │  │
│  │   - Real-time Room DAO reactive Flow queries          │  │
│  └───────────────────────┬───────────────────────────────┘  │
│                          │                                  │
│  ┌───────────────────────▼───────────────────────────────┐  │
│  │            Room Database (AppDatabase v2)             │  │
│  │  HabitEntity │ HabitLogEntity │ CategoryEntity        │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Key Schema & Entity Updates:
- **`CategoryEntity`**: Persistent table for user-created custom categories alongside default ones ("Productivity", "Health", "Learning", "Lifestyle").
- **Dynamic Stats Engine**: Replaces hardcoded default constants (`80`, `7`, `12`, `18`) in `HabitRepository` with accurate mathematical reductions over Room log records, returning 0 and graceful empty states when no data is logged.
- **Calendar Engine**: Custom date state management allowing arbitrary year/month traversal and date selection.
