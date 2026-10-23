# HabitFlow — Daily Habit Tracker & Analytics

An offline-first, native Android habit tracker built with Kotlin and Jetpack Compose. Faithfully transforms the prototype into a fully working application featuring daily tracking, interactive completion gauges, activity heatmaps, weekly charts, habit detail drill-downs, and scheduled reminder notifications backed by Room.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed based on your requirements:
> - **Storage Strategy**: Local on-device persistence powered by Room database with reactive `Flow` queries for instant, offline-first performance.
> - **Notifications**: Local Android notifications scheduled for habit reminder times (e.g., 9:00 AM) using `AlarmManager` and `NotificationManager`.
> - **Initial Data**: Prepopulated with the exact prototype state (Mansoor's profile, today's 5 habits with 4/5 completed, 7-day streak, and contribution heatmap history) so the app looks complete on first launch while allowing full editing.

---

## 1. Overview & Core Concept

- **What It Does**: HabitFlow helps users build sustainable daily routines through micro-actions, streak psychology, and clear visual feedback. Users can check off daily habits, inspect activity heatmaps, view completion rates, set custom reminders, and analyze streak metrics.
- **Target Persona**: Individuals seeking a distraction-free, aesthetically pleasing habit companion with zero cloud lock-in.
- **Key Value**: A fast, tactile experience with satisfying completion animations, comprehensive progress visualizations, and dependable offline persistence.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Daily Tracking (Today Screen)**:
   - Header with personalized greeting ("Good Morning, Mansoor 🌱") and date indicator.
   - Circular progress ring showing habits completed (e.g. 4/5) and a linear status bar.
   - List of today's habits with custom iconography, frequency notes, and interactive checkmarks. Tapping the checkmark toggles state with instant feedback and updates the ring gauge. Tapping a habit card navigates to Habit Detail.
   - Floating/Header `+ Add` button opens an Add Habit sheet.

2. **Analytics & Insights (Progress Screen)**:
   - Month selector dropdown (`Oct 2026`).
   - **Daily Activity Heatmap**: Interactive multi-month contribution matrix (Monday–Sunday rows, color-scaled from soft tint to deep forest green).
   - **Weekly Progress Bar Chart**: Mon–Sun completion column bars with benchmark indicators.
   - **Completion Rate Ring**: Overall percentage donut chart with Completed vs. Missed counts.
   - **Streak Card**: Current streak count (7 days) with milestone progress dots.
   - **Category Breakdown**: Filterable tags (Health, Productivity, Learning, Lifestyle).

3. **Habit Detail & History Screen**:
   - Header with habit title, icon badge, category tag, and personal motivation note.
   - Attributes overview: Goal, Frequency, and Reminder time.
   - **Completion History Calendar**: Monthly grid showing completed days with filled circular markers and current day highlight.
   - **Streak Metrics**: Current streak, best streak, total completions, and longest streak badge.
   - "Edit Habit" sheet trigger to update habit parameters or adjust reminder time.

4. **Habits List & Profile Views**:
   - Dedicated habits tab for managing all active and archived habits.
   - Profile tab for user name customization, notification preferences, and data reset/export.

### Visual Identity & Theme

- **Palette**: Warm Organic & Sage Green theme extracted directly from the prototype:
  - *Background*: Neutral Off-White (`#F7F9F6`)
  - *Surface*: Pure White (`#FFFFFF`) with subtle tonal elevation and rounded corners (20.dp)
  - *Primary*: Deep Forest Green (`#2D6A4F` / `#22543D`)
  - *Primary Container / Accent*: Soft Sage Green (`#E8F5E9` / `#D4EDDA`)
  - *Category Tags*:
    - Health: Sage Green (`#DCFCE7` / `#16A34A`)
    - Productivity: Soft Cyan/Blue (`#E0F2FE` / `#0284C7`)
    - Learning: Lavender/Purple (`#F3E8FF` / `#7E22CE`)
    - Lifestyle: Warm Orange (`#FFEDD5` / `#C2410C`)
- **Typography**: Clean modern sans-serif typography hierarchy with bold title weights, legible metric counters, and accessible touch targets.
- **Micro-Interactions**: Spring-animated circular progress rings, checkmark completion bounce, and calendar date highlights.

