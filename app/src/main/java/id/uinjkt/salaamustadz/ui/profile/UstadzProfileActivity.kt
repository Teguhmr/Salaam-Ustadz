package id.uinjkt.salaamustadz.ui.profile

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.ActivityUstadzProfileBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleAdapter
import id.uinjkt.salaamustadz.ui.adapter.review.ReviewAdapter
import id.uinjkt.salaamustadz.ui.article.ArticleViewModel
import id.uinjkt.salaamustadz.ui.article.activity.UstadzArticleActivity
import id.uinjkt.salaamustadz.ui.article.detail.DetailArticleActivity
import id.uinjkt.salaamustadz.ui.consult.ConsultReviewViewModel
import id.uinjkt.salaamustadz.ui.consult.ReviewActivity
import id.uinjkt.salaamustadz.ui.consult.WriteQuestionActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.capitalizeWords
import id.uinjkt.salaamustadz.utils.dialog.ProfileUstadzDialog
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.parcelable
import id.uinjkt.salaamustadz.utils.show
import id.uinjkt.salaamustadz.utils.toast

class UstadzProfileActivity : BaseActivity<ActivityUstadzProfileBinding>() {

    private var userUstadz: User? = null

    private lateinit var reviewAdapter: ReviewAdapter
    private val consultrviewViewModel: ConsultReviewViewModel by lazy {
        ViewModelProvider(this)[ConsultReviewViewModel::class.java]
    }
    override fun getViewBinding(): ActivityUstadzProfileBinding {
        return ActivityUstadzProfileBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        userUstadz = intent.parcelable(Constants.KEY_USER)
    }

    private val articleViewModel: ArticleViewModel by lazy {
        ViewModelProvider(this)[ArticleViewModel::class.java]
    }
    override fun setupUI() {
        val role = intent.getStringExtra(Constants.KEY_ROLE)
        if (userUstadz?.image.isNullOrEmpty()){
            binding.imageProfile.setImageResource(R.drawable.profile_user)
        } else {
            Glide
                .with(this)
                .load(StorageUtil.pathToReference(userUstadz?.image.toString()))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(binding.imageProfile)
        }
        if (userUstadz?.imageBg.isNullOrEmpty()){
            Glide.with(this)
                .load(StorageUtil.pathToReference("/images/bg_profile.png"))
                .into(binding.imgBackgroundProfile)
        } else {
            Glide.with(this)
                .load(StorageUtil.pathToReference(userUstadz?.imageBg.toString()))
                .into(binding.imgBackgroundProfile)
        }
        binding.txtUsername.text = role.plus(" ${userUstadz?.name}")

        binding.btnProfileUstadz.setOnClickListener {
            ProfileUstadzDialog(this, userUstadz).show(supportFragmentManager, "dialogProfileUstadz")
        }

        binding.btnConsult.setOnClickListener {
            val intent = Intent(this, WriteQuestionActivity::class.java)
            intent.putExtra(Constants.KEY_USTADZ, userUstadz)
            startActivity(intent)

        }

        binding.toolbar.title = getString(R.string.label_profile).plus(" ${role?.capitalizeWords()}")

        setToolbar(binding.toolbar)
        supportActionBar?.title = ""
        val upArrow = ResourcesCompat.getDrawable(resources, R.drawable.arrow_back, null)
        upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        var isShow: Boolean? = false
        var scrollRange = -1
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, offset ->
            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange
            }
            if (scrollRange + offset == 0) {
                //collapse map
                isShow = true
                upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.dark_blue_primary), PorterDuff.Mode.SRC_ATOP)
                binding.imageProfile.gone()

            } else if (isShow == true) {
                //expanded map
                isShow = false
                upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
                binding.imageProfile.show()

            }

        }

    }

    override fun setupAction() {
        binding.txtViewMoreArticle.setOnClickListener {
            val intent = Intent(this@UstadzProfileActivity, UstadzArticleActivity::class.java)
            intent.putExtra(Constants.KEY_USER_NAME, userUstadz?.name)
            intent.putExtra(Constants.KEY_USER_ID, userUstadz?.id)
            startActivity(intent)
        }
        binding.txtViewMoreReview.setOnClickListener {
            val intent = Intent(this@UstadzProfileActivity, ReviewActivity::class.java)
            intent.putExtra(Constants.KEY_USER_ID, userUstadz?.id)
            startActivity(intent)
        }
    }

    override fun setupProcess() {
        articleViewModel.fetchUserArticle(userUstadz?.id.toString())
        consultrviewViewModel.getMyReview(userUstadz?.id.toString())
    }

    override fun setupObserver() {
        articleViewModel.userArticleLiveData.observe(this){result ->
            when (result){
                is Result.Error -> {}
                is Result.Loading -> {}
                is Result.Success -> {
                    if (!result.data.isNullOrEmpty()) {
                        val articleAdapter = ArticleAdapter(this, result.data.reversed(), 3)

                        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
                        binding.apply {
                            rvArticle.layoutManager = linearLayoutManager
                            rvArticle.setHasFixedSize(true)
                            rvArticle.adapter = articleAdapter
                        }

                        articleAdapter.setOnItemClickListener(object : ArticleAdapter.OnItemClickListener{
                            override fun onItemClick(article: Article) {
                                val intent = Intent(this@UstadzProfileActivity, DetailArticleActivity::class.java)
                                intent.putExtra(Constants.KEY_ARTICLE, article)
                                intent.putExtra(Constants.KEY_USER_ID, sharedPref.getString(Constants.KEY_USER_ID))
                                startActivity(intent)
                            }

                        })
                        binding.emptyArticle.gone()
                    } else {
                        binding.emptyArticle.show()
                    }
                }
            }
        }

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
                        val totalRating = result.data.sumOf { it.rating.toDouble() }.toFloat()

                        binding.apply {
                            emptyReview.gone()
                            rvReview.show()

                            //set average rating
                            appCompatRatingBar.rating = totalRating / result.data.size

                            reviewAdapter = ReviewAdapter(result.data)
                            rvReview.layoutManager = LinearLayoutManager(this@UstadzProfileActivity, LinearLayoutManager.VERTICAL,false)
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