package com.libenh.opush.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.libenh.opush.registration.RegistrationTracker

/**
 * Hook MiPush 注册入口，记录请求并强制走系统 xmsf。
 */
object MiPushClientHook {

    private val CLASSES = listOf(
        "com.xiaomi.mipush.sdk.MiPushClient",
        "com.xiaomi.mipush.sdk.PushServiceClient"
    )

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        for (name in CLASSES) {
            val clazz = XposedHelpers.findClassIfExists(name, lpparam.classLoader) ?: continue
            try {
                XposedBridge.hookAllMethods(clazz, "registerPush", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val pkg = lpparam.packageName
                        XposedBridge.log("[OPush][$pkg] registerPush BEFORE args=${param.args?.size}")
                        RegistrationTracker.onRegisterRequested(pkg)

                        // 尝试把目标服务指向 xmsf（若 SDK 有相关字段/方法）
                        forceSystemChannel(param, lpparam)
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("[OPush][${lpparam.packageName}] registerPush AFTER")
                        RegistrationTracker.onRegisterInvoked(lpparam.packageName)
                    }
                })

                // 兼容其他初始化入口
                for (m in listOf("register", "initialize", "enablePush")) {
                    try {
                        XposedBridge.hookAllMethods(clazz, m, object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                XposedBridge.log("[OPush][${lpparam.packageName}] $name.$m called")
                                RegistrationTracker.onRegisterRequested(lpparam.packageName)
                            }
                        })
                    } catch (_: Throwable) {}
                }

                XposedBridge.log("[OPush] hooked $name in ${lpparam.packageName}")
            } catch (t: Throwable) {
                XposedBridge.log("[OPush] MiPushClientHook $name: ${t.message}")
            }
        }
    }

    private fun forceSystemChannel(param: XC_MethodHook.MethodHookParam, lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 若第一个参数是 Context，记录
            val ctx = param.args?.firstOrNull() as? Context
            if (ctx != null) {
                RegistrationTracker.bindContext(lpparam.packageName, ctx)
            }
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] forceSystemChannel: ${t.message}")
        }
    }
}
