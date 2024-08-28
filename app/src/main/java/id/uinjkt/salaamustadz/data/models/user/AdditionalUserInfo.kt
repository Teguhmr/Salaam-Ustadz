package id.uinjkt.salaamustadz.data.models.user

import android.os.Parcelable
import id.uinjkt.salaamustadz.utils.emptyString
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdditionalUserInfo(
    var birthPlace: String? = emptyString(),
    var education: String? = emptyString(),
    var knowledgeField: List<String>? = emptyList(),
    var lecturer: String? = emptyString(),
    var papers: String? = emptyString()
): Parcelable
