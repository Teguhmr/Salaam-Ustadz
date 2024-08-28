package id.uinjkt.salaamustadz.ui.notification

import androidx.recyclerview.widget.LinearLayoutManager
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityNotificationBinding
import id.uinjkt.salaamustadz.ui.adapter.notification.NotificationAdapter
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager

class NotificationActivity : BaseActivity<ActivityNotificationBinding>() {

    private lateinit var adapterNotification: NotificationAdapter
    private lateinit var sharedPreferenceManager: PreferenceManager


    override fun getViewBinding(): ActivityNotificationBinding {
        return ActivityNotificationBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {

    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        sharedPreferenceManager = PreferenceManager(this)
        val notificationItems = mutableListOf(
//            NotificationItem("Artikel", sharedPreferenceManager.getBoolean(Constants.KEY_ARTICLE_ENABLED)),
//            NotificationItem("Dzikir", sharedPreferenceManager.getBoolean(Constants.KEY_DZIKIR_ENABLED)),
            NotificationItem("Jadwal Shalat", sharedPreferenceManager.getBoolean(Constants.KEY_REMINDERS_ENABLED))
        )

        // Initialize and set the adapter
        adapterNotification = NotificationAdapter(this, notificationItems)
        binding.rvNotification.apply{
            layoutManager = LinearLayoutManager(this@NotificationActivity, LinearLayoutManager.VERTICAL, false)
            adapter = adapterNotification
        }
    }

    override fun setupAction() {

    }

    override fun setupProcess() {

    }

    override fun setupObserver() {

    }
}