package id.uinjkt.salaamustadz.data.models.article

import android.os.Parcelable
import id.uinjkt.salaamustadz.utils.emptyString
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    var articleId:  String? = null, // Nullable to set the ID later
    val imageUrl: String = emptyString(),
    val title: String = emptyString(),
    val category: String = emptyString(),
    val keyword: List<String> = emptyList(),
    val textArticle: String = emptyString(),
    val userId: String = emptyString(),
    val userName: String = emptyString(),
    val dateCreated: String = emptyString(),
    val dateUpdated: String = emptyString(),
    var imgProfile:  String? = null,
    var userRole: String? = emptyString(),
    var numberOfViews: Int? = 0,
    var favorite: Boolean = false // New field
) : Parcelable