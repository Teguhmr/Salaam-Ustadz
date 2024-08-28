package id.uinjkt.salaamustadz.ui.doa

import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.remote.repository.DoaRepository
import id.uinjkt.salaamustadz.databinding.ActivityDoaBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.doa.AdapterDoa
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.show
import kotlinx.coroutines.launch
import timber.log.Timber

class DoaActivity : BaseActivity<ActivityDoaBinding>() {

    private lateinit var viewModel: DoaViewModel

    override fun getViewBinding(): ActivityDoaBinding {
        return ActivityDoaBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/bg_doa.png"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
        viewModel = ViewModelProvider(this, DoaViewModelFactory(DoaRepository()))[DoaViewModel::class.java]
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            viewModel.dataState.collect { state ->
                when (state){
                    is Result.Loading -> {
                        showLoading()
                    }
                    is Result.Success -> {
                        dismissLoading()

                        val adapterDoa = AdapterDoa(state.data!!, onMenuSelected = { position, name ->
                            val intent = Intent(this@DoaActivity, DetailDoaActivity::class.java)
                            intent.putExtra(Constants.KEY_POSITION, position.toString())
                            intent.putExtra(Constants.KEY_DOA, name)
                            startActivity(intent)
                        })
                        with(binding){
                            rvlistQuran.apply {
                                layoutManager = LinearLayoutManager(this@DoaActivity, LinearLayoutManager.VERTICAL, false)
                                adapter = adapterDoa
                            }
                        }
                        binding.searchView.addTextChangedListener { editable ->
                            adapterDoa.filter.filter(editable)
                        }

                        binding.searchView.setOnEditorActionListener { textView, id, keyEvent ->
                            if(id == EditorInfo.IME_ACTION_SEARCH) {
                                val query = textView.text.toString()
                                adapterDoa.filter.filter(query)
                                return@setOnEditorActionListener true
                            }
                            false
                        }
                    }
                    is Result.Error -> {
                        dismissLoading()
                        binding.emptyLayout.show()
                        Timber.tag("Error").d(state.message)
                    }
                }

            }

        }
    }

}