package id.uinjkt.salaamustadz.ui.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.subtractMinutes

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val reminderScheduler = ReminderScheduler(context)
        val preferenceManager = PreferenceManager(context)

        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            val getJsonString = preferenceManager.getString(Constants.KEY_PRAYER_SCHEDULE)
            val city = preferenceManager.getString(Constants.KEY_CITY)

            // Your code here
            if (getJsonString != null && reminderScheduler.isRemindersEnabled()){
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                val retrievedMap: Map<String, String> = Gson().fromJson(getJsonString, mapType)
                val remainingTime = preferenceManager.getInt(Constants.KEY_NOTIFICATION_TIME)

                for ((prayer, time) in retrievedMap) {
                    val reminderMessage =  "$remainingTime menit menuju $prayer pada $time. ($city)"
                    reminderScheduler.scheduleReminderWithNotification(subtractMinutes(time, remainingTime), reminderMessage)
                }
            }
        }
    }
}