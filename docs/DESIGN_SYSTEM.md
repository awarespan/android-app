# AWARESPAN Android Design System

*Brand Message: "Awareness Without Boundaries."*
*Visual Source of Truth: `docs/DESIGN.md`*

---

## 1. Design Philosophy

The AWARESPAN mobile operations console reflects **dark infrastructure at night**:
- **Void Black Canvas (`#000000`)**: Deep, unlit pure black backgrounds that optimize OLED power consumption and dark-environment readability for field operators.
- **Carbon Secondary Surfaces (`#1a1a1a`)**: Subtle contrast layer for cards, input containers, and structural sections.
- **Ember Orange (`#ff4f2b`) Action Trigger**: An intentional, razor-sharp accent used exclusively for primary user actions, selection highlights, and critical emphasis. Never used decoratively.
- **Zero-Radius Architectural Geometry (`0dp`)**: Absolute prohibition of rounded cards, pill buttons, soft bubbles, or Material 3 Expressive shapes. Every card, button, text input, badge, and dialog surface adheres to sharp, right-angle geometry.
- **Hairline Graphite Borders (`#3c3c3c`, 1dp)**: Structural hierarchy communicated through razor-thin borders and tonal shifts rather than drop shadows or soft elevations.
- **Monospaced Operational Metadata**: High-precision display of hardware node IDs, ISO-8601 timestamps, telemetry, and status indicators using monospaced typography.

---

## 2. Color Palette & Token System

Centralized in [`ui/theme/Color.kt`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/theme/Color.kt).

### Core Brand Tokens

| Token Name | Hex Code | Purpose / Usage |
| :--- | :--- | :--- |
| **`EmberOrange`** | `#ff4f2b` | Primary actions, selected states, critical alerts |
| **`VoidBlack`** | `#000000` | Main application background canvas |
| **`Carbon`** | `#1a1a1a` | Card surfaces, inputs, list items |
| **`CarbonSecondary`** | `#242424` | Elevated/nested cards, chip containers |
| **`GraphiteBorder`** | `#3c3c3c` | Hairline borders (1dp), horizontal dividers |
| **`SteelMid`** | `#737373` | Secondary metadata, muted captions, disabled borders |
| **`FogLight`** | `#bfbfbf` | Secondary body text, subheadings, labels |
| **`BoneWhite`** | `#f5f5f5` | Primary text, titles, prominent icons |

### Semantic Functional Tokens

| Token Name | Hex Code | Semantics |
| :--- | :--- | :--- |
| **`ThreatCritical`** | `#ff4f2b` | Level 4 / Critical threat (Active intrusion, violence) |
| **`ThreatHigh`** | `#ff8c00` | Level 3 / High threat (Suspicious gathering, loitering) |
| **`ThreatMedium`** | `#eab308` | Level 2 / Medium threat (Vandalism, perimeter anomaly) |
| **`ThreatLow`** | `#3b82f6` | Level 1 / Low threat (Unverified acoustic/motion spike) |
| **`StatusOnline`** | `#22c55e` | Node online, healthy heartbeat, active websocket |
| **`StatusWarning`** | `#f59e0b` | High latency, degraded sensor, pending sync |
| **`StatusError`** | `#ef4444` | Node offline, auth error, network failure |

---

## 3. Typography Hierarchy

Defined in [`ui/theme/Type.kt`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/theme/Type.kt).

| Role | Weight | Size / LineHeight | Style | Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **Display / Header** | Light (300) | 26sp / 32sp | Sans-serif | Screen headers, hero counters |
| **Title Medium** | Normal (400) | 18sp / 24sp | Sans-serif | Card headers, modal titles |
| **Body Large** | Normal (400) | 15sp / 22sp | Sans-serif | Narrative incident descriptions, messages |
| **Body Medium** | Normal (400) | 13sp / 18sp | Sans-serif | Secondary descriptions, list subtitles |
| **Operational Label** | Medium (500) | 11sp / 14sp | Monospace (Chivo Mono fallback) | Node IDs, hashes, timestamps, statuses |
| **Button Label** | SemiBold (600)| 12sp / 16sp | Monospace, Uppercase, Tracking +1.2sp | Action buttons, chip selectors |

---

## 4. Custom Architectural Components

All components are implemented in `ui/components/` and strictly reject Material 3 defaults:

### 1. [`AwarespanButton`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/components/AwarespanButton.kt)
- **Shape**: `RoundedCornerShape(0.dp)` (Sharp rectangles).
- **Height**: Minimum 48dp touch target with 4dp internal padding foundation.
- **Typography**: Monospaced uppercase with letter spacing.
- **Variants**:
  - `Primary`: Solid Ember Orange background (`#ff4f2b`), Void Black text.
  - `Ghost`: Transparent background, 1dp Graphite Border (`#3c3c3c`), Bone White text.
  - `Outline`: Transparent background, 1dp Ember Orange border, Ember Orange text.
  - `Critical`: Dark red/orange fill for irreversible security actions.

### 2. [`AwarespanCard`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/components/AwarespanCard.kt)
- **Shape**: `RoundedCornerShape(0.dp)`.
- **Surface**: Carbon (`#1a1a1a`).
- **Border**: 1dp solid Graphite Border (`#3c3c3c`).
- **Elevation**: 0dp (No drop shadows).

### 3. [`AwarespanTextField`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/components/AwarespanTextField.kt)
- **Shape**: 0dp rectangle.
- **Background**: Carbon (`#1a1a1a`).
- **Border**: Shifts dynamically from `#3c3c3c` (idle) to `#ff4f2b` (focused).
- **Label**: Monospaced uppercase prompt.

### 4. [`AwarespanBadge`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/components/AwarespanBadge.kt) & [`ThreatBadge`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/components/ThreatBadge.kt)
- **Shape**: Sharp 0dp pill-free container.
- **Border**: 1dp matching the semantic status token.
- **Text**: Monospaced uppercase indicator.

### 5. [`ConfidenceMeter`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/components/ConfidenceMeter.kt)
- **Geometry**: Sharp rectangular progress bar (0dp radius) with 1dp border, color-coded by confidence threshold (Critical/High/Medium/Low).

### 6. [`StateContainers`](file:///mnt/WindowsDrive/Shared/Projects/SIP/android-frontend/android/app/src/main/java/com/awarespan/guardian/ui/components/StateContainers.kt)
- **Loading State**: Circular indicator styled in Ember Orange with monospaced progress readout.
- **Empty State**: Minimalist terminal icon, bone white message, graphite border.
- **Error State**: Critical warning banner with retry action trigger.
- **Offline Banner**: Sticky high-contrast bar warning operator of stale cache data and disconnections.

---

## 5. Mobile Adaptation & Accessibility
1. **Touch Target Size**: All interactive elements maintain a minimum hit target of 48×48dp according to WCAG AAA standards.
2. **Contrast Ratio**: Void Black (`#000000`) and Bone White (`#f5f5f5`) produce a contrast ratio exceeding 18:1; Ember Orange (`#ff4f2b`) against Void Black achieves 5.8:1.
3. **Screen Reader Support**: All visual-only icons have explicit `contentDescription` or `null` decorative designations; all threat badges expose readable severity strings.
4. **Haptic & Visual Feedback**: Focused, pressed, and disabled states are visually distinct without relying on translucent overlays.
