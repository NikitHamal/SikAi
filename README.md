# SikAi — सिकाइ

**SikAi** ("learning" in Nepali) is an offline-first native Android learning app
for Nepali students preparing for **Class 8**, **SEE (Class 10)**, and **NEB
(Class 12)** examinations. It pairs a multi-provider AI tutor with downloadable
past papers, MCQ practice, study plans, and Devanagari-aware notes — wrapped in
a custom **Neo-Vedic** Material 3 design system (parchment + cosmic indigo +
vedic gold, sharp 2 dp corners, hairline borders, no shadows, L-shaped corner
markers on sacred surfaces).

> Package: `com.sikai.learn` · minSdk 24 · targetSdk 34 · Kotlin + Jetpack
> Compose · Hilt · Room · DataStore · WorkManager · Retrofit · CameraX

---

## Features

| Screen        | What it does                                                      |
|---------------|-------------------------------------------------------------------|
| Onboarding    | Class · language · subjects · exam date in 5 steps                |
| Home          | नमस्ते greeting, streak, quick actions, library tiles            |
| AI Tutor      | Multi-provider chat with Direct/Simple/Socratic/Exam-answer modes |
| Snap & Solve  | CameraX capture → multimodal solve (no OCR — full image upload)   |
| Past Papers   | SEE & NEB archive, downloadable for offline review                |
| Downloads     | Manifest-driven, SHA-256 verified, paused / resumable             |
| Quizzes       | 5-question packs, per-topic mastery tracking                      |
| Study Plan    | Auto-generated schedule from class & exam date                    |
| Notes         | Manual notes + saved AI answers                                   |
| Progress      | Streak, XP, attempt history, weak-topic insights                  |
| Settings      | Theme, language, providers, about                                 |

### AI provider matrix

The orchestrator picks the highest-priority enabled provider that satisfies the
requested capability set, with automatic capability-aware fallback.

| Provider     | Auth                       | Capabilities                        |
|--------------|----------------------------|-------------------------------------|
| **Qwen**     | None (browser-emulation)   | TEXT, VISION, FILE_UPLOAD, THINKING, SEARCH |
| DeepInfra    | Free key (BYOK)            | TEXT, VISION, STREAMING             |
| Gemini       | Free key (BYOK)            | TEXT, VISION, FILE_UPLOAD, STREAMING |
| OpenRouter   | BYOK                       | TEXT, VISION, STREAMING             |
| NVIDIA NIM   | BYOK                       | TEXT, STREAMING                     |
| DeepSeek     | BYOK                       | TEXT, THINKING, STREAMING           |

API keys are stored locally with **EncryptedSharedPreferences** backed by
**Android Keystore** (AES-256-GCM master key). Keys are **never** transmitted
to the SikAi backend.

---

## Build

```bash
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

The repo intentionally commits a development keystore at
`app/release-keystore.jks` and `app/signing.properties` so anyone can produce a
verifiable signed APK. **Replace these for production** before publishing.

### CI

`.github/workflows/android-release.yml` builds a signed APK on every push to
`main` and on tag pushes. The artifact is named `SikAi-<short-sha>.apk` and is
attached to a generated GitHub Release for tagged builds.

---

## Backend

Cloudflare Worker in `backend/` (R2 + D1):

```bash
cd backend
npm install
wrangler d1 create sikai
wrangler r2 bucket create sikai-content
# update database_id in wrangler.toml
wrangler d1 execute sikai --file=schema.sql --remote
wrangler deploy
```

Endpoints: `/health`, `/manifest`, `/manifest/:class`, `/files/:fileKey`,
`/questions?class=&subject=`. Override the URL the app talks to in
`app/build.gradle.kts → buildConfigField("CONTENT_API_BASE", ...)`.

---

## Architecture

```
ui/screens/<feature>          ←  Compose screens + ViewModels (MVVM)
ui/components/NeoVedic*       ←  custom design system (no Material You)
data/repository               ←  domain repos (single source of truth)
data/db (Room) + dao          ←  15 entities, indexed for class/subject lookups
data/remote (Retrofit)        ←  ManifestApi
ai/provider                   ←  AiProvider abstraction + AiOrchestrator
ai/providers/<vendor>         ←  Qwen, Gemini, DeepInfra, OpenRouter, NVIDIA, DeepSeek
core/storage                  ←  UserPreferences (DataStore) + SecureKeyStore
core/di                       ←  Hilt modules with @AiHttp / @ContentHttp / @DownloadHttp qualifiers
```

First launch idempotently seeds: subjects, sample questions, past papers,
built-in providers, and a fallback content manifest (`assets/seed_manifest.json`).

---

## Privacy

- API keys: **on-device only** (Android Keystore + EncryptedSharedPreferences)
- Backups: API keys excluded from auto-backup & data extraction
- Analytics: none
- Telemetry: none

---

## License

Educational use. The included development keystore is a placeholder — generate
your own before distributing.
