# AWARESPAN Authentication & Security Architecture

*Namespace: `com.awarespan.guardian`*  
*Security Standard: Zero Trust, Android Keystore Cryptographic Isolation*

---

## 1. Authentication Lifecycle

The AWARESPAN Android application implements an operator authentication model backed by JWT access tokens and long-lived refresh tokens:

```text
┌─────────────────┐             ┌─────────────────────┐             ┌──────────────────┐
│  Login Screen   │             │   Spring Boot API   │             │ SecureTokenStore │
│ (Jetpack Compose)│            │  (AuthApiService)   │             │(EncryptedPrefs)  │
└────────┬────────┘             └──────────┬──────────┘             └────────┬─────────┘
         │                                 │                                 │
         │ 1. POST /api/v1/auth/login      │                                 │
         │────────────────────────────────>│                                 │
         │                                 │                                 │
         │ 2. Return JWT + RefreshToken    │                                 │
         │<────────────────────────────────│                                 │
         │                                 │                                 │
         │ 3. Save tokens securely         │                                 │
         │──────────────────────────────────────────────────────────────────>│
         │                                 │                                 │
         │ 4. Transition to Dashboard      │                                 │
         │                                 │                                 │
```

---

## 2. Secure Storage: Android Keystore & EncryptedSharedPreferences

Tokens are stored using [`SecureTokenStore.java`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/data/local/SecureTokenStore.java):
- **Encryption Scheme**: Backed by `androidx.security.crypto.EncryptedSharedPreferences`.
- **Master Key**: Generated using `MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)` within the hardware-backed Android Keystore system.
- **Key Encryption**: Master keys are protected by the device hardware security module (Secure Element / StrongBox when available).
- **Fallback Protection**: Prevents plaintext leaks in `shared_prefs/` XML files, unencrypted memory dumps, or adb backups.

---

## 3. Automatic Token Rotation (`TokenRefreshAuthenticator`)

HTTP requests include the access token via `AuthInterceptor`:
```http
Authorization: Bearer <ACCESS_TOKEN>
```

When an access token expires:
1. OkHttp intercepts the `401 Unauthorized` response via [`TokenRefreshAuthenticator.java`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/data/remote/interceptor/TokenRefreshAuthenticator.java).
2. The authenticator synchronizes threads to avoid race conditions.
3. A synchronous refresh request is issued to `POST /api/v1/auth/refresh` using the stored refresh token.
4. On success: The new access token is saved in `SecureTokenStore`, and the failed request is retried with the new token.
5. On failure (e.g. revoked refresh token): The authenticator purges stored credentials and triggers a clean session logout, directing the operator back to [`LoginScreen.kt`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/screen/auth/LoginScreen.kt).

---

## 4. Enhanced Diagnostic Error Messages

Earlier iterations returned opaque "Unexpected login failure" messages. The updated system classifies failures into clear operational states:

| Failure Mode | Status Code | Diagnostic Indicator Shown to Operator |
| :--- | :--- | :--- |
| **Invalid Credentials** | `401 Unauthorized` | `"Invalid operator username or password. Check credentials and retry."` |
| **Account Inactive** | `403 Forbidden` | `"Account suspended or missing operator role. Contact security administrator."` |
| **Gateway Offline** | Connection Timeout | `"Gateway unreachable. Check cellular/Wi-Fi connection or backend health."` |
| **Server Fault** | `500 Internal Error` | `"Server internal fault during authentication. Incident logged."` |

---

## 5. R8 / ProGuard Keep Rules for Authentication

To ensure serialization stability under code obfuscation, the following rules are enforced in [`proguard-rules.pro`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/proguard-rules.pro):
```proguard
# Preserve Auth DTOs and JSON serialization
-keep class com.awarespan.guardian.data.remote.dto.LoginRequest { *; }
-keep class com.awarespan.guardian.data.remote.dto.LoginResponse { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
```
