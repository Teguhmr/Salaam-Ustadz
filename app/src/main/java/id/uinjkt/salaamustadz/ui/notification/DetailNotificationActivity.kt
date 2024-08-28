package id.uinjkt.salaamustadz.ui.notification

import androidx.recyclerview.widget.LinearLayoutManager
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityDetailNotificationBinding
import id.uinjkt.salaamustadz.ui.adapter.notification.NotificationTimeAdapter
import id.uinjkt.salaamustadz.ui.adapter.notification.NotificationTypeAdapter
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.MyNotification
import id.uinjkt.salaamustadz.utils.MyNotification.ARTICLE
import id.uinjkt.salaamustadz.utils.MyNotification.DZIKIR
import id.uinjkt.salaamustadz.utils.MyNotification.PRAYER_TIME
import id.uinjkt.salaamustadz.utils.NotificationTimeRemaining
import id.uinjkt.salaamustadz.utils.NotificationType
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.show

class DetailNotificationActivity : BaseActivity<ActivityDetailNotificationBinding>() {
    private lateinit var adapterNotification: NotificationTypeAdapter
    private lateinit var adapterTimeNotification: NotificationTimeAdapter
    private lateinit var notificationName: String
    private lateinit var sharedPreference: PreferenceManager

    override fun getViewBinding(): ActivityDetailNotificationBinding {
        return ActivityDetailNotificationBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        notificationName = intent.getStringExtra(Constants.KEY_NOTIFICATION_NAME).toString()
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.label_notification).plus(" $notificationName")
        sharedPreference = PreferenceManager(this)
        val listNotifType = listOf(
            ListItemNotification(NotificationType.NOTIFICATION_VIBRATE.title),
            ListItemNotification(NotificationType.ONLY_NOTIFICATION.title)
        )

        val listNotifTime = listOf(
            ListItemNotification(NotificationTimeRemaining.MINUTES_30.title),
            ListItemNotification(NotificationTimeRemaining.MINUTES_15.title),
            ListItemNotification(NotificationTimeRemaining.MINUTES_5.title),
            ListItemNotification(NotificationTimeRemaining.MINUTES_1.title)
        )

        adapterNotification = NotificationTypeAdapter(this, listNotifType, notificationName){itemText ->
            when (MyNotification.getType(notificationName)){
                ARTICLE -> {
                    sharedPreference.putString(Constants.KEY_NOTIFICATION_TYPE_ARTICLE, itemText)
                }
                DZIKIR -> {
                    sharedPreference.putString(Constants.KEY_NOTIFICATION_TYPE_DZIKIR, itemText)
                }
                PRAYER_TIME -> {
                    sharedPreference.putString(Constants.KEY_NOTIFICATION_TYPE_PRAYER, itemText)
                }
            }

        }
        adapterTimeNotification = NotificationTimeAdapter(this, listNotifTime)

        binding.rvNotification.apply {
            layoutManager = LinearLayoutManager(this@DetailNotificationActivity, LinearLayoutManager.VERTICAL, false)
            adapter = adapterNotification
        }

        binding.rvNotificationTime.apply {
            layoutManager = LinearLayoutManager(this@DetailNotificationActivity, LinearLayoutManager.VERTICAL, false)
            adapter = adapterTimeNotification
        }

        when (MyNotification.getType(notificationName)){
            ARTICLE -> {}
            DZIKIR -> {}
            PRAYER_TIME -> {
                binding.apply {
                    tvBeforeTime.show()
                    rvNotificationTime.show()
                }
            }
        }

    }

    override fun setupAction() {
    }

    override fun setupProcess() {
    }

    override fun setupObserver() {
    }

}