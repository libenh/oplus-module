package com.libenh.opush.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 绕过 SDK 内部对 MIUI / xmsf 存在性的检查，减少因「不是 MIUI」而走应用自建通道。
 */
object ManifestCheckerHook {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val names = listOf(
            "com.xiaomi.mipush.sdk.ManifestChecker",
            "com.xiaomi.channel.commonutils.android.MIUIUtils",
            "com.xiaomi.mipush.sdk.MIUIUtils"
        )
        for (name in names) {
            val clazz = XposedHelpers.findClassIfExists(name, lpparam.classLoader) ?: continue
            try {
                // isMIUI / check 类方法：尽量让其认为可走系统推送
                for (method in listOf("isMIUI", "isMiuiRom", "shouldUseMIUIPush", "check")) {
                    try {
                        XposedBridge.hookAllMethods(clazz, method, object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                // 对 boolean 返回值的 isMIUI 类：强制 true（使 SDK 走系统通道）
                                if (method.startsWith("is") || method.startsWith("should")) {
                                    param.result = true
                                    XposedBridge.log("[OPush][${lpparam.packageName}] $name.$method -> true")
                                }
                            }
                        })
                    } catch (_: Throwable) {}
                }
                XposedBridge.log("[OPush] ManifestChecker/MIUIUtils hooked in ${lpparam.packageName}")
            } catch (t: Throwable) {
                XposedBridge.log("[OPush] ManifestCheckerHook $name: ${t.message}")
            }
        }
    }
}
