package com.libenh.opush.hooks

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 尝试禁用应用自己的 XMPushService，避免与系统级 xmsf 冲突。
 */
object XMPushServiceDisableHook {

    private const val SERVICE_NAME = "com.xiaomi.push.service.XMPushService"

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val serviceClass = XposedHelpers.findClassIfExists(SERVICE_NAME, lpparam.classLoader)
            if (serviceClass == null) return

            XposedBridge.hookAllMethods(serviceClass, "onCreate", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("[OPush] block ${lpparam.packageName} XMPushService.onCreate")
                }
            })

            XposedBridge.log("[OPush] XMPushService hooks installed for ${lpparam.packageName}")
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] XMPushServiceDisableHook: ${t.message}")
        }
    }

    fun disableComponent(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            val cn = ComponentName(packageName, SERVICE_NAME)
            pm.setComponentEnabledSetting(
                cn,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            XposedBridge.log("[OPush] disabled XMPushService component for $packageName")
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] disableComponent failed: ${t.message}")
        }
    }
}
