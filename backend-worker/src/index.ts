/**
 * SikAi Content Platform Worker.
 *
 * No R2 — all files are hosted externally (GitHub Releases, etc.) and
 * referenced by `file_url` in the manifest. D1 handles all structured data.
 *
 * Public (app) endpoints:
 *   GET  /health                                    -> liveness
 *   GET  /v1/manifest                               -> full manifest (optional ?classLevel=&subject=&type=)
 *   GET  /v1/questions?classLevel=10&subject=Math   -> quiz questions
 *   GET  /v1/subjects?classLevel=10                  -> subjects for class
 *   POST /v1/analytics                               -> log analytics event
 *   GET  /v1/config                                  -> app config (feature flags)
 *
 * Admin endpoints (require Bearer token):
 *   POST /v1/admin/login                             -> authenticate, get JWT
 *   GET  /v1/admin/stats                             -> dashboard overview
 *   ── Manifest CRUD ──
 *   GET  /v1/admin/manifest                         -> list (paginated)
 *   POST /v1/admin/manifest                          -> upsert rows
 *   PUT  /v1/admin/manifest/:id                      -> update single row
 *   DELETE /v1/admin/manifest/:id                     -> delete row
 *   ── Questions CRUD ──
 *   GET  /v1/admin/questions                          -> list (with filters)
 *   POST /v1/admin/questions                          -> create
 *   PUT  /v1/admin/questions/:id                      -> update
 *   DELETE /v1/admin/questions/:id                     -> delete
 *   POST /v1/admin/questions/bulk                     -> bulk import
 *   POST /v1/admin/questions/bulk-delete              -> bulk delete
 *   ── Manifest bulk ──
 *   POST /v1/admin/manifest/bulk-delete                -> bulk delete
 *   ── Subjects bulk ──
 *   POST /v1/admin/subjects/bulk-delete                -> bulk delete
 *   ── Subjects CRUD ──
 *   GET  /v1/admin/subjects                           -> list
 *   POST /v1/admin/subjects                           -> upsert
 *   DELETE /v1/admin/subjects/:id                     -> delete
 *   ── Analytics ──
 *   GET  /v1/admin/analytics                          -> analytics dashboard
 *   ── Config ──
 *   GET  /v1/admin/config                             -> get all config
 *   PUT  /v1/admin/config/:key                        -> update config
 */

export interface Env {
  DB: D1Database;
  ADMIN_TOKEN: string;
  JWT_SECRET: string;
  ALLOWED_ORIGIN: string;
}

interface ManifestRow {
  id: string;
  title: string;
  type: string;
  classLevel: number;
  subject: string;
  year: number | null;
  fileUrl: string | null;
  sizeBytes: number;
  checksumSha256: string | null;
  version: number;
  updatedAt: number;
  language: string;
  tags: string[];
}

interface QuestionRow {
  id: string;
  classLevel: number;
  subject: string;
  topic: string;
  prompt: string;
  options: string[];
  correctIndex: number;
  explanation: string | null;
  source: string;
  language: string;
  difficulty: string;
}

interface SubjectRow {
  id: string;
  displayName: string;
  classLevel: number;
  icon: string | null;
  sortOrder: number;
}

interface AnalyticsEvent {
  deviceId: string;
  eventType: string;
  classLevel?: number;
  subject?: string;
  metadata?: string;
}

interface JwtPayload {
  sub: string;
  exp: number;
  iat: number;
}

