# ModProjectTracker Documentation Index

This index lists the current English documentation set and the files considered canonical.

## Recommended reading order

1. `README.md` - repo overview.
2. `docs/PROJECT_CURRENT_STATE_FULL.md` - detailed current state.
3. `docs/README_PHASE_STATUS.md` - phase matrix and gate checklist.
4. `docs/README_PROGRESS_SECTIONS.md` - short copy-paste progress blocks.
5. `docs/MIGRATION_SUMMARY.md` - AI integration and prompt/transport update summary.

## Canonical docs

- `README.md`
- `app/README.md`
- `docs/PROJECT_CURRENT_STATE_FULL.md`
- `docs/README_PHASE_STATUS.md`
- `docs/README_PROGRESS_SECTIONS.md`
- `docs/README_AUTOMATION.md`
- `docs/PHASE3_GATE_EVIDENCE.md`
- `docs/MIGRATION_SUMMARY.md`
- `docs/CHANGELOG_DOCS.md`
- `docs/DOCUMENTATION_INDEX.md`

## Progress source of truth

- Data tracker: `docs/phase-tracker.json`
- Automation script: `scripts/Update-ReadmeProgress.ps1`
- Synced marker blocks:
  - `README.md`
  - `app/README.md`
  - `docs/README_PHASE_STATUS.md`
  - `docs/README_PROGRESS_SECTIONS.md`

## Deprecated or removed docs

- `docs/BAO_CAO_TINH_TRANG.md`
- `docs/DETAILED_CHANGES.md`
- `docs/GEMINSERVICE_FIX_REPORT.md`
- `docs/HUONG_DAN_NHANH.md`
- `docs/QUICK_REFERENCE.md`
- `docs/TIENICH_GEMINSERVICE_FIX.md`
- Historical backup files in `legacy/docs/`

If you need to regenerate synced progress blocks, update `docs/phase-tracker.json` first and then run the automation script.

