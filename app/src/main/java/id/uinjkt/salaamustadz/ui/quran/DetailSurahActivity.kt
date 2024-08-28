package id.uinjkt.salaamustadz.ui.quran

import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.remote.repository.QuranRepository
import id.uinjkt.salaamustadz.databinding.ActivityDetailSurahBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.quran.AdapterAyah
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.capitalizeWords
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.hide
import id.uinjkt.salaamustadz.utils.toast
import kotlinx.coroutines.launch

class DetailSurahActivity : BaseActivity<ActivityDetailSurahBinding>() {
    private var noSurah: Int? = null
    private var surah: String? = null
    private lateinit var adapterAyah: AdapterAyah

    private val surahViewModel: SurahViewModel by lazy {
        ViewModelProvider(this, QuranViewModelFactory(QuranRepository()))[SurahViewModel::class.java]
    }
    override fun getViewBinding(): ActivityDetailSurahBinding {
        return ActivityDetailSurahBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        noSurah = intent.getIntExtra(Constants.KEY_POSITION, 1)
        surah = intent.getStringExtra(Constants.KEY_SURAH)
        surahViewModel.setDetailSurah(noSurah.toString())
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/quranbg.png"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)
        binding.tvTitleSurah.text = surah
    }

    override fun setupAction() {

    }

    override fun setupProcess() {

    }

    override fun setupObserver() {
        lifecycleScope.launch {
            surahViewModel.dataDetailState.collect {state->
                when (state){
                    is Result.Error -> {
                        dismissLoading()
                        toast(state.message.toString())
                    }
                    is Result.Loading -> showLoading(true)
                    is Result.Success -> {
                        val data = state.data
                        if (data != null){
                            val modelAyat = data.ayat
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (::adapterAyah.isInitialized) {
                                    adapterAyah.notifyDataSetChanged()
                                } else {
                                    adapterAyah = AdapterAyah(modelAyat)
                                    binding.rvlistQuran.apply {
                                        layoutManager = LinearLayoutManager(this@DetailSurahActivity, LinearLayoutManager.VERTICAL,false)
                                        setHasFixedSize(true)
                                        adapter = adapterAyah
                                    }
                                }
                                binding.progressBar.hide()
                            }, 1000)

                            with(binding){
                                data.apply {
                                    tvSurahName.text = namaLatin
                                    tvAyahMeaning.text = arti
                                    tvCityAndTotalAyah.text = tempatTurun.capitalizeWords().plus(" • ").plus("$jumlahAyat ayat")

                                    if (nomor == 1)
                                        tvBismillah.text = getString(R.string.label_taawuz)
                                    else
                                        tvBismillah.text = getString(R.string.label_bismillah)
                                }

                            }

                            dismissLoading()


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
}