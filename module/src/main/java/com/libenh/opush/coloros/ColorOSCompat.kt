package com.libenh.opush.coloros

import android.os.Build
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object ColorOSCompat {

    @Volatile private var detected = false

    @Volatile var isOPlus = false
        private set

    @Volatile var colorOsVersion: String? = null
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
                "ro.product.manufacturer",
                "ro.product.system.brand"
            )
            val values = props.associateWith { getProp(it) }

            val brand = (values["ro.product.brand"] + values["ro.product.manufacturer"] + values["ro.product.system.brand"]).lowercase()
            isOPlus = brand.contains("oppo") || brand.contains("oneplus") || brand.contains("realme") ||
                values.any { (k, v) ->
                    v.isNotBlank() && (k.contains("oplus") || k.contains("oppo") ||
                        v.contains("ColorOS", true) || v.contains("OxygenOS", true))
                }

            colorOsVersion = values["ro.build.version.oplusrom"]
                ?: values["ro.build.version.opporom"]
                ?: values["ro.oplus.version"]

            XposedBridge.log("[OPush] isOPlus=$isOPlus colorOs=$colorOsVersion sdk=${Build.VERSION.SDK_INT}")
            values.filter { it.value.isNotBlank() }.forEach { (k, v) ->
                XposedBridge.log("[OPush] prop $k=$v")
            }
        } catch (t: Throwable) {
            XposedBridge.log("[OPush] ColorOS detect: ${t.message}")
        }
    }

    private fun getProp(key: String): String = try {
        val clz = Class.forName("android.os.SystemProperties")
        XposedHelpers.callStaticMethod(clz, "get", key, "") as? String ?: ""
    } catch (_: Throwable) {
        ""
    }

    /** O系优先 startService */
    fun preferStartService(): Boolean {
        detectAndLog()
        return isOPlus
    }
}
