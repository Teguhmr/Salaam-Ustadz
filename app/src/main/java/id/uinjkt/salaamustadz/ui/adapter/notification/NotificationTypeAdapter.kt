package id.uinjkt.salaamustadz.ui.adapter.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.ItemNotifSettingBinding
import id.uinjkt.salaamustadz.ui.notification.ListItemNotification
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.MyNotification
import id.uinjkt.salaamustadz.utils.MyNotification.ARTICLE
import id.uinjkt.salaamustadz.utils.MyNotification.DZIKIR
import id.uinjkt.salaamustadz.utils.MyNotification.PRAYER_TIME
import id.uinjkt.salaamustadz.utils.PreferenceManager

class NotificationTypeAdapter(private val context: Context,
                              private val items: List<ListItemNotification>,
                              private val notificationName: String,
                              private val onSelected: (String) -> Unit) :
    RecyclerView.Adapter<NotificationTypeAdapter.ViewHolder>() {

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
        val sharedPreference =  PreferenceManager(context)
        holder.radioButton.isChecked = item.selected

        when(MyNotification.getType(notificationName)){
            ARTICLE -> {
                holder.radioButton.isChecked =
                    item.text == sharedPreference.getString(Constants.KEY_NOTIFICATION_TYPE_ARTICLE).toString()
                if (!sharedPreference.getBoolean(Constants.KEY_ARTICLE_ENABLED)){
                    holder.itemView.setDisable()
                } else {
                    holder.itemView.setEnable()
                }
            }
            DZIKIR ->  {
                holder.radioButton.isChecked =
                    item.text == sharedPreference.getString(Constants.KEY_NOTIFICATION_TYPE_DZIKIR).toString()
                if (!sharedPreference.getBoolean(Constants.KEY_DZIKIR_ENABLED)){
                    holder.itemView.setDisable()
                } else {
                    holder.itemView.setEnable()
                }
            }
            PRAYER_TIME -> {
                holder.radioButton.isChecked =
                    item.text == sharedPreference.getString(Constants.KEY_NOTIFICATION_TYPE_PRAYER).toString()
                if (!sharedPreference.getBoolean(Constants.KEY_REMINDERS_ENABLED)){
                    holder.itemView.setDisable()
                } else {
                    holder.itemView.setEnable()
                }
            }
        }

        holder.tvName.text = item.text

        holder.itemView.setOnClickListener {
            // Update the selected state of the clicked item
            item.selected = true
            onSelected.invoke(item.text)
            // Update the selected state of other items
            for (i in items.indices) {
                if (i != position) {
                    items[i].selected = false
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun View.setDisable() {
        isEnabled = false
        foreground = ContextCompat.getDrawable(context, R.color.white_transparent)
    }
    private fun View.setEnable() {
        isEnabled = true
        foreground = ContextCompat.getDrawable(context, android.R.color.transparent)
    }
    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(binding: ItemNotifSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvName: TextView = binding.tvName
        val radioButton: RadioButton = binding.radioButton
    }
}
