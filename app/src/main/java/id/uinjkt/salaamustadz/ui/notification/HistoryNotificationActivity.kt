package id.uinjkt.salaamustadz.ui.notification

import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityHistoryNotificationBinding

class HistoryNotificationActivity : BaseActivity<ActivityHistoryNotificationBinding>() {

    override fun getViewBinding(): ActivityHistoryNotificationBinding {
        return ActivityHistoryNotificationBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        setToolbar(binding.toolbar)
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
    }

    override fun setupObserver() {
    }

}