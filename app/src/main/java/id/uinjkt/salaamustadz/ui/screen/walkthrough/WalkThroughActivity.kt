package id.uinjkt.salaamustadz.ui.screen.walkthrough

import android.content.Intent
import androidx.viewpager.widget.ViewPager
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.auth.WalkThrough
import id.uinjkt.salaamustadz.databinding.ActivityWalkThroughBinding
import id.uinjkt.salaamustadz.ui.auth.AuthActivity
import id.uinjkt.salaamustadz.ui.dzikir.DotIndicator
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.WalkthroughPreferenceManager

class WalkThroughActivity : BaseActivity<ActivityWalkThroughBinding>() {
    private lateinit var preferenceManager: WalkthroughPreferenceManager
    private lateinit var adapterWalkThrough: WalkThroughPagerAdapter
    override fun getViewBinding(): ActivityWalkThroughBinding {
        return ActivityWalkThroughBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        preferenceManager = WalkthroughPreferenceManager(this)
    }

    override fun setupUI() {
        val listData = arrayListOf(
            WalkThrough(
                "Konsultasi Bersama Ustadz Terpercaya",
                "Melakukan konsultasi tanpa rasa takut. Anda dapat memilih ustadz dan ustadzah untuk sesi konsultasi anda",
                R.drawable.walkthrough_1
            ),
            WalkThrough(
                "Membaca Artikel Keislaman",
                "Jangan takut! sekarang anda dapat membaca artikel keislaman yang ditulis langsung dari para ahli agama",
                R.drawable.walkthrough_2
            ),
            WalkThrough(
                "Tuntunan Ibadah Untuk Keseharian Anda",
                "Kegiatan sehari-harimu akan lebih berwarna dan terasa menenangkan dengan memanfaatkan fitur tuntunan ibadah kami",
                R.drawable.walkthrough_3
            ))

        adapterWalkThrough = WalkThroughPagerAdapter(this, listData)
        binding.viewPager.adapter = adapterWalkThrough
        dotIndicator.updateDotIndicator(0)

        binding.apply {
            btnNext.setOnClickListener {
                val currentItem = viewPager.currentItem
                if (currentItem < adapterWalkThrough.count - 1) {
                    viewPager.currentItem = currentItem + 1 // Move to the next page
                }
            }

            tvSkip.setOnClickListener {
                preferenceManager.putBoolean(Constants.KEY_IS_FIRST_TIME, false)
                startActivity(Intent(this@WalkThroughActivity, AuthActivity::class.java))
                finish()
            }
        }
    }

    private val dotIndicator: DotIndicator by lazy {
        DotIndicator(adapterWalkThrough, binding.dotsLayout, this,
            dotTransition = R.drawable.dots_transition_walkthrough,
            dotActive = R.drawable.dot_active_walkthrough,
            dotInactive = R.drawable.dot_inactive_walkthrough)
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
        with(binding){
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    // Not needed
                }

                override fun onPageSelected(position: Int) {
                    when (viewPager.adapter) {
                        adapterWalkThrough -> {
                            dotIndicator.updateDotIndicator(position)
                            if (position == adapterWalkThrough.count - 1){
                                btnNext.text = getString(R.string.label_finish)
                                btnNext.setOnClickListener {
                                    preferenceManager.putBoolean(Constants.KEY_IS_FIRST_TIME, false)
                                    startActivity(Intent(this@WalkThroughActivity, AuthActivity::class.java))
                                    finish()
                                }
                            }

                        }
                    }


                }

                override fun onPageScrollStateChanged(state: Int) {
                    // Not needed
                }
            })
        }
    }

    override fun setupObserver() {
    }

}