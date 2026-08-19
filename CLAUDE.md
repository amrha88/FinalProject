# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Single-module Gradle/Android project (module `:app`). On Windows use `gradlew.bat` instead of `./gradlew`.

- Build debug APK: `./gradlew assembleDebug`
- Install to a connected device/emulator: `./gradlew installDebug`
- Run JVM unit tests (`app/src/test`): `./gradlew test`
- Run a single unit test class: `./gradlew test --tests "com.example.automate.ExampleUnitTest"`
- Run instrumented tests (`app/src/androidTest`, needs a device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`
- Compile only (fastest correctness check while iterating): `./gradlew compileDebugKotlin`

The project needs `app/google-services.json` (Firebase project config) to build; it's already checked into this repo. Firebase App Check uses the debug provider (`AutomateApplication.kt`), so a debug build talks to the real Firebase backend without extra setup.

There is no dependency-injection framework (no Hilt/Koin) — every ViewModel is constructed by hand via an anonymous `ViewModelProvider.Factory` inside `ui/navigation/AppNavGraph.kt`.

## Architecture

Kotlin + Jetpack Compose (Material3), package root `com.example.automate`. Layering is a light MVVM / clean-architecture split:

```
ui/screens      -> Compose screens (stateful Screen + stateless Content split)
ui/viewmodel    -> one ViewModel + UiState per screen, exposes StateFlow, calls domain/repository interfaces
domain/repository -> abstract interfaces (contracts) — the only thing ViewModels/engine depend on
data/repository -> Firebase-backed implementations of those interfaces (the only layer touching FirebaseAuth/Firestore/Firebase.ai)
domain/engine   -> ReminderEngine: business logic shared across ViewModels
domain/model    -> data classes/enums shared by every layer
ui/components   -> reusable Compose widgets
ui/navigation   -> Screen.kt (routes) + AppNavGraph.kt (the single NavHost + all ViewModel wiring)
util            -> stateless helpers (file/URI, image compression, locale switching, AI model config)
```

The point of the `domain/repository` interface layer is dependency inversion: `AuthViewModel`, `ReminderEngine`, etc. only know about interfaces like `VehicleRepository`/`ReminderRepository`, never the concrete `Firebase*`/`Firestore*` classes, even though in practice every interface currently has exactly one Firebase-backed implementation.

### Firestore data layout

Everything is scoped under the signed-in user, `users/{uid}/`:

- `users/{uid}` — profile doc: name, age, `hasLicense`, avatar photo and driving-licence photo (both stored as base64 strings)
- `users/{uid}/vehicles/{vehicleId}` — vehicle doc (make/model/plate/photo/specs)
- `.../vehicles/{vehicleId}/documents` — scanned or manually entered documents (licence, insurance, inspection, maintenance, repair invoice)
- `.../vehicles/{vehicleId}/history` — maintenance/repair history events
- `.../vehicles/{vehicleId}/reminders` — per-vehicle reminders (licence/insurance/inspection expiry, service due)
- `users/{uid}/reminders` — vehicle-independent reminders
- `.../vehicles/{vehicleId}/maintenanceState` — last known state per `MaintenanceItemType`

### The reminder pipeline (cross-cutting — easy to break when adding a new save path)

Three different places can create/update a `VehicleReminder`, and only one of them is atomic:

1. **New document** (AI scan or manual entry) — `VehicleDocumentsViewModel.saveConfirmedDocument()`, non-editing branch → `FirestoreVehicleDocumentRepository.saveConfirmedDocumentAtomic()`, which batches the document write + history event + reminder writes together.
2. **Editing an existing document** — `updateDocument()` only writes the document itself; `VehicleDocumentsViewModel` has to separately call `reminderRepository.deactiveOldReminders()` + `saveReminder()` for each reminder `ReminderEngine.buildRemindersFromDocument()` returns. Forgetting this step is a real bug that shipped once already.
3. **Direct/manual entry** (vehicle setup questions, `VehicleLicenceScreen` save) → `ReminderEngine.syncRemindersFromManual()`, which deactivates the old reminder of that type then creates a new one as two separate (non-batched) writes.

Every date field these reminders key off of must be a `yyyy-MM-dd` string — that's why date entry anywhere in the app goes through `ui/components/AppDateField.kt` (a Material3 `DatePickerDialog` wrapper) instead of free text; any other format silently fails every downstream expiry check.

"Expired / expiring soon" is computed client-side, independently, in three places that all need to agree: `NotificationsScreen.statusFor()` (30-day "soon" window), `AuthViewModel.refreshLicenceAlerts()` (drives the red badge on the Home vehicle card and the Documents feature card), and `VehicleDocumentsScreen.isDocumentExpired()` (drives the ACTIVE/EXPIRED chip on a document card — reads the document's own expiry field directly rather than going through a reminder).

### Navigation

Single `NavHost` in `AppNavGraph.kt`; routes are defined in `Screen.kt`. The bottom nav bar (`ui/components/BottomNavBar.kt`) is shared across Home/AiAssistant/Profile/Settings/VehicleDetails via one `mainBottomBar` lambda built once in `AppNavGraph`. New screens follow the existing pattern: add a `Screen`, add a `composable {}` block in `AppNavGraph.kt`, construct any new ViewModel inline there.

### AI features (Firebase AI Logic / Gemini)

Model name lives in `util/AiConfig.kt`. Each AI capability has its own repository in `data/repository/`: `FirebaseAiChatRepository` (free-form chat), `FirebaseWarningLightRepository` (dashboard-warning-light photo → structured `WarningLightResult` via a JSON response schema), `FirebaseVehicleLicenceAnalysisRepository` / `FirebaseVehicleDocumentAnalysisRepository` / `FirebaseVehicleHistoryAnalysisRepository` (photo → structured extraction for that document type), `FirebaseVehicleSpecsRepository` (text-only spec lookup). Images are downscaled client-side before upload (`util/ImageProcessingUtils.kt`; `ui/components/ProfileImagePicker.kt` takes a `maxDimension`) rather than sent full-resolution.

### Localization

English (`res/values/strings.xml`) and Hebrew (`res/values-iw/strings.xml`), switched in-app independent of the OS locale via `util/LocaleManager.kt` (`AppCompatDelegate.setApplicationLocales` + a manual `Activity.recreate()`, since `MainActivity` is a plain `ComponentActivity` and can't auto-recreate). `MainActivity` forces `LocalLayoutDirection.Ltr` globally — Hebrew only changes the text, layout stays LTR.

### Known dead code

`ui/screens/LicencesScreen.kt` and `ui/components/VehicleActionCard.kt` are not referenced from `AppNavGraph.kt` or anywhere else — don't assume they're live. The actual licence screen (route `licences/{vehicleId}`) is `VehicleLicenceScreen.kt`.

## Directory reference

One line per file — what it's for, not what it obviously is by name.

### `data/repository/` (Firebase-backed implementations)
- `FirebaseAuthRepository` — auth (login/register/password/email change) + the `users/{uid}` profile doc
- `FirestoreVehicleRepository` — vehicle CRUD + specs
- `FirestoreVehicleLicenceRepository`, `FirestoreVehicleDocumentRepository`, `FirestoreVehicleHistoryRepository`, `FirestoreReminderRepository` — CRUD for their respective subcollections; `FirestoreVehicleDocumentRepository` is the one with the atomic batch-write methods
- `FirebaseAiChatRepository`, `FirebaseWarningLightRepository`, `FirebaseVehicleLicenceAnalysisRepository`, `FirebaseVehicleDocumentAnalysisRepository`, `FirebaseVehicleHistoryAnalysisRepository`, `FirebaseVehicleSpecsRepository` — Gemini-backed AI calls, see above

### `domain/model/` (data classes & enums)
- `Vehicle`, `VehicleSpecs` — the vehicle itself and its specs (fuel/engine/transmission, possibly multiple `EngineVariant`s)
- `VehicleLicence`, `VehicleLicenceExtraction` — the vehicle's registration/licence data; `*Extraction` types are the raw shape returned by an AI scan before the user confirms it
- `VehicleDocument`, `VehicleDocumentExtraction`, `VehicleDocumentType`, `VehicleDocumentStatus` — the generic "any document" model (licence/insurance/inspection/maintenance/repair/other); `status` tracks ACTIVE/REPLACED/ARCHIVED for the document record itself, unrelated to whether it's expired
- `VehicleHistoryEvent`, `VehicleHistoryEventType` — a maintenance/repair timeline entry
- `VehicleReminder`, `ReminderType`, `ReminderStatus`, `DatePrecision` — see the reminder pipeline above
- `MaintenanceItem`, `MaintenanceItemType`, `MaintenanceAction`, `VehicleMaintenanceItemState`, `MaintenanceExtraction`, `MaintenanceTextExtraction`, `ConfirmedMaintenanceUpdate` — per-part maintenance tracking (oil, filters, brakes, etc.) and the AI extraction/confirmation flow for it
- `WarningLightResult`, `WarningLightAnalysis`, `WarningSeverity` — dashboard warning-light scan result
- `UserProfile` — the user profile doc shape
- `CarCatalog` — static manufacturer/model/year data used for autocomplete in `AddVehicleScreen`

### `ui/viewmodel/`
- `AuthViewModel` — the "everything" ViewModel: session, profile (incl. avatar + licence photo), vehicle list, vehicle specs, and licence-expiry alert computation (`expiredLicenceVehicleIds`); shared across Home/Profile/Settings/VehicleDetails
- `VehicleLicenceViewModel`, `VehicleDocumentsViewModel`, `VehicleHistoryViewModel` — one per vehicle-scoped feature screen, each owns its repository calls + reminder syncing for that feature
- `AiAssistantViewModel` — warning-light scan list/state for a vehicle
- `AiChatViewModel` — the chat conversation state
- `NotificationsViewModel` — loads and sorts reminders for the notifications list (both vehicle-scoped and vehicle-independent)

### `ui/screens/`
- `SplashScreen`, `LoginScreen`, `SignUpScreen`, `ForgotPasswordScreen` — auth flow
- `AppGuideScreen` — onboarding (after sign-up) and in-app help (from Settings), same screen with an `isOnboarding` flag
- `HomeScreen` — vehicle list + entry points
- `AddVehicleScreen` — also handles editing (`editingVehicleId` param)
- `VehicleSetupQuestionsScreen` — first-run questions (inspection/licence due dates) right after adding a vehicle
- `VehicleDetailsScreen` — per-vehicle hub linking to the other vehicle-scoped screens
- `VehicleLicenceScreen` — the real licence screen (see "Known dead code" above)
- `VehicleDocumentsScreen` — document list, AI-scan review form, and manual entry, all sharing one `DocumentReviewForm`
- `VehicleHistoryScreen` — maintenance/repair timeline
- `NotificationsScreen` — reminders list with the OVERDUE/DUE/SOON/UPCOMING color coding
- `AiAssistantScreen` — warning-light photo scanner entry point
- `ChatbotScreen` — the actual chat UI
- `ProfileScreen`, `SettingsScreen` — user profile (incl. licence photo capture) and app settings (language, help, account deletion)

### `ui/components/`
- `AppTextFields.kt` (`AppTextField`), `AppDateField.kt` (`AppDateField`) — styled text/date inputs; date fields always store `yyyy-MM-dd`
- `AutocompleteTextField` — manufacturer/model autocomplete, backed by `CarCatalog`
- `VehicleCard`, `VehicleFeatureCard` — the Home vehicle tile and the feature tiles on `VehicleDetailsScreen` (both support a red alert badge)
- `AvatarImage`, `ProfileImagePicker` — base64 image display and gallery/camera pickers (used for avatar and licence photos, with a configurable `maxDimension`)
- `WelcomeCard`, `AssistantBanner`, `AutomateRobot` — Home-screen header card, the "need help" banner, and the mascot image used there and in the AI screens
- `AiDisclaimerNote` — the "this is an AI estimate" disclaimer shown near AI-generated results
- `BottomNavBar` — the shared bottom navigation bar