const JSON_HEADERS = { "content-type": "application/json; charset=utf-8" };

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    if (method === "OPTIONS") return preflight(env);

    try {
      if (path === "/health") return json({ ok: true, service: "sikai-content", time: Date.now() }, env);

      // ── Public ──
      if (path === "/v1/manifest" && method === "GET") return await handleManifest(request, env);
      if (path === "/v1/questions" && method === "GET") return await handleQuestions(request, env);
      if (path === "/v1/subjects" && method === "GET") return await handleSubjects(request, env);
      if (path === "/v1/analytics" && method === "POST") return await handleAnalytics(request, env);
      if (path === "/v1/config" && method === "GET") return await handleConfig(env);

      // ── Admin ──
      if (path === "/v1/admin/login" && method === "POST") return await handleAdminLogin(request, env);

      if (path.startsWith("/v1/admin/")) {
        const auth = await verifyAdminJwt(request, env);
        if (!auth.ok) return unauthorized(env, auth.error);

        if (path === "/v1/admin/stats" && method === "GET") return await handleAdminStats(env);
        if (path === "/v1/admin/manifest" && method === "GET") return await handleAdminManifestList(request, env);
        if (path === "/v1/admin/manifest" && method === "POST") return await handleAdminUpsert(request, env);
        if (path === "/v1/admin/manifest/bulk-delete" && method === "POST") return await handleAdminManifestBulkDelete(request, env);
        if (path.match(/^\/v1\/admin\/manifest\/[^/]+$/) && method === "PUT") {
          const id = decodeURIComponent(path.split("/").pop()!);
          return await handleAdminManifestUpdate(id, request, env);
        }
        if (path.match(/^\/v1\/admin\/manifest\/[^/]+$/) && method === "DELETE") {
          const id = decodeURIComponent(path.split("/").pop()!);
          return await handleAdminManifestDelete(id, env);
        }
        if (path === "/v1/admin/questions" && method === "GET") return await handleAdminQuestions(request, env);
        if (path === "/v1/admin/questions" && method === "POST") return await handleAdminQuestionCreate(request, env);
        if (path === "/v1/admin/questions/bulk" && method === "POST") return await handleAdminQuestionBulk(request, env);
        if (path === "/v1/admin/questions/bulk-delete" && method === "POST") return await handleAdminQuestionBulkDelete(request, env);
        if (path.match(/^\/v1\/admin\/questions\/[^/]+$/) && method === "PUT") {
          const id = decodeURIComponent(path.split("/").pop()!);
          return await handleAdminQuestionUpdate(id, request, env);
        }
        if (path.match(/^\/v1\/admin\/questions\/[^/]+$/) && method === "DELETE") {
          const id = decodeURIComponent(path.split("/").pop()!);
          return await handleAdminQuestionDelete(id, env);
        }
        if (path === "/v1/admin/subjects" && method === "GET") return await handleAdminSubjects(env);
        if (path === "/v1/admin/subjects" && method === "POST") return await handleAdminSubjectUpsert(request, env);
        if (path === "/v1/admin/subjects/bulk-delete" && method === "POST") return await handleAdminSubjectBulkDelete(request, env);
        if (path.match(/^\/v1\/admin\/subjects\/[^/]+$/) && method === "DELETE") {
          const id = decodeURIComponent(path.split("/").pop()!);
          return await handleAdminSubjectDelete(id, env);
        }
        if (path === "/v1/admin/analytics" && method === "GET") return await handleAdminAnalytics(request, env);
        if (path === "/v1/admin/config" && method === "GET") return await handleAdminConfig(env);
        if (path.match(/^\/v1\/admin\/config\/[^/]+$/) && method === "PUT") {
          const key = decodeURIComponent(path.split("/").pop()!);
          return await handleAdminConfigUpdate(key, request, env);
        }

        return json({ error: "not_found", path }, env, 404);
      }

      return json({ error: "not_found", path }, env, 404);
    } catch (err) {
      console.error("worker error", err);
      return json({ error: "internal", message: (err as Error).message }, env, 500);
    }
  },
} satisfies ExportedHandler<Env>;

// ─── JWT ─────────────────────────────────────────────────────────────

async function signJwt(payload: JwtPayload, secret: string): Promise<string> {
  const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT" })).replace(/=/g, "");
  const body = btoa(JSON.stringify(payload)).replace(/=/g, "");
  const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  const sig = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(`${header}.${body}`));
  const sigB64 = btoa(String.fromCharCode(...new Uint8Array(sig))).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
  return `${header}.${body}.${sigB64}`;
}

