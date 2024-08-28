package id.uinjkt.salaamustadz.ui.article.activity

import android.content.Intent
import android.view.KeyEvent
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.databinding.ActivityUstadzArticleBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleAdapter
import id.uinjkt.salaamustadz.ui.article.ArticleViewModel
import id.uinjkt.salaamustadz.ui.article.detail.DetailArticleActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.hideKeyboard
import id.uinjkt.salaamustadz.utils.show

class UstadzArticleActivity : BaseActivity<ActivityUstadzArticleBinding>() {
    private val articleViewModel: ArticleViewModel by lazy {
        ViewModelProvider(this)[ArticleViewModel::class.java]
    }
    private lateinit var articleAdapter: ArticleAdapter
    private var userId: String? = null
    private var userName: String? = null



    override fun getViewBinding(): ActivityUstadzArticleBinding {
        return ActivityUstadzArticleBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        userId = intent.getStringExtra(Constants.KEY_USER_ID)
        userName = intent.getStringExtra(Constants.KEY_USER_NAME)

        setToolbar(binding.materialToolbar)
        supportActionBar?.title = getString(R.string.article).plus(" Ust. $userName")
    }

    override fun setupUI() {
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
        articleViewModel.fetchUserArticle(userId!!)
    }

    override fun setupObserver() {
        fetchArticle()
    }

    private fun fetchArticle() {
        val rvMyArticle= binding.rvMyArticle

        articleViewModel.userArticleLiveData.observe(this){ state ->
            when(state) {
                is Result.Loading -> {
                    binding.progressBar.show()
                }
                is Result.Success -> {
                    binding.progressBar.gone()
                    if (state.data?.isNotEmpty() == true){
                        rvMyArticle.show()
                        binding.emptyLayout.gone()
                        articleAdapter = ArticleAdapter(this, state.data.reversed())
                        rvMyArticle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
                        rvMyArticle.setHasFixedSize(true)
                        rvMyArticle.adapter = articleAdapter
                        binding.searchView.addTextChangedListener { editable ->
                            editable?.let {
                                articleAdapter.filter.filter(it.toString())
                            }
                        }
                        with(binding.searchView){
                            setOnEditorActionListener { _, actionId, event ->
                                if (event != null && (actionId == KeyEvent.ACTION_DOWN || event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    // Handle the "Done" action (e.g., perform the search)
                                    hideKeyboard(this@UstadzArticleActivity)
                                    return@setOnEditorActionListener true
                                }
                                return@setOnEditorActionListener false
                            }
                        }
                        articleAdapter.setOnItemClickListener(object : ArticleAdapter.OnItemClickListener {
                            override fun onItemClick(article: Article) {
                                // Handle the item click here, e.g., start the new activity with an Intent
                                val intent = Intent(this@UstadzArticleActivity, DetailArticleActivity::class.java)
                                intent.putExtra(Constants.KEY_ARTICLE, article) // Pass any relevant data to the detail activity
                                intent.putExtra(Constants.KEY_USER_NAME, userName)
                                intent.putExtra(Constants.KEY_USER_ID, userId)
                                startActivity(intent)
                            }
                        })
                    } else {
                        rvMyArticle.gone()
                        binding.emptyLayout.show()
                    }

                }
                is Result.Error -> {binding.progressBar.gone()}
            }
        }
    }

}