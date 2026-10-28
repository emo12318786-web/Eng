# EDA Emergency Link 0.3

Android emergency helper prototype for testing on a personal device. It is not a certified rescue system.

## Included in this build
- SOS text prepared with timestamp and available location text, shared through Android's share sheet.
- Current active network type and Android's validated-Internet status.
- Permission-gated display of the last known GPS location.
- Basic nearby Wi-Fi scan display and shortcut to Android Wi-Fi settings.

## Important limitations
- The share sheet does not prove a message was sent or delivered.
- No automatic satellite access, satellite messaging, offline phone-to-phone mesh, automatic SMS transmission, persistent SOS retry queue, or rescue-centre integration.
- The app cannot discover Wi-Fi passwords or bypass access controls.
- Wi-Fi does not guarantee Internet access. Location may be unavailable or inaccurate indoors.
- Test this prototype before relying on it in an emergency. Keep official emergency contacts and offline maps available.

## Build
GitHub Actions builds a debug APK on pushes to the default branch and via manual workflow dispatch. After a successful run, download the `emergency-link-debug-apk` artifact from the workflow run.
