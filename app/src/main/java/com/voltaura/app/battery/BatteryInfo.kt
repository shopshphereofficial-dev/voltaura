package com.voltaura.app.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlin.math.abs

data class BatteryReading(
    val levelPercent: Int,
    val voltageMv: Int,
    val currentUa: Long,
    val watts: Double,
    val isCharging: Boolean,
    val temperatureC: Double,
    val source: String,
)

object BatteryReader {

    fun read(context: Context): BatteryReading {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intent: Intent? =
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1

        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0

        var currentUa = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).toLong()
        if (currentUa == Int.MIN_VALUE.toLong() || currentUa == -2147483648L) currentUa = 0L

        val watts = (voltageMv / 1000.0) * (abs(currentUa) / 1_000_000.0)

        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val source = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "None"
        }

        return BatteryReading(pct, voltageMv, currentUa, watts, charging, tempTenths / 10.0, source)
    }
}
