package com.muzamil.reminder.scheduling

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.muzamil.reminder.MainActivity
import com.muzamil.reminder.R
import com.muzamil.reminder.data.ReminderEntity
import kotlin.math.absoluteValue

object NotificationHelper {
    private const val BASE_CHANNEL_ID = "reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(BASE_CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Time-sensitive reminder notifications"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun soundUri(context: Context, reminder: ReminderEntity): Uri =
        SoundCatalog.resolveUri(context, reminder.soundKey, reminder.customSoundUri)

    private fun channelFor(context: Context, reminder: ReminderEntity, sound: Uri): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return BASE_CHANNEL_ID
        val id = "reminder_${(sound.toString() + reminder.vibrationEnabled).hashCode().absoluteValue}"
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(id) == null) {
            val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build()
            val channel = NotificationChannel(id, "Reminder alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminder alert sound"
                setSound(sound, attrs)
                enableVibration(reminder.vibrationEnabled)
                if (reminder.vibrationEnabled) vibrationPattern = longArrayOf(0, 250, 180, 250)
            }
            manager.createNotificationChannel(channel)
        }
        return id
    }

    fun show(context: Context, reminder: ReminderEntity, alertId: Long = 0L) {
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val sound = soundUri(context, reminder)
        val channelId = channelFor(context, reminder, sound)
        val openIntent = PendingIntent.getActivity(
            context, reminder.id.toInt(), Intent(context, MainActivity::class.java).putExtra("reminderId", reminder.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snooze = actionIntent(context, reminder.id, "SNOOZE", reminder.snoozeDurationMinutes)
        val complete = actionIntent(context, reminder.id, "COMPLETE", 0)
        val stop = actionIntent(context, reminder.id, "STOP", 0)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(if (reminder.priority == "IMPORTANT" || reminder.priority == "URGENT") "IMPORTANT REMINDER" else "REMINDER")
            .setContentText(reminder.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.description.ifBlank { reminder.title }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setContentIntent(openIntent)
            .addAction(0, "SNOOZE ${reminder.snoozeDurationMinutes} MIN", snooze)
            .addAction(0, "COMPLETE", complete)
            .addAction(0, "STOP", stop)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(sound).setVibrate(if (reminder.vibrationEnabled) longArrayOf(0, 250, 180, 250) else longArrayOf(0))
        }
        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), builder.build())
    }

    private fun actionIntent(context: Context, reminderId: Long, action: String, minutes: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action; putExtra("reminderId", reminderId); putExtra("minutes", minutes)
        }
        val request = (reminderId.hashCode() * 31 + action.hashCode()) and Int.MAX_VALUE
        return PendingIntent.getBroadcast(context, request, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
