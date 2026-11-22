package com.libenh.opush.hooks

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 禁用/拦截应用自己的 XMPushService，避免双通道冲突。
 */
object XMPushServiceDisableHook {

    private const val SERVICE = "com.xiaomi.push.service.XMPushService"

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(SERVICE, lpparam.classLoader) ?: return

        try {
            XposedBridge.hookAllMethods(clazz, "onCreate", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("[OPush][${lpparam.packageName}] block XMPushService.onCreate -> stopSelf")
                    try {
                        (param.thisObject as? Service)?.stopSelf()
                    } catch (_: Throwable) {}
                }
            })

            XposedBridge.hookAllMethods(clazz, "onStartCommand", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("[OPush][${lpparam.packageName}] block XMPushService.onStartCommand")
                    param.result = Service.START_NOT_STICKY
                }
            })

            XposedBridge.hookAllMethods(clazz, "onBind", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("[OPush][${lpparam.packageName}] block XMPushService.onBind")
                    param.result = null
                }
            })

            XposedBridge.log("[OPush] XMPushService blocked for ${lpparam.packageName}")
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] XMPushServiceDisableHook: ${t.message}")
        }
    }

    fun disableComponent(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            val cn = ComponentName(packageName, SERVICE)
            pm.setComponentEnabledSetting(
                cn,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            XposedBridge.log("[OPush] component disabled: $packageName/$SERVICE")
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] disableComponent: ${t.message}")
        }
    }
}