async function verifyJwt(token: string, secret: string): Promise<JwtPayload | null> {
  const parts = token.split(".");
  if (parts.length !== 3) return null;
  const [header, body, sig] = parts;
  const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["verify"]);
  const sigBuf = Uint8Array.from(atob(sig.replace(/-/g, "+").replace(/_/g, "/")), (c) => c.charCodeAt(0));
  const valid = await crypto.subtle.verify("HMAC", key, sigBuf, new TextEncoder().encode(`${header}.${body}`));
  if (!valid) return null;
  try {
    const payload = JSON.parse(atob(body.replace(/-/g, "+").replace(/_/g, "/")));
    if (payload.exp && payload.exp < Date.now() / 1000) return null;
    return payload as JwtPayload;
  } catch { return null; }
}

type AuthResult = { ok: true } | { ok: false; error: string };

async function verifyAdminJwt(request: Request, env: Env): Promise<AuthResult> {
  const auth = request.headers.get("authorization") ?? "";
  if (!auth.startsWith("Bearer ")) return { ok: false, error: "missing_token" };
  const token = auth.slice(7);
  if (env.ADMIN_TOKEN && token === env.ADMIN_TOKEN) return { ok: true };
  const payload = await verifyJwt(token, env.JWT_SECRET || env.ADMIN_TOKEN);
  if (!payload) return { ok: false, error: "invalid_token" };
  return { ok: true };
}

function unauthorized(env: Env, reason = "unauthorized"): Response {
  return json({ error: reason }, env, 401);
}

// ─── Public: Manifest ───────────────────────────────────────────────

async function handleManifest(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const classLevel = url.searchParams.get("classLevel");
  const subject = url.searchParams.get("subject");
  const type = url.searchParams.get("type");

  let sql = "SELECT * FROM content_manifest WHERE 1=1";
  const binds: (string | number)[] = [];
  if (classLevel) { sql += " AND class_level = ?"; binds.push(Number(classLevel)); }
  if (subject) { sql += " AND subject = ?"; binds.push(subject); }
  if (type) { sql += " AND type = ?"; binds.push(type); }
  sql += " ORDER BY updated_at DESC";

  const result = await env.DB.prepare(sql).bind(...binds).all<RawManifestRow>();
  const items = (result.results ?? []).map(rowToManifest);
  return json({ items, generatedAt: Date.now() }, env);
}

// ─── Public: Questions ──────────────────────────────────────────────

async function handleQuestions(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const classLevel = url.searchParams.get("classLevel");
  const subject = url.searchParams.get("subject");
  const limit = Math.min(parseInt(url.searchParams.get("limit") ?? "20"), 100);
  const offset = parseInt(url.searchParams.get("offset") ?? "0");

  if (!classLevel) return json({ error: "classLevel is required" }, env, 400);

  let sql = "SELECT * FROM question WHERE class_level = ?";
  const binds: (string | number)[] = [Number(classLevel)];
  if (subject) { sql += " AND subject = ?"; binds.push(subject); }
  sql += " ORDER BY RANDOM() LIMIT ? OFFSET ?";
  binds.push(limit, offset);

  const result = await env.DB.prepare(sql).bind(...binds).all<RawQuestionRow>();
  const items = (result.results ?? []).map(rowToQuestion);
  return json({ items }, env);
}

// ─── Public: Subjects ───────────────────────────────────────────────

async function handleSubjects(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const classLevel = url.searchParams.get("classLevel");

  let sql = "SELECT * FROM subject";
  const binds: (string | number)[] = [];
  if (classLevel) { sql += " WHERE class_level = ?"; binds.push(Number(classLevel)); }
  sql += " ORDER BY sort_order ASC";

  const result = await env.DB.prepare(sql).bind(...binds).all<RawSubjectRow>();
  const items = (result.results ?? []).map(rowToSubject);
  return json({ items }, env);
}

