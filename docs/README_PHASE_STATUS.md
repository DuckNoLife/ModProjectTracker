# Phase Status Analysis

This document maps the planned phases to the code that currently exists in the repository.

## Status definitions

- `Done`: implemented and present in the repo.
- `Partial`: important pieces exist, but the workflow is not complete.
- `Not Yet`: not implemented in the repo.

## Admin app requirement matrix

| Area | Status | Evidence |
|---|---|---|
| Local auth + admin role | Done | `LoginActivity`, `RegisterActivity`, `SessionManager` |
| Project CRUD + validation | Done | `ProjectListActivity`, `ProjectFormActivity`, `ProjectDetailActivity`, `ValidationUtil` |
| Project search/sort/filter | Done | `ProjectListActivity` |
| SQLite schema + soft delete | Done | `DatabaseHelper`, `Constants` |
| Local/cloud sync | Done | `SyncCoordinator`, `SyncWorker`, `SyncMergePolicyTest` |
| Cloudinary upload | Done | `CloudinaryUploadService`, `ProjectFormActivity`, `docs/PHASE3_GATE_EVIDENCE.md` |
| Expense CRUD per project | Done | `ExpenseListActivity`, `ExpenseFormActivity`, `ExpenseAdapter` |
| Full expense approval workflow | Partial | `ExpenseDetailActivity` exists; notes/history are still missing |
| Gemini AI assistant | Partial | `AiAssistantActivity` preloads role/context prompt; `GeminiService` sends REST requests with retry/cooldown |

## Flutter user app requirement matrix

| Area | Status | Notes |
|---|---|---|
| Flutter app scaffold | Not Yet | No `pubspec.yaml` or `.dart` source in this workspace |
| User auth + offline sync | Not Yet | No Flutter source available |
| Read-only projects + expense submission | Not Yet | No Flutter source available |

## Summary

- Phase 1-2: done.
- Phase 3: done and gate evidence is recorded.
- Phase 4: partial; expense CRUD, detail, search/sort, and dashboard overview are present, but the approval workflow is incomplete.
- Phase 5: partial; the AI assistant preload flow is present, but history/scoping hardening is pending.
- Phase 6-8: not yet started.

## Auto gate block

<!-- AUTO_PHASE_GATE:START -->
_Auto generated from docs/phase-tracker.json on 2026-04-21._

## Phase matrix (synced)

| Phase | Mo ta | Trang thai |
|---|---|---|
| 1 | Local models + SQLite | Done |
| 2 | Admin Project CRUD | Done |
| 3 | Cloudinary + Sync | Done |
| 4 | Expense UI + Approval | Partial (lifecycle and core flows are implemented; full approval history/workflow is pending) |
| 5 | Gemini AI | Partial (context-preloaded assistant is implemented; hardening is pending) |
| 6-8 | Flutter user app | Not Yet |

## Gate checklist

### Phase 3 - Sync & Cloudinary gate
- [x] Tombstone schema (isDeleted/deletedAt) with safe migration support
- [x] Two-way delete sync (local <-> Firebase) based on lastModified
- [x] Periodic background sync with retry/backoff
- [x] Offline -> online merge scenarios documented in the report
- [x] Cloudinary flow has configuration validation and clear error handling
<!-- AUTO_PHASE_GATE:END -->









