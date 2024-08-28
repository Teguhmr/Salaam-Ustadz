package id.uinjkt.salaamustadz.data.models.chat

import com.google.firebase.Timestamp
import id.uinjkt.salaamustadz.utils.emptyString
import java.util.Date

data class ChatRooms(
    var id: String? = emptyString(),
    var receiverId: String? = emptyString(),
    var receiverName: String? = emptyString(),
    var receiverImage: String? = emptyString(),
    var receiverActivity: String? = emptyString(),
    var receiverThoughts: String? = emptyString(),

    var lastText: String? = emptyString(),
    var lastTextFrom: String? = emptyString(),
    var timestamp: Timestamp? = Timestamp(Date()),
    var dateAdded: Timestamp? = Timestamp(Date()),
    var messageNumber: Long = 0L,

    var senderId: String? = emptyString(),
    var senderName: String? = emptyString(),
    var senderImage: String? = emptyString(),
    var senderActivity: String? = emptyString(),
    var senderThoughts: String? = emptyString(),

    var unreadCount: Long = 0L,
    var lastMessageDelStatus: ArrayList<String>? = ArrayList(),
    var lastMessageId: String? = emptyString(),

    var senderLastMessageNumber: Long = 0L,
    var receiverLastMessageNumber: Long = 0L,

    var titleQuestionSender: String? = emptyString(),
    var firstTextSender: String? = emptyString(),
    var firstTextReceiver: String? = emptyString(),
    var lastMessageReadStatus: String? = emptyString(),
    var chatAvailability: Int? = 0,
    var endTime: Timestamp? = null,
    var endBy: String? = emptyString(),

    var anonymousSender: Boolean? = false
)
