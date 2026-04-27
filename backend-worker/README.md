# SikAi Content Platform

Cloudflare Worker backend + SvelteKit admin dashboard for the SikAi learning app.

## Architecture

```
Android App  ──HTTP──>  Cloudflare Worker  ──>  D1 (SQLite)  +  R2 (files)
                                ^
Admin Dashboard ──HTTP──>─────┘
```

## Backend Worker (`backend-worker/`)

### API Endpoints

**Public (app) endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Liveness check |
| GET | `/v1/manifest` | Full content manifest |
| GET | `/v1/manifest?classLevel=10` | Filtered manifest |
| GET | `/v1/manifest?classLevel=10&subject=Mathematics` | Filtered by class + subject |
| GET | `/v1/files/:fileKey` | Stream file from R2 |
| GET | `/v1/questions?classLevel=10&subject=Mathematics` | Quiz questions |
| GET | `/v1/subjects?classLevel=10` | Subjects for class |
| POST | `/v1/analytics` | Log analytics event |
| GET | `/v1/config` | App config (feature flags) |

**Admin endpoints (require `Authorization: Bearer <token>`):**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/admin/login` | Authenticate, get JWT |
| GET | `/v1/admin/stats` | Dashboard overview stats |
| GET/POST | `/v1/admin/manifest` | List / upsert manifest |
| PUT/DELETE | `/v1/admin/manifest/:id` | Update / delete manifest entry |
| POST/DELETE | `/v1/admin/files/:fileKey` | Upload / delete R2 file |
| GET/POST | `/v1/admin/questions` | List / create questions |
| PUT/DELETE | `/v1/admin/questions/:id` | Update / delete question |
| POST | `/v1/admin/questions/bulk` | Bulk import questions |
| GET/POST | `/v1/admin/subjects` | List / upsert subjects |
| DELETE | `/v1/admin/subjects/:id` | Delete subject |
| GET | `/v1/admin/analytics` | Analytics dashboard data |
| GET/PUT | `/v1/admin/config` | App config management |

### Setup

```bash
cd backend-worker
npm install

# Create D1 database and R2 bucket
npx wrangler d1 create sikai-content
npx wrangler r2 bucket create sikai-content

# Paste the database_id into wrangler.toml

# Set admin secrets
npx wrangler secret put ADMIN_TOKEN
npx wrangler secret put JWT_SECRET

# Apply schema (local, then remote)
npm run db:apply
npm run db:apply:remote

# Seed data
npm run seed:remote

# Develop locally
npm run dev

# Deploy
npm run deploy
```

### Adding Content via API

```bash
# Login
TOKEN=$(curl -s https://your-worker/v1/admin/login \
  -H 'Content-Type: application/json' \
  -d '{"password":"your-admin-token"}' | jq -r .token)

# Upload a PDF to R2
curl -H "Authorization: Bearer $TOKEN" \
  --data-binary @math.pdf \
  https://your-worker/v1/admin/files/see/2080/math.pdf

# Add manifest entry
curl -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"id":"see-2080-math","title":"SEE 2080 Math","type":"past_paper","classLevel":10,"subject":"Mathematics","year":2080,"fileKey":"see/2080/math.pdf","sizeBytes":524288,"checksumSha256":"abc123","language":"en","tags":["see","2080","math"]}]}' \
  https://your-worker/v1/admin/manifest

# Add quiz questions in bulk
curl -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"questions":[{"id":"q10-math-001","classLevel":10,"subject":"Mathematics","topic":"Algebra","prompt":"Solve: 2x + 3 = 7","options":["x = 2","x = 5","x = 3","x = 1"],"correctIndex":0,"explanation":"2x = 4, x = 2","difficulty":"easy"}]}' \
  https://your-worker/v1/admin/questions/bulk
```

## Admin Dashboard (`admindashboard/`)

Modern dark-mode SvelteKit admin dashboard for managing content, questions, and viewing analytics.

```bash
cd admindashboard
npm install

# Configure API base URL
cp .env.example .env
# Edit .env to set VITE_API_BASE to your worker URL

# Develop
npm run dev

# Build for production (static)
npm run build
```

Deploy the `build/` directory to Cloudflare Pages, Netlify, or any static host.

## Android App Integration

The Android app (`app/`) automatically syncs from the backend on boot:
- Content manifest refreshes on every app launch
- Subjects sync from the backend
- Questions sync for the user's class level

Point `app/signing.properties` `backendBaseUrl` to your deployed worker URL.

## Database Schema

The D1 database has 6 tables:
- `content_manifest` — Downloadable content (past papers, syllabi, etc.)
- `question` — MCQ quiz questions
- `subject` — Subjects per class level
- `analytics_event` — App usage analytics
- `admin_session` — JWT session tokens
- `app_config` — Feature flags and announcements