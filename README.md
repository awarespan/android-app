# AWARESPAN — Android Operations Console

> **Awareness Without Boundaries.**  
> Real-Time Edge Intelligence, Incident Verification & Autonomous Policy Control.

[![AWARESPAN Android CI](https://github.com/awarespan/android-app/actions/workflows/android-ci.yml/badge.svg)](https://github.com/awarespan/android-app/actions/workflows/android-ci.yml)
[![Version](https://img.shields.io/badge/version-0.4.0-ff4f2b.svg)](https://github.com/awarespan/android-app/releases/tag/v0.4.0)

---

## 1. Overview

**AWARESPAN Android** is the mobile operations console for the AWARESPAN edge intelligence security network. It provides security operators with real-time threat detection feeds, edge node telemetry monitoring, AI prediction verification, and autonomous response policy controls.

The application follows the **AWARESPAN Zero-Radius Design System** (`docs/DESIGN.md`):
- **Void Black Canvas (`#000000`)** optimized for night operations and OLED power efficiency.
- **Carbon Surfaces (`#1a1a1a`)** with 1dp hairline **Graphite Borders (`#3c3c3c`)**.
- **Ember Orange (`#ff4f2b`)** reserved strictly for primary actions, critical alerts, and selection states.
- **Architectural 0dp Geometry**: Sharp right angles on all cards, buttons, inputs, dialogs, and status badges.
- **Monospaced Operational Metadata** for high-precision hardware identifiers and ISO timestamps.

---

## 2. System Architecture

```text
  Raspberry Pi Edge Nodes (Hardware Inference)
                     │
                     │ MQTT / Secure Telemetry
                     ▼
  AWARESPAN Spring Boot API Gateway (api.awarespan.bchbenjamin.in)
                     ▲
                     │ HTTPS (REST) & WSS (/ws/events)
                     ▼
  AWARESPAN Android App (com.awarespan.guardian)
         ├── Jetpack Compose UI (0dp Architectural Design)
         ├── Dagger Hilt Dependency Injection
         ├── Java Domain Models & Repository Contracts
         ├── Room Local Cache (Offline Operations)
         └── Android Keystore (Encrypted Token Storage)
```

> [!IMPORTANT]
> The Android application interacts exclusively with the authenticated Spring Boot gateway (`https://api.awarespan.bchbenjamin.in/`). It never communicates directly with edge hardware over raw SSH, RTSP, or local database ports.

---

## 3. Application Specifications

- **Application ID / Package**: `com.awarespan.guardian`
- **Application Label**: `AWARESPAN`
- **Version Name**: `0.4.0`
- **Version Code**: `5`
- **Minimum SDK**: `26` (Android 8.0 Oreo)
- **Target SDK**: `34` (Android 14)
- **Primary Language**: Kotlin (UI / Compose) & Java (Domain / Data Layers)

---

## 4. Documentation Index

- [**Design System Specification**](docs/DESIGN_SYSTEM.md): Tokens, typography, 0dp components, and mobile adaptations.
- [**Architecture Documentation**](docs/ARCHITECTURE.md): Multi-paradigm layering, Hilt DI, Room cache, and edge isolation.
- [**API & Network Integration**](docs/API_INTEGRATION.md): Gateway verification, REST contracts, and WebSocket event stream.
- [**Authentication & Security**](docs/AUTHENTICATION.md): Android Keystore, token rotation, and error diagnostics.
- [**Build & Release Guide**](docs/BUILD_AND_RELEASE.md): Compiling, signing configurations, and CI/CD automation.
- [**Testing & Verification**](docs/TESTING.md): Unit tests, live health probes, and UI verification.

---

## 5. Quick Start & Build Instructions

### Prerequisites
- JDK 17 (Temurin 17 recommended)
- Android SDK (API 34, Build Tools 34.0.0)

### 5.1 Run Unit Tests
```bash
cd android
./gradlew testDebugUnitTest
```

### 5.2 Build Debug APK
```bash
cd android
./gradlew assembleDebug
```
Output artifact: `android/app/build/outputs/apk/debug/app-debug.apk`.

### 5.3 Build Signed Release APK
Configure signing credentials via environment variables:
```bash
export AWARESPAN_KEYSTORE_FILE="/path/to/keystore.jks"
export AWARESPAN_KEYSTORE_PASSWORD="KeystorePassword"
export AWARESPAN_KEY_ALIAS="key-alias"
export AWARESPAN_KEY_PASSWORD="KeyPassword"

cd android
./gradlew assembleRelease
```
Output artifact: `android/app/build/outputs/apk/release/app-release.apk`.

---

## 6. License & Security

Proprietary — All rights reserved by AWARESPAN.  
For security disclosures or operational inquiries, contact `security@awarespan.bchbenjamin.in`.
