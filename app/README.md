# ModProjectTracker - Admin App (`app` module)

This document describes the current state of the native Android admin module in `app/`.

## Scope

- Role: admin app.
- Stack: Java + XML + SQLite + Firebase Realtime Database + WorkManager + Cloudinary + Gemini REST API (OkHttp).
- Authentication: local SQLite session management, not Firebase Auth.

## What works today

- Local auth: `LoginActivity`, `RegisterActivity`, `SessionManager`.
- Project CRUD: `ProjectListActivity`, `ProjectFormActivity`, `ProjectDetailActivity`, `ProjectAdapter`.
- Project search, sort, and filter in `ProjectListActivity`.
- Soft delete for `Project` and `Expense` using `isDeleted` and `deletedAt`.
- Sync core with `SyncCoordinator` using `lastModified` merge rules.
- Background sync with `SyncWorker` and periodic scheduling.
- Cloudinary image upload from `ProjectFormActivity`.
- Expense module: `ExpenseListActivity`, `ExpenseFormActivity`, `ExpenseDetailActivity`, `ExpenseAdapter`.
- Expense lifecycle states: `Draft`, `Pending Approval`, `Approved`, `Paid`, `Rejected`.
- Expense detail screen with transition actions.
- Expense search, sort, and dashboard expense overview counters.
- AI assistant with preloaded role prompt and project/expense context in `AiAssistantActivity`.
- Gemini REST request handling in `GeminiService` with retry/backoff and rate-limit cooldown.

## Current limitations

- Full approval workflow is still incomplete; permission matrix is intentionally out of scope.
- Approval notes/history trail is still missing.
- Advanced dashboard analytics are still missing.
- Cloudinary UX hardening still needs work.
- AI assistant hardening still needs conversation history, stricter per-account data isolation, and fallback improvements.

## Quick reference files

- `app/src/main/java/com/example/modprojecttracker/database/DatabaseHelper.java`
- `app/src/main/java/com/example/modprojecttracker/sync/SyncCoordinator.java`
- `app/src/main/java/com/example/modprojecttracker/sync/SyncWorker.java`
- `app/src/main/java/com/example/modprojecttracker/cloud/CloudinaryUploadService.java`
- `app/src/main/java/com/example/modprojecttracker/activity/ExpenseListActivity.java`
- `app/src/main/java/com/example/modprojecttracker/activity/ExpenseFormActivity.java`
- `app/src/main/java/com/example/modprojecttracker/activity/ExpenseDetailActivity.java`
- `app/src/main/java/com/example/modprojecttracker/activity/AiAssistantActivity.java`
- `app/src/main/java/com/example/modprojecttracker/ai/GeminiService.java`

## Auto progress block

<!-- AUTO_PHASE_APP_PROGRESS:START -->
_Auto generated from docs/phase-tracker.json on 2026-04-21._

**Module status:** Phase 3 gate is closed (Sync + Cloudinary); Phase 4 includes expense detail and dashboard KPIs; Phase 5 has AI preload context and Gemini REST integration.

### Completed
- Local login/register/session
- Project list/detail/form with validation
- Soft-delete schema (isDeleted/deletedAt) for Project and Expense
- SyncCoordinator merge logic for local/cloud and tombstones
- SyncWorker periodic background sync with exponential backoff
- Admin expense CRUD per project
- Expense lifecycle status UI (Draft/Pending Approval/Approved/Paid/Rejected)
- Expense title field with inline validation
- Expense status filter in Expense List
- Expense detail transitions
- Expense search/sort in Expense List
- Dashboard expense overview counters
- Dashboard KPIs (budget/spend/pending/utilization)
- AI assistant preload context flow (chat screen + Gemini service + context summary)
- Phase 3 gate evidence recorded in docs/PHASE3_GATE_EVIDENCE.md

### In progress
- Expense approval workflow completion (without expanding permission matrix)
- Approval notes + history/status transition log
- Phase 5 hardening: conversation history and fallback strategy
- Stricter per-account AI data scoping

### Open gaps
- Approval notes + history trail for status transitions
- Flutter user app source
- Automated sync regression test script
- Strict per-account AI context isolation
<!-- AUTO_PHASE_APP_PROGRESS:END -->













