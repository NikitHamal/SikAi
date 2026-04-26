export interface Env {
  CONTENT_BUCKET: R2Bucket;
  DB: D1Database;
}

const cacheHeaders = {
  "Cache-Control": "public, max-age=300, s-maxage=3600",
  "Content-Type": "application/json; charset=utf-8"
};

const sampleManifest = [
  {
    id: "see-math-2080-sample",
    title: "SEE Mathematics Sample Paper 2080",
    type: "past_paper",
    classLevel: 10,
    subject: "Mathematics",
    year: 2080,
    fileKey: "past-papers/10/mathematics/2080-sample.pdf",
    sizeBytes: 0,
    checksumSha256: "replace-with-real-sha256",
    version: 1,
    updatedAt: "2026-04-26T00:00:00Z",
    language: "en",
    tags: ["SEE", "mathematics"]
  }
];

function json(data: unknown, init: ResponseInit = {}) {
  return new Response(JSON.stringify(data, null, 2), { ...init, headers: { ...cacheHeaders, ...(init.headers || {}) } });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);
    if (url.pathname === "/health") return json({ ok: true, service: "sikai-content", aiProxy: false });
    if (url.pathname === "/manifest") return json({ version: 1, updatedAt: new Date().toISOString(), items: sampleManifest });
    const classMatch = url.pathname.match(/^\/manifest\/(\d+)$/);
    if (classMatch) return json({ version: 1, items: sampleManifest.filter(item => item.classLevel === Number(classMatch[1])) });
    const fileMatch = url.pathname.match(/^\/files\/(.+)$/);
    if (fileMatch) {
      const key = decodeURIComponent(fileMatch[1]);
      const object = await env.CONTENT_BUCKET.get(key);
      if (!object) return json({ error: "file_not_found" }, { status: 404, headers: { "Cache-Control": "no-store" } });
      return new Response(object.body, { headers: { "Cache-Control": "public, max-age=86400", "ETag": object.httpEtag, "Content-Type": object.httpMetadata?.contentType || "application/octet-stream" } });
    }
    if (url.pathname === "/questions") {
      const classLevel = Number(url.searchParams.get("class") || "10");
      const subject = url.searchParams.get("subject") || "Mathematics";
      const topic = url.searchParams.get("topic") || "%";
      const limit = Math.min(Number(url.searchParams.get("limit") || "20"), 100);
      try {
        const result = await env.DB.prepare("SELECT id,classLevel,subject,topic,prompt,optionsCsv,answerIndex,explanation FROM questions WHERE classLevel = ? AND subject = ? AND topic LIKE ? LIMIT ?")
          .bind(classLevel, subject, topic, limit).all();
        return json({ items: result.results || [] });
      } catch {
        return json({ items: [] });
      }
    }
    return json({ error: "not_found" }, { status: 404, headers: { "Cache-Control": "no-store" } });
  }
};
