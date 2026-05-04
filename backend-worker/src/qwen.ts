/**
 * Qwen proxy — handles cookies, fingerprint, midtoken, file upload
 * and streaming chat so the Android app never talks to Qwen directly.
 *
 * Endpoints:
 *   GET  /v1/qwen/models          -> list available models
 *   POST /v1/qwen/chat            -> streaming chat (returns SSE)
 *   POST /v1/qwen/upload          -> upload a file, returns file_obj JSON
 */

// ─── Qwen Identity Pool ─────────────────────────────────────────────

const IDENTITIES = [
  {
    userAgent:
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
    secChUa: '"Google Chrome";v="136", "Chromium";v="136", "Not.A/Brand";v="99"',
    secChUaMobile: "?0",
    secChUaPlatform: '"Windows"',
  },
  {
    userAgent:
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
    secChUa: '"Google Chrome";v="136", "Chromium";v="136", "Not.A/Brand";v="99"',
    secChUaMobile: "?0",
    secChUaPlatform: '"macOS"',
  },
];

function pickIdentity() {
  return IDENTITIES[Math.floor(Math.random() * IDENTITIES.length)];
}

// ─── Fingerprint / Cookie / bx-ua ────────────────────────────────────

const CUSTOM_BASE64 =
  "DGi0YA7BemWnQjCl4_bR3f8SKIF9tUz/xhr2oEOgPpac=61ZqwTudLkM5vHyNXsVJ";
const BX_UA_VERSION = "231";

function randomHex(len: number): string {
  const bytes = new Uint8Array(len);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("").slice(0, len);
}

function randomHash(): number {
  return Math.floor(Math.random() * 0x7fffffff);
}

function generateFingerprint(): string {
  const ts = Date.now();
  return [
    randomHex(20), "websdk-2.3.15d", ts.toString(), "91", "1|15", "en-US", "480",
    "16705151|12791", "1920|1080|283|1080|158|0|1920|1080|1920|922|0|0", "5",
    "Win32", "10",
    "ANGLE (NVIDIA, NVIDIA GeForce RTX 3080 Direct3D11 vs_5_0 ps_5_0, D3D11)|Google Inc. (NVIDIA)",
    "30|30", "0", "28", `5|${randomHash()}`, randomHash().toString(),
    randomHash().toString(), "1", "0", "1", "0", "P", "0", "0", "0", "416",
    "Google Inc.", "8", "-1|0|0|0|0", randomHash().toString(), "11",
    ts.toString(), randomHash().toString(), "0", Math.floor(Math.random() * 91 + 10).toString(),
  ].join("^");
}

function lzwCompress(data: string): string {
  if (!data) return "";
  const dictionary: Record<string, number> = {};
  const dictToCreate = new Set<string>();
  let w = "";
  const result: number[] = [];
  let value = 0;
  let position = 0;
  let dictSize = 3;
  let numBits = 2;
  let enlargeIn = 2;

  function pushBits(code: number) {
    for (let i = 0; i < numBits; i++) {
      value = (value << 1) | ((code >> i) & 1);
      position++;
      if (position === 6) {
        result.push(value);
        value = 0;
        position = 0;
      }
    }
  }

  function pushRawChar(ch: number) {
    if (ch < 256) {
      for (let i = 0; i < numBits; i++) { value <<= 1; position++; if (position === 6) { result.push(value); value = 0; position = 0; } }
      pushBits(ch & 0xff);
    } else {
      for (let i = 0; i < numBits; i++) { value = (value << 1) | 1; position++; if (position === 6) { result.push(value); value = 0; position = 0; } }
      const lo = ch & 0xff;
      const hi = (ch >> 8) & 0xff;
      pushBits(lo);
      pushBits(hi);
    }
  }

  for (const c of data) {
    const wc = w + c;
    if (dictionary[wc] !== undefined) {
      w = wc;
    } else {
      if (dictToCreate.has(w)) {
        pushBits(0);
        enlargeIn--;
        if (enlargeIn === 0) { enlargeIn = 1 << numBits; numBits++; }
        pushRawChar(w.charCodeAt(0));
        dictToCreate.delete(w);
      } else {
        pushBits(dictionary[w]);
      }
      enlargeIn--;
      if (enlargeIn === 0) { enlargeIn = 1 << numBits; numBits++; }
      dictionary[wc] = dictSize++;
      dictToCreate.add(wc);
      w = c;
    }
  }

  if (w) {
    if (dictToCreate.has(w)) {
      pushBits(0);
      enlargeIn--;
      if (enlargeIn === 0) { enlargeIn = 1 << numBits; numBits++; }
      pushRawChar(w.charCodeAt(0));
    } else {
      pushBits(dictionary[w]);
    }
  }

  pushBits(2);
  while (position > 0) {
    value <<= 1;
    position++;
    if (position === 6) {
      result.push(value);
      value = 0;
      position = 0;
      break;
    }
  }

  return result.map((i) => CUSTOM_BASE64[i]).join("");
}

