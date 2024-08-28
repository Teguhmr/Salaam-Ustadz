package id.uinjkt.salaamustadz.ui.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil


class ArticleViewModel: ViewModel() {
    private val _searchState: MutableLiveData<Result<List<Article>>> = MutableLiveData()
    val searchArticleLiveData: LiveData<Result<List<Article>>>
        get() = _searchState

    private var userArticlesLiveData: MutableLiveData<Result<List<Article>>> = MutableLiveData()
    val userArticleLiveData: LiveData<Result<List<Article>>>
        get() = userArticlesLiveData

    private var articlesLiveData: MutableLiveData<Result<List<Article>>> = MutableLiveData()
    val articleLiveData: LiveData<Result<List<Article>>>
        get() = articlesLiveData

    private var articlesCategoriesLiveData: MutableLiveData<Result<List<Article>>> = MutableLiveData()
    val articlesCategoryLiveData: LiveData<Result<List<Article>>>
        get() = articlesCategoriesLiveData

    private var screenStatesLiveData: MutableLiveData<Result<Boolean>> = MutableLiveData()
    val screenStateLiveData: LiveData<Result<Boolean>>
        get() = screenStatesLiveData

    private var categoriesLiveData: MutableLiveData<Result<Array<String>>> = MutableLiveData()
    val categoryLiveData: LiveData<Result<Array<String>>>
        get() = categoriesLiveData

    private val database = FirebaseDatabase.getInstance()
    private val articlesRef = database.reference.child("articles")
    private val categoriesRef: DatabaseReference = database.reference.child("categories")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = storage.reference

    private val pagingConfig = PagingConfig(
        pageSize = Companion.PAGE_SIZE,
        enablePlaceholders = false
    )

    private val PAGE_SIZE = 20
    private val PREFETCH_DISTANCE = 10
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false
    private var currentSearchQuery: String? = null
    private val searchResultsList = mutableListOf<Article>() // Replace with your data type

    init {
        fetchAllArticle()
        retrieveCategoryData()
    }

    fun searchArticles(query: String) {
        // Set loading state
        _searchState.postValue(Result.Loading())
        val searchResultsList = mutableListOf<Article>()

        articlesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                searchResultsList.clear()
                for (articleSnapshot in dataSnapshot.children) {
                    val article = articleSnapshot.getValue(Article::class.java)

                    // Check if the article title or content contains the query
                    if (article != null && (article.title.contains(query, ignoreCase = true) || article.keyword.any { it.contains(query, ignoreCase = true) })) {
                        searchResultsList.add(article)
                    }
                }
                _searchState.postValue(Result.Success(searchResultsList))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur during the read operation
                _searchState.postValue(Result.Error(databaseError.message))
            }
        })
    }
    fun fetchUserArticle(currentUser: String) {
        val query: Query = articlesRef.orderByChild("userId").equalTo(currentUser)
        val articlesList: ArrayList<Article> = ArrayList()
        userArticlesLiveData.postValue(Result.Loading())
        // Add a ValueEventListener to the reference
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                articlesList.clear() // Clear the previous data
                for (articleSnapshot in dataSnapshot.children) {
                    val article = articleSnapshot.getValue(Article::class.java)
                    article?.let {
                       articlesList.add(it)
                    }
                }

                userArticlesLiveData.postValue(Result.Success(articlesList))

            }

            override fun onCancelled(databaseError: DatabaseError) {
                userArticlesLiveData.postValue(Result.Error(databaseError.message))

            }
        })
    }

    private fun fetchAllArticle() {
        articlesLiveData.postValue(Result.Loading())
        val articlesList: ArrayList<Article> = ArrayList()

        // Add a ValueEventListener to the reference
        articlesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                articlesList.clear() // Clear the previous data
                for (articleSnapshot in dataSnapshot.children) {
                    val article = articleSnapshot.getValue(Article::class.java)
                    article?.let {
                       articlesList.add(it)
                    }
                }

                articlesLiveData.postValue(Result.Success(articlesList))

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
                articlesLiveData.postValue(Result.Error(databaseError.message))
            }
        })
    }

    private fun retrieveCategoryData() {
        val categoryNamesList: ArrayList<String> = ArrayList()

        categoriesLiveData.postValue(Result.Loading())
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                categoryNamesList.clear() // Clear the list before adding new data
                for (categorySnapshot in dataSnapshot.children) {
                    val categoryName = categorySnapshot.child("name").getValue(String::class.java)
                    categoryName?.let {
                        categoryNamesList.add(it)
                    }

                }
                categoriesLiveData.postValue(Result.Success(categoryNamesList.toTypedArray()))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that may occur
                categoriesLiveData.postValue(Result.Error(databaseError.message))

            }
        })
    }

    fun fetchCategoryArticle(category: String) {
        articlesCategoriesLiveData.postValue(Result.Loading())


        val query: Query = if (category == "Semua") {
            articlesRef.limitToLast(50)
        } else {
            articlesRef.orderByChild("category").equalTo(category).limitToLast(30)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val articlesList: ArrayList<Article> = ArrayList()
                for (articleSnapshot in dataSnapshot.children) {
                    val article = articleSnapshot.getValue(Article::class.java)
                    article?.let {
                        articlesList.add(it)
                    }
                }
                articlesCategoriesLiveData.postValue(Result.Success(articlesList.reversed()))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                articlesCategoriesLiveData.postValue(Result.Error(databaseError.message))
            }
        })

    }

    fun deleteRefBucket(path: String) {
        val fileToDeleteRef: StorageReference = storage.getReferenceFromUrl(
            StorageUtil.pathToReference(path)
            .toString())

        // Use delete() to delete the file from Firebase Storage
        fileToDeleteRef.delete()
    }

    fun deleteArticle(articleId: String) {
        val articleToDeleteRef: DatabaseReference = articlesRef.child(articleId)

        articleToDeleteRef.removeValue()
            .addOnSuccessListener {
                // Delete success
                screenStatesLiveData.postValue(Result.Success(true))

            }
            .addOnFailureListener {
                // Delete failed

            }
    }


    companion object {
        private const val PAGE_SIZE = 10
    }
}