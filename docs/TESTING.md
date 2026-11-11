# AWARESPAN Android Verification & Testing Guide

*Namespace: `com.awarespan.guardian`*

---

## 1. Unit Testing Strategy

The test suite validates data transformation, security rules, network error recovery, and domain invariants without requiring a connected physical device or emulator.

### Executing the Test Suite
```bash
cd android
./gradlew testDebugUnitTest
```

### Test Coverage Breakdown

| Test Suite | File | Focus / Verification Area |
| :--- | :--- | :--- |
| **`IncidentMapperTest`** | [`IncidentMapperTest.java`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/test/java/com/awarespan/guardian/data/mapper/IncidentMapperTest.java) | Validates two-way mapping between `IncidentDto`, `IncidentEntity` (Room), and domain `Incident`. Ensures null safety and timestamp formatting. |
| **`WebSocketMessageParserTest`**| [`WebSocketMessageParserTest.java`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/test/java/com/awarespan/guardian/data/remote/websocket/WebSocketMessageParserTest.java) | Parses live telemetry frames, verifies payload extraction for `INCIDENT_DETECTED` and `HEARTBEAT` messages. |
| **`AuthRepositoryTest`** | [`AuthRepositoryTest.java`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/test/java/com/awarespan/guardian/data/repository/AuthRepositoryTest.java) | Tests successful login flow, token encryption into `SecureTokenStore`, and proper error propagation on HTTP 401/403. |
| **`AutopilotPolicyTest`** | [`AutopilotPolicyTest.java`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/test/java/com/awarespan/guardian/domain/model/AutopilotPolicyTest.java) | Tests policy invariants: threshold clamping (0.0 to 1.0), timeout limits, and permitted threat type rules. |
| **`IncidentStateTest`** | [`IncidentStateTest.java`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/test/java/com/awarespan/guardian/domain/model/IncidentStateTest.java) | Validates state transitions (`DETECTED` -> `INVESTIGATING` -> `CONFIRMED` / `FALSE_ALARM` / `AUTO_HANDLED`). |

---

## 2. Integration Verification

### Gateway Health Probe
Verify live backend availability before running end-to-end integration flows:
```bash
# Gateway health probe
curl -s -i https://api.awarespan.bchbenjamin.in/health

# Spring Boot Actuator probe
curl -s -i https://api.awarespan.bchbenjamin.in/actuator/health
```
Expected response: HTTP 200 with JSON payload `{"status":"UP","service":"sip-backend"}`.

### WebSocket Handshake Verification
Test live WSS upgrade handshake:
```bash
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" \
     -H "Host: api.awarespan.bchbenjamin.in" \
     -H "Origin: https://api.awarespan.bchbenjamin.in" \
     https://api.awarespan.bchbenjamin.in/ws/events
```
Expected response: HTTP 101 Switching Protocols.

---

## 3. UI Component & Screen Verification

The redesign was validated against the AWARESPAN Zero-Radius Design System:
1. **Login Screen**: Verified error state diagnostics on invalid credentials, endpoint display, and password masking.
2. **Dashboard**: Verified active threat counters, online edge node badges, and recent incident cards.
3. **Incident Feed**: Verified filter chips (All, Unverified, Active, Resolved), search bar, and empty/loading states.
4. **Incident Detail**: Verified separation of immutable edge AI predictions from human operator verifications.
5. **Autopilot Controls**: Verified slider behavior, local change detection, and sync status indicators.
6. **Map Radar**: Verified coordinate radar canvas with fallback handling for unlocated/off-grid nodes.
