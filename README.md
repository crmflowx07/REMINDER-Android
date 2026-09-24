# REMINDER — Android App

REMINDER is an offline-first Android reminder and personal planning app built around one product rule: **simple on the surface, powerful under the hood**.

## What is implemented

- Kotlin + Jetpack Compose + Material 3
- MVVM-style UI state + Repository + Room database
- Offline reminder CRUD
- One-time, daily, weekdays, weekends, weekly, monthly, yearly, specific-days and custom interval recurrence
- Custom recurrence units: minutes, hours, days, weeks and months
- Multiple advance alerts plus the at-time alert
- AlarmManager scheduling with exact-alarm capability check and fallback
- Notification actions: configurable Snooze, Complete and Stop
- Reboot / clock / timezone rescheduling receiver
- Overdue reminders and completed history
- Built-in original reminder sounds (10 WAV resources)
- Per-reminder sound selection and vibration
- Android system picker for custom sounds; persistent URI permission
- Quick natural-language parsing for common offline phrases
- Home, Planner, Calendar, Search, Routines, Categories
- Meeting fields (location, meeting link, person/company, optional end time)
- Ideas, Plans, Goals and Travel/Places
- Optional family profiles (Me/spouse/child/parent/pet/other)
- Optional one-tap demo seed using the supplied sample reminders/routines/travel data
- Light, Dark and System themes
- Local JSON export/import
- Notification / exact-alarm / battery settings shortcuts
- Three-screen onboarding and notification permission request
- Accessibility-friendly Material components and readable tap targets

## Important scope note

The supplied product specification explicitly defines V1 as stability-first. This project implements the V1 core plus several local expansion modules already requested in the specification, including Family Profiles, Ideas, Plans, Goals and Travel. Widgets, cloud backup, cross-device sync, OCR/document scanning, advanced AI suggestions, Business/Employee Mode, billing enforcement, advanced analytics and location-aware reminders remain roadmap items so the offline reminder core stays reliable.

## Toolchain

- Android SDK: API 36
- minSdk: 26
- targetSdk: 36
- JDK: 17
- Android Gradle Plugin: 8.11.1
- Gradle: 8.13
- Kotlin: 1.9.24

## Open in Android Studio

1. Install a current Android Studio version with Android SDK 36 and JDK 17.
2. Open this folder as an Android project.
3. If a Gradle wrapper JAR is not present, create it once using an installed Gradle:

   `gradle wrapper --gradle-version 8.13`

4. Sync Gradle.
5. Run the `app` configuration on Android 8.0 (API 26) or newer.

## Build

Debug APK:

`./gradlew assembleDebug`

Release AAB:

`./gradlew bundleRelease`

Before Play release, configure your own signing key in Android Studio / secure CI. Do not commit passwords or keystore secrets to source control.

## Reliability notes

- Android 13+ notification permission is requested after onboarding.
- Exact alarm access is user-controlled. If it is unavailable, REMINDER uses an allowed inexact fallback instead of silently failing.
- Pending alerts are rescheduled after reboot, timezone changes, manual clock changes, and app replacement.
- Core reminder data is local Room data. Android cloud backup is disabled in this V1 build; users can export/import a local JSON backup.

## UI reference

The user requested an exact UI match, but no UI reference URL was present in the supplied message/file. The current Compose theme is a clean premium Material 3 implementation and is structured so the visual layer can be swapped without changing reminder/storage/scheduling behavior.
