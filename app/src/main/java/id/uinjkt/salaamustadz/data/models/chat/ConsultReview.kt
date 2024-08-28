package id.uinjkt.salaamustadz.data.models.chat

import com.google.firebase.Timestamp
import id.uinjkt.salaamustadz.utils.emptyString
import java.util.Date

data class ConsultReview (
    val id: String? = emptyString(),
    val chatRoomId: String? = emptyString(),
    val rating: Float = 0f,
    val titleQuestion: String? = emptyString(),
    val question: String? = emptyString(),
    val textReview: String? = emptyString(),
    val senderId: String? = emptyString(),
    val senderName: String? = emptyString(),
    val ustadzId: String? = emptyString(),
    val ustadzName: String? = emptyString(),
    val createdAt: Timestamp? = Timestamp(Date()),
    val updatedAt: Timestamp? = Timestamp(Date())
)