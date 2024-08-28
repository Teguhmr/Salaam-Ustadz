package id.uinjkt.salaamustadz.ui.about

import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityAboutUsBinding

class AboutUsActivity : BaseActivity<ActivityAboutUsBinding>() {
    override fun getViewBinding(): ActivityAboutUsBinding {
        return ActivityAboutUsBinding.inflate(layoutInflater)
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