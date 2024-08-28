package id.uinjkt.salaamustadz.ui.dzikir

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.models.dzikir.Dzikir
import id.uinjkt.salaamustadz.databinding.ActivityDetailDzikirBinding
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.DzikirType
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import org.json.JSONArray

class DetailDzikirActivity : BaseActivity<ActivityDetailDzikirBinding>() {
    private var dzikirType: DzikirType = DzikirType.DZIKIR_PAGI

    override fun getViewBinding(): ActivityDetailDzikirBinding {
        return ActivityDetailDzikirBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        dzikirType = DzikirType.getType(intent.getStringExtra(Constants.KEY_DZIKIR_TYPE).orEmpty())
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = dzikirType.title

        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/bg_dzikir.jpg"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)

        when(dzikirType) {
            DzikirType.DZIKIR_PAGI -> setListDzikirPagi()
            DzikirType.DZIKIR_PETANG -> setListDzikirPetang()
        }
        var currentAdapter = adapterDzikirPagi

        binding.apply {
            btnNext.setOnClickListener {
                when (viewPager.adapter) {
                    adapterDzikirPagi ->{
                        currentAdapter = adapterDzikirPagi
                    }
                    adapterDzikirPetang -> {
                        currentAdapter = adapterDzikirPetang
                    }
                }
                val currentItem = viewPager.currentItem
                if (currentItem < currentAdapter.count - 1) {
                    viewPager.currentItem = currentItem + 1 // Move to the next page
                }
            }

            btnPrev.isEnabled = false
            btnPrev.setOnClickListener {
                val currentItem = viewPager.currentItem
                if (currentItem > 0) {
                    viewPager.currentItem = currentItem - 1 // Move to the previous page
                }
            }
        }
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
                        adapterDzikirPagi -> {
                            dotIndicator1.updateDotIndicator(position)
                            btnNext.isEnabled = position != adapterDzikirPagi.count - 1
                        }
                        adapterDzikirPetang ->{
                            dotIndicator2.updateDotIndicator(position)
                            btnNext.isEnabled = position != adapterDzikirPetang.count - 1
                        }
                    }

                    btnPrev.isEnabled = position != 0

                }

                override fun onPageScrollStateChanged(state: Int) {
                    // Not needed
                }
            })
        }
    }

    override fun setupObserver() {
    }

    private fun readDzikirFile(fileName: String): ArrayList<Dzikir> {
        val dzikirList = ArrayList<Dzikir>()

        try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)

            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val title = item.getString("title")
                val countOfReads = item.getString("countOfReads")
                val arabic = item.getString("arabic")
                val translate = item.getString("translate")

                val dzikir = Dzikir(title, countOfReads, arabic, translate)
                dzikirList.add(dzikir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dzikirList
    }

    private val adapterDzikirPagi: DzikirPagerAdapter by lazy {
        DzikirPagerAdapter(this, readDzikirFile("raw/dzikir_pagi.json"))
    }
    private val adapterDzikirPetang: DzikirPagerAdapter by lazy {
        DzikirPagerAdapter(this, readDzikirFile("raw/dzikir_petang.json"))
    }
    private val dotIndicator1: DotIndicator by lazy {
        DotIndicator(adapterDzikirPagi, binding.dotsLayout, this)
    }

    private val dotIndicator2: DotIndicator by lazy {
        DotIndicator(adapterDzikirPetang, binding.dotsLayout, this)
    }

    private fun setListDzikirPagi() {
        binding.viewPager.adapter = adapterDzikirPagi
        dotIndicator1.updateDotIndicator(0)
    }

    private fun setListDzikirPetang() {
        binding.viewPager.adapter = adapterDzikirPetang
        dotIndicator2.updateDotIndicator(0)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(context: Context, dzikirType: DzikirType) {
            val intent = Intent(context, DetailDzikirActivity::class.java)
            intent.putExtra(Constants.KEY_DZIKIR_TYPE, dzikirType.type)
            context.startActivity(intent)
        }
    }

}