// ─── Public: Analytics ──────────────────────────────────────────────

async function handleAnalytics(request: Request, env: Env): Promise<Response> {
  let body: AnalyticsEvent;
  try { body = await request.json() as AnalyticsEvent; } catch { return json({ error: "invalid_json" }, env, 400); }
  if (!body.deviceId || !body.eventType) return json({ error: "deviceId and eventType required" }, env, 400);

  await env.DB.prepare(
    "INSERT INTO analytics_event (device_id, event_type, class_level, subject, metadata, created_at) VALUES (?1,?2,?3,?4,?5,?6)"
  ).bind(body.deviceId, body.eventType, body.classLevel ?? null, body.subject ?? null, body.metadata ?? null, Date.now()).run();

  return json({ ok: true }, env);
}

// ─── Public: Config ─────────────────────────────────────────────────

async function handleConfig(env: Env): Promise<Response> {
  const result = await env.DB.prepare("SELECT key, value FROM app_config").all<{ key: string; value: string }>();
  const config: Record<string, string> = {};
  for (const row of result.results ?? []) { config[row.key] = row.value; }
  return json(config, env);
}

// ─── Admin: Login ────────────────────────────────────────────────────

async function handleAdminLogin(request: Request, env: Env): Promise<Response> {
  let body: { password?: string };
  try { body = await request.json() as typeof body; } catch { return json({ error: "invalid_json" }, env, 400); }
  if (!body.password || body.password !== env.ADMIN_TOKEN) return json({ error: "invalid_credentials" }, env, 401);

  const now = Math.floor(Date.now() / 1000);
  const token = await signJwt({ sub: "admin", iat: now, exp: now + 86400 }, env.JWT_SECRET || env.ADMIN_TOKEN);
  return json({ token, expiresIn: 86400 }, env);
}

// ─── Admin: Stats ───────────────────────────────────────────────────

async function handleAdminStats(env: Env): Promise<Response> {
  const [manifest, questions, subjects, events] = await Promise.all([
    env.DB.prepare("SELECT COUNT(*) as count FROM content_manifest").first<{ count: number }>(),
    env.DB.prepare("SELECT COUNT(*) as count FROM question").first<{ count: number }>(),
    env.DB.prepare("SELECT COUNT(*) as count FROM subject").first<{ count: number }>(),
    env.DB.prepare("SELECT COUNT(*) as count FROM analytics_event").first<{ count: number }>(),
  ]);
  const qByClass = await env.DB.prepare("SELECT class_level, COUNT(*) as count FROM question GROUP BY class_level ORDER BY class_level").all<{ class_level: number; count: number }>();
  const mByType = await env.DB.prepare("SELECT type, COUNT(*) as count FROM content_manifest GROUP BY type ORDER BY count DESC").all<{ type: string; count: number }>();

  return json({
    totalManifest: manifest?.count ?? 0,
    totalQuestions: questions?.count ?? 0,
    totalSubjects: subjects?.count ?? 0,
    totalEvents: events?.count ?? 0,
    questionsByClass: (qByClass.results ?? []).reduce<Record<number, number>>((acc, r) => { acc[r.class_level] = r.count; return acc; }, {}),
    manifestByType: (mByType.results ?? []).reduce<Record<string, number>>((acc, r) => { acc[r.type] = r.count; return acc; }, {}),
  }, env);
}

// ─── Admin: Manifest CRUD ───────────────────────────────────────────

async function handleAdminManifestList(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const limit = Math.min(parseInt(url.searchParams.get("limit") ?? "50"), 200);
  const offset = parseInt(url.searchParams.get("offset") ?? "0");
  const count = await env.DB.prepare("SELECT COUNT(*) as total FROM content_manifest").first<{ total: number }>();
  const result = await env.DB.prepare("SELECT * FROM content_manifest ORDER BY updated_at DESC LIMIT ? OFFSET ?").bind(limit, offset).all<RawManifestRow>();
  return json({ items: (result.results ?? []).map(rowToManifest), total: count?.total ?? 0, limit, offset }, env);
}

