package id.uinjkt.salaamustadz.ui.dzikir

import android.view.MenuItem
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityDzikirBinding
import id.uinjkt.salaamustadz.utils.DzikirType
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil

class DzikirActivity : BaseActivity<ActivityDzikirBinding>() {
    override fun getViewBinding(): ActivityDzikirBinding {
        return ActivityDzikirBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/bg_dzikir.jpg"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)
    }

    override fun setupAction() {
        with(binding){
            cvDzikirPagi.setOnClickListener {
                DetailDzikirActivity.start(this@DzikirActivity, DzikirType.DZIKIR_PAGI)
            }
            cvDzikirPetang.setOnClickListener {
                DetailDzikirActivity.start(this@DzikirActivity, DzikirType.DZIKIR_PETANG)
            }
        }
    }

    override fun setupProcess() {
    }

    override fun setupObserver() {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}