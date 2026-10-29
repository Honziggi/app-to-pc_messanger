# App to PC Messenger

Personal messenger between a Linux PC and an Android phone. The phone app (**Instant** / **App to PC**) shows a chat UI. A Python script on the PC encrypts a message and delivers it through **Firebase Cloud Messaging (FCM)**. The app decrypts it with AES-GCM and stores it in a local Room database.

Gradle project name: `Instant`. Application ID: `com.example.apptopc`.

## How it works

```
PC (send_msg.py)
  → AES-256-GCM encrypt
  → FCM notification (title + encrypted body)
  → Android MessagingService
  → decrypt
  → Room (nitro_chat_db)
  → Chat screen
```

1. On first launch the app generates an AES-256 key and stores it in SharedPreferences (`security_pref` / `terminal_key`).
2. FCM also issues a device registration token (logged as `FCM_TOKEN`).
3. You copy **both** values into `send_msg.py`.
4. Typing a line on the PC encrypts it and sends an FCM notification to that token.
5. `MessagingService` decrypts the notification body and inserts a `ChatMessage`.

The phone currently **receives** PC messages. Sending from the app back to the PC is not wired up yet.

## Features

- Login screen (hardcoded credentials; see below)
- Chat history persisted with Room
- Incoming messages over FCM, encrypted with AES-GCM (12-byte nonce + ciphertext + tag, Base64)
- Extra **Terminal** tab in the UI (local mock tabs only; commands are not sent anywhere)

## Requirements

**Android**

- Android Studio (or JDK 17 + Android SDK)
- Device or emulator with Google Play services
- `minSdk` 24, `compileSdk` / `targetSdk` 35

**PC sender**

- Python 3
- Firebase project with Cloud Messaging enabled
- Service account JSON for that project

Python packages:

```bash
pip install firebase-admin pycryptodome
```

A local `venv/` is already used in this repo; activate it if you prefer:

```bash
source venv/bin/activate
```

## Firebase setup

1. Create a Firebase project and register an Android app with package name `com.example.apptopc`.
2. Download `google-services.json` into `app/`.
3. In Firebase Console → Project settings → Service accounts, generate a private key. Save it as `service-account.json` in the **repo root** (same folder as `send_msg.py`).
4. Keep both JSON files private. They grant access to your Firebase project.

`send_msg.py` loads credentials with:

```python
cred = credentials.Certificate("service-account.json")
```

## Run the Android app

1. Open this folder in Android Studio.
2. Sync Gradle and run the `app` configuration on a physical device (FCM on emulators is unreliable unless Play services are present).
3. Default login:
   - Username: `admin`
   - Password: `admin`
4. In Logcat, filter for:
   - `FCM_TOKEN` — device token
   - `ENCRYPTION_KEY` — Base64 AES key

Copy those strings into `send_msg.py`:

```python
MY_PHONE_TOKEN = "<paste FCM token>"
encryption_key = "<paste encryption key>"
```

The token can change after app reinstall or when FCM rotates it. If sends fail, refresh both values from Logcat.

## Send a message from the PC

From the repo root, with `service-account.json` present and the token/key filled in:

```bash
python send_msg.py
```

Type a line and press Enter. The phone should show the decrypted text in **NITRO Terminal Chat**. Type `exit` or `quit` to stop.

## Project layout

```
app/src/main/java/com/example/apptopc/
  MainActivity.kt          # Login, chat UI, terminal UI stub
  MessagingService.kt      # FCM receive + AES-GCM decrypt + key storage
  ChatViewModel.kt         # Unused in-memory list (chat uses Room)
  data/
    AppDatabase.kt         # Room database nitro_chat_db
    ChatDao.kt
    ChatMessage.kt
send_msg.py                # PC CLI sender
app/google-services.json   # Firebase Android config (do not share)
service-account.json       # Firebase Admin SDK key (do not share)
```

## Current limitations

- Login is a local check (`admin` / `admin`), not a real account system.
- PC → phone only. The chat composer on the phone is not implemented.
- Terminal screen is UI-only (`onSendCommand` is empty).
- Token and encryption key in `send_msg.py` are hardcoded; they must match the installed app instance.
- FCM payload uses a **notification** message. Delivery while the app is in the background follows FCM notification rules; keep the app in the foreground while testing if messages do not appear.

## Security notes

This is a personal/dev project, not production auth.

- Do not commit `service-account.json`, live FCM tokens, or the AES key.
- The AES key lives in app SharedPreferences and must be copied to the PC by hand.
- Change the hardcoded login before using this on a real device you care about.