async function handleAdminUpsert(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { items?: ManifestRow[] };
  const items = body.items ?? [];
  if (!items.length) return json({ ok: true, upserted: 0 }, env);

  const stmts = items.map((it) =>
    env.DB.prepare(
      `INSERT INTO content_manifest (id, title, type, class_level, subject, year, file_url, size_bytes, checksum_sha256, version, updated_at, language, tags_csv)
       VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13)
       ON CONFLICT(id) DO UPDATE SET
         title=excluded.title, type=excluded.type, class_level=excluded.class_level,
         subject=excluded.subject, year=excluded.year, file_url=excluded.file_url,
         size_bytes=excluded.size_bytes, checksum_sha256=excluded.checksum_sha256,
         version=excluded.version, updated_at=excluded.updated_at,
         language=excluded.language, tags_csv=excluded.tags_csv;`
    ).bind(it.id, it.title, it.type, it.classLevel, it.subject, it.year ?? null,
      it.fileUrl ?? null, it.sizeBytes ?? 0, it.checksumSha256 ?? null,
      it.version ?? 1, it.updatedAt ?? Date.now(), it.language ?? "en",
      (it.tags ?? []).join(","))
  );
  await env.DB.batch(stmts);
  return json({ ok: true, upserted: items.length }, env);
}

async function handleAdminManifestUpdate(id: string, request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as Partial<ManifestRow>;
  const sets: string[] = [];
  const binds: (string | number | null)[] = [];
  if (body.title !== undefined) { sets.push("title = ?"); binds.push(body.title); }
  if (body.type !== undefined) { sets.push("type = ?"); binds.push(body.type); }
  if (body.classLevel !== undefined) { sets.push("class_level = ?"); binds.push(body.classLevel); }
  if (body.subject !== undefined) { sets.push("subject = ?"); binds.push(body.subject); }
  if (body.year !== undefined) { sets.push("year = ?"); binds.push(body.year); }
  if (body.fileUrl !== undefined) { sets.push("file_url = ?"); binds.push(body.fileUrl); }
  if (body.sizeBytes !== undefined) { sets.push("size_bytes = ?"); binds.push(body.sizeBytes); }
  if (body.checksumSha256 !== undefined) { sets.push("checksum_sha256 = ?"); binds.push(body.checksumSha256); }
  if (body.version !== undefined) { sets.push("version = ?"); binds.push(body.version); }
  if (body.language !== undefined) { sets.push("language = ?"); binds.push(body.language); }
  if (body.tags !== undefined) { sets.push("tags_csv = ?"); binds.push((body.tags ?? []).join(",")); }
  if (sets.length === 0) return json({ error: "no_fields" }, env, 400);
  sets.push("updated_at = ?"); binds.push(Date.now());
  binds.push(id);
  await env.DB.prepare(`UPDATE content_manifest SET ${sets.join(", ")} WHERE id = ?`).bind(...binds).run();
  return json({ ok: true }, env);
}

async function handleAdminManifestDelete(id: string, env: Env): Promise<Response> {
  await env.DB.prepare("DELETE FROM content_manifest WHERE id = ?").bind(id).run();
  return json({ ok: true }, env);
}

async function handleAdminManifestBulkDelete(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { ids: string[] };
  if (!body.ids?.length) return json({ error: "ids array required" }, env, 400);
  const stmts = body.ids.map(id => env.DB.prepare("DELETE FROM content_manifest WHERE id = ?").bind(id));
  await env.DB.batch(stmts);
  return json({ ok: true, deleted: body.ids.length }, env);
}

// ─── Admin: Questions CRUD ──────────────────────────────────────────

