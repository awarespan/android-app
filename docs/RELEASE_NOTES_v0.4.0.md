# AWARESPAN Android Application — v0.4.0 Release Notes

**Release Tag**: `v0.4.0`  
**Version Name**: `0.4.0`  
**Version Code**: `5`  
**Package / Namespace**: `com.awarespan.guardian`  
**Brand**: AWARESPAN  
**Tagline**: *Awareness Without Boundaries.*  
**Date**: November 12, 2026  

---

## 1. Executive Summary

Version `0.4.0` is a major milestone release delivering the complete rebranding from the legacy SIP Guardian prototype to **AWARESPAN**, a modernized mobile operational interface built for the edge AI safety ecosystem. This release introduces a comprehensive Jetpack Compose redesign following the authoritative zero-radius dark infrastructure design system, verifies production backend connectivity, enhances Android Keystore encryption, resolves R8 obfuscation issues, and prepares the repository for continuous GitHub Release automation.

---

## 2. Key Highlights & Changes

### 2.1 Complete Rebranding to AWARESPAN
- Renamed all application packages and identifiers from `com.sip.guardian` to `com.awarespan.guardian`.
- Updated application label, launcher icon, vector assets, and notification services to **AWARESPAN**.
- Synchronized brand messaging across all screens: *"Awareness Without Boundaries"*.
- Deprecated legacy backend endpoints in favor of the active production gateway.

### 2.2 Design System Overhaul (Zero-Radius Dark Infrastructure)
Strictly implemented according to `docs/DESIGN.md`:
- **Void Black Canvas (`#000000`)**: Deep pure black background across all screens for OLED power efficiency and dark environment operation.
- **Ember Orange (`#FF4F2B`)**: Intentional action indicator for critical CTAs, active threat warnings, and focused inputs.
- **Zero-Radius Architecture (`0dp`)**: Complete elimination of rounded cards, pill buttons, and Material 3 Expressive shapes in favor of sharp, architectural geometry.
- **Hairline Graphite Dividers (`#3C3C3C`, 1dp)**: Structural spatial separation without drop shadows.
- **Monospaced Operational Readouts**: Terminal typography for telemetry timestamps, sensor IDs, and confidence scores.
- **Custom Components**: `AwarespanButton`, `AwarespanCard`, `AwarespanBadge`, `AwarespanTextField`, `ConfidenceMeter`, `ThreatBadge`, and uniform `StateContainers`.

### 2.3 API Gateway & Real-Time Integration
- Verified live production gateway at `https://api.awarespan.bchbenjamin.in/` (HTTP 200, TLS 1.3).
- Verified real-time WebSocket event stream at `wss://api.awarespan.bchbenjamin.in/ws/events`.
- Configured dynamic fallback resolution in Gradle properties (`AWARESPAN_API_BASE_URL` -> `SIP_API_BASE_URL` -> default).
- Implemented human verification workflows separating immutable edge AI inference from human operator decisions.

### 2.4 Security & Cryptographic Hardening
- **`SecureTokenStore`**: Implemented `EncryptedSharedPreferences` backed by AES-256-GCM MasterKey inside the hardware Android Keystore.
- **Automatic Token Rotation**: `TokenRefreshAuthenticator` handles transparent JWT renewal upon receiving HTTP 401.
- **Zero Cleartext Traffic**: Enforced `android:usesCleartextTraffic="false"` in `AndroidManifest.xml`.
- **Diagnostic Error Classification**: Descriptive, actionable error banners replace opaque failure messages.

### 2.5 Critical Bug Fixes & Stability Improvements
- **R8 ProGuard Login Bug Resolved**: Fixed `ClassCastException` and `NullPointerException` during release builds where R8 stripped DTO fields and constructor parameters (`LoginResponse`, `LoginRequest`, `AutopilotPolicyDto`).
- **Memory & Daemon Optimization**: Resolved Gradle daemon lock contention and out-of-memory crashes on constrained host environments by enforcing worker limits and single-use daemons.
- **Foreground Service Compliance**: Corrected Android 14/15 foreground service permissions (`FOREGROUND_SERVICE_DATA_SYNC`).

---

## 3. Verified Release Artifacts

| Artifact | File Name | Size (Bytes) | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **Release APK (Signed)** | `app-release.apk` | 1,579,152 | `d6a53bb19849980e15607b945ef08ebe426a14143f0a8e40bde0d2d3164d9fa1` |
| **Release APK (Unsigned)** | `app-release-unsigned.apk` | 1,564,331 | `d1e80f06be65658443126bcca1ab2deef4ff6b590bd92d18d1392739ac47e1e4` |
| **Debug APK** | `app-debug.apk` | 13,248,512 | *Built via `./gradlew assembleDebug`* |

---

## 4. Known Limitations & Roadmap

1. **Hardware Edge Nodes**: Node interactions were verified against live backend mocks and MockWebServer. Physical edge device hardware (Raspberry Pi 5) requires active local broker connection through the Spring Boot API Gateway.
2. **Video Streaming**: Video evidence playback currently relies on static media captures and cached MP4 clips; live RTSP transcoding stream support is planned for v0.5.0.
3. **Signed Release Workflow**: When publishing to the Google Play Console or production distribution, configure the required keystore secrets in GitHub Actions or local environment variables (`AWARESPAN_KEYSTORE_FILE`, `AWARESPAN_KEYSTORE_PASSWORD`, `AWARESPAN_KEY_ALIAS`, `AWARESPAN_KEY_PASSWORD`).

---

## 5. Verification Checklist

- [x] Application ID verified: `com.awarespan.guardian`
- [x] Version verified: `0.4.0` (versionCode `5`)
- [x] Zero references to legacy package `com.sip.guardian` in `src/`
- [x] All 5 unit test suites passing (`IncidentMapperTest`, `WebSocketMessageParserTest`, `AuthRepositoryTest`, `AutopilotPolicyTest`, `IncidentStateTest`)
- [x] Production backend health verified: `https://api.awarespan.bchbenjamin.in/actuator/health` (HTTP 200)
- [x] Release build verified with R8 minification and resource shrinking enabled (`minifyEnabled true`, `shrinkResources true`)
- [x] Release APK generated and verified with `aapt dump badging`
