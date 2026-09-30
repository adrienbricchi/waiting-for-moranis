# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Waiting For Moranis** is a single-module (`app`) Android app that tracks release dates of movies and TV shows,
using The Movie Database (TMDB) API, and optionally mirrors them as all-day events in a Google Calendar.

- Package: `org.adrienbricchi.waitingformoranis`, main branch: `develop`.
- Pure Java 17 sources (the Kotlin plugin is applied only for `-ktx` AndroidX dependencies), Lombok for models.
- Library versions live inline in `app/build.gradle` and root `build.gradle` (no version catalog); read them there rather than trusting a copy.

## Setup

`apikey.properties` must exist at the project root, or Gradle configuration fails (it's read with `FileInputStream`, and is gitignored):

```
TMDB_KEY=your_tmdb_api_key
```

Any dummy value is enough to build and run unit tests, as CI does. Lombok needs the IDE plugin and annotation processing enabled.

## Commands

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # install on a connected device
./gradlew test                   # JVM unit tests (JUnit 5)
./gradlew lint                   # Android lint (abortOnError = false, reports in app/build/reports/)
./gradlew connectedAndroidTest   # instrumented tests (device/emulator required)

# Single test class / method
./gradlew testDebugUnitTest --tests 'org.adrienbricchi.waitingformoranis.utils.ReleaseUtilsTest'
./gradlew testDebugUnitTest --tests 'org.adrienbricchi.waitingformoranis.utils.ReleaseUtilsTest.someMethod'
```

CI (`.github/workflows/android-ci.yml`) runs `test` then `lint` on pushes/PRs to `develop` and `main`.

## Testing

- Unit tests in `app/src/test` run on the JVM with **JUnit Jupiter** (`useJUnitPlatform()`), not on Android.
- `unitTests.returnDefaultValues = true`: Android framework calls return defaults instead of throwing, so tests can silently pass through them.
- `app/src/test/java/android/text/TextUtils.java` is a hand-written stub shadowing the framework class, so code using `TextUtils` behaves for real in tests. Add similar stubs there if other framework utilities need real behavior.
- Current coverage: Jackson deserialization of TMDB payloads (`service/tmdb/*Test`), `Release`, and `ReleaseUtils`.

## Architecture

Layers under `app/src/main/java/org/adrienbricchi/waitingformoranis/`: `models/` (Room entities), `service/` (persistence, tmdb, google), `ui/`, `utils/`.

### Persistence (`service/persistence`)

- `AppDatabase` is a Room singleton (`appdatabase`) with `Movie` and `Show` entities, `CustomTypeConverters` for lists/locales/dates.
- Schemas are exported to `app/schemas/<AppDatabase FQN>/<version>.json`; auto-migrations depend on them.
  When changing an entity: bump the `version`, add an `@AutoMigration(from, to)`, and commit the newly generated schema JSON.
- `fallbackToDestructiveMigration()` is enabled, so a missing migration wipes the user's list instead of crashing.

### TMDB (`service/tmdb`)

- `TmdbService` wraps Volley with `utils/JacksonRequest` (typed Jackson deserialization into `TmdbMovie`, `TmdbShow`, `TmdbPage`, `TmdbError`), then maps TMDB DTOs to the Room models.
- A movie/show isn't re-fetched within 30 minutes of its last refresh (`DELAY_TIMEOUT_MS`).
- API key: the user's own key from SharedPreferences (`tmdb_api_key`, set in Settings) takes precedence over `BuildConfig.TMDB_KEY`.

### Release date resolution

`ReleaseUtils.getRelease()` picks the release date to display/sync from a movie's per-country `Release` list, based on the device locale.
This is the core business logic, and the best-tested part.

### Google Calendar (`service/google/CalendarService`)

- Uses `CalendarContract` directly (no Google API client), with runtime READ/WRITE_CALENDAR permissions.
- The chosen calendar id is stored in SharedPreferences (`current_google_calendar_id`).
- Each `Movie`/`Show` keeps its `calendarEventId` and an `isUpdateNeededInCalendar` flag; sync creates/updates/deletes events based on them.
- Contains one-off data-migration patches (e.g. 1.0.1 → 1.2.0 event id format); keep them when refactoring.

### UI (`ui/`)

- `MainActivity` hosts a ViewPager2 + TabLayout (`SectionsPagerAdapter`) with `MovieListFragment` and `ShowListFragment`, plus an onboarding overlay (circular reveal) toggled by the item counts each list fragment reports back (`itemsCountCache`).
- Fragments talk to `MainActivity` through the **Fragment Result API** (`setFragmentResult` / `setFragmentResultListener`), not callbacks.
- The toolbar search is broadcast to every fragment implementing `SearchEventListener`.
- `Add*DialogFragment` searches TMDB and adds entries; `ui/preferences` holds `SettingsActivity`/`SettingsFragment` (calendar choice, custom TMDB key) and the calendar onboarding dialog.
- Services follow an `Optional<XService> init(@Nullable Context|Activity)` pattern; callers chain `.map(...)`/`.ifPresent(...)` instead of null-checking.

## Code Style

- The `.idea` folder (code style included) is committed; auto-format modified files with it. No hook enforces it.
- Every source file carries the AGPL-3.0-only copyright header (`Copyright (C) 2020-2025`, `SPDX-License-Identifier: AGPL-3.0-only`); copy it from an existing file.
- `lombok.config` disables `@ConstructorProperties` and `@Generated` annotations.
