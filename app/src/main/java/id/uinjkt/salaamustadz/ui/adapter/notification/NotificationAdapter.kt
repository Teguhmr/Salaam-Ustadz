package id.uinjkt.salaamustadz.ui.adapter.notification

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.uinjkt.salaamustadz.databinding.ItemListNotificationBinding
import id.uinjkt.salaamustadz.ui.notification.DetailNotificationActivity
import id.uinjkt.salaamustadz.ui.notification.NotificationItem
import id.uinjkt.salaamustadz.ui.prayer.ReminderScheduler
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.MyNotification
import id.uinjkt.salaamustadz.utils.MyNotification.ARTICLE
import id.uinjkt.salaamustadz.utils.MyNotification.DZIKIR
import id.uinjkt.salaamustadz.utils.MyNotification.PRAYER_TIME
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.subtractMinutes
import id.uinjkt.salaamustadz.utils.toast

class NotificationAdapter(private val context: Context, private val notificationItems: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private val sharedPreference = PreferenceManager(context)
    private val reminderHandler = ReminderScheduler(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notificationItems[position]
        holder.notificationText.text = item.text
        holder.notificationSwitch.isChecked = item.toggled

        holder.itemViewConstraint.setOnClickListener {
            val intent = Intent(context, DetailNotificationActivity::class.java)
            intent.putExtra(Constants.KEY_NOTIFICATION_NAME, MyNotification.getType(item.text).title)
            context.startActivity(intent)
        }


        holder.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            item.toggled = isChecked

            when (MyNotification.getType(item.text)){
                ARTICLE -> sharedPreference.putBoolean(Constants.KEY_ARTICLE_ENABLED, isChecked)
                DZIKIR -> sharedPreference.putBoolean(Constants.KEY_DZIKIR_ENABLED, isChecked)
                PRAYER_TIME -> {
                    if (sharedPreference.getString(Constants.KEY_PRAYER_SCHEDULE) == null){
                        context.toast("Silahkan ke menu jadwal shalat terlebih dahulu")
                        holder.notificationSwitch.isChecked = !isChecked
                    } else {
                        sharedPreference.putBoolean(Constants.KEY_REMINDERS_ENABLED, isChecked)
                        if (reminderHandler.isRemindersEnabled()){
                            setReminderPrayer()
                        } else {
                            cancelReminderPrayer()
                        }
                    }

                }

            }
        }

    }

    private fun cancelReminderPrayer(){
        val getJsonString = sharedPreference.getString(Constants.KEY_REMINDERS_TIME)

        if (getJsonString != null){
            val mapType = object : TypeToken<ArrayList<Int>>() {}.type
            val retrievedMap: ArrayList<Int> = Gson().fromJson(getJsonString, mapType)

            for (time in retrievedMap) {
                reminderHandler.cancelRemindersNotification(time)

            }
        }
    }

    private fun setReminderPrayer(){
        val getJsonString = sharedPreference.getString(Constants.KEY_PRAYER_SCHEDULE)
        val city = sharedPreference.getString(Constants.KEY_CITY)

        // Your code here
        if (getJsonString != null && reminderHandler.isRemindersEnabled()){
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            val retrievedMap: Map<String, String> = Gson().fromJson(getJsonString, mapType)
            val remainingTime = sharedPreference.getInt(Constants.KEY_NOTIFICATION_TIME)

            for ((prayer, time) in retrievedMap) {
                val reminderMessage =  "$remainingTime menit menuju $prayer pada $time. ($city)"
                reminderHandler.scheduleReminderWithNotification(subtractMinutes(time, remainingTime), reminderMessage)
            }
        }
    }

    override fun getItemCount(): Int = notificationItems.size

    inner class ViewHolder(binding: ItemListNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        val notificationText: TextView = binding.notificationText
        val notificationSwitch: MaterialSwitch = binding.notificationSwitch
        val itemViewConstraint: ConstraintLayout = binding.itemViewConstraint

    }
}
