package com.libenh.opush

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.libenh.opush.hooks.MiPushClientHook
import com.libenh.opush.hooks.XMPushServiceDisableHook
import com.libenh.opush.hooks.PushMessageForwardHook
import com.libenh.opush.hooks.ManifestCheckerHook
import com.libenh.opush.coloros.ColorOSCompat
import com.libenh.opush.registration.RegistrationTracker

class OModule : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val pkg = lpparam.packageName
        if (pkg == "com.libenh.opush") return

        XposedBridge.log("[OPush] load: $pkg")
        ColorOSCompat.detectAndLog()

        when (pkg) {
            "android" -> {
                // system_server: 预留
            }
            "com.xiaomi.xmsf" -> {
                XposedBridge.log("[OPush] xmsf runtime hooks")
                try {
                    PushMessageForwardHook.hookXmsf(lpparam)
                } catch (t: Throwable) {
                    XposedBridge.log("[OPush] xmsf hook fail: ${t.message}")
                }
            }
            else -> {
                // 目标应用
                try {
                    MiPushClientHook.hook(lpparam)
                    XMPushServiceDisableHook.hook(lpparam)
                    ManifestCheckerHook.hook(lpparam)
                    PushMessageForwardHook.hookApp(lpparam)
                    RegistrationTracker.attach(lpparam)
                } catch (t: Throwable) {
                    XposedBridge.log("[OPush] app hooks fail in $pkg: ${t.message}")
                }
            }
        }
    }
}
