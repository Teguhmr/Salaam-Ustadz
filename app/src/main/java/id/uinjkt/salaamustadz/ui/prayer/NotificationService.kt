package id.uinjkt.salaamustadz.ui.prayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.NotificationType
import id.uinjkt.salaamustadz.utils.PreferenceManager

class NotificationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("notification_message")

        // Create a notification channel if not already created
        val channelId = "prayer_time_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Time",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.jadwal_sholat)
            .setContentTitle("SalaamUstadz: Upcoming Prayer Reminder")
            .setContentText(message)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND) // You can also add sound and lights
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val sharedPreferences = PreferenceManager(applicationContext)
        if (sharedPreferences.getString(Constants.KEY_NOTIFICATION_TYPE_PRAYER) == (NotificationType.NOTIFICATION_VIBRATE.title)){
            // Vibrate the device
            @Suppress("DEPRECATION") val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(1000)
            }
        }

        // Create a pending intent to open the app when the notification is tapped
        val resultIntent = Intent(this, PrayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, builder.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, builder.build(), FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()

        } else {
            startForeground(1, builder.build())
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }


        return START_STICKY
    }
}
