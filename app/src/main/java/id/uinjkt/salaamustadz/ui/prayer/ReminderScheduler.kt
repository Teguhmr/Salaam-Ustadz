package id.uinjkt.salaamustadz.ui.prayer

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ParseException
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.getTimeInMillisForSpecificTime
import timber.log.Timber
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val sharedPreferences = PreferenceManager(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun enableReminders() {
        sharedPreferences.putBoolean(Constants.KEY_REMINDERS_ENABLED, true)
    }

    fun disableReminders() {
        sharedPreferences.putBoolean(Constants.KEY_REMINDERS_ENABLED, false)
    }

    fun cancelRemindersNotification(requestCode: Int = 0){
        // Cancel any scheduled reminders when disabling
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    fun isRemindersEnabled(): Boolean {
        return sharedPreferences.getBoolean(Constants.KEY_REMINDERS_ENABLED, true)
    }

    @SuppressLint("SimpleDateFormat")
    fun scheduleReminderWithNotification(eventTimes: String, reminderMessage: String) {
        val listTimeReminder = ArrayList<Int>()
        try {
            val alarmTimeMillis = getTimeInMillisForSpecificTime(eventTimes)
            val daysToRepeat = mutableListOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
            )

            for (i in 0 until daysToRepeat.size) {
                val day = daysToRepeat[i]
                val dayAlarmTimeMillis =
                    alarmTimeMillis + (day - Calendar.MONDAY) * 24 * 60 * 60 * 1000 // Adjust for the specific day
                val currentTimeMillis = System.currentTimeMillis()

                if (dayAlarmTimeMillis > currentTimeMillis) {
                    val intent = Intent(context, ReminderReceiver::class.java)
                    val uniqueId = "${reminderMessage.hashCode()}$day"
                    val notificationId = uniqueId.hashCode()

                    val message = when {
                        i >= 4 -> "Ketuk untuk perbarui. ".plus(reminderMessage)// Change message for days 4 and beyond
                        else -> reminderMessage // Use the default message
                    }

                    intent.putExtra("notification_message", message)

                    listTimeReminder.add(notificationId)

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        when {
                            alarmManager.canScheduleExactAlarms() -> {
                                // permission granted
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    dayAlarmTimeMillis,
                                    pendingIntent
                                )

                            }

                        }
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            dayAlarmTimeMillis,
                            pendingIntent
                        )
                    }

                    Timber.tag("time").d(dayAlarmTimeMillis.toString())
                    Timber.tag("time").d(message)
                }
            }

            val storedTimeReminders: ArrayList<Int> =
                Gson().fromJson(sharedPreferences.getString(Constants.KEY_REMINDERS_TIME),
                    object : TypeToken<ArrayList<Int>>() {}.type
                ) ?: ArrayList()

            storedTimeReminders.addAll(listTimeReminder)
            val uniqueTimeReminders = LinkedHashSet(storedTimeReminders)

            val jsonString = Gson().toJson(uniqueTimeReminders.toList())
            sharedPreferences.putString(Constants.KEY_REMINDERS_TIME, jsonString)

        } catch (e: ParseException) {
            e.printStackTrace()
        }



    }

}

