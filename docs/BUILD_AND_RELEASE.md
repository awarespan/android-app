# AWARESPAN Android Build & Release Guide

*Application ID: `com.awarespan.guardian`*
*Version Name: `0.5.0`*
*Version Code: `6`*

---

## 1. Prerequisites & Environment

- **Operating System**: Linux (Ubuntu 22.04+ recommended) or macOS / Windows with WSL2.
- **Java Development Kit**: OpenJDK 17 (e.g., Eclipse Temurin 17.0.12+).
- **Android SDK**:
  - `compileSdk`: 35
  - `targetSdk`: 35
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `buildToolsVersion`: 35.0.0
- **Gradle**: 8.9 (managed via the included `android/gradlew` wrapper).

---

## 2. Build the APK from source

The Android app is a client. It contains **no bootstrap administrator credentials,
node credentials, backend API key, or SSH login**. Operators sign in using accounts
that already exist in the backend. The API base URL is a build-time endpoint setting;
operator credentials are entered at runtime and sent to the backend over HTTPS.

### 2.1 Install the build tools

- JDK 17 (verify with `java -version`).
- Android SDK Platform 35 and Android SDK Build-Tools 35.0.0.
- Android SDK Platform-Tools (`adb`) if you want to install the APK from a terminal.
- Git.

Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) to your Android SDK directory and ensure
`$ANDROID_HOME/platform-tools` is on `PATH`. The Gradle 8.9 wrapper is included; do
not install a separate Gradle version.

### 2.2 Clone and configure the backend URL

```bash
git clone https://github.com/awarespan/android-app.git
cd android-app
```

The production backend URL is already the default. To build against another backend,
export the URL before invoking Gradle:

```bash
export AWARESPAN_API_BASE_URL="https://your-backend.example/"
```

The URL must end with `/` because Retrofit resolves API paths relative to it. Do not
put a username, password, `DEVICE_API_KEY`, `PI_DEVICE_SECRET`, or bootstrap admin
credentials in the Android project or APK. The backend must have an operator account
provisioned separately before login can succeed.

### 2.3 Run unit tests and build the debug APK

```bash
cd android
./gradlew --no-daemon clean testDebugUnitTest assembleDebug
```

The APK is written to:

`android/app/build/outputs/apk/debug/app-debug.apk`

The unit-test report is written to:

`android/app/build/reports/tests/testDebugUnitTest/index.html`

### 2.4 Install on a connected Android device

Enable Developer Options and USB debugging, connect the device, then run:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open **AWARESPAN**, enter the operator username and password provisioned by the
backend administrator, and sign in. If login fails, verify the configured backend URL,
backend health, and that the operator account exists. The app does not create or
bootstrap administrator accounts.

### 2.5 Build a signed release APK

Keep the release keystore outside the repository. Supply all signing values through
the environment (or your CI secret store):

```bash
export AWARESPAN_KEYSTORE_FILE="/absolute/path/to/release.jks"
export AWARESPAN_KEYSTORE_PASSWORD="<keystore-password>"
export AWARESPAN_KEY_ALIAS="<key-alias>"
export AWARESPAN_KEY_PASSWORD="<key-password>"

cd android
./gradlew --no-daemon clean testDebugUnitTest assembleRelease
```

With complete signing configuration, the signed APK is written to
`app/build/outputs/apk/release/app-release.apk`. Without it, Gradle produces an
unsigned release APK, which cannot be installed as a normal signed release. Never
commit the keystore or signing values.

## 3. Signature & Checksum Verification

### 3.1 Verifying APK Signature
Verify that the release APK has been properly signed with Android APK Signature Scheme v2/v3 using `apksigner`:
```bash
$ANDROID_HOME/build-tools/35.0.0/apksigner verify -v android/app/build/outputs/apk/release/app-release.apk
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
