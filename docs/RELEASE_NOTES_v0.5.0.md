# AWARESPAN Android Application — v0.5.0 Release Notes

**Release Tag**: `v0.5.0`  
**Version Name**: `0.5.0`  
**Version Code**: `6`  
**Package / Namespace**: `com.awarespan.guardian`  
**Brand**: AWARESPAN  
**Tagline**: *Awareness Without Boundaries.*  
**Date**: November 17, 2026  

---

## 1. Executive Summary

Version `0.5.0` delivers enhanced edge synchronization capabilities, displaying real-time Raspberry Pi edge detection labels and acoustic deterrence events directly within the operator event history stream. This release maintains strict backward and forward compatibility with the central backend API and preserves the authoritative zero-radius industrial design language.

---

## 2. Key Highlights & Changes

### 2.1 Pi Detection Labels & Threat Telemetry in Event Stream
- Integrated real-time parsing and display of edge inference detection labels (e.g. `gun`, `knife`, `person`, `dog`) into `EventHistoryViewModel`.
- Added threat classification badges displaying granular confidence scores and model identification.

### 2.2 Surface Pi Deterrence Response Events
- Real-time visualization of edge acoustic deterrence actions (e.g. `speaker_warning`, frequency sweep deterrence) directly in the event timeline.
- Displays actuation status, target domain, and response duration for full operational transparency.

### 2.3 Build & Packaging Stability
- Resolved KSP annotation processor incremental compilation edge cases.
- Enforced AndroidX flags for CI and release workflows.
- Successfully verified cryptographic signature scheme (APK Signature Scheme v2) against canonical `sip-beta` key.

---

## 3. Verified Release Artifacts

| Artifact | File Name | Size (Bytes) | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **Release APK (Signed)** | `app-release.apk` | 1,745,559 | `7cbe9229388461968782273c32479c70ab497ca4090d4bcb3175a1b16da998f7` |
| **Checksum Manifest** | `SHA256SUMS.txt` | 82 | `7cbe9229388461968782273c32479c70ab497ca4090d4bcb3175a1b16da998f7  app-release.apk` |

### Signing Certificate Verification
- **Signer DN**: `CN=SIP Guardian, OU=Dev, O=SIP, L=City, ST=State, C=US`
- **Certificate SHA-256 Fingerprint**: `5B:BA:5B:B3:E8:3D:1E:1B:E8:E9:BB:31:73:CF:72:E1:D0:BD:32:16:F1:25:13:26:35:80:CF:A9:31:DB:65:22`
- **Signature Scheme**: APK Signature Scheme v2 Verified (`true`)
