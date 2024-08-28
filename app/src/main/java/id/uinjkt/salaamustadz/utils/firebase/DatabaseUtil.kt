package id.uinjkt.salaamustadz.utils.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.uinjkt.salaamustadz.data.models.article.Article
import timber.log.Timber

object DatabaseUtil {
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val categoriesRef: DatabaseReference = database.reference.child("categories")
    private val articlesReference = database.reference.child("articles")


    fun updateFavoriteStatus(userId: String, article: Article, onSuccess:(newFavoriteStatus: Boolean) -> Unit) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersRef: DatabaseReference = database.getReference("users")

        val userFavoriteRef: DatabaseReference =
            usersRef.child(userId).child("favorites_article").child(article.articleId ?: "")
        val newFavoriteStatus = !article.favorite
        userFavoriteRef.setValue(newFavoriteStatus)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update the article's favorite status locally
                    article.favorite = newFavoriteStatus
                    // Update the toolbar action icon based on the new favorite status
                    onSuccess.invoke(newFavoriteStatus)

                    // If unfavoriting, remove the entry from the favorites list
                    if (!newFavoriteStatus){
                        userFavoriteRef.removeValue()
                    }

                } else {
                    // Handle the error here if needed
                }
            }


    }

    fun getFavoriteStatus(userId: String, onSuccess: (userFavorites: MutableMap<String, Boolean>) -> Unit){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersRef: DatabaseReference = database.getReference("users")

        // Retrieve the user's favorites using a ValueEventListener
        usersRef.child(userId).child("favorites_article").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userFavorites: MutableMap<String, Boolean> = mutableMapOf()
                var articleId: String?
                for (articleSnapshot in dataSnapshot.children) {
                    articleId = articleSnapshot.key
                    val isFavorite = articleSnapshot.value as? Boolean ?: false
                    articleId?.let {
                        userFavorites[it] = isFavorite
                    }
                }
                onSuccess.invoke(userFavorites)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.e("Error: ${databaseError.message}")
            }
        })

    }

    interface FavoriteArticleListener {
        fun onLoading()
        fun onFavoriteArticlesFetched(articles: List<Article>)
        fun onFetchError(error: String)
    }

    fun fetchFavoriteArticleData(
        userFavorites: Map<String, Boolean>,
        listener: FavoriteArticleListener
    ) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val articlesRef: DatabaseReference = database.getReference("articles")

        val favoriteArticleIds = userFavorites.filterValues { it }.keys.toList()
        val favoriteArticles = mutableListOf<Article>()

        listener.onLoading()
        articlesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                favoriteArticles.clear()
                for (articleSnapshot in dataSnapshot.children) {
                    val article = articleSnapshot.getValue(Article::class.java)
                    if (article != null && article.articleId in favoriteArticleIds) {
                        favoriteArticles.add(article)
                    }
                }

                listener.onFavoriteArticlesFetched(favoriteArticles)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFetchError(databaseError.message)
            }
        })
    }

    fun incrementNumberOfViews(articleId: String) {
        val articleReference = articlesReference.child(articleId)

        // Retrieve the current value of numberOfViews
        articleReference.child("numberOfViews").get().addOnSuccessListener { dataSnapshot ->
            val currentViews = dataSnapshot.getValue(Int::class.java)

            // Increment the value of numberOfViews by 1
            val newViews = currentViews?.plus(1) ?: 1

            // Update the numberOfViews field in the database
            articleReference.child("numberOfViews").setValue(newViews)
                .addOnSuccessListener {
                    // The numberOfViews was successfully updated
                    Timber.d("Firebase: numberOfViews incremented successfully!")
                }
                .addOnFailureListener { e ->
                    // An error occurred while updating numberOfViews
                    Timber.e("Firebase: Error incrementing numberOfViews: ${e.message}")
                }
        }.addOnFailureListener { e ->
            // An error occurred while retrieving the current value of numberOfViews
            Timber.e("Firebase: Error retrieving numberOfViews: ${e.message}")
        }
    }
}