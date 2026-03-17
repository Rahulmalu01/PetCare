# PetCare App

PetCare is a Kotlin Android app that helps pet owners manage animal profiles, health records, and care reminders with a secure Supabase backend.

## 🚀 Quick Overview

- Native Android app built with Kotlin and Jetpack components.
- Supabase backend for authentication, Postgres data storage, and realtime updates.
- Google OAuth sign-in support (via Supabase).
- Pet management, health tracking, appointment scheduling, and notification reminders.

## ✨ Core Features

- User sign up, login, and secure session handling.
- Add/edit/delete pets (name, species, breed, age, photo, notes).
- Track vaccinations, medications, vet visits and behavior logs.
- Configurable reminders for medicine, grooming, checkups, and vaccines.
- Persist data in Supabase tables (pets, health_records, reminders).

## 📁 Repository Structure

- `app/` — Android app module source code.
- `build.gradle.kts`, `settings.gradle.kts` — Gradle configuration.
- `MOBILE_PRESENTATION.md` — demo walkthrough.
- `RUN_DEPLOY.md` — setup and deployment guide.

## 🛠️ Prerequisites

1. Android Studio (latest stable)
2. Java JDK 17+
3. Supabase project with Auth + Database
4. Google Cloud OAuth credentials (for Google sign-in)

## 🔧 Setup

1. Copy `local.properties.template` to `local.properties`.
2. Set keys in `local.properties`:
   - `api.url` (Supabase URL)
   - `api.key` (Supabase anon/public key)
   - `server.client.id` (Google OAuth client ID)
3. Configure Supabase:
   - Create required tables (check `RUN_DEPLOY.md`).
   - Enable Google provider under Authentication.

## ▶️ Run

1. Open project in Android Studio.
2. Sync Gradle.
3. Launch on device/emulator.

## 🧪 Build Release APK

1. Build > Generate Signed Bundle / APK.
2. Select APK, complete signing.

## 🤝 Contribution

1. Fork repo
2. Create branch `feature/<name>`
3. Add changes, commit, push
4. Open a PR with description and testing notes

## 📌 Notes

- Keep secrets out of source control.
- Requires network access to Supabase.

## 🐾 License

MIT
