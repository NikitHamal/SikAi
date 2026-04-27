# SikAi

> **Sik (सिक) + AI** — an AI learning copilot for Nepali students preparing
> for **Class 8**, **Class 10 SEE**, and **Class 12 NEB** board exams.

SikAi runs entirely on-device. Users bring their own API keys (or rely on the
free, no-auth providers), pick their class and subjects, and get a tutor,
quizzes, study plans, past papers, and a "snap & solve" camera that hands a
photo of a problem straight to a multimodal model.

| | |
|---|---|
| **Stack** | Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, WorkManager, Retrofit, Kotlinx Serialization |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 |
| **Backend** | Cloudflare Worker (D1 + R2) for the content manifest only — see [`backend-worker/`](backend-worker) |

## Screens

1. **Onboarding** — pick class (8 / 10 / 12), subjects, study minutes, exam date.
2. **Home** — quick actions and stats.
3. **AI Tutor** — multi-mode chat (Socratic, Direct, Simple, Exam, Step-by-Step).
4. **Snap & Solve** — pick a photo of a problem; multimodal provider returns a worked solution.
5. **Past Papers** — class-filtered SEE / NEB papers from the manifest.
6. **Downloads** — offline materials manager (WorkManager + SHA-256 verified).
7. **Quizzes** — 8-question MCQ runs, seeded offline + AI-generated as needed.
8. **Study Plan** — day-by-day plan that respects the exam date.
9. **Notes** — personal notes (CRUD).
10. **Progress** — streak, recent attempts, weak topics.
11. **Settings** — theme, providers, API keys, recent provider activity.

## Design system

A **Neo-Vedic** look:

- **Vellum** parchment background `#F2EFE9`
- **Cosmic indigo** `#1A233A` for primary surfaces
- **Vedic gold** `#C5A059` for accents
- 2 dp sharp corners, hairline borders, no shadows
- L-shaped corner markers on emphasized cards

Live in `app/src/main/java/com/sikai/learn/ui/theme` and
`.../ui/components` (`NeoVedicCard`, `NeoVedicButton`, `NeoVedicTextField`,
`NeoVedicAiAnswerCard`, etc.).

## AI provider stack

The app talks to AI through a capability-aware abstraction (`AiProvider`,
`AiProviderRegistry`, `AiOrchestrator`). For each request the orchestrator
walks the user's enabled providers in priority order and stops at the first
one that supports the required capability and succeeds.

| Provider | Auth | Notes |
|---|---|---|
| **Qwen** (default) | None | Public chat API (per `NikitHamal/Flashy`). Identity rotation, midtoken, SSE. |
| **DeepInfra** | None | Public OpenAI-compatible echo. |
| **Gemini** | API key | 1.5 Pro / Flash, multimodal. |
| **OpenRouter** | API key | Routes to any compatible model. |
| **NVIDIA** | API key | NIM-hosted OpenAI-compatible models. |
| **DeepSeek** | API key | Reasoning + chat. |
| **Custom OpenAI-compatible** | API key | User-defined `baseUrl` + key for self-hosted/Together/Groq/etc. |

User-supplied keys live in `EncryptedSharedPreferences` backed by an
`AndroidKeyStore` master key. **They never leave the device.**

For multimodal solves (`AiTask.SOLVE_FILE`) the orchestrator filters to
providers that advertise `VISION`, `FILE_UPLOAD`, or `PDF` — there is **no
on-device OCR**.

## Backend (Cloudflare Worker)

The worker (in [`backend-worker/`](backend-worker)) exposes a read-only
content manifest from D1 plus file proxying from R2. The Android app:

1. Calls `GET /v1/manifest?classLevel=10` on demand.
2. Mirrors the response into Room (`content_manifest`).
3. Streams files from `BACKEND_BASE_URL/files/{fileKey}` (or a direct
   `fileUrl`) using `WorkManager`, verifies SHA-256, stores under
   `filesDir/downloads/`.

Set `backendBaseUrl` in `app/signing.properties` to point at your worker.

## Build & sign

```bash
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

### Keystore note

We ship a deterministic release keystore at
`app/release-keystore.jks` along with `app/signing.properties` so every CI
build signs the APK with the **same key**, which is required to keep the same
package signature across upgrades. **These credentials are intentionally
non-secret** — they are not user-installable production keys; they are for
reproducible community builds. If you fork the project for distribution
through Play Store or another channel, **regenerate your own keystore** and
keep its credentials out of the repo.

The CI workflow (`.github/workflows/android-release.yml`) auto-generates the
keystore with the credentials in `signing.properties` if it's missing, so you
can clone-and-build without committing binaries.

### CI

Pushing to `main` builds an APK named `SikAi-${SHORT_SHA}.apk` and uploads it
as a workflow artifact. Pushing a `v*` tag also publishes a GitHub Release
with the APK attached.

## Project layout

```
app/
  src/main/java/com/sikai/learn/
    ai/              # provider abstraction + Qwen / Gemini / OpenAI-compat impls
    data/
      local/         # Room entities + DAOs
      remote/        # Retrofit interface for the worker
      repository/    # AiProviderRepository, ContentManifestRepository, …
      secure/        # EncryptedKeyStore (AES256_GCM)
      download/      # DownloadWorker, DownloadManager
    di/              # Hilt modules
    domain/model/    # Pure Kotlin domain types
    presentation/    # Compose screens + ViewModels
    ui/              # Neo-Vedic theme + components
    util/            # Formatters etc.
  src/main/assets/seed/
    questions.json   # offline MCQ seed
    manifest.json    # offline manifest seed
backend-worker/
  src/index.ts       # Worker entrypoint
  migrations/        # D1 schema
  sample-manifest/   # Seed SQL
  wrangler.toml
.github/workflows/android-release.yml
```

## Roadmap

- Wire the Snap & Solve provider chain to write attempts back into the
  question bank (so missed problems become quizable).
- Multi-language UI (Nepali, English, Maithili).
- Offline AI fallback via on-device Gemma-3n / Qwen2 1.5B.
- Spaced repetition scheduler in Study Plan.
- Optional sync of notes to the worker (E2E-encrypted).

## License

MIT — see `LICENSE` if/when added.
