package com.muzamil.reminder.scheduling

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.muzamil.reminder.R

data class BuiltInSound(val key: String, val name: String, val rawRes: Int)
data class DeviceTone(val name: String, val uri: Uri)

object SoundCatalog {
    private const val URI_PREFIX = "uri:"

    val builtIns = listOf(
        BuiltInSound("classic_bell", "Classic Bell", R.raw.classic_bell),
        BuiltInSound("gentle_chime", "Gentle Chime", R.raw.gentle_chime),
        BuiltInSound("morning_bell", "Morning Bell", R.raw.morning_bell),
        BuiltInSound("soft_piano", "Soft Piano", R.raw.soft_piano),
        BuiltInSound("elegant", "Elegant", R.raw.elegant),
        BuiltInSound("digital_alert", "Digital Alert", R.raw.digital_alert),
        BuiltInSound("important_alert", "Important Alert", R.raw.important_alert),
        BuiltInSound("bright_chime", "Bright Chime", R.raw.bright_chime),
        BuiltInSound("clock_reminder", "Clock Reminder", R.raw.clock_reminder),
        BuiltInSound("strong_reminder", "Strong Reminder", R.raw.strong_reminder)
    )

    fun rawResFor(key: String): Int = builtIns.firstOrNull { it.key == key }?.rawRes ?: R.raw.classic_bell

    fun keyForUri(uri: Uri): String = URI_PREFIX + uri.toString()
    fun uriFromKey(key: String): Uri? = if (key.startsWith(URI_PREFIX)) runCatching { Uri.parse(key.removePrefix(URI_PREFIX)) }.getOrNull() else null

    fun resolveUri(context: Context, soundKey: String, customSoundUri: String?): Uri {
        customSoundUri?.let { runCatching { return Uri.parse(it) } }
        uriFromKey(soundKey)?.let { return it }
        return Uri.parse("android.resource://${context.packageName}/${rawResFor(soundKey)}")
    }

    fun displayName(context: Context, soundKey: String, customSoundUri: String?): String {
        val custom = customSoundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
        if (custom != null) {
            return runCatching { RingtoneManager.getRingtone(context, custom)?.getTitle(context) }.getOrNull().orEmpty().ifBlank { "My Sound" }
        }
        uriFromKey(soundKey)?.let { uri ->
            return runCatching { RingtoneManager.getRingtone(context, uri)?.getTitle(context) }.getOrNull().orEmpty().ifBlank { "Phone tone" }
        }
        return builtIns.firstOrNull { it.key == soundKey }?.name ?: "Classic Bell"
    }

    fun deviceTones(context: Context): List<DeviceTone> {
        val seen = linkedSetOf<String>()
        val result = mutableListOf<DeviceTone>()
        val types = listOf(RingtoneManager.TYPE_NOTIFICATION, RingtoneManager.TYPE_ALARM, RingtoneManager.TYPE_RINGTONE)
        types.forEach { type ->
            runCatching {
                val manager = RingtoneManager(context).apply { setType(type) }
                val cursor = manager.cursor
                cursor.use {
                    var position = 0
                    while (it.moveToNext()) {
                        val uri = manager.getRingtoneUri(position++) ?: continue
                        if (!seen.add(uri.toString())) continue
                        val title = runCatching { RingtoneManager.getRingtone(context, uri)?.getTitle(context) }.getOrNull().orEmpty().ifBlank { "Phone tone" }
                        result += DeviceTone(title, uri)
                    }
                }
            }
        }
        return result.sortedBy { it.name.lowercase() }
    }
}
