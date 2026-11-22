package com.libenh.opush.coloros

import android.os.Build
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * ColorOS / OPlus 环境检测与兼容逻辑。
 * ColorOS 17 对 bindService、后台启动、进程冻结更严格。
 */
object ColorOSCompat {

    @Volatile
    private var detected = false

    @Volatile
    var isOPlus = false
        private set

    @Volatile
    var colorOsVersion: String? = null
        private set

    fun detectAndLog() {
        if (detected) return
        detected = true

        try {
            val props = listOf(
                "ro.oplus.version",
                "ro.oppo.version",
                "ro.build.version.oplusrom",
                "ro.build.version.opporom",
                "ro.product.brand",
                "ro.product.manufacturer"
            )
            val values = props.associateWith { getProp(it) }
            isOPlus = values.any { (k, v) ->
                v.isNotBlank() && (
                    k.contains("oplus") || k.contains("oppo") ||
                    v.contains("ColorOS", true) ||
                    v.contains("OxygenOS", true) ||
                    listOf("OPPO", "OnePlus", "realme").any { brand ->
                        values["ro.product.brand"]?.contains(brand, true) == true ||
                        values["ro.product.manufacturer"]?.contains(brand, true) == true
                    }
                )
            }

            colorOsVersion = values["ro.build.version.oplusrom"]
                ?: values["ro.build.version.opporom"]
                ?: values["ro.oplus.version"]

            XposedBridge.log("[OPush] isOPlus=$isOPlus colorOsVersion=$colorOsVersion sdk=${Build.VERSION.SDK_INT}")
            values.filter { it.value.isNotBlank() }.forEach { (k, v) ->
                XposedBridge.log("[OPush] prop $k=$v")
            }
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] ColorOS detect failed: ${t.message}")
        }
    }

    private fun getProp(key: String): String {
        return try {
            val clz = Class.forName("android.os.SystemProperties")
            XposedHelpers.callStaticMethod(clz, "get", key, "") as? String ?: ""
        } catch (_: Throwable) {
            ""
        }
    }

    /** ColorOS 建议优先 startService 而不是 bindService */
    fun preferStartService(): Boolean {
        detectAndLog()
        return isOPlus
    }
}
