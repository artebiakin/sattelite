# Satellite SMS Demo

An Android MVP app demonstrating `SmsManager` for sending SMS, with a live **satellite / non-terrestrial network indicator** — designed to test One NZ's Starlink Direct-to-Cell SMS service in New Zealand.

## Features

- Send SMS via `SmsManager` (API 24+, long-message safe via multipart)
- SENT / DELIVERED status log with result-code descriptions
- Satellite network banner using `ServiceState.isUsingNonTerrestrialNetwork()` (Android 15 / API 35+)
- In-app One NZ satellite testing guide
- Runtime permission handling (SEND_SMS + READ_PHONE_STATE)

## Build requirements

| Tool | Version |
|------|---------|
| Android Studio | Meerkat (2024.3.1) or newer |
| Android SDK | Platform 35 + Build-Tools 35.0.0 |
| JDK | 17 or 21 |
| Gradle | 8.14.3 (wrapper included) |

## How to build

```bash
# 1. Clone the repo
git clone https://github.com/artebiakin/sattelite.git
cd sattelite

# 2. Open in Android Studio OR build from the command line:
./gradlew assembleDebug

# APK output:
# app/build/outputs/apk/debug/app-debug.apk

# 3. Run JVM unit tests (no device needed):
./gradlew testDebugUnitTest
```

If you don't have the Android SDK, open the project in Android Studio — it will prompt you to install the missing SDK components automatically.

## Install on device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Testing satellite SMS in New Zealand (One NZ)

### Compatible phones (as of early 2026)
- Samsung Galaxy S24 Ultra
- Samsung Galaxy Z Fold 6
- Samsung Galaxy Z Flip 6
- Oppo Find X8 Pro
- (More Samsung and Oppo models being added)

### Steps

1. Insert a **One NZ SIM** with a plan that includes satellite coverage.
2. Update the phone to **Android 15 (API 35)** or later.
3. Install the app and grant **SMS** and **Phone** permissions.
4. Travel to a **rural or remote location** in New Zealand with no terrestrial cell coverage.
5. Step outside with a **clear, unobstructed view of the sky**.
6. Wait ~30 seconds. The banner at the top will change to
   **"On satellite (non-terrestrial) network"** when satellite acquisition is confirmed.
7. Enter a New Zealand mobile number (e.g. `+642712345678`) and a short message.
8. Tap **Send SMS** and watch the status log for SENT / DELIVERED events.

### Notes
- Satellite SMS only supports **standard SMS** (no MMS, RCS, or iMessage).
- Delivery can take **1-2 minutes** depending on satellite pass timing.
- The satellite indicator requires Android 15+. On older Android versions it shows "Unknown".
- No special API is needed to route SMS over satellite — the modem handles it automatically when terrestrial coverage is unavailable.

## Architecture

```
MainActivity           -> permission launcher, Compose host
SmsViewModel           -> UI state, orchestrates sender + monitor
SmsSender              -> SmsManager wrapper, PendingIntent receivers, Flow<SmsEvent>
SmsResultCodes         -> pure result-code -> string mapping (unit-tested)
SatelliteStatusMonitor -> TelephonyCallback -> StateFlow<SatelliteState>
ui/SmsDemoScreen       -> Compose UI (satellite banner, form, log, help card)
```

## How satellite SMS works

The `SmsManager` API is unchanged for satellite. When a compatible phone is in an area with **no terrestrial cell coverage** and **clear sky visibility**, the baseband modem automatically routes SMS through SpaceX Starlink Direct-to-Cell satellites. The carrier (One NZ) transparently delivers the message to the destination number. Your app calls exactly the same `sendTextMessage()` or `sendMultipartTextMessage()` as for normal cellular SMS.