async function handleAdminQuestions(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const classLevel = url.searchParams.get("classLevel");
  const subject = url.searchParams.get("subject");
  const limit = Math.min(parseInt(url.searchParams.get("limit") ?? "50"), 200);
  const offset = parseInt(url.searchParams.get("offset") ?? "0");

  let countSql = "SELECT COUNT(*) as total FROM question";
  let sql = "SELECT * FROM question";
  const binds: (string | number)[] = [];
  const conditions: string[] = [];
  if (classLevel) { conditions.push("class_level = ?"); binds.push(Number(classLevel)); }
  if (subject) { conditions.push("subject = ?"); binds.push(subject); }
  if (conditions.length) {
    const where = " WHERE " + conditions.join(" AND ");
    countSql += where; sql += where;
  }
  sql += " ORDER BY created_at DESC LIMIT ? OFFSET ?";
  const count = await env.DB.prepare(countSql).bind(...binds).first<{ total: number }>();
  const result = await env.DB.prepare(sql).bind(...binds, limit, offset).all<RawQuestionRow>();
  return json({ items: (result.results ?? []).map(rowToQuestion), total: count?.total ?? 0, limit, offset }, env);
}

async function handleAdminQuestionCreate(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as QuestionRow;
  if (!body.id || !body.prompt) return json({ error: "id and prompt required" }, env, 400);
  await env.DB.prepare(
    "INSERT INTO question (id, class_level, subject, topic, prompt, options_csv, correct_index, explanation, source, language, difficulty) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11)"
  ).bind(body.id, body.classLevel, body.subject, body.topic ?? "general", body.prompt,
    (body.options ?? []).join("|"), body.correctIndex, body.explanation ?? null,
    body.source ?? "admin", body.language ?? "en", body.difficulty ?? "medium").run();
  return json({ ok: true, id: body.id }, env, 201);
}

async function handleAdminQuestionBulk(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { questions: QuestionRow[] };
  if (!body.questions?.length) return json({ error: "questions array required" }, env, 400);
  const stmts = body.questions.map((q) =>
    env.DB.prepare(
      "INSERT INTO question (id, class_level, subject, topic, prompt, options_csv, correct_index, explanation, source, language, difficulty) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11) ON CONFLICT(id) DO UPDATE SET prompt=excluded.prompt, options_csv=excluded.options_csv, correct_index=excluded.correct_index, explanation=excluded.explanation, difficulty=excluded.difficulty"
    ).bind(q.id, q.classLevel, q.subject, q.topic ?? "general", q.prompt,
      (q.options ?? []).join("|"), q.correctIndex, q.explanation ?? null,
      q.source ?? "admin", q.language ?? "en", q.difficulty ?? "medium")
  );
  await env.DB.batch(stmts);
  return json({ ok: true, upserted: body.questions.length }, env);
}

async function handleAdminQuestionUpdate(id: string, request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as Partial<QuestionRow>;
  const sets: string[] = [];
  const binds: (string | number | null)[] = [];
  if (body.prompt !== undefined) { sets.push("prompt = ?"); binds.push(body.prompt); }
  if (body.classLevel !== undefined) { sets.push("class_level = ?"); binds.push(body.classLevel); }
  if (body.subject !== undefined) { sets.push("subject = ?"); binds.push(body.subject); }
  if (body.topic !== undefined) { sets.push("topic = ?"); binds.push(body.topic); }
  if (body.options !== undefined) { sets.push("options_csv = ?"); binds.push(body.options.join("|")); }
  if (body.correctIndex !== undefined) { sets.push("correct_index = ?"); binds.push(body.correctIndex); }
  if (body.explanation !== undefined) { sets.push("explanation = ?"); binds.push(body.explanation); }
  if (body.difficulty !== undefined) { sets.push("difficulty = ?"); binds.push(body.difficulty); }
  if (sets.length === 0) return json({ error: "no_fields" }, env, 400);
  binds.push(id);
  await env.DB.prepare(`UPDATE question SET ${sets.join(", ")} WHERE id = ?`).bind(...binds).run();
  return json({ ok: true }, env);
}

