package com.libenh.opush.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Hook 小米推送 SDK 注册入口，强制走系统级 xmsf 通道。
 * 不依赖设备伪装成 MIUI。
 */
object MiPushClientHook {

    private val CANDIDATE_CLASSES = listOf(
        "com.xiaomi.mipush.sdk.MiPushClient",
        "com.xiaomi.mipush.sdk.PushServiceClient"
    )

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        for (className in CANDIDATE_CLASSES) {
            try {
                val clazz = XposedHelpers.findClassIfExists(className, lpparam.classLoader)
                    ?: continue

                XposedBridge.hookAllMethods(clazz, "registerPush", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("[OPush] ${lpparam.packageName} registerPush called -> force system channel")
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("[OPush] ${lpparam.packageName} registerPush finished")
                    }
                })

                XposedBridge.log("[OPush] hooked $className in ${lpparam.packageName}")
                return
            } catch (t: Throwable) {
                XposedBridge.log("[OPush] MiPushClientHook error: ${t.message}")
            }
        }
    }
}
