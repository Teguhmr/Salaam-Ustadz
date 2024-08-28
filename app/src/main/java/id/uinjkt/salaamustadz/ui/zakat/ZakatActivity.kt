package id.uinjkt.salaamustadz.ui.zakat

import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityZakatBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.zakat.DokumentasiSTFAdapter
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.recyclerview.AutoScrollManager
import id.uinjkt.salaamustadz.utils.recyclerview.SetUpIndicator
import timber.log.Timber

class ZakatActivity : BaseActivity<ActivityZakatBinding>() {

    private val zakatViewModel: ZakatViewModel by lazy {
        ViewModelProvider(this)[ZakatViewModel::class.java]
    }

    private lateinit var zakatAdapter: DokumentasiSTFAdapter
    private var autoScrollManager: AutoScrollManager? = null

    override fun getViewBinding(): ActivityZakatBinding {
        return ActivityZakatBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        with(binding){
            btnInfoSTF.setOnClickListener {
                openWebPageInBrowser("https://www.stfuinjakarta.org/")
            }
            btnDonasiSTF.setOnClickListener {
                openWebPageInBrowser("https://www.stfuinjakarta.org/donasi/program/")
            }
        }
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/bg_zakat.png"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)

        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/stf/stf_donasi.png"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgInfoStf)

    }

    override fun setupAction() {
    }

    override fun setupProcess() {

    }

    override fun setupObserver() {
        val linearLayoutManager = LinearLayoutManager(this@ZakatActivity, LinearLayoutManager.HORIZONTAL,false)
        binding.rvDocumentation.layoutManager = linearLayoutManager
        binding.rvDocumentation.setHasFixedSize(true)
        autoScrollManager = AutoScrollManager(binding.rvDocumentation)
        autoScrollManager?.startAutoScroll()
        zakatViewModel.imagesSTFLiveData.observe(this){state ->
            when(state){
                is Result.Error -> {
                    dismissLoading()
                    Timber.tag("listError").d(state.message)

                }
                is Result.Loading -> {
                    showLoading()
                }
                is Result.Success -> {
                    dismissLoading()
                    Timber.tag("list").d(state.data.toString())
                    if (state.data?.isNotEmpty() == true){

                        zakatAdapter = DokumentasiSTFAdapter(this, state.data)
                        with(binding){
                            rvDocumentation.adapter = zakatAdapter
                            rvDocumentation.onFlingListener = null

                            val snapHelper = PagerSnapHelper()
                            snapHelper.attachToRecyclerView(rvDocumentation)
                            SetUpIndicator(this@ZakatActivity, rvDocumentation, snapHelper, linearLayoutManager, binding.dotLayout, state.data)
                        }
                    }
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openWebPageInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        autoScrollManager?.stopAutoScroll()
    }

}