async function handleAdminQuestionDelete(id: string, env: Env): Promise<Response> {
  await env.DB.prepare("DELETE FROM question WHERE id = ?").bind(id).run();
  return json({ ok: true }, env);
}

async function handleAdminQuestionBulkDelete(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { ids: string[] };
  if (!body.ids?.length) return json({ error: "ids array required" }, env, 400);
  const stmts = body.ids.map(id => env.DB.prepare("DELETE FROM question WHERE id = ?").bind(id));
  await env.DB.batch(stmts);
  return json({ ok: true, deleted: body.ids.length }, env);
}

// ─── Admin: Subjects ────────────────────────────────────────────────

async function handleAdminSubjects(env: Env): Promise<Response> {
  const result = await env.DB.prepare("SELECT * FROM subject ORDER BY class_level, sort_order").all<RawSubjectRow>();
  return json({ items: (result.results ?? []).map(rowToSubject) }, env);
}

async function handleAdminSubjectUpsert(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { items: SubjectRow[] };
  if (!body.items?.length) return json({ error: "items array required" }, env, 400);
  const stmts = body.items.map((s) =>
    env.DB.prepare(
      "INSERT INTO subject (id, display_name, class_level, icon, sort_order) VALUES (?1,?2,?3,?4,?5) ON CONFLICT(id) DO UPDATE SET display_name=excluded.display_name, class_level=excluded.class_level, icon=excluded.icon, sort_order=excluded.sort_order"
    ).bind(s.id, s.displayName, s.classLevel, s.icon ?? null, s.sortOrder ?? 0)
  );
  await env.DB.batch(stmts);
  return json({ ok: true, upserted: body.items.length }, env);
}

async function handleAdminSubjectDelete(id: string, env: Env): Promise<Response> {
  await env.DB.prepare("DELETE FROM subject WHERE id = ?").bind(id).run();
  return json({ ok: true }, env);
}

async function handleAdminSubjectBulkDelete(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { ids: string[] };
  if (!body.ids?.length) return json({ error: "ids array required" }, env, 400);
  const stmts = body.ids.map(id => env.DB.prepare("DELETE FROM subject WHERE id = ?").bind(id));
  await env.DB.batch(stmts);
  return json({ ok: true, deleted: body.ids.length }, env);
}

// ─── Admin: Analytics ───────────────────────────────────────────────

async function handleAdminAnalytics(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const days = parseInt(url.searchParams.get("days") ?? "30");
  const sinceMs = Date.now() - days * 86400000;

  const [totalEvents, byType, byClass, recent] = await Promise.all([
    env.DB.prepare("SELECT COUNT(*) as count FROM analytics_event WHERE created_at > ?1").bind(sinceMs).first<{ count: number }>(),
    env.DB.prepare("SELECT event_type, COUNT(*) as count FROM analytics_event WHERE created_at > ?1 GROUP BY event_type ORDER BY count DESC").bind(sinceMs).all<{ event_type: string; count: number }>(),
    env.DB.prepare("SELECT class_level, COUNT(*) as count FROM analytics_event WHERE created_at > ?1 AND class_level IS NOT NULL GROUP BY class_level ORDER BY class_level").bind(sinceMs).all<{ class_level: number; count: number }>(),
    env.DB.prepare("SELECT * FROM analytics_event ORDER BY created_at DESC LIMIT 100").all<RawAnalyticsRow>(),
  ]);

  return json({
    totalEvents: totalEvents?.count ?? 0,
    byType: (byType.results ?? []).reduce<Record<string, number>>((acc, r) => { acc[r.event_type] = r.count; return acc; }, {}),
    byClassLevel: (byClass.results ?? []).reduce<Record<number, number>>((acc, r) => { acc[r.class_level] = r.count; return acc; }, {}),
    recent: (recent.results ?? []).map(rowToAnalytics),
    period: `${days}d`,
  }, env);
}

