# AWARESPAN API & Network Integration Guide

*Verified Gateway Endpoint: `https://api.awarespan.bchbenjamin.in/`*  
*Real-Time Stream: `wss://api.awarespan.bchbenjamin.in/ws/events`*

---

## 1. Verified Gateway Status & Endpoints

Live endpoint verification was conducted using TLS 1.3/Google Trust Services certificates.

### Gateway Verification Matrix

| Endpoint | Target URL | Status | Protocol / Details |
| :--- | :--- | :--- | :--- |
| **Production API (Active)** | `https://api.awarespan.bchbenjamin.in/` | **200 OK** | Spring Boot 3.3.4, TLS 1.3 |
| **Health Check** | `https://api.awarespan.bchbenjamin.in/health` | **200 OK** | `{"status":"UP","service":"sip-backend"}` |
| **Actuator** | `https://api.awarespan.bchbenjamin.in/actuator/health` | **200 OK** | Spring Boot Actuator probe |
| **WebSocket** | `wss://api.awarespan.bchbenjamin.in/ws/events` | **101 Handshake** | STOMP / WebSocket Event Stream |
| *Legacy Endpoint (Deprecated)* | `https://api.sip.bchbenjamin.in/` | **FAILED** | SSL Handshake alert (`unrecognized_name`) |

---

## 2. Authentication Contracts

### `POST /api/v1/auth/login`
Authenticates an operator and issues a signed JWT access token.

- **Request Body**:
  ```json
  {
    "username": "operator1",
    "password": "ValidPassword123!"
  }
  ```
- **Success Response (`200 OK`)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
    "refreshToken": "d748f93e-2b10-482a...",
    "userId": "usr_99214",
    "username": "operator1",
    "role": "ROLE_OPERATOR"
  }
  ```
- **Error Response (`401 Unauthorized`)**:
  ```json
  {
    "timestamp": "2026-11-12T00:19:24.182Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid username or password"
  }
  ```

### `POST /api/v1/auth/refresh`
Rotates the access token using the encrypted refresh token. Executed automatically by `TokenRefreshAuthenticator`.

- **Request Body**:
  ```json
  {
    "refreshToken": "d748f93e-2b10-482a..."
  }
  ```
- **Success Response (`200 OK`)**: Returns updated JWT access token.

### `POST /api/v1/auth/logout`
Revokes active session tokens server-side.

---

## 3. Core Operational API Contracts

### Incident Management
- **`GET /api/v1/incidents`**: Paginated feed of reported incidents.
  - Query parameters: `status`, `severity`, `nodeId`, `page`, `size`.
  - Response: `PageDto<IncidentDto>`.
- **`GET /api/v1/incidents/{id}`**: Detailed incident record with raw sensor readings, immutable edge AI detection results, and annotations.
- **`POST /api/v1/incidents/{id}/verify`**: Human operator verification:
  - Request: `{"status": "CONFIRMED" | "FALSE_ALARM" | "UNCERTAIN", "notes": "Verified intrusion via camera feed"}`.

### Edge Node Telemetry & Registry
- **`GET /api/v1/nodes`**: Complete edge node inventory.
- **`GET /api/v1/nodes/{id}`**: Heartbeat, battery level, operational status (`ONLINE`, `OFFLINE`, `DEGRADED`), GPS coordinates, and firmware version.

### Autopilot Policy Control
- **`GET /api/v1/autopilot/policy`**: Active autonomous deterrence parameters.
- **`PUT /api/v1/autopilot/policy`**: Operator updates for autonomous thresholds, deterrence timeouts, and permitted countermeasure rules.

---

## 4. Real-Time WebSocket Specification

The Android client establishes a persistent connection via [`SipWebSocketClient`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/data/remote/websocket/SipWebSocketClient.java).

- **URI**: `wss://api.awarespan.bchbenjamin.in/ws/events`
- **Headers**:
  ```http
  Authorization: Bearer <JWT_ACCESS_TOKEN>
  X-Client-Version: 0.4.0
  ```
- **Event Types**:
  1. `INCIDENT_DETECTED`: Broadcast when an edge node detects a new security anomaly.
  2. `INCIDENT_UPDATED`: Broadcast when status changes (e.g. human verification or autopilot resolution).
  3. `NODE_HEARTBEAT`: Periodic node health and telemetry ping.
  4. `NODE_OFFLINE`: Node timeout warning.
  5. `AUTOPILOT_TRIGGERED`: Automated deterrence action initiated.

---

## 5. Security & Isolation Guarantee

The client interacts exclusively with the authenticated Spring Boot gateway over TLS. No direct socket, SSH, or MQTT connections are ever initiated to edge Raspberry Pi nodes or backing database servers.