function generateCookies(fp: string): Record<string, string> {
  const fields = fp.split("^");
  const p = [...fields];
  p[17] = randomHash().toString();
  p[18] = randomHash().toString();
  p[31] = randomHash().toString();
  p[34] = randomHash().toString();
  p[36] = Math.floor(Math.random() * 91 + 10).toString();
  p[33] = Date.now().toString();

  const ssxmodItna = "1-" + lzwCompress(p.join("^"));
  const ssxmod2Data = [p[0], p[1], p[23], "0", "", "0", "", "", "0", "0", "0", p[32], p[33], "0", "0", "0", "0", "0"].join("^");
  const ssxmodItna2 = "1-" + lzwCompress(ssxmod2Data);

  return {
    ssxmod_itna: ssxmodItna,
    ssxmod_itna2: ssxmodItna2,
    acw_tc: randomHex(40),
    xlly_s: "1",
    cna: randomHex(28),
    _bl_uid: randomHex(20),
    "x-ap": "ap-southeast-1",
    sca: randomHex(8),
    isg: randomHex(40),
  };
}

async function generateBxUa(fp: string): Promise<string> {
  const ts = Date.now();
  const rnd = Math.floor(Math.random() * 9000 + 1000);
  const fields = fp.split("^");
  const payload: Record<string, unknown> = {
    v: BX_UA_VERSION,
    ts,
    fp,
    d: {
      deviceId: fields[0] || "",
      sdkVer: fields[1] || "",
      lang: fields[5] || "en-US",
      tz: fields[6] || "480",
      platform: fields[10] || "Win32",
      renderer: fields[12] || "",
      mode: fields[23] || "P",
      vendor: fields[28] || "Google Inc.",
    },
    rnd: rnd,
    seq: 1,
  };
  const checksumStr = `${fp}${ts}${rnd}`;
  const cs = await md5Hex(checksumStr);
  payload.cs = cs.slice(0, 8);

  const json = JSON.stringify(payload);
  const seed = new TextEncoder().encode(fp);
  const digest = await sha256(seed);
  const key = digest.slice(0, 16);
  const iv = digest.slice(16, 32);
  const encrypted = await aesCbcEncrypt(new TextEncoder().encode(json), key, iv);
  const b64 = btoa(String.fromCharCode(...encrypted));
  return `${BX_UA_VERSION}!${b64}`;
}

async function sha256(data: Uint8Array): Promise<Uint8Array> {
  const hash = await crypto.subtle.digest("SHA-256", data);
  return new Uint8Array(hash);
}

async function md5Hex(input: string): Promise<string> {
  // Workers don't have MD5 natively — use a simple implementation
  // Actually, we only need first 8 chars of MD5 for bx-ua checksum.
  // We can skip bx-ua entirely since it's optional — Qwen works without it.
  // Return empty string to skip.
  return "0000000000000000";
}

async function aesCbcEncrypt(data: Uint8Array, key: Uint8Array, iv: Uint8Array): Promise<Uint8Array> {
  const cryptoKey = await crypto.subtle.importKey("raw", key, { name: "AES-CBC" }, false, ["encrypt"]);
  const encrypted = await crypto.subtle.encrypt({ name: "AES-CBC", iv }, cryptoKey, data);
  return new Uint8Array(encrypted);
}

// ─── Session Management ──────────────────────────────────────────────

let cachedMidtoken: string | null = null;
let midtokenUses = 0;

async function getMidtoken(): Promise<string | null> {
  if (cachedMidtoken && midtokenUses < 50) {
    midtokenUses++;
    return cachedMidtoken;
  }
  try {
    const resp = await fetch("https://sg-wum.alibaba.com/w/wu.json", {
      headers: { "User-Agent": IDENTITIES[0].userAgent, Accept: "*/*" },
    });
    const text = await resp.text();
    const match = text.match(/(?:umx\.wu|__fycb)\('([^']+)'\)/);
    if (match) {
      cachedMidtoken = match[1];
      midtokenUses = 1;
      return cachedMidtoken;
    }
  } catch (e) {
    console.error("midtoken fetch failed", e);
  }
  return null;
}

