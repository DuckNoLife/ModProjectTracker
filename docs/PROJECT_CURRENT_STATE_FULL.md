# ModProjectTracker - Current State (Full Snapshot)

_Last updated: 2026-04-21_

This document is the current code-backed snapshot of the Android module in this workspace.

## 1) Current scope

- Available source: `ModProjectTracker` Android admin app, built with Java + XML.
- Not available: Flutter user app `MyProjectTracker`.
- Authentication: local SQLite plus `SessionManager`, not Firebase Auth.

## 2) Confirmed features

### Auth and session

- `LoginActivity`, `RegisterActivity`, `SessionManager`.
- Admin-only access gate.

### Project management

- `ProjectListActivity`: list, search, sort, and status filtering.
- `ProjectFormActivity`: create/update, validation, and optional cloud image upload.
- `ProjectDetailActivity`: detail view with owner-only edit/delete actions.
- `ProjectAdapter`: card list with owner badge.

### Data layer

- `DatabaseHelper` creates `users`, `projects`, and `expenses` tables.
- Foreign keys, cascade relations, and soft-delete tombstones (`isDeleted`, `deletedAt`).
- Queries for total spend, pending count, and filtering by date, status, and type.

### Sync and offline support

- `SyncCoordinator`: merges local and cloud records using `lastModified`.
- `SyncWorker`: periodic background sync with retry/failure handling.
- `MainActivity`: schedules the worker and triggers best-effort sync on resume.
- `SyncMergePolicyTest`: verifies local newer, cloud newer, and tombstone newer cases.

### Cloudinary

- `CloudinaryUploadService`: uploads images using an unsigned preset.
- `ProjectFormActivity`: opens the document picker, uploads the file, and stores `imageUrl`.

### Expense module

- `ExpenseListActivity`: project-scoped list with total, status filter, search, sort, delete, edit, and detail entry.
- `ExpenseFormActivity`: create/update expense, required title, and status lifecycle spinner.
- `ExpenseDetailActivity`: detail screen with lifecycle transitions (`Draft -> Pending Approval -> Approved -> Paid`, with Reject at Pending).
- `ExpenseAdapter`: shows title, claimant, amount, metadata, and actions.
- Navigation from `ProjectDetailActivity` to `ExpenseListActivity`.

### AI assistant

- `AiAssistantActivity` and `GeminiService` provide the AI assistant flow.
- `AiAssistantActivity` composes a preloaded prompt from:
  - role/rules prompt in `R.string.ai_preload_role_prompt`,
  - aggregated context (`projects`, lifecycle totals, and recent expense rows),
  - user question.
- `GeminiService` uses direct REST calls through `OkHttp` to the Google Generative Language API.
- Default model is `gemini-2.5-flash` with optional override from `BuildConfig.GEMINI_MODEL`.

## 3) Navigation map

- Launcher: `LoginActivity`.
- After login: `MainActivity`.
- Bottom navigation: Dashboard -> Projects -> Settings dialog.
- Project flow: Dashboard/List -> Detail -> Form.
- Expense flow: ProjectDetail -> ExpenseList -> ExpenseDetail/ExpenseForm.

## 4) Business rules enforced

- Only logged-in admins can access the main dashboard and modules.
- Only the project owner can edit or delete a project.
- Project and expense deletion use soft delete, not hard delete.
- Main lists only show active records (`isDeleted = 0`).

## 5) Phase status

- Phase 1: done.
- Phase 2: done.
- Phase 3: done, with Sync + Cloudinary gate completed.
- Phase 4: partial, with expense detail, transitions, search/sort, and dashboard overview already in place.
- Phase 5: partial, with the AI assistant baseline already in place.
- Phase 6-8: not yet.

## 6) Open gaps

1. Full approval workflow is still missing, excluding any permission matrix.
2. Automated regression tests for approval and sync are still missing.
3. Cloudinary UX hardening still needs camera, crop, and progress polish.
4. Gemini hardening still needs conversation history and fallback quality improvements.
5. Advanced dashboard analytics are still missing.
6. The Flutter user app source is still absent from the workspace.
7. AI context scoping is not strict per-account: `getProjectsVisibleToAdmin` is role-based and expenses are loaded globally before project filtering.

## 7) Source of truth for progress updates

- `docs/phase-tracker.json`
- `scripts/Update-ReadmeProgress.ps1`
- `docs/README_AUTOMATION.md`

When updating progress, edit `docs/phase-tracker.json` first and then run the automation script to sync the marker blocks.

