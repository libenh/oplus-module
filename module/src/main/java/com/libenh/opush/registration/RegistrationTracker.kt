package com.libenh.opush.registration

import android.content.Context
import android.os.Handler
import android.os.Looper
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.ConcurrentHashMap

/**
 * 记录注册请求，超时无回传则打日志提醒（解决「一点反应都没有」）。
 */
object RegistrationTracker {

    private const val TIMEOUT_MS = 15_000L

    private val requestedAt = ConcurrentHashMap<String, Long>()
    private val contexts = ConcurrentHashMap<String, Context>()
    private val handler = Handler(Looper.getMainLooper())

    fun attach(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 预留：可挂接注册结果回调
        XposedBridge.log("[OPush] RegistrationTracker attached for ${lpparam.packageName}")
    }

    fun bindContext(pkg: String, ctx: Context) {
        contexts[pkg] = ctx.applicationContext
    }

    fun onRegisterRequested(pkg: String) {
        val now = System.currentTimeMillis()
        requestedAt[pkg] = now
        XposedBridge.log("[OPush][$pkg] register REQUESTED at $now")

        handler.postDelayed({
            val t = requestedAt[pkg] ?: return@postDelayed
            if (t == now) {
                // 超时仍未清除
                XposedBridge.log("[OPush][$pkg] !!! REGISTER TIMEOUT ${TIMEOUT_MS}ms no response !!!")
                XposedBridge.log("[OPush][$pkg] 建议: 1)确认已安装并启动 xmsf  2)作用域包含该应用+xmsf  3)查看 xmsf 连接状态")
            }
        }, TIMEOUT_MS)
    }

    fun onRegisterInvoked(pkg: String) {
        XposedBridge.log("[OPush][$pkg] register method returned")
    }

    fun onRegisterSuccess(pkg: String, regId: String?) {
        requestedAt.remove(pkg)
        XposedBridge.log("[OPush][$pkg] REGISTER SUCCESS regId=$regId")
    }

    fun onRegisterFailed(pkg: String, reason: String?) {
        requestedAt.remove(pkg)
        XposedBridge.log("[OPush][$pkg] REGISTER FAILED reason=$reason")
    }
}
