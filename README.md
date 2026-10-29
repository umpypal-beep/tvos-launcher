# tvos-launcher
Apple TV style custom launcher for Skyworth SWTV-24AE-4K (Android 14).
Push to GitHub -> Actions tab -> download `tvos-launcher-apk` artifact.
Install: `adb install -r app-debug.apk`
Set default: `adb shell cmd package set-home-activity com.tvos.launcher/.MainActivity`
