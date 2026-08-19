# Automate

Automate is an Android app for keeping track of everything that comes with owning a car: registration and licence renewals, insurance, inspections, maintenance history, and dashboard warning lights, all in one place instead of a folder of paperwork and a mental note to deal with it later.

The app leans on Firebase's Gemini integration for the tedious parts. Point the camera at a licence, an insurance policy, a service invoice, or a warning light on the dashboard, and the app reads it and fills in the details for you to confirm. Manual entry is always available too, it's just not the only option.

## Features

- Add and manage multiple vehicles in one account
- Scan documents (licence, insurance, inspection, service invoices) with AI-assisted extraction, or enter them by hand
- Automatic expiry tracking for licence, insurance, and inspection dates, with reminders that show up on the vehicle itself once something is due or overdue
- Maintenance history per vehicle, including per-part tracking for things like oil, filters, brakes, battery, and tires
- Photograph a dashboard warning light and get an explanation, a severity rating, and what to do next
- A chat assistant for asking questions about a specific vehicle
- A place to keep a photo of your driving licence, in case you forget the physical card
- English and Hebrew, switchable in-app regardless of the device's system language
- Per-user data isolation through Firebase Authentication and Firestore

## Tech stack

Kotlin and Jetpack Compose (Material 3) for the UI, built as a single Gradle module. Architecture is MVVM, with a repository/domain layer that keeps the ViewModels decoupled from Firebase. The backend is Firebase throughout: Authentication, Cloud Firestore, App Check, and Firebase AI Logic for the Gemini-powered scanning and chat features.

## Getting started

You'll need Android Studio, a Firebase project with Email/Password authentication and Firestore enabled, and your own `google-services.json` from that project.

```bash
git clone https://github.com/amrha88/FinalProject.git
cd FinalProject
```

Drop `google-services.json` into `app/`, then:

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build and install to a connected device or emulator
```

## Project structure

Screens live in `ui/screens`, backed by ViewModels in `ui/viewmodel`. Those talk to interfaces in `domain/repository`, which are implemented against Firebase in `data/repository`. Shared logic like syncing reminders lives in `domain/engine`. `CLAUDE.md` has a fuller breakdown of the architecture and what's in each directory, if you want more detail than this file goes into.

## Localization

English and Hebrew are both fully supported, and the language can be changed from Settings without affecting the rest of the device.
