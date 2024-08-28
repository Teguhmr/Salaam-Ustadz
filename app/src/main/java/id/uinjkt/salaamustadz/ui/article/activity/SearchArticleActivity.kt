package id.uinjkt.salaamustadz.ui.article.activity

import id.uinjkt.salaamustadz.base.BaseActivity
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.platform.MaterialSharedAxis
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.databinding.ActivitySearchArticleBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleAdapter
import id.uinjkt.salaamustadz.ui.article.ArticleViewModel
import id.uinjkt.salaamustadz.ui.article.detail.DetailArticleActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Message
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.show

class SearchArticleActivity : BaseActivity<ActivitySearchArticleBinding>() {
    private lateinit var articleViewModel: ArticleViewModel
    private lateinit var preferenceManager: PreferenceManager

    override fun getViewBinding(): ActivitySearchArticleBinding {
        return ActivitySearchArticleBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {

    }

    override fun setupUI() {
        preferenceManager = PreferenceManager(this)
        articleViewModel = ViewModelProvider(this)[ArticleViewModel::class.java]
        setSupportActionBar(binding.searchToolbar)

        val enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z,true)
        val returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z,false)
        window.enterTransition = enterTransition
        window.returnTransition = returnTransition
    }

    override fun setupAction() {
        with(binding){
            searchInput.addTextChangedListener { editable ->
                editable?.let {
//                    articleViewModel.loadInitialData(it.toString())
                }
                if (editable?.isNotEmpty() == true){
                    btnClose.show()
                    btnClose.setOnClickListener {
                        searchInput.setText("")
                    }
                } else {
                    btnClose.gone()
                }
            }

            searchInput.setOnEditorActionListener { textView, id, _ ->
                if(id == EditorInfo.IME_ACTION_SEARCH) {
                    val query = textView.text.toString()
                    if(query.isNotEmpty())
                        articleViewModel.searchArticles(query)
                }
                return@setOnEditorActionListener false
            }
        }
    }

    override fun setupProcess() {

    }

    override fun setupObserver() {
        with(binding){
            articleViewModel.searchArticleLiveData.observe(this@SearchArticleActivity){state ->
                when (state) {
                    is Result.Loading -> {
                        messageLayout.root.gone()
                        loading.show()
                    }
                    is Result.Success -> {
                        val adapterArticleList = ArticleAdapter(this@SearchArticleActivity, state.data!!)
                        rvSearchArticleList.adapter = adapterArticleList
                        adapterArticleList.setOnItemClickListener(object : ArticleAdapter.OnItemClickListener{
                            override fun onItemClick(article: Article) {
                                val intent = Intent(this@SearchArticleActivity, DetailArticleActivity::class.java)
                                intent.putExtra(Constants.KEY_ARTICLE, article) // Pass any relevant data to the detail activity
                                intent.putExtra(
                                    Constants.KEY_USER_ID, preferenceManager.getString(
                                        Constants.KEY_USER_ID)) // Pass any relevant data to the detail activity
                                startActivity(intent)
                            }

                        })
                        if (state.data.isEmpty()) {
                            showMessage(Message.SEARCH_EMPTY,"")
                        }
                        loading.gone()
                    }
                    is Result.Error -> {
                        loading.gone()
                        showMessage(Message.ERROR, state.message)
                    }
                }

            }
        }

    }

    private fun showMessage(error: Message, errorMsg: String?) {
        val messageBinding = binding.messageLayout
        when(error) {
            Message.SEARCH_EMPTY -> {
                messageBinding.messageIcon.setImageResource(R.drawable.ic_information)
                messageBinding.messageTitle.text = getString(R.string.not_found)
                messageBinding.messageSubtitle.text = getString(R.string.sorry_message)
            }
            Message.ERROR -> {
                messageBinding.messageTitle.text = errorMsg
                messageBinding.messageSubtitle.text = getString(R.string.network_subtitle)
            }
        }
        messageBinding.root.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}