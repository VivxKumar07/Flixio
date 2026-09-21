import http from "node:http";

const PORT = Number(process.env.PORT || 10000);
const SUPABASE_URL = (process.env.SUPABASE_URL || "").replace(/\/$/, "");
const SUPABASE_ANON_KEY = process.env.SUPABASE_ANON_KEY || "";
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || "";
const SUPABASE_PUBLISHABLE_KEY = process.env.SUPABASE_PUBLISHABLE_KEY || SUPABASE_ANON_KEY;
const PUBLIC_URL = (process.env.BACKEND_PUBLIC_URL || process.env.RENDER_EXTERNAL_URL || "").replace(/\/$/, "");

if (!SUPABASE_URL || !SUPABASE_ANON_KEY || !SUPABASE_SERVICE_ROLE_KEY) {
  console.error("Missing SUPABASE_URL, SUPABASE_ANON_KEY or SUPABASE_SERVICE_ROLE_KEY");
  process.exit(1);
}

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info, x-supabase-api-version",
  "Access-Control-Allow-Methods": "GET,POST,PATCH,PUT,DELETE,OPTIONS"
};

function json(res, status, body) {
  res.writeHead(status, { ...cors, "Content-Type": "application/json" });
  res.end(JSON.stringify(body));
}

async function readBody(req) {
  const chunks = [];
  for await (const chunk of req) chunks.push(chunk);
  return Buffer.concat(chunks);
}

async function supabase(path, options = {}) {
  const headers = new Headers(options.headers || {});
  headers.set("apikey", headers.get("apikey") || SUPABASE_ANON_KEY);
  if (!headers.has("Content-Type") && options.body && !(options.body instanceof Uint8Array)) {
    headers.set("Content-Type", "application/json");
  }
  return fetch(SUPABASE_URL + path, { ...options, headers });
}

function serviceHeaders(extra = {}) {
  return {
    apikey: SUPABASE_SERVICE_ROLE_KEY,
    Authorization: `Bearer ${SUPABASE_SERVICE_ROLE_KEY}`,
    ...extra
  };
}

async function rpc(name, args) {
  return supabase(`/rest/v1/rpc/${name}`, {
    method: "POST",
    headers: serviceHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(args || {})
  });
}

async function handleHealth(res) {
  const start = Date.now();
  try {
    const r = await rpc("health_ping", {});
    const text = await r.text();
    const latency = Date.now() - start;
    json(res, r.ok ? 200 : 503, {
      status: r.ok ? (latency < 3000 ? "healthy" : "slow") : "degraded",
      database: r.ok ? "connected" : "error",
      latency_ms: latency,
      timestamp: new Date().toISOString(),
      ...(r.ok ? {} : { error: text })
    });
  } catch (e) {
    json(res, 503, { status: "down", database: "unreachable", error: e.message });
  }
}

async function handleDeleteAccount(req, res) {
  const auth = req.headers.get("authorization");
  if (!auth?.startsWith("Bearer ")) return json(res, 401, { error: "Missing Authorization header" });

  const userRes = await supabase("/auth/v1/user", {
    headers: { apikey: SUPABASE_ANON_KEY, Authorization: auth }
  });
  const user = await userRes.json().catch(() => null);
  if (!userRes.ok || !user?.id) return json(res, 401, { error: "Invalid or expired session" });

  const del = await fetch(`${SUPABASE_URL}/auth/v1/admin/users/${encodeURIComponent(user.id)}`, {
    method: "DELETE",
    headers: serviceHeaders()
  });
  const body = await del.text();
  if (!del.ok) return json(res, 500, { error: body });
  json(res, 200, { success: true });
}

async function handleTvExchange(req, res) {
  const body = JSON.parse((await readBody(req)).toString("utf8") || "{}");
  if (!body.code || !body.device_nonce) {
    return json(res, 400, { error: "code and device_nonce are required" });
  }

  const consumedRes = await rpc("consume_tv_login_session", {
    p_code: body.code,
    p_device_nonce: body.device_nonce
  });
  const consumedText = await consumedRes.text();
  if (!consumedRes.ok) return json(res, 400, { error: consumedText });

  let consumed;
  try { consumed = JSON.parse(consumedText); } catch { return json(res, 500, { error: "Invalid RPC response" }); }
  const row = Array.isArray(consumed) ? consumed[0] : consumed;
  const userId = row?.approved_user_id;
  if (!userId) return json(res, 400, { error: "No approved user found" });

  const userRes = await fetch(`${SUPABASE_URL}/auth/v1/admin/users/${encodeURIComponent(userId)}`, {
    headers: serviceHeaders()
  });
  const userBody = await userRes.json().catch(() => null);
  const email = userBody?.user?.email;
  if (!userRes.ok || !email) return json(res, 400, { error: "Approved user email not found" });

  const linkRes = await fetch(`${SUPABASE_URL}/auth/v1/admin/generate_link`, {
    method: "POST",
    headers: serviceHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({
      type: "magiclink",
      email,
      options: { redirect_to: "https://example.com/ignore" }
    })
  });
  const link = await linkRes.json().catch(() => null);
  if (!linkRes.ok) return json(res, 500, { error: link?.msg || link?.message || "Failed to create token" });

  const tokenHash =
    link?.properties?.hashed_token ||
    (link?.properties?.action_link ? new URL(link.properties.action_link).searchParams.get("token_hash") : null);

  if (!tokenHash) return json(res, 500, { error: "Failed to create token hash" });

  const verifyRes = await fetch(`${SUPABASE_URL}/auth/v1/verify`, {
    method: "POST",
    headers: { ...serviceHeaders(), "Content-Type": "application/json" },
    body: JSON.stringify({ type: "magiclink", token_hash: tokenHash })
  });
  const verify = await verifyRes.json().catch(() => null);
  if (!verifyRes.ok || !verify?.access_token) {
    return json(res, 500, { error: verify?.msg || verify?.message || "Session exchange failed" });
  }

  json(res, 200, {
    access_token: verify.access_token,
    refresh_token: verify.refresh_token,
    token_type: verify.token_type,
    expires_in: verify.expires_in
  });
}

