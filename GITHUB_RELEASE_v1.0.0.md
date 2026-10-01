# Flixio v1.0.0 — Initial Official Release

Welcome to the official **v1.0.0** release of **Flixio**! 🎉

**Flixio** is a modern, open-source media player and catalog aggregator built with Kotlin Multiplatform and Compose Multiplatform for Android and iOS. It brings together content discovery, seamless cloud synchronization, an extensible addon ecosystem, and a powerful dual-engine video player into a unified, glassmorphic interface.

---

## 🌟 What's New in v1.0.0

### ☁️ Supabase Cloud Synchronization
- Effortless cross-device sync for your watch progress, continue watching history, and personal library collections.
- Secure, token-based cloud sync with offline caching so your history is always available.

### 🎬 Visual & UI Polish
- **Glassmorphic Floating Navigation**: Pure Gaussian blur bottom navigation bar without tinting or muddy backgrounds. Selected tabs now feature crisp high-contrast text and glowing active indicators.
- **Jitter-Free Hero Carousel**: Stabilized tilted hero backdrop container with smooth crossfading between titles.
- **Direct Downloads Shortcut**: Clean downloads access button directly in the Library header (matching Nuvio Beta).
- **Personalized Home Experience**: Dynamic time-based greetings (*"Good morning"*, *"Good afternoon"*, *"Good evening"*) with Clash Display typography.
- **745+ Categorized Profile Avatars**: Multi-profile support with personalized avatars bundled locally into the app.

### ⚡ Dual-Engine Media Playback
- **Media3 (ExoPlayer) & MPV (`libmpv`)**: Complete playback compatibility across modern codecs, HDR, and spatial audio formats.
- **Intuitive Player Gestures**:
  - Left swipe: Volume adjustment with boost up to **150%** (with warning guide above 100%).
  - Right swipe: Brightness control.
  - Horizontal swipe: Precision scrubbing.
- **Custom Font Importer & Styled Subtitles**: Full support for ASS/SSA styling, SRT, VTT, and the ability to import custom `.ttf`/`.otf` font files directly from device storage.

### 🔌 Addons & Streaming Sources
- **Stremio Protocol Addons**: Full compatibility with Stremio v3 manifests (Catalogs, Streams, Subtitles).
- **CloudStream Addon Runtime**: Streamlined support for resolving streaming sources via CloudStream plugins during playback.
- **Debrid Resolvers**: Native integration with Real-Debrid, Premiumize, Torbox, AllDebrid, and Debrid-Link.
- **MDBList Metadata & Ratings**: Integrated MDBList API client for custom user lists, watchlists, and community ratings.
- **Telegram Stream Integration**: Groundwork and TDLib client engine integrated with QR code and phone login verification flows (marked as *Coming Soon* in Integrations).

### 🛠️ Bug Fixes & Stability
- Fixed tracking settings crash caused by duplicate localization string resource definitions.
- Resolved race conditions in background authorization polling.
- Hardened profile switching and zero-profile first-time setup flows.

---

## 📦 Download Packages

| File | Description | Recommended For |
|---|---|---|
| **`androidApp-full-universal-release.apk`** | Universal Android build (all architectures bundled) | **Recommended for all users** |
| **`androidApp-full-arm64-v8a-release.apk`** | 64-bit ARM build (optimized for file size) | Modern Android phones & tablets |
| **`androidApp-full-armeabi-v7a-release.apk`** | 32-bit ARM build | Older Android devices |
| **`androidApp-full-x86_64-release.apk`** | 64-bit x86 build | Android emulators & Chromebooks |
| **`androidApp-full-x86-release.apk`** | 32-bit x86 build | Older x86 devices & emulators |
| **`Flixio-v1.0.0.ipa`** | iOS package (built via GitHub Actions) | Sideloading on iOS (AltStore / TrollStore) |

---

## 📲 How to Install

1. Download the recommended **`androidApp-full-universal-release.apk`** (or your architecture's APK) below.
2. If prompted on Android, allow installing apps from unknown sources.
3. Open Flixio, create or sync your profile, configure your addons, and enjoy streaming!
