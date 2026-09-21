# Flixio Render Backend

Render-compatible Nuvio backend gateway.

It proxies Supabase Auth/REST/Storage APIs to Supabase Cloud and implements the Nuvio-specific discovery and Edge Function routes needed by the client.

## Required environment variables

- SUPABASE_URL
- SUPABASE_ANON_KEY
- SUPABASE_SERVICE_ROLE_KEY
- SUPABASE_PUBLISHABLE_KEY (optional; falls back to anon key)
- BACKEND_PUBLIC_URL (optional; Render can use RENDER_EXTERNAL_URL automatically)

Never put service-role keys in Git.
