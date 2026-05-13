# Phase 3 Gate Evidence

_Last updated: 2026-04-20_

This document records the evidence used to close the Phase 3 gate for Sync + Cloudinary.

## 1) Gate checklist

| Gate condition | Status | Evidence |
|---|---|---|
| Tombstone schema (`isDeleted` / `deletedAt`) with safe migration support | Done | `DatabaseHelper.onCreate/onUpgrade`, `Constants` |
| Two-way sync based on `lastModified` | Done | `SyncCoordinator`, `DatabaseHelper.upsertProject`, `DatabaseHelper.upsertExpense` |
| Periodic background sync with retry/backoff | Done | `MainActivity.schedulePeriodicSync`, `SyncWorker` |
| Offline-to-online merge scenario documented | Done | Section 2 below and `SyncMergePolicyTest` |
| Cloudinary flow with config checks and clear error handling | Done | Section 3 below and `CloudinaryUploadService` |

## 2) Offline-to-online merge evidence

The merge policy is covered by a JVM unit test that maps directly to the offline-to-online cases:

- Offline create: local record does not exist on the cloud, so local wins.
- Offline update: local `lastModified` is newer than cloud, so local wins.
- Reconnect when cloud is newer: cloud wins.
- Offline delete tombstone newer than cloud copy: tombstone wins.

Evidence source:

- File: `app/src/test/java/com/example/modprojecttracker/sync/SyncMergePolicyTest.java`
- Cases:
  - `shouldPushLocal_whenRemoteMissing_afterOfflineCreate`
  - `shouldPushLocal_whenLocalIsNewer_afterOfflineUpdate`
  - `shouldPullRemote_whenCloudIsNewer_afterReconnect`
  - `shouldPushLocal_tombstone_whenOfflineDeleteIsNewerThanCloudCopy`

Command run on 2026-04-20:

```powershell
Set-Location "C:\Users\Admin\AndroidStudioProjects\ModProjectTracker"
.\gradlew.bat :app:testDebugUnitTest
```

Result: `BUILD SUCCESSFUL`.

## 3) Cloudinary config and error handling evidence

Evidence source:

- `app/src/main/java/com/example/modprojecttracker/cloud/CloudinaryUploadService.java`
- `app/src/main/java/com/example/modprojecttracker/activity/ProjectFormActivity.java`

Handled failure paths:

- Missing `CLOUDINARY_CLOUD_NAME` or `CLOUDINARY_UPLOAD_PRESET` returns a clear error.
- HTTP upload failure returns `Upload failed: <status code>`.
- Successful response with an empty `secure_url` returns a clear error.
- File read or upload exceptions return the fallback message `Upload error`.

UI integration:

- `ProjectFormActivity` shows upload state (`uploading`, `failed`, `URL ready`) and displays toast errors.

## 4) Verification commands

```powershell
Set-Location "C:\Users\Admin\AndroidStudioProjects\ModProjectTracker"
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugJavaWithJavac
```

Both commands completed successfully in the documentation update session on 2026-04-20.

