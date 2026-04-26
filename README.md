# SikAi

SikAi means **learning** in Nepali. It is a native Android learning app for students in Nepal, focused on Class 8, Class 10 SEE, and Class 12 NEB exam preparation.

## Features

- Neo-Vedic Compose design system with parchment backgrounds, cosmic indigo text, Vedic gold accents, hairline borders, 2dp corners, and dark mode.
- Onboarding for class, language, subjects, and optional exam date with no login requirement.
- AI Tutor with provider abstraction, context-aware SikAi prompts, modes for Socratic tutoring, simple explanations, direct answers, exam answers, quizzes, notes, flashcards, study planning, and weakness analysis.
- Snap & Solve using CameraX, gallery picker, and file/PDF picker. Images and files go to multimodal/file-capable providers directly; SikAi does not require an OCR-first path.
- Offline-first Room database for profiles, subjects, manifests, downloads, past papers, questions, quizzes, weak topics, notes, saved AI answers, study plans, provider configs, and provider logs.
- Manifest-driven downloads with local SHA-256 verification and bundled seed content for offline demo.
- Quizzes from local sample data with saved attempts and weak-topic tracking.
- Settings for custom providers, local encrypted API keys, default models, fallback order foundation, theme, language placeholder, and profile changes.
- Cloudflare Worker backend for `/manifest`, `/manifest/:class`, `/files/:fileKey`, `/questions`, and `/health`.
- GitHub Actions release workflow that builds, signs, renames, uploads, and publishes exactly one APK.

## Screenshots

Add release screenshots here after running the app:

- `docs/screenshots/home.png`
- `docs/screenshots/tutor.png`
- `docs/screenshots/solve.png`
- `docs/screenshots/progress.png`

## Tech Stack

- Kotlin, Jetpack Compose, Material 3 foundation
- MVVM + clean domain/data/presentation boundaries
- Hilt, Coroutines, Flow
- Room, DataStore, AndroidX Security Crypto
- WorkManager-ready download worker
- OkHttp, Retrofit dependency, Kotlin Serialization
- Coil dependency, CameraX
- Cloudflare Workers, R2, D1
- GitHub Actions, signed release APK

## Setup

```bash
./gradlew clean
./gradlew test
./gradlew assembleRelease
```

The package name is `com.sikai.learn` and the Android app module is `app`.

## AI Providers

SikAi is provider-agnostic. Built-in provider configs include Qwen, DeepInfra, Gemini, OpenRouter, NVIDIA NIM, and DeepSeek, plus custom provider support. Provider configs are stored in Room. User API keys are stored only on-device using Android Keystore-backed `EncryptedSharedPreferences` and are never uploaded to the SikAi backend.

To configure a provider:

1. Open Settings.
2. Paste the provider API key into the provider card or add a custom provider.
3. Choose the default provider.
4. Tap Test to check provider health.

Custom providers support:

- Provider name
- Base URL
- Local API key
- Text model
- Multimodal model
- Request format: OpenAI-compatible, Gemini-compatible, or custom simple foundation
- File upload/PDF capability flag

SikAi intentionally does not implement WAF bypass, proxy abuse, identity rotation, or rate-limit evasion. Provider failures are logged locally with non-secret failure reasons and routed through the fallback chain.

## Downloads

The Android app first tries the backend manifest URL from `BuildConfig.DEFAULT_MANIFEST_URL`. If unavailable, it loads `app/src/main/assets/manifest_seed.json`. Downloads are copied from assets for demo content or fetched through URL/R2 file routes, then verified with SHA-256 before Room metadata is saved.

## Cloudflare Backend

The `backend/` folder contains a Worker-compatible content service:

- `GET /manifest`
- `GET /manifest/:class`
- `GET /files/:fileKey`
- `GET /questions?class=&subject=&topic=&limit=`
- `GET /health`

Configure `backend/wrangler.toml` with your R2 bucket and D1 database IDs, then deploy:

```bash
cd backend
npm install
npm run deploy
```

The backend serves downloadable content and public metadata only. AI calls do not go through the backend.

## GitHub Actions Release

`.github/workflows/android-release.yml` runs on pushes to `main`, matching tags, and manual dispatch. It:

1. Sets up JDK 17.
2. Runs `./gradlew clean` and `./gradlew test`.
3. Builds `:app:assembleRelease`.
4. Renames the APK to `SikAi-${SHORT_COMMIT_HASH}.apk`.
5. Uploads exactly that APK as an artifact.
6. Creates/updates a GitHub Release with tag `sikai-${SHORT_COMMIT_HASH}` and title `SikAi ${SHORT_COMMIT_HASH}`.

## Keystore Note

`app/release-keystore.jks` and `app/signing.properties` are intentionally committed for repeatable release signing as requested. Do not store AI provider API keys in the repository.

## Roadmap

- Add real production content manifests and R2 files.
- Add richer markdown rendering and citation-style answer cards.
- Add Hilt-enabled queued WorkManager download orchestration.
- Add optional Firebase Auth module behind repository interfaces.
- Add Nepali localization strings and bundled Devanagari font assets.
- Add encrypted settings export without API keys.
