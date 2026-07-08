# Bill Umaba

An offline-first Android application for tracking culinary expenses and reviews. Log restaurant visits, record menu items with ratings, and scan paper receipts with on-device OCR — no internet required.

## Features

- **Visit Logging** — Record restaurant name, address, date, and receipt photos
- **Menu Item Tracking** — Add individual items with name, quantity, price, rating, and notes per menu
- **Receipt Scanning** — 4-stage pipeline for digitizing paper receipts:
  1. **Auto-Frame** — ML Kit Document Scanner auto-straightens & crops the receipt; graceful fallback to regular camera if Google Play Services is unavailable
  2. **OCR** — ML Kit Text Recognition Latin (on-device) extracts text; editable review screen
  3. **Text Mapping** — Auto-parses OCR output into structured data using 3 preset parsers (General, Restaurant, Retail) with auto-detection
  4. **Dynamic Patterns** — Save custom parsing rules per restaurant via a visual builder (no regex required); automatically applied on subsequent scans
- **Dashboard** — Browse past visits with search, filter, and sort
- **100% Offline** — All data stays on-device; zero data uploads

## Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 (Material You) |
| **Architecture** | MVVM + Repository pattern |
| **DI** | Dagger Hilt |
| **Database** | Room (SQLite, schema v2) |
| **Navigation** | Jetpack Navigation Compose |
| **Image Loading** | Coil |
| **ML Kit** | Document Scanner + Text Recognition Latin (on-device) |
| **Testing** | JUnit 4, Espresso, Compose UI Test |
| **Build** | Gradle Kotlin DSL, KSP |

## Project Structure

```
bill-umaba-v4/
├── android/                        # Android project root
│   ├── build.gradle.kts            # Top-level Gradle config
│   ├── settings.gradle.kts
│   ├── gradle/libs.versions.toml   # Version catalog
│   └── app/
│       ├── build.gradle.kts        # App-level config (SDK, flavors, deps)
│       └── src/
│           ├── main/java/com/pndnwngi/billumaba/
│           │   ├── di/              # Hilt DI modules
│           │   ├── data/
│           │   │   ├── database/    # Room DB, entities, DAOs, migrations
│           │   │   ├── ocr/         # ML Kit OCR engine
│           │   │   ├── parser/      # Receipt parsers (general, restaurant, retail, pattern)
│           │   │   ├── repository/  # Data access layer
│           │   │   └── storage/     # File I/O & image compression
│           │   └── ui/
│           │       ├── theme/       # M3 dynamic theming
│           │       ├── navigation/  # NavHost & screen routes
│           │       ├── components/  # Shared composables
│           │       ├── dashboard/   # Visit list & metrics
│           │       ├── addedit/     # Add/edit visit form + scanning
│           │       ├── detail/      # Visit detail view
│           │       ├── ocr/         # OCR review screen
│           │       └── patterns/    # Pattern list & visual builder
│           ├── test/                # Unit tests
│           └── androidTest/         # Instrumented tests
├── docs/
│   ├── prd.md                      # Product requirements
│   └── architecture.md             # Architecture specification
└── backend/                        # (placeholder for future backend)
```

## Prerequisites

- **Android Studio** Hedgehog or later
- **Android SDK** Platform 35
- **JDK 17**
- A device or emulator running **Android 8.0 (API 26)** or higher

> ML Kit Document Scanner requires Google Play Services. The app auto-detects availability and falls back to the regular camera if GMS is missing.

## Getting Started

```bash
# Clone the repository
git clone <repo-url>
cd bill-umaba-v4/android

# Build debug APK
./gradlew assembleStandardDebug

# Install on connected device/emulator
./gradlew installStandardDebug
```

Open the `android/` directory in Android Studio, sync Gradle, and run the `standardDebug` variant.

## Build Variants

| Flavor | Application ID | App Name |
|--------|---------------|----------|
| `standard` | `com.pndnwngi.billumaba` | Bill Umaba |
| `mimo` | `com.pndnwngi.billumaba.mimo` | Bill Umaba Mimo |

## Testing

```bash
# Unit tests (JVM)
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## License

Proprietary. All rights reserved.
