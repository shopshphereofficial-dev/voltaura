package com.voltaura.app.data

import android.content.Context

class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("voltaura", Context.MODE_PRIVATE)

    var chargingEnabled: Boolean
        get() = sp.getBoolean("charging_enabled", false)
        set(v) { sp.edit().putBoolean("charging_enabled", v).apply() }

    var chargingStyle: Int
        get() = sp.getInt("charging_style", 0)
        set(v) { sp.edit().putInt("charging_style", v).apply() }

    var glowEnabled: Boolean
        get() = sp.getBoolean("glow_enabled", false)
        set(v) { sp.edit().putBoolean("glow_enabled", v).apply() }

    var glowStyle: Int
        get() = sp.getInt("glow_style", 0)
        set(v) { sp.edit().putInt("glow_style", v).apply() }

    var glowColor: Int
        get() = sp.getInt("glow_color", 0xFF00E5FF.toInt())
        set(v) { sp.edit().putInt("glow_color", v).apply() }

    var glowWidth: Int
        get() = sp.getInt("glow_width", 26)
        set(v) { sp.edit().putInt("glow_width", v).apply() }

    var glowDuration: Int
        get() = sp.getInt("glow_duration", 2500)
        set(v) { sp.edit().putInt("glow_duration", v).apply() }
}
