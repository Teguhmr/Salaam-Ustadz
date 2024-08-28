package id.uinjkt.salaamustadz.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.data.models.chat.Chat
import id.uinjkt.salaamustadz.data.models.chat.ChatRooms
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.data.remote.api.ApiClient
import id.uinjkt.salaamustadz.data.remote.api.ApiService
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatViewModel(private val senderId: String, private val receiverId: String, private var chatRoomId: String): ViewModel() {
    private lateinit var database: FirebaseFirestore

    private var chatsLiveData: MutableLiveData<Result<List<Chat>?>> = MutableLiveData()
    val chatLiveData: LiveData<Result<List<Chat>?>>
        get() = chatsLiveData

    private var chatRoomLiveData: MutableLiveData<Result<ChatRooms?>> = MutableLiveData()
    val chatRoomsLiveData: LiveData<Result<ChatRooms?>>
        get() = chatRoomLiveData

    private var senderDetailsLiveData: MutableLiveData<Result<User?>> = MutableLiveData()
    val senderDetailLiveData: LiveData<Result<User?>>
        get() = senderDetailsLiveData

    private var receiverDetailsLiveData: MutableLiveData<Result<User?>> = MutableLiveData()
    val receiverDetailLiveData: LiveData<Result<User?>>
        get() = receiverDetailsLiveData

    init {
        fetchSenderDetail()
        fetchReceiverDetail()
        fetchReceiverDetails()
        fetchPreviousChats()

    }

    private fun fetchPreviousChats() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener { documentRoom ->
            val room = documentRoom.toObject(ChatRooms::class.java)
            val lastMessageNumber: Long = if(senderId==room?.senderId) {
                room.senderLastMessageNumber
            } else {
                room?.receiverLastMessageNumber!!
            }
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).addSnapshotListener { documentChats, error ->
                if(error==null && documentChats!=null) {
                    val chats = mutableListOf<Chat>()
                    for (document in documentChats) {
                        val chat = document.toObject(Chat::class.java)
                        if (chat.timestamp > lastMessageNumber && chat.datetime != null) {
                            if (chat.delFor == "Me") {
                                if (chat.delBy?.contains(senderId)!!) chat.text =
                                    "Pesan dihapus untuk Anda"
                            } else if (chat.delFor == "Everyone") {
                                chat.text = "Pesan ini telah dihapus"
                            }
                            chat.id = document.id
                            if (chat.status == "Delivered") {
                                document.reference.update(Field.status, "Read")
                            }
                            chats.add(chat)
                        }

                    }

                    val sortedChats = chats.sortedBy { it.timestamp }
                    chatsLiveData.postValue(Result.Success(sortedChats))
                }
            }
        }
    }

    fun deleteMessage(id: String, flag: String) {
        if(flag=="Everyone") {
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener {
                val room = it.toObject(ChatRooms::class.java)
                if(room?.lastMessageId==id) {
                    val map: HashMap<String,Any> = HashMap()
                    room.lastMessageDelStatus?.add(senderId)
                    room.lastMessageDelStatus?.add(receiverId)
                    map[Field.lastMessageDelStatus] = room.lastMessageDelStatus!!
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(map)
                }
            }
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).document(id).get().addOnSuccessListener {
                val chat = it.toObject(Chat::class.java)
                val map: HashMap<String, Any> = HashMap()
                chat?.delBy?.add(senderId)
                chat?.delBy?.add(receiverId)
                map[Field.delFor] = flag
                map[Field.delBy] = chat?.delBy!!
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).document(id).update(map)
            }
        } else {
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener {
                val room = it.toObject(ChatRooms::class.java)
                if(room?.lastMessageId==id) {
                    val map: HashMap<String,Any> = HashMap()
                    room.lastMessageDelStatus?.add(senderId)
                    map[Field.lastMessageDelStatus] = room.lastMessageDelStatus!!
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(map)
                }
            }
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).document(id).get().addOnSuccessListener {
                val chat = it.toObject(Chat::class.java)
                val  map: HashMap<String,Any> = HashMap()
                chat?.delBy?.add(senderId)
                map[Field.delFor] = flag
                map[Field.delBy] = chat?.delBy!!
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).document(id).update(map)
            }
        }
    }

    fun updateStatusMessage(senderStatus: Boolean) {
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Field.fromId, receiverId)
            .whereEqualTo(Field.status, "Sent").get().addOnSuccessListener { document ->
                for (doc in document) {
                    if (senderStatus) {
                        val  map: HashMap<String,Any> = HashMap()
                        map[Field.status] = "Read"
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).document(doc.id).update(map)

                        updateLastStatusMessage(doc.id)
                        updateReadCount()
                    }
                }


        }

    }
    fun sendNotification(
        token: String,
        userId: String,
        chatRoomId: String,
        name: String,
        notificationId: String,
        message: String,
        fcmToken: String,
        typeMessage: String
    ) {
        ApiClient.getInstance().create(ApiService::class.java).sendMessage(token, userId, chatRoomId, name, notificationId, message, fcmToken, typeMessage).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) {
                    try {
                        if(response.body()!=null) {
                            val responseJson = JSONObject(response.body()!!)
                            val results: JSONArray = responseJson.getJSONArray("results")
                            if(responseJson.getInt("failure")==1) {
                                results.get(0) as JSONObject
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    //TODO
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
            }

        })
    }

    private fun updateLastStatusMessage(id: String) {
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener {
            val room = it.toObject(ChatRooms::class.java)
            if(room?.lastMessageId==id) {
                val map: HashMap<String,Any> = HashMap()
                map[Field.lastMessageReadStatus] = "Read"
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(map)
            }
        }
    }

    fun updateTypingStatus(status: String) {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener {
            val room = it.toObject(ChatRooms::class.java)
            if(senderId==room?.senderId)
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update("sender_activity",status)
            else if(senderId==room?.receiverId)
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update("receiver_activity",status)
        }
    }

    private fun updateReadCount() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener {
            val doc = it.toObject(ChatRooms::class.java)
            if(doc?.lastTextFrom == receiverId) {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it.id).update(Field.unreadCount,0)
            }
        }
    }

    fun updateChatAvailable(availabilityChat: Int) {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(Field.chatAvailability, availabilityChat)
    }


    fun updateEndBy(endBy: String) {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(Field.endBy, endBy)
    }

    fun updateEndTime(endTime: Timestamp) {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(Field.endTime, endTime)
    }


    private fun fetchReceiverDetails() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).addSnapshotListener { document1, _ ->
            val room = document1?.toObject(ChatRooms::class.java)
            chatRoomLiveData.postValue(Result.Success(room))
        }
    }

    private fun fetchSenderDetail() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(senderId).addSnapshotListener { value, _ ->
            val sender = value?.toObject(User::class.java)
            senderDetailsLiveData.postValue(Result.Success(sender))
        }
    }

    private fun fetchReceiverDetail() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(receiverId).addSnapshotListener { value, _ ->
            val receiver = value?.toObject(User::class.java)
            receiverDetailsLiveData.postValue(Result.Success(receiver))
        }
    }


    fun sendMessage(chat: Chat) {
        var unreadCount: Long
        var messageNumber: Long
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener {
            val room: ChatRooms = it.toObject(ChatRooms::class.java)!!
            unreadCount = room.unreadCount + 1
            messageNumber = room.messageNumber + 1
            chat.timestamp = messageNumber
            val map = HashMap<String,Any>()
            map[Field.datetime] = FieldValue.serverTimestamp()
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(Constants.KEY_COLLECTION_CHAT).add(chat).addOnSuccessListener { doc ->
                val hashmap: HashMap<String,Any> = HashMap()
                hashmap[Field.id] = doc.id
                doc.update(hashmap)
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(
                    Field.lastText,chat.text,
                    Field.messageNumber,messageNumber,
                    Field.timestamp, FieldValue.serverTimestamp(),
                    Field.lastTextFrom,senderId,
                    Field.unreadCount,unreadCount,
                    Field.lastMessageDelStatus,ArrayList<String>(),
                    Field.lastMessageId,doc.id,
                    Field.lastMessageReadStatus, chat.status)
            }
        }
    }

}