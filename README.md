# ModProjectTracker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Language: Java](https://img.shields.io/badge/Language-Java-007396?logo=openjdk&logoColor=white)](https://www.java.com/)

ModProjectTracker is an Android administration app for managing projects, budgets, and expenses. It is designed for administrators to create and monitor projects, track expense lifecycles, review dashboard metrics, and keep local data synchronized with cloud services.

## Overview

This project provides a clean admin-focused workflow for project tracking, including:

- project management and budget monitoring
- expense submission, approval, and payment tracking
- dashboard summaries for quick decision-making
- background synchronization for updated data
- an optional AI assistant entry point for support and guidance

## Key Features

- Dashboard with project and expense KPIs
- Project CRUD: create, view, edit, and delete projects
- Expense lifecycle tracking: Draft → Pending Approval → Approved → Paid
- WorkManager-based periodic sync with retry/backoff support
- Local SQLite storage through `DatabaseHelper`
- Optional AI assistant integration via `AiAssistantActivity` and `GeminiService`

## Screenshots

Add screenshots here before publishing to GitHub:

- Dashboard
- Project list and detail screens
- Project form
- Expense management screens
- AI assistant screen

## Tech Stack

- Android
- Java
- AndroidX
- Material Components
- RecyclerView
- WorkManager
- SQLite
- Gradle wrapper

## Project Structure

- `app/` — Android application module
- `docs/` — project documentation and phase/status notes
- `gradle/` — Gradle wrapper files
- `legacy/` — archived backup files and historical sources
- `scripts/` — utility scripts

## Prerequisites

- Android Studio recommended
- JDK 11 or later
- Android SDK installed
- Android emulator or physical device

## Setup and Run

1. Clone or open the project in Android Studio.
2. Let Gradle sync and download dependencies.
3. Run the `app` module on an emulator or connected device.

PowerShell commands for Windows:

```powershell
cd C:\Users\Admin\AndroidStudioProjects\ModProjectTracker
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Testing

Run the available tests from the project root:

```powershell
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

If you change UI flows or device-specific logic, verify the app manually on an emulator or physical device as well.

## Configuration

- `app/google-services.json` is included in this workspace. Replace it with your own configuration if you use Firebase or Google services.
- Keep secrets, API keys, and private credentials out of source control.
- Use local configuration or CI secrets for sensitive values.

## Security

If you discover a vulnerability or sensitive configuration issue, please report it privately. See `SECURITY.md` for details.

## GitHub Ready Notes

Before publishing the repository:

- review the `.gitignore` file
- verify that no secrets are committed
- use a clear repository description
- keep commit messages short and meaningful
- add screenshots to make the project page look more professional

## Contributing

Contributions are welcome. Please see `CONTRIBUTING.md` for branch naming, testing, pull request, and security guidelines.

## License

This project is licensed under the MIT License. See `LICENSE` for details.

## Documentation

Additional project notes and historical status documents are available in the `docs/` folder.

















