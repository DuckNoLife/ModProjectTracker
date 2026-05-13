# README Progress Sections (Copy-Paste)

This document provides short English sections that can be copied into reports or other README files.

## Executive summary

- The Android admin app has completed the local core in Phase 1-2.
- Phase 3 is complete and the Sync + Cloudinary gate is closed.
- Phase 4 includes expense CRUD, detail, search/sort, and approval transitions, but the full approval workflow is still incomplete.
- Phase 5 has a context-preloaded AI assistant (Gemini REST), and the Flutter user app source is still missing.

## Current progress

- Local authentication is stable.
- Project CRUD is stable.
- Sync, tombstones, and background worker support are present.
- Cloudinary upload is present, but the UX still needs hardening.
- The expense module has list, form, and detail screens plus transition buttons.
- Approval notes and history are still missing.
- The AI assistant preloads role instructions and project/expense context before each query.

## Open gaps

- Full approval workflow, excluding any permission matrix work.
- Advanced dashboard analytics.
- Automated regression tests for sync.
- AI assistant hardening: history, stricter account data scope, and fallback UX.
- Flutter user app source.

## Auto-synced snapshot

<!-- AUTO_PROGRESS_SECTIONS:START -->
_Auto generated from docs/phase-tracker.json on 2026-04-21._

## Executive Summary (synced)
- Overall status: Phase 3 gate is closed (Sync + Cloudinary); Phase 4 includes expense detail and dashboard KPIs; Phase 5 has AI preload context and Gemini REST integration.

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

### Open gaps
- Approval notes + history trail for status transitions
- Flutter user app source
- Automated sync regression test script
- Strict per-account AI context isolation

### Next milestones
1. Complete Phase 4: approval notes/history + dashboard summary
2. Harden Phase 5: AI context/history/fallback UX
3. Build automated sync regression tests

### Risk / assumption
- Assumption: local auth + Firebase Realtime Database, not Firebase Auth.
- Risk: skipping sync regression tests may cause local/cloud drift.
- Risk: incomplete camera/upload UX may affect demos.
- Risk: broad AI context scope may expose more admin-visible data than intended.
<!-- AUTO_PROGRESS_SECTIONS:END -->

















