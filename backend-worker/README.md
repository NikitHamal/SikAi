# SikAi content worker

Read-only Cloudflare Worker that serves the SikAi content manifest plus the
underlying PDFs. The Android client uses these endpoints to discover and
download Class 8 / Class 10 SEE / Class 12 NEB resources.

## Prereqs

- Node 18+
- A Cloudflare account
- `npx wrangler login`

## One-time setup

```bash
cd backend-worker
npm install

# Create D1 + R2 (the names match wrangler.toml).
npx wrangler d1 create sikai-content
npx wrangler r2 bucket create sikai-content

# Paste the printed `database_id` into wrangler.toml.

# Apply schema (local for dev, then remote when ready).
npm run db:apply
npm run db:apply:remote
npm run seed:remote   # optional: seed the sample manifest
```

## Develop locally

```bash
npm run dev
# -> http://127.0.0.1:8787/v1/manifest
# -> http://127.0.0.1:8787/v1/manifest?classLevel=10
```

Point the Android app at the dev URL by setting `BACKEND_BASE_URL` in
`app/signing.properties` (the value is read at build time and baked into
`BuildConfig`).

## Deploy

```bash
npm run deploy
```

## Adding content

1. Upload a PDF: `curl -H "x-sikai-key: $TOKEN" --data-binary @math.pdf https://<worker>/v1/admin/files/see/2080/math.pdf`
2. Register the manifest row by POSTing JSON to `/v1/admin/manifest`.
3. The Android app refreshes the manifest on launch (and on demand from the
   Past Papers screen).

Set `ADMIN_TOKEN` via `npx wrangler secret put ADMIN_TOKEN` to lock down
admin endpoints. Read endpoints stay public.
