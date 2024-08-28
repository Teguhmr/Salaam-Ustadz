package id.uinjkt.salaamustadz.ui.adapter.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.ItemNotifSettingBinding
import id.uinjkt.salaamustadz.ui.notification.ListItemNotification
import id.uinjkt.salaamustadz.ui.prayer.ReminderScheduler
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.subtractMinutes

class NotificationTimeAdapter(context: Context, private val items: List<ListItemNotification>) :
    RecyclerView.Adapter<NotificationTimeAdapter.ViewHolder>() {
    private val sharedPreference =  PreferenceManager(context)
    private val reminderHandler = ReminderScheduler(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNotifSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.radioButton.isChecked = item.selected

        holder.radioButton.isChecked =
            item.text == sharedPreference.getInt(Constants.KEY_NOTIFICATION_TIME).toString()

        if (!sharedPreference.getBoolean(Constants.KEY_REMINDERS_ENABLED)){
            holder.itemView.apply {
                isEnabled = false
                foreground = ContextCompat.getDrawable(context, R.color.white_transparent)
            }
        }

        holder.tvName.text = item.text.plus(" Menit")
        holder.itemView.setOnClickListener {
            // Update the selected state of the clicked item
            item.selected = true

            if (reminderHandler.isRemindersEnabled()){
                cancelReminderPrayer()
                sharedPreference.putInt(Constants.KEY_NOTIFICATION_TIME, item.text.toInt())
                setReminderPrayer()
            }

            // Update the selected state of other items
            for (i in items.indices) {
                if (i != position) {
                    items[i].selected = false
                }
            }
            notifyDataSetChanged()
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
    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(binding: ItemNotifSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvName: TextView = binding.tvName
        val radioButton: RadioButton = binding.radioButton
    }
}
