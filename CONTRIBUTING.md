# Contributing to ModProjectTracker

Thanks for your interest in contributing to ModProjectTracker. Issues, bug reports, feature ideas, and pull requests are welcome.

## 1) Report issues clearly

- Open a GitHub issue with a short, descriptive title.
- Include steps to reproduce, expected behavior, and actual behavior.
- Add device/emulator details and relevant Android Studio or Gradle version information when possible.

## 2) Work on a branch

- Create a branch with a clear name, for example:
  - `feature/<short-description>`
  - `fix/<short-description>`
  - `docs/<short-description>`
- Keep the branch focused on a single task when possible.

## 3) Follow code style

- Keep Java code consistent with the existing style in the project.
- Use descriptive names for classes, methods, variables, and commits.
- Prefer small, readable changes over large unrelated edits.

## 4) Test your changes

- Add or update unit tests when it makes sense.
- From the project root on Windows, run:

```powershell
.\gradlew.bat test
```

- If your change affects UI or device behavior, also run instrumentation tests when available:

```powershell
.\gradlew.bat connectedAndroidTest
```

## 5) Open a pull request

- Open a pull request from your feature branch into the target branch.
- Include a short summary of what changed and why.
- Link the related issue if there is one.
- Mention any setup, migration, or testing notes reviewers should know.

## 6) Keep the project secure

- Do not commit secrets, API keys, private credentials, or local machine settings.
- Keep sensitive values out of source control and use secure configuration methods instead.

## Need help?

If you'd like help choosing a task, open an issue and label it `help wanted`.

Thanks for contributing!