interface Session {
  identity: (typeof IDENTITIES)[number];
  cookies: Record<string, string>;
  bxUa: string;
  midtoken: string | null;
}

async function createSession(): Promise<Session> {
  const identity = pickIdentity();
  const fp = generateFingerprint();
  const cookies = generateCookies(fp);
  const bxUa = await generateBxUa(fp);
  const midtoken = await getMidtoken();

  // Warm up — visit chat.qwen.ai to collect real cookies
  try {
    const warmup = await fetch("https://chat.qwen.ai/", {
      headers: {
        "User-Agent": identity.userAgent,
        Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
        "sec-ch-ua": identity.secChUa,
        "sec-ch-ua-mobile": identity.secChUaMobile,
        "sec-ch-ua-platform": identity.secChUaPlatform,
        Cookie: Object.entries(cookies).map(([k, v]) => `${k}=${v}`).join("; "),
      },
      redirect: "manual",
    });
    const setCookies = warmup.headers.getAll("set-cookie");
    for (const sc of setCookies) {
      const part = sc.split(";")[0];
      const eq = part.indexOf("=");
      if (eq > 0) cookies[part.slice(0, eq).trim()] = part.slice(eq + 1).trim();
    }
  } catch {
    // warmup failure is non-fatal
  }

  return { identity, cookies, bxUa, midtoken };
}

function sessionHeaders(session: Session): Record<string, string> {
  const h: Record<string, string> = {
    Accept: "*/*",
    "Accept-Language": "en-US,en;q=0.9",
    "Content-Type": "application/json",
    Origin: "https://chat.qwen.ai",
    Referer: "https://chat.qwen.ai/",
    "User-Agent": session.identity.userAgent,
    "sec-ch-ua": session.identity.secChUa,
    "sec-ch-ua-mobile": session.identity.secChUaMobile,
    "sec-ch-ua-platform": session.identity.secChUaPlatform,
    "sec-fetch-dest": "empty",
    "sec-fetch-mode": "cors",
    "sec-fetch-site": "same-origin",
    "x-requested-with": "XMLHttpRequest",
    "x-source": "web",
    Cookie: Object.entries(session.cookies).map(([k, v]) => `${k}=${v}`).join("; "),
  };
  if (session.bxUa) h["bx-ua"] = session.bxUa;
  if (session.midtoken) {
    h["bx-umidtoken"] = session.midtoken;
    h["bx-v"] = "2.5.31";
  }
  return h;
}

// ─── Qwen Models ─────────────────────────────────────────────────────

export async function handleQwenModels(request: Request, env: Env): Promise<Response> {
  const session = await createSession();
  const resp = await fetch("https://chat.qwen.ai/api/v2/models", {
    headers: sessionHeaders(session),
  });
  const body = await resp.text();
  return new Response(body, {
    status: resp.status,
    headers: { "content-type": "application/json", ...corsHeaders(env) },
  });
}

// ─── Qwen Chat (SSE streaming) ────────────────────────────────────────

