package com.libenh.opush

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.libenh.opush.hooks.MiPushClientHook
import com.libenh.opush.hooks.XMPushServiceDisableHook
import com.libenh.opush.coloros.ColorOSCompat

class OModule : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val pkg = lpparam.packageName

        if (pkg == "com.libenh.opush") return

        XposedBridge.log("[OPush] load: $pkg")

        ColorOSCompat.detectAndLog()

        if (pkg != "android" && pkg != "com.xiaomi.xmsf") {
            try {
                MiPushClientHook.hook(lpparam)
                XMPushServiceDisableHook.hook(lpparam)
            } catch (t: Throwable) {
                XposedBridge.log("[OPush] hook failed in $pkg: ${t.message}")
            }
        }

        if (pkg == "com.xiaomi.xmsf") {
            XposedBridge.log("[OPush] xmsf loaded, ready for runtime hooks")
        }
    }
}
