# AWARESPAN Android Build & Release Guide

*Application ID: `com.awarespan.guardian`*  
*Version Name: `0.4.0`*  
*Version Code: `5`*

---

## 1. Prerequisites & Environment

- **Operating System**: Linux (Ubuntu 22.04+ recommended) or macOS / Windows with WSL2.
- **Java Development Kit**: OpenJDK 17 (e.g., Eclipse Temurin 17.0.12+).
- **Android SDK**:
  - `compileSdk`: 34
  - `targetSdk`: 34
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `buildToolsVersion`: 34.0.0
- **Gradle**: 8.9 (managed via the included `android/gradlew` wrapper).

---

## 2. Local Build Instructions

### 2.1 Running Unit Tests
Unit tests cover all domain logic, entity mappers, WebSocket frame parsers, and repository authenticators:
```bash
cd android
./gradlew testDebugUnitTest
```
HTML test reports are generated at `android/app/build/reports/tests/testDebugUnitTest/index.html`.

### 2.2 Assembling Debug Build
```bash
cd android
./gradlew assembleDebug
```
Output artifact: `android/app/build/outputs/apk/debug/app-debug.apk`.

### 2.3 Assembling Signed Release Build
Release signing credentials must **never** be hardcoded in version control. Configure them using environment variables:

```bash
export AWARESPAN_KEYSTORE_FILE="/path/to/release.jks"
export AWARESPAN_KEYSTORE_PASSWORD="SecureKeystorePassword"
export AWARESPAN_KEY_ALIAS="release-key-alias"
export AWARESPAN_KEY_PASSWORD="SecureKeyPassword"

cd android
./gradlew assembleRelease
```
Output artifact: `android/app/build/outputs/apk/release/app-release.apk`.

> [!NOTE]
> If signing environment variables are omitted, the build completes safely and generates an unsigned artifact (`app-release-unsigned.apk`) suitable for staging/PR verification without throwing unhandled exceptions.

---

## 3. Signature & Checksum Verification

### 3.1 Verifying APK Signature
Verify that the release APK has been properly signed with Android APK Signature Scheme v2/v3 using `apksigner`:
```bash
$ANDROID_HOME/build-tools/34.0.0/apksigner verify -v android/app/build/outputs/apk/release/app-release.apk
```
Expected output:
```text
Verifies
Verified using v1 scheme (JAR signing): true
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
```

### 3.2 Generating SHA-256 Checksum
Generate an immutable cryptographic hash for release publication:
```bash
cd android/app/build/outputs/apk/release
sha256sum app-release.apk > SHA256SUMS.txt
cat SHA256SUMS.txt
```

---

## 4. GitHub Actions CI/CD Pipeline

The repository provides two automated GitHub Actions workflows:

### 4.1 CI Workflow (`.github/workflows/android-ci.yml`)
- Triggers on push and pull requests affecting `android/**`.
- Validates that code compiles cleanly.
- Executes `./gradlew testDebugUnitTest` and uploads test reports as CI artifacts.
- Builds `assembleDebug` to verify packaging and asset merging.

### 4.2 Release Workflow (`.github/workflows/android-release.yml`)
- Triggers on tag pushes matching `v*` (e.g. `v0.4.0`) or manual workflow dispatch.
- Decodes the secret base64 keystore (`AWARESPAN_RELEASE_KEYSTORE_BASE64`) securely in runner memory.
- Assembles the production release APK with full R8 optimization and tree shaking.
- Computes the SHA-256 checksum and publishes a new GitHub Release with attached binaries.
