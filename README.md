# REMINDER — Android

**REMINDER** is an offline-first Android reminder and personal planning app.

## Build status

The repository contains a reproducible Android source archive and GitHub Actions workflows.

- CI reconstructs the full Android Studio project, verifies the archive checksum, installs Android SDK 36, runs unit tests, and builds an installable debug APK.
- The release workflow builds a **signed release APK** after secure repository signing secrets are configured.
- Package: `com.muzamil.reminder`
- Version: `1.0.0` (versionCode 1)
- minSdk: 26
- targetSdk / compileSdk: 36
- JDK: 17
- Gradle: 8.13
- Android Gradle Plugin: 8.11.1

## Source archive

`source/reminder-source.zip.b64` is the Base64-encoded Android Studio source archive.

SHA-256 of decoded ZIP:

`8f9625a909cf964e1161e612aba93fb0385b92c3bd06bf1c37b28622df6839f6`

Restore locally:

```bash
base64 --decode source/reminder-source.zip.b64 > reminder-source.zip
unzip reminder-source.zip
```

## Features

Kotlin, Jetpack Compose, Material 3, Room, offline reminder CRUD, recurring reminders, multiple advance alerts, actionable notifications, snooze/complete/stop, AlarmManager scheduling, reboot/timezone rescheduling, 10 built-in WAV sounds, custom sound picker, routines, planner/calendar, search, overdue/completed history, categories, meetings, ideas, plans, goals, travel, optional family profiles, JSON export/import, onboarding, dark mode, and settings.

## Signed Uptodown build

Never commit the private signing key to this public repository. Add these GitHub Actions repository secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Then run **Actions → Signed Release APK → Run workflow**. The output artifact is:

`REMINDER-1.0.0-release.apk`

For a tagged public release, create/push tag `v1.0.0`; the workflow attaches the signed APK to the GitHub Release.

## Uptodown

Uptodown accepts a local supported app file or a direct URL to the APK. Submit the final **signed release APK**, not the source archive.