// ─── Admin: Config ──────────────────────────────────────────────────

async function handleAdminConfig(env: Env): Promise<Response> {
  const result = await env.DB.prepare("SELECT key, value, updated_at FROM app_config").all<{ key: string; value: string; updated_at: number }>();
  return json({ items: result.results ?? [] }, env);
}

async function handleAdminConfigUpdate(key: string, request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { value: string };
  if (!body.value) return json({ error: "value required" }, env, 400);
  await env.DB.prepare("INSERT INTO app_config (key, value, updated_at) VALUES (?1, ?2, ?3) ON CONFLICT(key) DO UPDATE SET value=excluded.value, updated_at=excluded.updated_at").bind(key, body.value, Date.now()).run();
  return json({ ok: true }, env);
}

// ─── CORS ───────────────────────────────────────────────────────────

function preflight(env: Env): Response {
  const headers = new Headers();
  applyCors(headers, env);
  headers.set("access-control-allow-methods", "GET, POST, PUT, DELETE, OPTIONS");
  headers.set("access-control-allow-headers", "content-type, authorization, x-sikai-key");
  headers.set("access-control-max-age", "86400");
  return new Response(null, { status: 204, headers });
}

function applyCors(headers: Headers, env: Env): void {
  headers.set("access-control-allow-origin", env.ALLOWED_ORIGIN || "*");
  headers.set("vary", "Origin");
}

function json(body: unknown, env: Env, status = 200): Response {
  const headers = new Headers(JSON_HEADERS);
  applyCors(headers, env);
  return new Response(JSON.stringify(body), { status, headers });
}

// ─── Row mappers ────────────────────────────────────────────────────

interface RawManifestRow {
  id: string; title: string; type: string; class_level: number; subject: string;
  year: number | null; file_url: string | null; size_bytes: number;
  checksum_sha256: string | null; version: number; updated_at: number;
  language: string; tags_csv: string;
}

function rowToManifest(row: RawManifestRow): ManifestRow {
  return {
    id: row.id, title: row.title, type: row.type, classLevel: row.class_level,
    subject: row.subject, year: row.year, fileUrl: row.file_url,
    sizeBytes: row.size_bytes, checksumSha256: row.checksum_sha256,
    version: row.version, updatedAt: row.updated_at, language: row.language,
    tags: (row.tags_csv ?? "").split(",").map((s) => s.trim()).filter(Boolean),
  };
}

interface RawQuestionRow {
  id: string; class_level: number; subject: string; topic: string; prompt: string;
  options_csv: string; correct_index: number; explanation: string | null;
  source: string; language: string; difficulty: string; created_at: number;
}

function rowToQuestion(row: RawQuestionRow): QuestionRow {
  return {
    id: row.id, classLevel: row.class_level, subject: row.subject, topic: row.topic,
    prompt: row.prompt, options: row.options_csv.split("|").filter(Boolean),
    correctIndex: row.correct_index, explanation: row.explanation, source: row.source,
    language: row.language, difficulty: row.difficulty,
  };
}

interface RawSubjectRow {
  id: string; display_name: string; class_level: number; icon: string | null; sort_order: number;
}

function rowToSubject(row: RawSubjectRow): SubjectRow {
  return { id: row.id, displayName: row.display_name, classLevel: row.class_level, icon: row.icon, sortOrder: row.sort_order };
}

interface RawAnalyticsRow {
  id: number; device_id: string; event_type: string; class_level: number | null;
  subject: string | null; metadata: string | null; created_at: number;
}

function rowToAnalytics(row: RawAnalyticsRow): AnalyticsEvent & { id: number; createdAt: number } {
  return {
    deviceId: row.device_id, eventType: row.event_type, classLevel: row.class_level ?? undefined,
    subject: row.subject ?? undefined, metadata: row.metadata ?? undefined, id: row.id, createdAt: row.created_at,
  };
}