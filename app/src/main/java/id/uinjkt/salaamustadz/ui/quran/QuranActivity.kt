package id.uinjkt.salaamustadz.ui.quran

import android.content.Intent
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.remote.repository.QuranRepository
import id.uinjkt.salaamustadz.databinding.ActivityQuranBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.quran.AdapterSurah
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.show
import kotlinx.coroutines.launch
import timber.log.Timber

class QuranActivity : BaseActivity<ActivityQuranBinding>() {
    private val surahViewModel: SurahViewModel by lazy {
        ViewModelProvider(this, QuranViewModelFactory(QuranRepository()))[SurahViewModel::class.java]
    }
    override fun getViewBinding(): ActivityQuranBinding {
        return ActivityQuranBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {

    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/quranbg.png"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)

    }

    override fun setupAction() {

    }

    override fun setupProcess() {

    }

    override fun setupObserver() {
        lifecycleScope.launch {
            surahViewModel.dataState.collect { state ->
                when (state){
                    is Result.Loading -> {
                        showLoading()
                    }
                    is Result.Success -> {
                        val adapterSurah = AdapterSurah(state.data!!){ position, surahName ->
                            val intent = Intent(this@QuranActivity, DetailSurahActivity::class.java)
                            intent.putExtra(Constants.KEY_POSITION, position)
                            intent.putExtra(Constants.KEY_SURAH, surahName)
                            startActivity(intent)
                        }
                        binding.rvlistQuran.apply {
                            layoutManager = LinearLayoutManager(this@QuranActivity, LinearLayoutManager.VERTICAL,false)
                            setHasFixedSize(true)
                            adapter = adapterSurah
                        }
                        binding.searchView.addTextChangedListener { editable ->
                            adapterSurah.filter.filter(editable)
                        }

                        binding.searchView.setOnEditorActionListener { textView, id, keyEvent ->
                            if(id == EditorInfo.IME_ACTION_SEARCH) {
                                val query = textView.text.toString()
                                adapterSurah.filter.filter(query)
                                return@setOnEditorActionListener true
                            }
                            false
                        }
                        dismissLoading()
                    }
                    is Result.Error -> {
                        dismissLoading()
                        binding.emptyLayout.show()
                        Timber.tag("surah").d(state.message.toString())
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