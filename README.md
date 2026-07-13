# VHAL Debug Monitor

Android Automotive application for monitoring and debugging Vehicle HAL (VHAL) properties in real-time. Ideal for development and integration testing with vehicle systems.

## 📋 Features

- ✅ Real-time VHAL property reading (ABS, Fuel Level, VIN)
- ✅ Refresh button to reconnect and retrieve fresh data
- ✅ Clear status indicators: "Refreshing...", "Connected", "N/A", or actual values
- ✅ Dual logging: App logs + System Logcat capture
- ✅ Copy all logs to clipboard (combined)
- ✅ Clean and intuitive single-panel interface
- ✅ Persistent log file for offline debugging

---

## 🚀 Quick Start

### Requirements

- Android Studio with Android Automotive emulator
- ADB (Android Debug Bridge)
- PowerShell or Terminal

### Installation

1. **Clone/download the project**
```bash
cd QRCodetoyota
```

2. **Build the APK**
```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on emulator**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

4. **Open the app**
```bash
adb shell am start -n com.tta.qrcodetoyota/.MainActivity
```

---

## 📱 How to Use

### Main Interface

The app displays a single clean panel with:

**Vehicle Status Section:**
- **VHAL:** Connection status
- **ABS:** ON/OFF or N/A if unavailable
- **Fuel Level:** Value in mm or N/A if unavailable
- **VIN:** Vehicle identification number or N/A if unavailable

**Log Management Section:**
- **"Copy Complete Logs to Clipboard"** button
- Copies all app logs + system Logcat (permissions, property changes, errors)

### Application Buttons

| Button | Function |
|--------|----------|
| **Refresh** | Reconnects to VHAL and retrieves fresh data |
| **Copy** | Copies app logs + system Logcat to clipboard |
| **✕** | Closes the app |

### VHAL Properties

| Property | Type | Updates | Behavior |
|----------|------|---------|----------|
| **VIN** | Static | Only on app restart | Does not change during runtime. Value is cached when app starts. |
| **ABS** | On Change | When value changes | Updates only when ABS status actually changes (ON ↔ OFF). Clicking refresh may not update if status hasn't changed. |
| **Fuel Level** | Continuous | Continuously | Updates in real-time as fuel level changes. Most likely to update when clicking refresh. |

### Refresh Behavior

**Before Refresh:**
```
VHAL: Connected
ABS: N/A
Fuel Level: N/A
VIN: N/A
```

**During Refresh:**
```
VHAL: Refreshing...
ABS: Refreshing...
Fuel Level: Refreshing...
VIN: Refreshing...
```

**After Refresh (with data):**
```
VHAL: Connected
ABS: ON              (updates if value changed)
Fuel Level: 50 mm    (most likely to update - continuous property)
VIN: JTHBP5C2XA5034817  (updates only after app restart)
```

**After Refresh (no data):**
```
VHAL: Connected
ABS: N/A             (no update if value didn't change)
Fuel Level: N/A      (unlikely - continuous property should update)
VIN: N/A             (needs app restart to update)
```

### Updating VHAL Properties

- **Fuel Level:** Click "Refresh" - will update (continuous property)
- **ABS:** Click "Refresh" - will update only if the ON/OFF status changed since last read
- **VIN:** Must restart the entire app - static property cached at startup

---

## 📋 Viewing Logs

### Copy to Clipboard (In-App)

**Step 1:** Click the **"Copy Complete Logs to Clipboard"** button

**Step 2:** Logs are copied! Now you can paste in:
- Text editor: `Ctrl + V`
- Email/Slack/Document
- Any messaging app

**Contents:**
- All app logs (FileLogger format)
- System Logcat (CarPropertyManager, permissions, property changes)

### Log Format

Logs appear in the following format:

```
2026-07-13 14:31:33.598  8747-9720  VhalReader      com.tta.qrcodetoyota  D  Property changed! ID: 287310858, Value: true
```

**Fields:**
- **Date/Time:** 2026-07-13 14:31:33.598
- **PID-TID:** 8747-9720 (Process and Thread ID)
- **Tag:** VhalReader (Log source)
- **Package:** com.tta.qrcodetoyota
- **Level:** D (Debug), I (Info), W (Warning), E (Error)
- **Message:** Log content

---

## 📁 Log Files

### Persistent Log (Permanent)

- **Location:** `/data/data/com.tta.qrcodetoyota/files/logs/app.log`
- **Access:**
```powershell
adb pull /data/data/com.tta.qrcodetoyota/files/logs/app.log
```
- **Content:** All app logs since app first ran

### System Logs

- **Captured via:** Logcat background thread
- **Available in:** Copy to Clipboard feature
- **Contains:** CarPropertyManager events, permission errors, property changes

---

## 🔧 Useful ADB Commands

### Install app as System App (with VHAL privileges)

```powershell
# 1. Launch emulator with writable system
emulator -writable-system -avd testautomotive

# 2. In another terminal:
adb root
adb remount

# 3. Create folder and copy APK
adb shell mkdir -p /system/priv-app/QRCodetoyota
adb push app/build/outputs/apk/debug/app-debug.apk /system/priv-app/QRCodetoyota/app.apk
adb shell chmod 644 /system/priv-app/QRCodetoyota/app.apk

# 4. Reboot
adb reboot
```

### Check Emulator Status

```powershell
adb devices
```

### Capture System Logs

```powershell
adb logcat
```

### Pull Persistent Log File

```powershell
adb pull /data/data/com.tta.qrcodetoyota/files/logs/app.log
```

### Grant Permissions via ADB

```powershell
# VHAL access
adb shell pm grant com.tta.qrcodetoyota android.car.permission.CAR_DYNAMICS

# Logcat reading
adb shell pm grant com.tta.qrcodetoyota android.permission.READ_LOGS
```

---

## 🐛 Troubleshooting

### "VHAL: Connection Error"

**Cause:** No permission to access VHAL

**Solution:**
1. Install as system app (see section above)
2. Or grant permission: `adb shell pm grant com.tta.qrcodetoyota android.car.permission.CAR_DYNAMICS`
3. Restart the app

### "No Permission" appears in logs

**Cause:** Missing android.car.permission.CAR_DYNAMICS

**Solution:** Follow the steps above

### Logs not appearing in clipboard copy

**Cause:** `READ_LOGS` permission not granted

**Solution:**
```powershell
adb shell pm grant com.tta.qrcodetoyota android.permission.READ_LOGS
```

### Data stays "N/A" after refresh

**Cause:** VHAL property not available or vehicle not simulated in emulator

**Solution:**
1. Check logs for "Property changed" events
2. Ensure emulator is running automotive OS
3. Verify CarPropertyManager access via logcat

---

## 🔐 Permissions Used

```xml
<!-- VHAL Access -->
android.car.permission.CAR_DYNAMICS

<!-- Logging -->
android.permission.READ_LOGS
```

---

## 📝 Architecture

### FileLogger
- Generates logs in Logcat format
- Writes to `/data/data/com.tta.qrcodetoyota/files/logs/app.log`
- Methods: d(), i(), w(), e() for different log levels

### LogCapturer
- Captures system Logcat via `Runtime.exec("logcat")`
- Stores in memory (up to 1000 lines)
- Included when copying logs to clipboard

### VhalReader
- Manages CarPropertyManager connection
- Registers callbacks for ABS, Fuel Level, VIN
- Updates UI via callbacks when property values change

### MainActivity
- Manages UI and user interactions
- Handles refresh button (disconnect/reconnect)
- Combines app logs + system logs for clipboard export

---

## 🎯 Complete Debug Flow

```
1. Open the app
2. Check initial status (should show N/A or actual values)
3. Click "Refresh" to reconnect and get fresh data
4. Observe "Refreshing..." → "Connected" (or data values)
5. Click "Copy Complete Logs to Clipboard"
6. Paste in text editor or email
7. Check for errors/permissions in logs
8. Use 'adb pull' to get persistent app.log if needed
```

---

## 📞 Support

For issues or questions:
1. Check logs for error messages and permission denials
2. Test the ADB commands above
3. Refer to the Troubleshooting section
4. Check that emulator is running automotive OS with VHAL support

---

**Version:** 1.1  
**Last Updated:** 2026-07-13  
**Status:** Prototype - Under Development
