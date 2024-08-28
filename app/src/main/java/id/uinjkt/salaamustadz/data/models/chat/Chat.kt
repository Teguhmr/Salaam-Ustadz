package id.uinjkt.salaamustadz.data.models.chat

import com.google.firebase.Timestamp
import id.uinjkt.salaamustadz.utils.emptyString
import java.util.Date

data class Chat(
    var id: String? = emptyString(),
    var fromId: String? = emptyString(),
    var text: String? = emptyString(),
    var datetime: Timestamp? = Timestamp(Date()),
    var timestamp: Long = 0L,
    var type: String? = emptyString(),
    var status: String? = emptyString(),
    var isSelected: Boolean = false,
    var delFor: String? = emptyString(),
    var delBy: ArrayList<String>? = ArrayList(),
    var replyAttached: Boolean = false,
    var replyTo: String? = emptyString(),
    var replyId: String? = emptyString(),
    var replyPos: Long = 0L,
    var replyText: String? = emptyString()
)