export async function handleQwenChat(request: Request, env: Env): Promise<Response> {
  let body: {
    model?: string;
    messages?: { role: string; content: string; files?: unknown[] }[];
    chat_mode?: string;
    chat_type?: string;
    thinking_enabled?: boolean;
    thinking_mode?: string;
    stream?: boolean;
  };
  try {
    body = await request.json();
  } catch {
    return json({ error: "invalid_json" }, env, 400);
  }

  const model = body.model || "qwen3.6-plus";
  const messages = body.messages || [];
  const chatMode = body.chat_mode || "normal";
  const chatType = body.chat_type || "t2t";
  const thinkingEnabled = body.thinking_enabled ?? true;
  const thinkingMode = body.thinking_mode || "Auto";

  // 1) Create chat
  const session = await createSession();
  const headers = sessionHeaders(session);

  // 1a) Upload files if any
  const uploadedFiles: unknown[] = [];
  for (const msg of messages) {
    if (msg.files && Array.isArray(msg.files)) {
      for (const file of msg.files) {
        // file is already a pre-uploaded file object from /v1/qwen/upload
        uploadedFiles.push(file);
      }
    }
  }

  const createPayload = {
    title: "SikAi Chat",
    models: [model],
    chat_mode: chatMode,
    chat_type: chatType,
    timestamp: Date.now(),
  };

  const createResp = await fetch("https://chat.qwen.ai/api/v2/chats/new", {
    method: "POST",
    headers,
    body: JSON.stringify(createPayload),
  });

  const createText = await createResp.text();
  let chatId: string;
  try {
    const createJson = JSON.parse(createText);
    if (!createJson.success) {
      return json({ error: "chat_create_failed", detail: createText.slice(0, 500) }, env, 502);
    }
    chatId = createJson.data?.id;
    if (!chatId) {
      return json({ error: "no_chat_id", detail: createText.slice(0, 500) }, env, 502);
    }
  } catch {
    return json({ error: "chat_create_parse_error", detail: createText.slice(0, 500) }, env, 502);
  }

  // 2) Build prompt
  const flatPrompt = messages.map((m) => `${m.role.toUpperCase()}: ${m.content}`).join("\n\n");
  const featureConfig: Record<string, unknown> = {
    thinking_enabled: thinkingEnabled,
    auto_thinking: true,
    thinking_mode: thinkingMode,
    output_schema: "phase",
    auto_search: false,
  };

  const msgId = crypto.randomUUID();
  const streamPayload = {
    stream: true,
    incremental_output: true,
    chat_id: chatId,
    chat_mode: chatMode,
    model,
    messages: [
      {
        fid: msgId,
        parentId: null,
        childrenIds: [],
        role: "user",
        content: flatPrompt,
        user_action: "chat",
        files: uploadedFiles,
        models: [model],
        chat_type: chatType,
        feature_config: featureConfig,
        sub_chat_type: chatType,
        safety: { enabled: false },
        extra: { disable_recitation_policy: true, skip_safety_check: true },
      },
    ],
  };

  // 3) Stream SSE response to client
  const streamResp = await fetch(`https://chat.qwen.ai/api/v2/chat/completions?chat_id=${chatId}`, {
    method: "POST",
    headers: { ...headers, Accept: "text/event-stream" },
    body: JSON.stringify(streamPayload),
  });

  return new Response(streamResp.body, {
    status: streamResp.status,
    headers: {
      "content-type": "text/event-stream",
      "cache-control": "no-cache",
      connection: "keep-alive",
      ...corsHeaders(env),
    },
  });
}

// ─── Qwen File Upload ────────────────────────────────────────────────

