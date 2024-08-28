package id.uinjkt.salaamustadz.data.models.user

import android.os.Parcelable
import com.google.firebase.Timestamp
import id.uinjkt.salaamustadz.utils.emptyString
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User(
    var id: String? = emptyString(),
    var name: String? = emptyString(),
    var email: String? = emptyString(),
    var password: String? = emptyString(),
    var role: String? = emptyString(),
    var birthDate: String? = emptyString(),
    var phone: String? = emptyString(),
    var createdAt: Timestamp? = Timestamp(Date()),
    var updatedAt: Timestamp? = Timestamp(Date()),
    var image: String? = emptyString(),
    var gender: String? = emptyString(),
    var fcmToken: String? = emptyString(),
    var onlineStatus: Boolean? = false,
    var onChatStatus: Boolean? = false,
    var lastSeen: Timestamp? = null,
    var textStatus: String? = emptyString(),
    var additionalInfo: AdditionalUserInfo? = null,
    var imageBg: String? = emptyString()

): Parcelable