async function handleTrackerExchange(req, res) {
  const auth = req.headers.get("authorization");
  if (!auth?.startsWith("Bearer ")) return json(res, 401, { error: "unauthorized" });

  const body = JSON.parse((await readBody(req)).toString("utf8") || "{}");
  const { code, tracker, access_token, refresh_token = null, expires_in = null, tracker_user_id = null, username = null } = body;

  if (!code || !tracker || !access_token) return json(res, 400, { error: "missing_required_field" });
  if (!["mal", "anilist", "kitsu"].includes(tracker)) return json(res, 400, { error: "bad_tracker" });

  const userRes = await supabase("/auth/v1/user", {
    headers: { apikey: SUPABASE_ANON_KEY, Authorization: auth }
  });
  const user = await userRes.json().catch(() => null);
  if (!userRes.ok || !user?.id) return json(res, 401, { error: "unauthorized" });

  const query = `/rest/v1/tracker_tv_login_sessions?select=*&code=eq.${encodeURIComponent(code)}&tracker=eq.${encodeURIComponent(tracker)}&limit=1`;
  const sessRes = await supabase(query, { headers: serviceHeaders() });
  const sessions = await sessRes.json().catch(() => null);
  const sess = Array.isArray(sessions) ? sessions[0] : null;

  if (!sessRes.ok) return json(res, 500, { error: "lookup_failed" });
  if (!sess) return json(res, 404, { error: "session_not_found" });
  if (new Date(sess.expires_at) < new Date()) return json(res, 410, { error: "session_expired" });
  if (sess.owner_user_id !== user.id) return json(res, 403, { error: "owner_mismatch" });

  const updateRes = await supabase(`/rest/v1/tracker_tv_login_sessions?code=eq.${encodeURIComponent(code)}`, {
    method: "PATCH",
    headers: serviceHeaders({ "Content-Type": "application/json", Prefer: "return=minimal" }),
    body: JSON.stringify({
      status: "ready",
      access_token,
      refresh_token,
      expires_in,
      tracker_user_id,
      tracker_username: username
    })
  });

  if (!updateRes.ok) return json(res, 500, { error: "update_failed", detail: await updateRes.text() });
  json(res, 200, { ok: true });
}

async function proxy(req, res, url) {
  const upstreamPath = url.pathname + url.search;
  const headers = new Headers();
  for (const [key, value] of req.headers) {
    if (!["host", "content-length", "connection"].includes(key.toLowerCase())) headers.set(key, value);
  }

  const body = ["GET", "HEAD"].includes(req.method) ? undefined : await readBody(req);
  const upstream = await fetch(SUPABASE_URL + upstreamPath, {
    method: req.method,
    headers,
    body
  });

  res.writeHead(upstream.status, {
    ...cors,
    "Content-Type": upstream.headers.get("content-type") || "application/json"
  });
  res.end(Buffer.from(await upstream.arrayBuffer()));
}

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url, `http://${req.headers.host || "localhost"}`);

    if (req.method === "OPTIONS") return json(res, 204, null);

    if (url.pathname === "/" || url.pathname === "/health") {
      return json(res, 200, { service: "flixio-backend", status: "ok" });
    }

    if (url.pathname === "/.well-known/nuvio") {
      return json(res, 200, {
        version: 1,
        service: "nuvio",
        self_hosted: true,
        backend_url: PUBLIC_URL,
        publishable_key: SUPABASE_PUBLISHABLE_KEY,
        capabilities: {
          email_password_auth: true,
          tv_login: true
        }
      });
    }

    if (url.pathname === "/functions/v1/health-check") return handleHealth(res);
    if (url.pathname === "/functions/v1/delete-account") return handleDeleteAccount(req, res);
    if (url.pathname === "/functions/v1/tv-logins-exchange") return handleTvExchange(req, res);
    if (url.pathname === "/functions/v1/tracker-tv-logins-exchange") return handleTrackerExchange(req, res);

    if (/^\/(auth|rest|storage|graphql|realtime)\//.test(url.pathname)) {
      return proxy(req, res, url);
    }

    return json(res, 404, { error: "not_found" });
  } catch (e) {
    console.error(e);
    json(res, 500, { error: e.message || "internal_server_error" });
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Flixio backend listening on 0.0.0.0:${PORT}`);
});
