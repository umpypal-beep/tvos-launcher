@echo off
REM Tier 1 debloat (reversible). Undo: adb shell cmd package install-existing <pkg>
for %%P in (jp.co.sharp.av.android.storedemoapplication com.mediatek.tv.retaildemo jp.co.sharp.av.android.epopdemo azan.android.av.sharp.co.jp jp.co.sharp.av.android.asia.webemanual com.Smartdevice.emanual com.smartdevice.recommendation com.android.adservices.api) do adb shell pm uninstall -k --user 0 %%P
