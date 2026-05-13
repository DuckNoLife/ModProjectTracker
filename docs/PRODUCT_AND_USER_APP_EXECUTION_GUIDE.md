# Product and User App Execution Guide

_Last updated: 2026-04-21_

## 1) Document Purpose

This is the single working document to:

- capture the product purpose and scope,
- summarize the current implemented state of `ModProjectTracker` (Android Admin app),
- define a concrete development direction for `UserProjectTracker` (Flutter app),
- provide an actionable execution checklist to continue delivery.

Use this file as the project handoff and execution reference.

---

## 2) Product Vision and Goals

## Core product goal

Build a two-side project expense platform:

1. **Admin side (`ModProjectTracker`)** for project planning, budget control, expense governance, sync, and AI-assisted analysis.
2. **User side (`UserProjectTracker`, Flutter)** for project visibility, expense submission, and lifecycle tracking.

## Business outcomes

- Keep project budgets and spending transparent.
- Enforce a clear expense lifecycle: `Draft -> Pending Approval -> Approved -> Paid`.
- Support offline-first behavior with safe sync.
- Reduce admin effort with AI-assisted summaries and guidance.

---

## 3) Current State: `ModProjectTracker` (Android Admin)

## Stack and architecture

- Platform: Android (Java + XML)
- Local storage: SQLite (`DatabaseHelper`)
- Cloud sync: Firebase Realtime Database
- Background sync: WorkManager
- Media upload: Cloudinary
- AI integration: Gemini REST via OkHttp (`GeminiService`)

## Implemented modules

- **Authentication and session**
  - `LoginActivity`, `RegisterActivity`, `SessionManager`
  - Admin-only gate

- **Project management**
  - List, search, sort, filter, create/edit, detail
  - Owner-aware permissions

- **Expense management**
  - Project-scoped expense list/form/detail
  - Lifecycle transitions in detail screen
  - Search/sort/filter support

- **Dashboard and KPIs**
  - Total projects, active projects, total budget
  - Tracked expense, pending amount, utilization

- **Sync and offline safety**
  - Merge by `lastModified`
  - Soft delete tombstones (`isDeleted`, `deletedAt`)
  - Periodic background sync with retry/backoff

- **AI assistant**
  - Preloaded role prompt from `strings.xml`
  - Preloaded app context (projects, totals, expenses)
  - Conversation history persisted per account
  - Clear-chat support per account
  - Fallback UX for network/rate-limit/auth failures

## Known gaps (admin app)

- Approval notes/history trail not complete
- Advanced analytics not complete
- AI hardening still needed (longer memory strategy, fallback quality tuning)
- Automated sync regression suite still limited

---

## 4) Product Data Model (Shared Concepts)

Use these shared entities across admin and user apps:

- **User**
  - `uid`, `username`, `name`, `role`, `createdAt`

- **Project**
  - `id`, `projectName`, `description`, `startDate`, `endDate`
  - `manager`, `status`, `budget`, `ownerId`
  - `lastModified`, `isDeleted`, `deletedAt`

- **Expense**
  - `id`, `projectId`, `title`, `date`, `amount`, `currency`
  - `type`, `paymentMethod`, `claimant`, `status`
  - `description`, `location`
  - `lastModified`, `isDeleted`, `deletedAt`

## Expense status contract

- Canonical statuses:
  - `Draft`
  - `Pending Approval`
  - `Approved`
  - `Paid`
  - `Rejected`

Any new client must use the same status naming to prevent sync drift.

---

## 5) Direction for `UserProjectTracker` (Flutter)

## Target role of Flutter app

`UserProjectTracker` should be the user-facing channel for:

- viewing assigned/available projects,
- creating and tracking own expense submissions,
- monitoring approval/payment progress,
- working offline and syncing when online.

## Recommended architecture (Flutter)

- **UI**: Flutter Material 3
- **State management**: Riverpod (or BLoC if team preference)
- **Local storage**: Drift or Hive (offline cache + pending queue)
- **Cloud sync**: Firebase Realtime Database (match admin backend)
- **Networking**: Dio
- **Auth**: align with current backend strategy (if no Firebase Auth yet, use current account system contract)
- **File upload**: Cloudinary-compatible upload service abstraction

## Feature boundaries for v1

- **In scope (v1)**
  - Login/session
  - Project list + detail (read-only)
  - Expense create/edit (own expenses)
  - Expense status timeline view
  - Offline create/update + sync queue

- **Out of scope (v1)**
  - Complex approval matrix
  - Advanced BI dashboards
  - Multi-tenant admin controls

---

## 6) Flutter Phased Plan (Practical)

## Phase 6 - Foundation (2-3 weeks)

Deliverables:

- Flutter project scaffold and routing
- App theme, typography, reusable components
- Environment configuration (`dev/stage/prod`)
- Local persistence baseline
- Session/auth shell

Exit criteria:

- App boots with login flow
- Local cache and dependency injection are stable

## Phase 7 - Core user workflows (3-4 weeks)

Deliverables:

- Project list and detail (read-only)
- Expense create/edit/list for current user
- Status display aligned with canonical lifecycle
- Basic validation and error states
- Offline queue for expense mutations

Exit criteria:

- User can submit expense offline and sync later
- Lifecycle display matches admin app statuses

## Phase 8 - Sync hardening and polish (2-3 weeks)

Deliverables:

- Conflict handling by `lastModified`
- Tombstone-aware updates
- Retry/backoff and connectivity observers
- UI polish, loading skeletons, empty states
- QA regression checklist

Exit criteria:

- No data loss in offline-online scenarios
- Stable sync behavior in repeated reconnect tests

---

## 7) Integration Contracts with Admin App

To keep both apps compatible, lock these contracts now:

- Same Firebase paths (`users`, `projects`, `expenses`)
- Same status values (exact text)
- Same `lastModified` conflict rule (newer wins unless tombstone newer)
- Same deletion strategy (`isDeleted`, `deletedAt` soft delete)
- Same currency and amount formatting assumptions where possible

If contract changes are needed, update both clients and this guide in the same release.

---

## 8) Immediate Backlog You Can Start Now

## Admin app (next)

1. Add approval notes/history log to expense transitions.
2. Add regression tests for sync edge cases.
3. Improve AI fallback and context retention strategy.
4. Add observability logs for sync + AI error categories.

## Flutter app (kickoff)

1. Create `UserProjectTracker` repo/module scaffold.
2. Define app layers: `data`, `domain`, `presentation`.
3. Implement login/session shell.
4. Build project list screen with local cache.
5. Build expense create form with lifecycle-safe defaults.

---

## 9) Definition of Done (Cross-App)

A feature is done only if:

- behavior is implemented,
- offline and online behavior is verified,
- sync/conflict behavior is verified,
- user-facing errors are actionable,
- docs are updated (`phase-tracker` + this guide + relevant README).

---

## 10) Suggested Weekly Execution Rhythm

- **Day 1**: plan + contract decisions
- **Day 2-4**: implementation
- **Day 5**: integration test + regression + doc updates

Every Friday, update:

- `docs/phase-tracker.json`
- auto-synced README blocks
- this file (`PRODUCT_AND_USER_APP_EXECUTION_GUIDE.md`)

---

## 11) Final Working Summary

- Admin app is in a strong state through Phase 3 and substantial Phase 4/5 progress.
- The next leverage point is launching Flutter `UserProjectTracker` with strict contract parity.
- Focus on a thin, reliable v1 first (auth, read-only projects, expense submission, offline sync), then harden.

This document is now your single execution baseline for continuing both tracks.

