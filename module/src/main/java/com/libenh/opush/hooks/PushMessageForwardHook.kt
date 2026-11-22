package com.libenh.opush.hooks

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.libenh.opush.coloros.ColorOSCompat

/**
 * ColorOS 17：优先 startService，避开 bindService 异常。
 * 在应用与 xmsf 两侧尝试干预消息传递路径。
 */
object PushMessageForwardHook {

    fun hookApp(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook Context.startService / bindService，对指向 mipush 相关的 Intent 做处理
        try {
            val ctxClazz = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader)

            XposedBridge.hookAllMethods(ctxClazz, "bindService", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!ColorOSCompat.preferStartService()) return
                    val intent = param.args?.getOrNull(0) as? Intent ?: return
                    if (!isMiPushRelated(intent)) return

                    XposedBridge.log("[OPush][${lpparam.packageName}] bindService -> redirect startService for MiPush")
                    try {
                        val ctx = param.thisObject as Context
                        // 尝试改为 startService
                        if (Build.VERSION.SDK_INT >= 26) {
                            ctx.startForegroundService(intent)
                        } else {
                            ctx.startService(intent)
                        }
                        // 让原 bind 失败，避免双通道
                        param.result = false
                    } catch (t: Throwable) {
                        XposedBridge.log("[OPush] redirect bind->start fail: ${t.message}")
                    }
                }
            })
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] PushMessageForwardHook.app: ${t.message}")
        }
    }

    fun hookXmsf(lpparam: XC_LoadPackage.LoadPackageParam) {
        // xmsf 侧：转发到目标应用时优先 startService
        val candidates = listOf(
            "com.xiaomi.push.sdk.MyPushMessageHandler",
            "com.xiaomi.push.service.MyMIPushNotificationHelper",
            "com.xiaomi.push.service.MIPushEventProcessor"
        )
        for (name in candidates) {
            val clazz = XposedHelpers.findClassIfExists(name, lpparam.classLoader) ?: continue
            try {
                XposedBridge.hookAllMethods(clazz, "startService", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("[OPush][xmsf] $name.startService")
                    }
                })
            } catch (_: Throwable) {}
        }

        // Hook Context.startService 从 xmsf 发出的
        try {
            val ctxClazz = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader)
            XposedBridge.hookAllMethods(ctxClazz, "startService", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args?.getOrNull(0) as? Intent ?: return
                    if (isMiPushRelated(intent)) {
                        XposedBridge.log("[OPush][xmsf] startService -> ${intent.component ?: intent.action}")
                    }
                }
            })
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] xmsf startService hook: ${t.message}")
        }
    }

    private fun isMiPushRelated(intent: Intent): Boolean {
        val cn = intent.component
        val action = intent.action ?: ""
        val pkg = cn?.packageName ?: intent.`package` ?: ""
        return pkg.contains("xiaomi") ||
            action.contains("mipush", true) ||
            action.contains("xiaomi", true) ||
            (cn?.className?.contains("PushMessageHandler") == true) ||
            (cn?.className?.contains("XMPushService") == true)
    }
}
