package id.uinjkt.salaamustadz.data.models.pray

import android.os.Parcelable
import id.uinjkt.salaamustadz.utils.emptyString
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pray(
    val resImage:Int = 0,
    val txtTitleHeader: String = emptyString(),
    val txtBody: String = emptyString(),
): Parcelable


