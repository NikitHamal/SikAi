# SikAi Backend

Cloudflare Worker exposing the read-only SikAi content API.

## Routes

| Route                         | Description                                          |
|-------------------------------|------------------------------------------------------|
| `GET /health`                 | Liveness probe (`{ ok: true }`)                      |
| `GET /manifest`               | All manifest entries                                 |
| `GET /manifest/:classLevel`   | Manifest filtered by class (8, 10, 12)               |
| `GET /files/:fileKey`         | Streams an R2 object with `cache-control: immutable` |
| `GET /questions?class=&subject=&limit=` | Random question pack                       |

## Setup

```bash
cd backend
npm install
# create D1 + R2
wrangler d1 create sikai
wrangler r2 bucket create sikai-content
# update wrangler.toml with the new database_id
wrangler d1 execute sikai --file=schema.sql --remote
wrangler deploy
```

The Android app reads `BuildConfig.CONTENT_API_BASE` (defaults to a Workers URL).
Override per-build by editing `app/build.gradle.kts → buildConfigField("CONTENT_API_BASE", ...)`.

## Notes

- API keys for AI providers are NEVER routed through this worker. They live on
  the user's device (Android Keystore via EncryptedSharedPreferences).
- All asset URLs returned in the manifest carry a SHA-256 checksum that the
  client verifies after download.
