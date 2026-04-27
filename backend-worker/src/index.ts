/**
 * SikAi content worker.
 *
 * Endpoints (all GET unless noted):
 *   GET  /health                                -> liveness
 *   GET  /v1/manifest                           -> full manifest
 *   GET  /v1/manifest?classLevel=10             -> filtered manifest
 *   GET  /v1/files/:fileKey                     -> stream R2 object
 *   POST /v1/admin/manifest        (admin only) -> upsert manifest rows
 *   POST /v1/admin/files/:fileKey  (admin only) -> upload file to R2
 *
 * The Android app only needs the read endpoints; admin endpoints are guarded
 * by the optional `ADMIN_TOKEN` variable so the worker can be deployed
 * read-only and content can be seeded out-of-band.
 */

export interface Env {
  DB: D1Database;
  FILES: R2Bucket;
  ADMIN_TOKEN: string;
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
  fileKey: string | null;
  sizeBytes: number;
  checksumSha256: string | null;
  version: number;
  updatedAt: number;
  language: string;
  tags: string[];
}

const JSON_HEADERS = { "content-type": "application/json; charset=utf-8" };

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname;

    if (request.method === "OPTIONS") {
      return preflight(env);
    }

    try {
      if (path === "/health") {
        return json({ ok: true, service: "sikai-content", time: Date.now() }, env);
      }

      if (path === "/v1/manifest" && request.method === "GET") {
        return await handleManifest(request, env);
      }

      if (path.startsWith("/v1/files/") && request.method === "GET") {
        const key = decodeURIComponent(path.slice("/v1/files/".length));
        return await handleFileGet(key, env);
      }

      if (path === "/v1/admin/manifest" && request.method === "POST") {
        if (!authorizedAdmin(request, env)) return unauthorized(env);
        return await handleAdminUpsert(request, env);
      }

      if (path.startsWith("/v1/admin/files/") && request.method === "POST") {
        if (!authorizedAdmin(request, env)) return unauthorized(env);
        const key = decodeURIComponent(path.slice("/v1/admin/files/".length));
        return await handleAdminUpload(key, request, env);
      }

      return json({ error: "not_found", path }, env, 404);
    } catch (err) {
      console.error("worker error", err);
      return json({ error: "internal", message: (err as Error).message }, env, 500);
    }
  },
} satisfies ExportedHandler<Env>;

async function handleManifest(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);
  const classLevel = url.searchParams.get("classLevel");
  const sql = classLevel
    ? "SELECT * FROM content_manifest WHERE class_level = ?1 ORDER BY updated_at DESC"
    : "SELECT * FROM content_manifest ORDER BY updated_at DESC";

  const stmt = classLevel
    ? env.DB.prepare(sql).bind(Number(classLevel))
    : env.DB.prepare(sql);
  const result = await stmt.all<RawRow>();
  const items = (result.results ?? []).map(rowToManifest);
  return json({ items, generatedAt: Date.now() }, env);
}

async function handleFileGet(key: string, env: Env): Promise<Response> {
  if (!key) return json({ error: "missing_key" }, env, 400);
  const obj = await env.FILES.get(key);
  if (obj === null) return json({ error: "not_found", key }, env, 404);

  const headers = new Headers();
  obj.writeHttpMetadata(headers);
  headers.set("etag", obj.httpEtag);
  headers.set("cache-control", "public, max-age=31536000, immutable");
  applyCors(headers, env);
  return new Response(obj.body, { headers });
}

async function handleAdminUpsert(request: Request, env: Env): Promise<Response> {
  const body = (await request.json()) as { items?: ManifestRow[] };
  const items = body.items ?? [];
  if (!items.length) return json({ ok: true, upserted: 0 }, env);

  const stmts = items.map((it) => {
    return env.DB
      .prepare(
        `INSERT INTO content_manifest (
            id, title, type, class_level, subject, year,
            file_url, file_key, size_bytes, checksum_sha256,
            version, updated_at, language, tags_csv
         ) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14)
         ON CONFLICT(id) DO UPDATE SET
            title=excluded.title,
            type=excluded.type,
            class_level=excluded.class_level,
            subject=excluded.subject,
            year=excluded.year,
            file_url=excluded.file_url,
            file_key=excluded.file_key,
            size_bytes=excluded.size_bytes,
            checksum_sha256=excluded.checksum_sha256,
            version=excluded.version,
            updated_at=excluded.updated_at,
            language=excluded.language,
            tags_csv=excluded.tags_csv;`
      )
      .bind(
        it.id,
        it.title,
        it.type,
        it.classLevel,
        it.subject,
        it.year ?? null,
        it.fileUrl ?? null,
        it.fileKey ?? null,
        it.sizeBytes ?? 0,
        it.checksumSha256 ?? null,
        it.version ?? 1,
        it.updatedAt ?? Date.now(),
        it.language ?? "en",
        (it.tags ?? []).join(",")
      );
  });

  await env.DB.batch(stmts);
  return json({ ok: true, upserted: items.length }, env);
}

async function handleAdminUpload(key: string, request: Request, env: Env): Promise<Response> {
  if (!key) return json({ error: "missing_key" }, env, 400);
  const ct = request.headers.get("content-type") ?? "application/octet-stream";
  await env.FILES.put(key, request.body, { httpMetadata: { contentType: ct } });
  const head = await env.FILES.head(key);
  return json({ ok: true, key, size: head?.size ?? 0, etag: head?.httpEtag }, env);
}

function authorizedAdmin(request: Request, env: Env): boolean {
  const expected = env.ADMIN_TOKEN;
  if (!expected) return false; // admin disabled when no token configured
  return request.headers.get("x-sikai-key") === expected;
}

function unauthorized(env: Env): Response {
  return json({ error: "unauthorized" }, env, 401);
}

function preflight(env: Env): Response {
  const headers = new Headers();
  applyCors(headers, env);
  headers.set("access-control-allow-methods", "GET, POST, OPTIONS");
  headers.set("access-control-allow-headers", "content-type, x-sikai-key");
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

interface RawRow {
  id: string;
  title: string;
  type: string;
  class_level: number;
  subject: string;
  year: number | null;
  file_url: string | null;
  file_key: string | null;
  size_bytes: number;
  checksum_sha256: string | null;
  version: number;
  updated_at: number;
  language: string;
  tags_csv: string;
}

function rowToManifest(row: RawRow): ManifestRow {
  return {
    id: row.id,
    title: row.title,
    type: row.type,
    classLevel: row.class_level,
    subject: row.subject,
    year: row.year,
    fileUrl: row.file_url,
    fileKey: row.file_key,
    sizeBytes: row.size_bytes,
    checksumSha256: row.checksum_sha256,
    version: row.version,
    updatedAt: row.updated_at,
    language: row.language,
    tags: (row.tags_csv ?? "").split(",").map((s) => s.trim()).filter(Boolean),
  };
}
