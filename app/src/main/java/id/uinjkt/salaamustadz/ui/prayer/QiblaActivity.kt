package id.uinjkt.salaamustadz.ui.prayer

import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityQiblaBinding
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import io.github.derysudrajat.compassqibla.CompassQibla

class QiblaActivity : BaseActivity<ActivityQiblaBinding>() {
    private var currentCompassDegree = 0f
    private var currentNeedleDegree = 0f
    override fun getViewBinding(): ActivityQiblaBinding {
        return ActivityQiblaBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/bg_mosque.jpg"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)
        CompassQibla.Builder(this).onGetLocationAddress { address ->
            binding.tvLocation.text = buildString {
                append(address.locality)
                append(", ")
                append(address.subAdminArea)
            }
        }.onDirectionChangeListener { qiblaDirection ->
            binding.tvDirection.text = if (qiblaDirection.isFacingQibla) "Qiblat sudah sejajar"
            else "${qiblaDirection.needleAngle.toInt()}°"

            val rotateCompass = RotateAnimation(
                currentCompassDegree, qiblaDirection.compassAngle, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 1000
            }
            currentCompassDegree = qiblaDirection.compassAngle

            binding.ivCompass.startAnimation(rotateCompass)

            val rotateNeedle = RotateAnimation(
                currentNeedleDegree, qiblaDirection.needleAngle, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 1000
            }
            currentNeedleDegree = qiblaDirection.needleAngle

            binding.ivNeedle.startAnimation(rotateNeedle)
        }.build()
    }

    override fun setupAction() {
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