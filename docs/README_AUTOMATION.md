# README Automation Guide

This document explains how to sync phase progress into the English README files.

## Single source of truth

- Source file: `docs/phase-tracker.json`
- Update this file first whenever phase or progress status changes.

## Synced files

- `README.md`
- `app/README.md`
- `docs/README_PHASE_STATUS.md`
- `docs/README_PROGRESS_SECTIONS.md`

These files are updated inside these marker blocks:

- `<!-- AUTO_PHASE_PROGRESS:START --> ... <!-- AUTO_PHASE_PROGRESS:END -->`
- `<!-- AUTO_PHASE_APP_PROGRESS:START --> ... <!-- AUTO_PHASE_APP_PROGRESS:END -->`
- `<!-- AUTO_PHASE_GATE:START --> ... <!-- AUTO_PHASE_GATE:END -->`
- `<!-- AUTO_PROGRESS_SECTIONS:START --> ... <!-- AUTO_PROGRESS_SECTIONS:END -->`

## How to run

### Preview only

```powershell
Set-Location "C:\Users\Admin\AndroidStudioProjects\ModProjectTracker"
powershell -ExecutionPolicy Bypass -File "scripts/Update-ReadmeProgress.ps1" -DryRun
```

### Write changes

```powershell
Set-Location "C:\Users\Admin\AndroidStudioProjects\ModProjectTracker"
powershell -ExecutionPolicy Bypass -File "scripts/Update-ReadmeProgress.ps1"
```

## Gate rules

- Each phase uses a checklist in `phaseGates`.
- Mark `done: true` only when code or test evidence exists.
- If a gate is not complete, the phase status should stay `Partial`.
- The script `Update-ReadmeProgress.ps1` blocks README writes when a phase is marked `Done` but one or more gate items are still `false`.
- `docs/phase-tracker.json` is always the source of truth; do not edit the README files directly to avoid drift.

## Recommended workflow

1. Finish the task or phase in code.
2. Update `docs/phase-tracker.json` with status, evidence, and gate data.
3. Run `Update-ReadmeProgress.ps1`.
4. Review the diff and commit the documentation together with code changes.

