package id.uinjkt.salaamustadz.ui.article.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.platform.MaterialSharedAxis
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.databinding.ActivityFavoritesArticlesBinding
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleAdapter
import id.uinjkt.salaamustadz.ui.article.detail.DetailArticleActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.firebase.DatabaseUtil
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.hideKeyboard
import id.uinjkt.salaamustadz.utils.show

class FavoriteArticlesActivity : AppCompatActivity() {
    private var _binding: ActivityFavoritesArticlesBinding? = null
    private val binding get() = _binding as ActivityFavoritesArticlesBinding
    private lateinit var articleAdapter: ArticleAdapter
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFavoritesArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z,true)
        val returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z,false)
        window.exitTransition = enterTransition
        window.reenterTransition = returnTransition

        userId = intent.getStringExtra(Constants.KEY_USER_ID)

        fetchFavoriteArticles()

        setSupportActionBar(binding.materialToolbar)

    }

    override fun onRestart() {
        super.onRestart()
        fetchFavoriteArticles()
    }

    private fun fetchFavoriteArticles() {
        DatabaseUtil.getFavoriteStatus(userId!!) {userFavorites ->
            val rvMyArticle= binding.rvMyArticle
            binding.progressBar.gone()
            DatabaseUtil.fetchFavoriteArticleData(userFavorites, object : DatabaseUtil.FavoriteArticleListener{
                override fun onLoading() {
                    binding.progressBar.gone()
                }
                override fun onFavoriteArticlesFetched(articles: List<Article>) {
                    if (articles.isNotEmpty()){
                        binding.emptyLayout.gone()
                        rvMyArticle.show()
                        articleAdapter = ArticleAdapter(this@FavoriteArticlesActivity, articles)
                        rvMyArticle.layoutManager = LinearLayoutManager(this@FavoriteArticlesActivity, LinearLayoutManager.VERTICAL,false)
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
                                    hideKeyboard(this@FavoriteArticlesActivity)
                                    return@setOnEditorActionListener true
                                }
                                return@setOnEditorActionListener false
                            }
                        }
                        articleAdapter.setOnItemClickListener(object : ArticleAdapter.OnItemClickListener {
                            override fun onItemClick(article: Article) {
                                // Handle the item click here, e.g., start the new activity with an Intent
                                val intent = Intent(this@FavoriteArticlesActivity, DetailArticleActivity::class.java)
                                intent.putExtra(Constants.KEY_ARTICLE, article) // Pass any relevant data to the detail activity
                                intent.putExtra(Constants.KEY_USER_ID, userId)
                                startActivity(intent)
                            }
                        })
                    } else {
                        binding.emptyLayout.show()
                        rvMyArticle.gone()
                    }
                }

                override fun onFetchError(error: String) {
                    binding.progressBar.gone()
                }
            })
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}