# ModProjectTracker

ModProjectTracker is an Android administration app for managing projects, budgets and expenses. It is designed for administrators to create and monitor projects, track expenses, manage approval workflows, and keep local data in sync with cloud services.

This README provides a concise overview, build instructions, configuration notes, and guidance for publishing the repository to GitHub.

## Highlights / Features

- Dashboard with project and expense KPIs (total projects, active projects, budgets, budget utilization, pending/paid expenses)
- Project CRUD: create, view, edit, delete projects
- Expense tracking with lifecycle states (Draft → Pending Approval → Approved → Paid; Reject option)
- Background periodic sync implemented with WorkManager and a SyncWorker/SyncCoordinator
- Local SQLite storage with a `DatabaseHelper` and two-way sync merge logic
- Optional AI assistant integration (AiAssistantActivity + GeminiService hooks)

## Tech stack

- Android, Java
- Jetpack components (RecyclerView, WorkManager, AppCompat, Material Components)
- Gradle build (wrapper included)
- Local persistence: SQLite via `DatabaseHelper`

## Repository layout (selected)

- app/ — Android module (source code, resources and app-level Gradle config)
- gradle/ & gradlew(.bat) — Gradle wrapper
- docs/ — design, changelogs and project state documents

## Prerequisites

- Android Studio (recommended) or command-line Android SDK/Gradle tools
- JDK 11 or later
- A connected Android device or emulator for runtime testing

## Build & Run

Recommended: open the project in Android Studio and run the `app` module.

Command-line (PowerShell on Windows):

```powershell
cd C:\Users\Admin\AndroidStudioProjects\ModProjectTracker
.\gradlew.bat assembleDebug    # build an APK
.\gradlew.bat installDebug     # install to a connected device/emulator
```

Run tests (if present):

```powershell
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

## Configuration

- `app/google-services.json`: present in this workspace. Replace with your own Firebase/Google configuration if you use Firebase features.
- Secrets and keys: do not commit private keys or credentials. Use local config or CI secrets.

## GitHub: create & push repository

Option A — Create repo on GitHub website and push locally:

1. Create a new repository on GitHub (https://github.com/new) named e.g. `ModProjectTracker`.
2. From the project root run (PowerShell):

```powershell
cd C:\Users\Admin\AndroidStudioProjects\ModProjectTracker
git init
git add .
git commit -m "Initial commit: ModProjectTracker"
git branch -M main
git remote add origin https://github.com/USERNAME/REPO.git
git push -u origin main
```

Option B — Using GitHub CLI (`gh`):

```powershell
cd C:\Users\Admin\AndroidStudioProjects\ModProjectTracker
gh repo create REPO --public --source=. --remote=origin --push
```

Notes:
- Replace `USERNAME/REPO` or `REPO` with your information.
- For private repositories, use `--private` with `gh` or choose the private setting in the web UI.
- If using HTTPS, you may be prompted for credentials or a Personal Access Token (PAT).

## .gitignore recommendation

If the repo does not already include a `.gitignore`, add one and include typical Android ignores:

- *.iml
- .gradle/
- /local.properties
- /build/
- /app/build/
- .idea/
- *.keystore

GitHub provides a standard Android `.gitignore` template that is recommended.

## Contributing

- Use branches and pull requests for feature development.
- Add a `CONTRIBUTING.md` and code style notes if you wish to accept external contributions.

## License

Add a `LICENSE` file with your chosen license (MIT, Apache-2.0, etc.). If unsure, MIT is a permissive option.

## Project documentation and status

See the `docs/` directory for canonical documentation and the included auto-generated phase progress. The project already contains a phase-tracker and detailed status files (for example `docs/PROJECT_CURRENT_STATE_FULL.md`).

---

If you want, I can next:

1) Create a recommended `.gitignore` file in the repository,
2) Draft a `CONTRIBUTING.md` and a sample `LICENSE` (MIT),
3) Provide step-by-step help to actually run the Git commands locally (or run them for you if you give permission and provide the remote URL/credentials).

Tell me which of the above you want me to do next.
