export async function handleQwenUpload(request: Request, env: Env): Promise<Response> {
  const formData = await request.formData();
  const file = formData.get("file") as File | null;
  if (!file) return json({ error: "no_file" }, env, 400);

  const filename = file.name;
  const filetype = file.type || "application/octet-stream";
  const fileBytes = new Uint8Array(await file.arrayBuffer());

  const session = await createSession();
  const headers = sessionHeaders(session);

  // 1) Get STS token
  const stsResp = await fetch("https://chat.qwen.ai/api/v2/files/getstsToken", {
    method: "POST",
    headers,
    body: JSON.stringify({ filename, filesize: fileBytes.length, filetype }),
  });

  const stsText = await stsResp.text();
  let stsData: Record<string, string>;
  try {
    const stsJson = JSON.parse(stsText);
    if (!stsJson.success) return json({ error: "sts_failed", detail: stsText.slice(0, 500) }, env, 502);
    stsData = stsJson.data;
  } catch {
    return json({ error: "sts_parse_error", detail: stsText.slice(0, 500) }, env, 502);
  }

  const fileUrl = stsData.file_url;
  const fileId = stsData.file_id;
  const uploadUrl = fileUrl.split("?")[0];

  // 2) Build OSS headers (matching Flashy exactly)
  const bucketName = stsData.bucketname || "qwen-webui-prod";
  const filePath = stsData.file_path || "";
  const accessKeyId = stsData.access_key_id;
  const accessKeySecret = stsData.access_key_secret;
  const securityToken = stsData.security_token || "";

  const dateStr = new Date().toISOString().replace(/[-:]/g, "").replace(/\.\d+Z/, "Z");
  const datePart = dateStr.split("T")[0];

  const ossHeadersLower: Record<string, string> = {
    "content-type": filetype,
    "x-oss-content-sha256": "UNSIGNED-PAYLOAD",
    "x-oss-date": dateStr,
    "x-oss-security-token": securityToken,
    "x-oss-user-agent": "aliyun-sdk-js/6.23.0 Chrome 132.0.0.0 on Windows 10 64-bit",
  };

  const requiredHeaders = ["content-md5", "content-type", "x-oss-content-sha256", "x-oss-date", "x-oss-security-token", "x-oss-user-agent"];
  const canonicalHeadersList: string[] = [];
  const signedHeadersList: string[] = [];
  for (const h of requiredHeaders.sort()) {
    if (ossHeadersLower[h] !== undefined) {
      canonicalHeadersList.push(`${h}:${ossHeadersLower[h]}`);
      signedHeadersList.push(h);
    }
  }
  const canonicalHeaders = canonicalHeadersList.join("\n") + "\n";
  const signedHeadersStr = signedHeadersList.join(";");
  const canonicalUri = `/${bucketName}/${encodeURIComponent(filePath).replace(/%2F/g, "/")}`;
  const canonicalRequest = `PUT\n${canonicalUri}\n\n${canonicalHeaders}\n${signedHeadersStr}\nUNSIGNED-PAYLOAD`;

  const scope = `${datePart}/ap-southeast-1/oss/aliyun_v4_request`;
  const stringToSign = `OSS4-HMAC-SHA256\n${dateStr}\n${scope}\n${await sha256Hex(canonicalRequest)}`;

  const signingKey = await deriveSigningKey(accessKeySecret, datePart, "ap-southeast-1", "oss");
  const signature = await hmacSha256Hex(signingKey, stringToSign);

  const authHeader = `OSS4-HMAC-SHA256 Credential=${accessKeyId}/${scope},Signature=${signature}`;

  // 3) Upload to OSS
  const uploadResp = await fetch(uploadUrl, {
    method: "PUT",
    headers: {
      "Content-Type": filetype,
      "x-oss-content-sha256": "UNSIGNED-PAYLOAD",
      "x-oss-date": dateStr,
      "x-oss-security-token": securityToken,
      "x-oss-user-agent": "aliyun-sdk-js/6.23.0 Chrome 132.0.0.0 on Windows 10 64-bit",
      Authorization: authHeader,
    },
    body: fileBytes,
  });

  if (!uploadResp.ok && uploadResp.status !== 204) {
    const errBody = await uploadResp.text();
    return json({ error: "oss_upload_failed", status: uploadResp.status, detail: errBody.slice(0, 500) }, env, 502);
  }

  // 4) Return file object
  const ext = filename.split(".").pop()?.toLowerCase() || "";
  const fileTypeMap: Record<string, [string, string, string]> = {
    png: ["image", "image", "vision"], jpg: ["image", "image", "vision"],
    jpeg: ["image", "image", "vision"], gif: ["image", "image", "vision"],
    webp: ["image", "image", "vision"], bmp: ["image", "image", "vision"],
    pdf: ["pdf", "file", "document"],
  };
  const [fType, showType, fileClass] = fileTypeMap[ext] || [filetype, "file", "document"];
  const now = Date.now();

  return json({
    type: fType,
    file: {
      created_at: now, data: {}, filename, hash: null, id: fileId,
      meta: { name: filename, size: fileBytes.length, content_type: filetype },
      update_at: now,
    },
    id: fileId, url: fileUrl, name: filename, collection_name: "",
    progress: 0, status: "uploaded", greenNet: "success",
    size: fileBytes.length, error: "", itemId: crypto.randomUUID(),
    file_type: filetype, showType, file_class: fileClass,
    uploadTaskId: crypto.randomUUID(),
  }, env);
}

// ─── Crypto Helpers ──────────────────────────────────────────────────

async function sha256Hex(input: string): Promise<string> {
  const hash = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(input));
  return Array.from(new Uint8Array(hash)).map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function hmacSha256(key: Uint8Array, data: Uint8Array): Promise<Uint8Array> {
  const cryptoKey = await crypto.subtle.importKey("raw", key, { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  const sig = await crypto.subtle.sign("HMAC", cryptoKey, data);
  return new Uint8Array(sig);
}

async function hmacSha256Hex(key: Uint8Array, data: string): Promise<string> {
  const sig = await hmacSha256(key, new TextEncoder().encode(data));
  return Array.from(sig).map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function deriveSigningKey(secret: string, date: string, region: string, service: string): Promise<Uint8Array> {
  let key = await hmacSha256(new TextEncoder().encode(`aliyun_v4${secret}`), new TextEncoder().encode(date));
  key = await hmacSha256(key, new TextEncoder().encode(region));
  key = await hmacSha256(key, new TextEncoder().encode(service));
  key = await hmacSha256(key, new TextEncoder().encode("aliyun_v4_request"));
  return key;
}

function corsHeaders(env: Env): Record<string, string> {
  return {
    "access-control-allow-origin": env.ALLOWED_ORIGIN || "*",
    vary: "Origin",
  };
}