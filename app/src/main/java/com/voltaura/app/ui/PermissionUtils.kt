package com.voltaura.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object PermissionUtils {

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun openOverlaySettings(context: Context) {
        val i = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.packageName)
        )
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }

    fun openNotificationListenerSettings(context: Context) {
        val i = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        ) ?: return false
        return enabled.contains(context.packageName)
    }
}
