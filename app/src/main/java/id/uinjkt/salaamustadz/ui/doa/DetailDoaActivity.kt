package id.uinjkt.salaamustadz.ui.doa

import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.remote.repository.DoaRepository
import id.uinjkt.salaamustadz.databinding.ActivityDetailDoaBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailDoaActivity : BaseActivity<ActivityDetailDoaBinding>() {

    private lateinit var id: String
    private lateinit var name: String
    private lateinit var viewModel: DoaViewModel

    override fun getViewBinding(): ActivityDetailDoaBinding {
        return ActivityDetailDoaBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        id = intent.getStringExtra(Constants.KEY_POSITION).toString()
        name = intent.getStringExtra(Constants.KEY_DOA).toString()
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = name
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/bg_doa.png"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
        viewModel = ViewModelProvider(this, DoaViewModelFactory(DoaRepository()))[DoaViewModel::class.java]
        viewModel.getDetailDoa(id)
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            viewModel.dataDetailState.collect { state ->
                when (state){
                    is Result.Loading -> {
                        showLoading()
                    }
                    is Result.Success -> {
                        dismissLoading()

                        with(binding){
                            state.data?.get(0)?.apply {
                                tvTitle.text = doa
                                tvArab.text = ayat
                                tvArti.text = getString(R.string.label_artinya).plus("\n\"$artinya\"")
                            }
                        }
                    }
                    is Result.Error -> {
                        dismissLoading()
                        Timber.tag("Error").d(state.message)
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