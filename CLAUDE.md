# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Waiting For Moranis** is an Android application that helps users track release dates for movies and TV shows. It fetches data from The Movie Database (TMDB) API and optionally syncs with Google Calendar to create release date events.

- **License**: GNU Affero General Public License v3.0 (AGPL-3.0-only)
- **Package**: `org.adrienbricchi.waitingformoranis`
- **Main Branch**: `develop`

## Build Commands

### Setup Requirements

1. Create `apikey.properties` file in project root with:
   ```
   TMDB_KEY=your_tmdb_api_key
   ```

2. Install Lombok plugin in Android Studio/IntelliJ:
   - Preferences → Plugins → Search and install `Lombok`
   - Preferences → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

### Common Build Tasks

```bash
# Build the project
./gradlew build

# Assemble debug APK
./gradlew assembleDebug

# Assemble release APK
./gradlew assembleRelease

# Clean build directory
./gradlew clean

# Run unit tests
./gradlew test

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Install debug build on connected device
./gradlew installDebug

# View all available tasks
./gradlew tasks
```

## Architecture Overview

### Data Layer

**Room Database** (`AppDatabase.java`)
- Version: 15 (with auto-migrations from v14)
- Entities: `Movie`, `Show`
- DAOs: `MovieDao`, `ShowDao`
- Custom type converters handle complex types (locales, date lists, etc.)
- Database name: `appdatabase`
- Migration strategy: fallbackToDestructiveMigration enabled

### Service Layer

**TMDB Service** (`service.tmdb.TmdbService`)
- Handles all TMDB API interactions using Volley for network requests
- Uses Jackson for JSON deserialization
- Implements 30-minute delay between requests for same movie/show (rate limiting)
- API key sources: Custom user key (stored in SharedPreferences) or build-time TMDB_KEY
- Base URL: `https://api.themoviedb.org/3/`
- Supports searching and refreshing both movies and TV shows

**Google Calendar Service** (`service.google.CalendarService`)
- Manages Google Calendar integration using Android's CalendarContract
- Permission handling: READ_CALENDAR, WRITE_CALENDAR
- Creates all-day events for movie releases and TV show episodes
- Stores calendar ID in SharedPreferences with key `current_google_calendar_id`
- Includes migration patches (e.g., v1.0.1 → v1.2.0 ID format changes)

### UI Layer

**MainActivity** (`ui.main.MainActivity`)
- Uses ViewPager2 with TabLayout for switching between Movies and Shows
- Implements Fragment Result API for communication between fragments
- Features circular reveal animation for onboarding screen
- Caches item counts to control onboarding visibility
- Search functionality broadcasts to all fragments implementing `SearchEventListener`

**Fragment Structure**
- `MovieListFragment` / `ShowListFragment`: Display tracked content in RecyclerView
- `AddMovieDialogFragment` / `AddShowDialogFragment`: Search and add new content
- `SettingsFragment`: Manages preferences including calendar and TMDB API key

### Models

**Movie** (`models.Movie`)
- Room entity with TMDB ID as primary key
- Fields: title, imageUrl, releaseDate, calendarEventId, productionCountries, releaseDates
- Status enum: CANCELED, IN_PRODUCTION, POST_PRODUCTION, RELEASED, UNKNOWN
- Uses Lombok annotations (@Data, @NoArgsConstructor)

**Show** (`models.Show`)
- Room entity for TV series tracking
- Tracks next episode details: nextEpisodeAirDate, nextEpisodeSeasonNumber, nextEpisodeNumber
- Also tracks last episode information
- Status enum: CANCELED, ENDED, RETURNING_SERIES, UNKNOWN
- Uses Lombok annotations

### Key Patterns

1. **Service Initialization**: Services use `Optional<Service> init(@Nullable Context)` pattern for safe initialization

2. **Network Requests**: Custom `JacksonRequest` wrapper around Volley for type-safe JSON handling

3. **Fragment Communication**: Uses Fragment Result API instead of callbacks
   - Fragments send results to MainActivity via `setFragmentResult()`
   - MainActivity listens via `setFragmentResultListener()`

4. **Calendar Event Tracking**: Each Movie/Show stores its `calendarEventId` and `isUpdateNeededInCalendar` flag for sync management

5. **Release Date Logic**: `ReleaseUtils.getRelease()` determines appropriate release date based on user locale and available release information

## Code Style

- The `.idea` folder is committed and contains code style settings
- Auto-format all files before committing (no pre-commit hooks enforced yet)
- Copyright header required on all source files (2020-2025, AGPL-3.0-only)

## Development Notes

- **Minimum SDK**: 28
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 17
- **Build Tools**: 36.0.0
- **Gradle**: 8.13
- **Android Gradle Plugin**: 8.12.3
- **Kotlin**: 1.8.0 (used for some Android components)

### Key Dependencies
- Room 2.7.1 (database)
- Jackson 2.19.0 (JSON parsing)
- Volley 1.2.1 (networking)
- Picasso 3.0.0-alpha06 (image loading)
- AndroidX Navigation 2.8.9
- Material Components 1.13.0
- Lombok 1.18.38

### Testing
Unit tests and integration tests are marked as `TODO` in CONTRIBUTING.md - test infrastructure needs to be established.

## Privacy & Security

- No user data is stored on external servers
- Google Calendar integration is optional and disabled by default
- TMDB API key can be user-provided or use build-time default
- Custom TMDB keys stored in SharedPreferences with key: `tmdb_api_key`
