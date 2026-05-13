# Docs Changelog

This file records markdown documentation changes so the current English docs set stays easy to audit.

## Entry format

- Date: `YYYY-MM-DD`
- Type: `Added` | `Updated` | `Removed`
- Files: related file list
- Notes: short summary of why the change happened

---

## 2026-04-21 (AI docs realignment with current code)

### Updated
- `README.md`
- `app/README.md`
- `docs/PROJECT_CURRENT_STATE_FULL.md`
- `docs/README_PHASE_STATUS.md`
- `docs/README_PROGRESS_SECTIONS.md`
- `docs/MIGRATION_SUMMARY.md`
- `docs/DOCUMENTATION_INDEX.md`
- `docs/phase-tracker.json`

### Notes
- Replaced outdated "direct message" AI wording with the current preload prompt + context flow.
- Updated Gemini integration description to direct REST (`OkHttp`) with `gemini-2.5-flash` default.
- Documented the current AI data scope limitation (not strict per-account isolation).

## 2026-04-21 (English docs cleanup and deduplication)

### Updated
- `README.md`
- `app/README.md`
- `docs/CHANGELOG_DOCS.md`
- `docs/DOCUMENTATION_INDEX.md`
- `docs/MIGRATION_SUMMARY.md`
- `docs/PHASE3_GATE_EVIDENCE.md`
- `docs/PROJECT_CURRENT_STATE_FULL.md`
- `docs/README_AUTOMATION.md`
- `docs/README_PHASE_STATUS.md`
- `docs/README_PROGRESS_SECTIONS.md`

### Removed
- `docs/BAO_CAO_TINH_TRANG.md`
- `docs/DETAILED_CHANGES.md`
- `docs/GEMINSERVICE_FIX_REPORT.md`
- `docs/HUONG_DAN_NHANH.md`
- `docs/QUICK_REFERENCE.md`
- `docs/TIENICH_GEMINSERVICE_FIX.md`

### Notes
- Converted the active documentation set to English only.
- Removed duplicate and outdated markdown docs to keep one canonical set.
- Updated the AI-related docs to reflect the then-current direct chat prompt flow.

## 2026-04-20 and earlier

See the repository history for the detailed phase-by-phase documentation updates.

