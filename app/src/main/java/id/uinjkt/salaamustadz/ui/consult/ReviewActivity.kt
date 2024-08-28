package id.uinjkt.salaamustadz.ui.consult

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityReviewBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.review.ReviewAdapter
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.emptyString
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.show
import id.uinjkt.salaamustadz.utils.toast

class ReviewActivity : BaseActivity<ActivityReviewBinding>() {
    private lateinit var reviewAdapter: ReviewAdapter
    private val consultrviewViewModel: ConsultReviewViewModel by lazy {
        ViewModelProvider(this)[ConsultReviewViewModel::class.java]
    }
    private var userId: String? = emptyString()
    override fun getViewBinding(): ActivityReviewBinding {
        return ActivityReviewBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        userId = intent.getStringExtra(Constants.KEY_USER_ID)
    }

    override fun setupUI() {
        setToolbar(binding.toolbar)
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
        consultrviewViewModel.getMyReview(userId.toString())
    }

    override fun setupObserver() {
        consultrviewViewModel.myReviewLiveData.observe(this){result ->
            when (result){
                is Result.Error -> {
                    dismissLoading()
                    toast(result.message.toString())
                }
                is Result.Loading -> {
                    showLoading()
                }
                is Result.Success -> {
                    dismissLoading()

                    if (!result.data.isNullOrEmpty()){
                        binding.apply {
                            emptyReview.gone()
                            rvReview.show()

                            reviewAdapter = ReviewAdapter(result.data)
                            reviewAdapter.setShowAllItems(true)
                            rvReview.layoutManager = LinearLayoutManager(this@ReviewActivity, LinearLayoutManager.VERTICAL,false)
                            rvReview.setHasFixedSize(true)
                            rvReview.adapter = reviewAdapter
                        }
                    } else {
                        binding.apply {
                            rvReview.gone()
                            emptyReview.show()
                        }
                    }
                }
            }

        }
    }

}