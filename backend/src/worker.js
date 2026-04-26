/**
 * SikAi backend — Cloudflare Worker
 *
 * Endpoints:
 *   GET  /health                 — liveness probe
 *   GET  /manifest               — full content manifest (JSON)
 *   GET  /manifest/:classLevel   — manifest filtered by class
 *   GET  /files/:fileKey         — proxy to R2 with conditional caching
 *   GET  /questions              — question pack (?class=&subject=&limit=)
 *
 * Bindings (configure in wrangler.toml):
 *   DB        — D1 database (manifest + questions)
 *   BUCKET    — R2 bucket    (downloadable assets)
 */

const JSON_HEADERS = { "content-type": "application/json; charset=utf-8" };
const CORS_HEADERS = {
  "access-control-allow-origin": "*",
  "access-control-allow-methods": "GET, OPTIONS",
  "access-control-allow-headers": "content-type",
};

function json(body, init = {}) {
  return new Response(JSON.stringify(body), {
    ...init,
    headers: { ...JSON_HEADERS, ...CORS_HEADERS, ...(init.headers || {}) },
  });
}

function notFound(msg = "Not found") {
  return json({ error: msg }, { status: 404 });
}

async function handleHealth() {
  return json({ ok: true, service: "sikai-backend", at: Date.now() });
}

async function listManifest(env, classLevel) {
  // D1 schema:
  //   CREATE TABLE manifest (
  //     id TEXT PRIMARY KEY, title TEXT, type TEXT, classLevel INTEGER,
  //     subject TEXT, year INTEGER, fileKey TEXT, sizeBytes INTEGER,
  //     checksumSha256 TEXT, version INTEGER, updatedAt INTEGER,
  //     language TEXT, tagsCsv TEXT, description TEXT
  //   );
  const stmt = classLevel == null
    ? env.DB.prepare("SELECT * FROM manifest ORDER BY classLevel, subject, type")
    : env.DB.prepare("SELECT * FROM manifest WHERE classLevel = ? ORDER BY subject, type").bind(Number(classLevel));
  const { results } = await stmt.all();
  const baseUrl = (env.PUBLIC_BASE_URL || "").replace(/\/$/, "");
  const items = (results || []).map((r) => ({
    ...r,
    fileUrl: r.fileKey ? `${baseUrl}/files/${r.fileKey}` : null,
  }));
  return json({ version: 1, count: items.length, items });
}

async function listQuestions(env, params) {
  const classLevel = Number(params.get("class") || 10);
  const subject = params.get("subject") || "";
  const limit = Math.min(Number(params.get("limit") || 50), 200);
  if (!subject) return json({ error: "subject required" }, { status: 400 });
  const { results } = await env.DB
    .prepare("SELECT * FROM questions WHERE classLevel = ? AND subject = ? ORDER BY RANDOM() LIMIT ?")
    .bind(classLevel, subject, limit)
    .all();
  return json({ classLevel, subject, items: results || [] });
}

async function serveFile(env, fileKey, request) {
  if (!fileKey) return notFound();
  const obj = await env.BUCKET.get(fileKey, {
    onlyIf: request.headers.get("if-none-match") ? { etagMatches: request.headers.get("if-none-match") } : undefined,
  });
  if (obj === null) return notFound("file not in bucket");
  const headers = new Headers(CORS_HEADERS);
  obj.writeHttpMetadata(headers);
  headers.set("etag", obj.httpEtag);
  headers.set("cache-control", "public, max-age=86400, immutable");
  return new Response(obj.body, { headers });
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }
    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, "") || "/";
    const segments = path.split("/").filter(Boolean);

    try {
      if (path === "/health") return handleHealth();

      if (segments[0] === "manifest") {
        const classLevel = segments[1];
        return await listManifest(env, classLevel);
      }

      if (segments[0] === "questions") {
        return await listQuestions(env, url.searchParams);
      }

      if (segments[0] === "files" && segments[1]) {
        const key = decodeURIComponent(segments.slice(1).join("/"));
        return await serveFile(env, key, request);
      }

      return notFound();
    } catch (err) {
      return json({ error: "server_error", message: String(err?.message || err) }, { status: 500 });
    }
  },
};
