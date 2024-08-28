package id.uinjkt.salaamustadz.ui.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!isRemindersEnabled(context)) {
            return
        }

        // Retrieve the notification message from the intent
        val message = intent?.getStringExtra("notification_message")

        // Create an intent to start the NotificationService
        val notificationServiceIntent = Intent(context, NotificationService::class.java)
        notificationServiceIntent.putExtra("notification_message", message)

        // Start the NotificationService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(notificationServiceIntent)
        } else {
            context.startService(notificationServiceIntent)
        }

    }

    private fun isRemindersEnabled(context: Context): Boolean {
        val sharedPreferences = PreferenceManager(context)
        return sharedPreferences.getBoolean(Constants.KEY_REMINDERS_ENABLED, true)
    }
}