---

## 3. Key Product Decisions & Trade-Offs

- **Room SQLite Persistence**:
  - *Chosen Approach*: Android Jetpack Room with Kotlin Coroutines and `Flow`.
  - *Why*: Delivers instantaneous launch times, zero network dependency, and complete privacy for habit records.
  - *Alternatives Considered*: Cloud Firestore (deferred per user confirmation to keep data local and offline).
- **Custom Canvas Charts vs. External Libraries**:
  - *Chosen Approach*: Custom Jetpack Compose Canvas implementations for the Activity Heatmap, Weekly Bar Chart, and Donut Gauge.
  - *Why*: Guarantees pixel-perfect alignment with the design prototype (exact cell spacing, corner radii, and color ramp) with zero external dependency bloat.
- **Local AlarmManager Reminders**:
  - *Chosen Approach*: Android `AlarmManager` with `BroadcastReceiver` and `NotificationManagerCompat`.
  - *Why*: Reliable trigger at specified reminder times even when the application is closed.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI                       │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐   │
│  │  Today Screen  │ │Progress Screen │ │ Habit Detail   │   │
│  │(Gauge, List)   │ │(Heatmap, Charts│ │(Calendar,Stats)│   │
│  └───────┬────────┘ └───────┬────────┘ └───────┬────────┘   │
└──────────┼──────────────────┼──────────────────┼────────────┘
           │                  │                  │
┌──────────▼──────────────────▼──────────────────▼────────────┐
│                       HabitViewModel                        │
│   - todayHabitsState : StateFlow<List<HabitWithLog>>        │
│   - progressStatsState : StateFlow<ProgressStats>           │
│   - toggleHabit(id, date) / addHabit(habit)                 │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                      HabitRepository                        │
│   - Observes Room DAO queries via reactive Flow             │
│   - Calculates streaks, heatmap matrix, and completion rates│
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│               Room SQLite Database (AppDatabase)            │
│  ┌──────────────────────┐      ┌─────────────────────────┐  │
│  │    HabitEntity       │1    *│     HabitLogEntity      │  │
│  │ (id, name, goal, ...)├──────┤ (habitId, date, isDone) │  │
│  └──────────────────────┘      └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Data Entities & Schema

1. **`HabitEntity`**:
   - `id: Long` (Primary Key, auto-generated)
   - `title: String` (e.g. "Code", "Read", "Workout")
   - `subtitle: String` (e.g. "Build something every day", "30 minutes")
   - `category: String` ("Productivity", "Health", "Learning", "Lifestyle")
   - `iconName: String` (e.g. "code", "book", "fitness", "water", "moon")
   - `goalDuration: String` (e.g. "1 hour per day")
   - `frequency: String` ("Daily", "Weekdays", "Weekends")
   - `reminderTime: String?` (e.g. "09:00 AM")
   - `reminderEnabled: Boolean`
   - `createdAt: Long`

2. **`HabitLogEntity`**:
   - `id: Long` (Primary Key, auto-generated)
   - `habitId: Long` (Foreign Key referencing HabitEntity)
   - `dateString: String` (ISO format `YYYY-MM-DD` for indexed querying)
   - `completed: Boolean`
   - `completedAt: Long`

3. **`UserPreference`**:
   - Stores user display name ("Mansoor"), reminder notification permissions, and avatar icon.

### Component Breakdown

- **`TodayScreen`**: Greeting header, circular progress widget, habit checklist cards with toggleable state, quick-add dialog.
- **`ProgressScreen`**: Custom Canvas `DailyActivityHeatmap` showing Jan–Oct grid, `WeeklyBarChart` for 7-day volume, `CompletionRateDonut`, streak badges, and category pills.
- **`HabitDetailScreen`**: Back navigation, motivation quote, specification rows, `MonthlyCalendarHistory` marking completed days, and streak stats cards.
- **`AddEditHabitDialog`**: Form with title, category selector chips, icon selector, goal inputs, and time picker for reminders.
- **`HabitReminderReceiver`**: Broadcast receiver that posts user notifications when alarms fire.
