# ModProjectTracker

ModProjectTracker is an Android admin app for project and expense management.

## What this repo contains

- `ModProjectTracker`: native Android app built with Java + XML.
- `MyProjectTracker`: Flutter user app, not present in this workspace.

## Current app purpose

The app helps an admin:

- manage projects and budgets,
- manage project expenses,
- review expense lifecycle states,
- sync local data with cloud storage,
- and use an in-app AI assistant for support and budget guidance.

## Current implementation status

- Phase 1-2: done. Local auth, project CRUD, validation, search/sort/filter, owner permissions.
- Phase 3: done. Sync and Cloudinary gate completed.
- Phase 4: partial. Expense detail, status transitions, search/sort, and dashboard overview are in place.
- Phase 5: partial. AI assistant is functional with preloaded context.
- Phase 6-8: not yet started. Flutter user app source is not in this workspace.

## Key technical areas

- Local auth: `LoginActivity`, `RegisterActivity`, `SessionManager`.
- Project CRUD: `ProjectListActivity`, `ProjectFormActivity`, `ProjectDetailActivity`.
- Data layer: `DatabaseHelper` with SQLite and soft delete.
- Sync: `SyncCoordinator`, `SyncWorker`, `SyncMergePolicyTest`.
- Cloud upload: `CloudinaryUploadService` and project image upload.
- Expense module: `ExpenseListActivity`, `ExpenseFormActivity`, `ExpenseDetailActivity`, `ExpenseAdapter`.
- AI assistant: `AiAssistantActivity`, `GeminiService`.

## AI assistant behavior

- `AiAssistantActivity` builds a preloaded prompt with `R.string.ai_preload_role_prompt`, context totals, project rows, expense rows, and the user question.
- `GeminiService` sends requests through direct REST calls (`OkHttp`) to the Google Generative Language API.
- Default model is `gemini-2.5-flash` (`GEMINI_MODEL` can override).
- Current data scope is admin-visible project data, not strict per-account isolation.

## Canonical docs

- `docs/PROJECT_CURRENT_STATE_FULL.md`
- `docs/README_PHASE_STATUS.md`
- `docs/README_PROGRESS_SECTIONS.md`
- `docs/README_AUTOMATION.md`
- `docs/PHASE3_GATE_EVIDENCE.md`
- `docs/MIGRATION_SUMMARY.md`
- `docs/DOCUMENTATION_INDEX.md`
- `docs/CHANGELOG_DOCS.md`

## Build

```powershell
Set-Location "C:\Users\Admin\AndroidStudioProjects\ModProjectTracker"
.\gradlew.bat :app:compileDebugJavaWithJavac
```

## Auto progress block

<!-- AUTO_PHASE_PROGRESS:START -->
_Auto generated from docs/phase-tracker.json on 2026-04-21._

**Overall status:** Phase 3 gate is closed (Sync + Cloudinary); Phase 4 includes expense detail and dashboard KPIs; Phase 5 has AI preload context and Gemini REST integration.

| Phase | Mo ta | Trang thai |
|---|---|---|
| 1 | Local models + SQLite | Done |
| 2 | Admin Project CRUD | Done |
| 3 | Cloudinary + Sync | Done |
| 4 | Expense UI + Approval | Partial (lifecycle and core flows are implemented; full approval history/workflow is pending) |
| 5 | Gemini AI | Partial (context-preloaded assistant is implemented; hardening is pending) |
| 6-8 | Flutter user app | Not Yet |

### Current progress
- Local SQLite authentication for admin users is stable.
- Project CRUD, validation, and owner permissions are working.
- Two-way sync merges by lastModified and supports soft-delete tombstones.
- WorkManager periodic sync with retry/backoff is active.
- Phase 3 gate evidence is documented in docs/PHASE3_GATE_EVIDENCE.md.
- Cloudinary flow includes configuration checks and clear error handling.
- Expense CRUD per project is implemented with list/form/detail screens.
- Expense lifecycle transitions are available: Draft -> Pending Approval -> Approved -> Paid (Reject at Pending).
- Expense list supports search and sort.
- Dashboard shows expense overview and budget KPIs.
- AI assistant composes a preloaded role + context prompt in AiAssistantActivity.
- GeminiService sends REST requests with retry/backoff and rate-limit cooldown handling.
- Default Gemini model is gemini-2.5-flash, configurable by GEMINI_MODEL.
- Expense title validation is enforced with inline required/min-length checks.
- SyncMergePolicyTest covers offline create/update, cloud newer, and tombstone newer scenarios.

### Next milestones
1. Complete Phase 4: approval notes/history + dashboard summary
2. Harden Phase 5: AI context/history/fallback UX
3. Build automated sync regression tests
<!-- AUTO_PHASE_PROGRESS:END -->















