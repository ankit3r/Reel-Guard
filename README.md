# ReelGuard

ReelGuard is an Android digital wellbeing app that helps limit short-form content consumption by tracking and restricting Instagram Reels, YouTube Shorts, and TikTok sessions, while allowing normal videos and feeds to remain accessible.

## Demo

**[📹 Watch Demo Video](https://drive.google.com/file/d/16qYMnXUR42R7ZabpfvfgMUqyVDH4r8CH/view?usp=drive_link)**

## Features

- **Reel/Shorts detection only**
  - Monitors Instagram Reels, YouTube Shorts, and TikTok using Android Accessibility Services.
  - Regular YouTube videos and Instagram home feed are not blocked.

- **Daily reel limit**
  - Configurable daily reel count limit (up to 10,000 reels per day).
  - Foreground blocking screen when the daily limit is reached.

- **Real-time usage stats**
  - Circular progress view showing reels watched vs daily limit.
  - "Reels remaining" and "Total reels today" cards updated in near real-time.
  - Persistent notification showing current reel count.

- **Secure access**
  - App-level lock with 4‑digit PIN.
  - Security question + answer flow for PIN recovery.
  - PIN and answers stored as SHA‑256 hashes (no plain text).

- **Uninstall protection (optional)**
  - Device Admin integration to make the app harder to uninstall.
  - Admin must be disabled before the app can be removed.

- **Modern Android stack**
  - Kotlin + Jetpack Compose for UI.
  - MVVM architecture with `ViewModel` + Repository.
  - Coroutines for background work.
  - SharedPreferences‑based persistence with per‑day keys.

## Architecture

- **UI Layer**
  - `AuthActivity`: PIN setup, login, and security question recovery.
  - `MainActivity`: reel usage dashboard (progress ring + stats).
  - `SettingsActivity`: daily reel limit configuration and usage reset.
  - `BlockActivity`: full‑screen block when the daily limit is hit.

- **Domain/Data Layer**
  - `PrefsRepository`: wraps SharedPreferences for:
    - daily reel count
    - reel limit
    - PIN hash and security question/answer hashes
  - `UsageViewModel`: exposes `LiveData` for:
    - `reelCount`
    - `reelLimit`

- **Services**
  - `ReelCounterService` (Accessibility Service):
    - Detects when the user is on Reels/Shorts screens via view IDs/text/desc.
    - Listens to scroll events and increments reel count with debouncing.
    - Triggers `BlockActivity` when the reel limit is reached.
  - `UsageMonitorService` (Foreground Service):
    - Keeps a low‑priority persistent notification with current reel count.
    - Periodically refreshes the notification.

- **Other components**
  - `BootReceiver`: restarts services on device boot (if enabled).
  - `MyDeviceAdminReceiver`: handles Device Admin lifecycle callbacks.

## How It Works

1. On first launch, user sets a 4‑digit PIN and a security question + answer.
2. After unlocking, user configures a daily reel limit in Settings.
3. Accessibility service detects when the user is in:
   - Instagram Reels viewer
   - YouTube Shorts player
   - TikTok main feed (treated as reels)
4. Each scroll to a new reel/short increments the daily counter.
5. When the counter reaches the daily limit:
   - `BlockActivity` is shown and continues to re‑appear if dismissed.
6. At any time, the main screen shows:
   - total reels today
   - remaining reels
   - visual progress towards the limit.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVVM, Repository
- **Async**: Kotlin Coroutines
- **System APIs**:
  - Accessibility Service
  - Foreground Service + Notifications
  - Device Admin (optional)
  - SharedPreferences

## Permissions

ReelGuard requires several elevated permissions to work correctly:

- **Usage Access** – to monitor app usage.
- **Accessibility Service** – to detect Reels/Shorts screens and scroll events.
- **Draw over other apps** – for block screens.
- **Post Notifications** – for the persistent usage notification.
- **Device Admin** (optional) – to prevent easy uninstallation.

All permissions are requested transparently with in‑app guidance.

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/ankit3r/Reel-Guard.git
