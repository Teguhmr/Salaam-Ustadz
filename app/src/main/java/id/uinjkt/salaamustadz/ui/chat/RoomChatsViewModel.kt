package id.uinjkt.salaamustadz.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import id.uinjkt.salaamustadz.data.models.chat.ChatRooms
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import id.uinjkt.salaamustadz.utils.capitalizeWords
import timber.log.Timber

class RoomChatsViewModel(private val senderId: String, private val receiverId: String) : ViewModel() {
    private var chatsLiveData: MutableLiveData<Result<List<ChatRooms>?>> = MutableLiveData(Result.Loading())
    val chatLiveData: LiveData<Result<List<ChatRooms>?>>
        get() = chatsLiveData

    private var senderDetailsLiveData: MutableLiveData<Result<User?>> = MutableLiveData()
    val senderDetailLiveData: LiveData<Result<User?>>
        get() = senderDetailsLiveData
    init {
        fetchChatList()
        fetchSenderDetail()
    }

    private fun fetchChatList() {
        val database = FirebaseFirestore.getInstance()
        val usersRef = database.collection(Constants.KEY_COLLECTION_USER)
        val users = mutableListOf<ChatRooms>()

        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo(
            Field.senderId,senderId).orderBy(Field.timestamp, Query.Direction.DESCENDING).addSnapshotListener { documents1, error ->
            if(error!=null) {
                chatsLiveData.postValue(Result.Error(error.toString()))
                Timber.tag(error.message.toString()).e("Error Getting the Users")
            } else {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo(Field.receiverId,senderId).orderBy(Field.timestamp, Query.Direction.DESCENDING).addSnapshotListener { documents2, error2 ->
                    if(error2!=null) {
                        chatsLiveData.postValue(Result.Error(error2.toString()))
                        Timber.tag(error2.message.toString()).e("Error Getting the Users")
                    } else {
                        users.clear()
                        val documents = documents1?.documents?.union(documents2?.documents!!)
                        for (document in documents!!) {
                            val chatRoom = document.toObject(ChatRooms::class.java)
                            usersRef.whereEqualTo(Field.id, chatRoom?.receiverId)
                                .addSnapshotListener { profileSnapshot, error ->
                                    if (error != null) {
                                        Timber.tag("Error").w("Listen Failed $error")
                                    }
                                    if (profileSnapshot != null && !profileSnapshot.isEmpty ) {
                                        for (profile in profileSnapshot.documents) {
                                            val currentProfileReceiver = profile.toObject(User::class.java)
                                            if (chatRoom?.receiverId.equals(currentProfileReceiver?.id))
                                                currentProfileReceiver?.apply {
                                                    chatRoom?.receiverImage = image
                                                    chatRoom?.receiverName = role?.capitalizeWords().plus(" $name")
                                                }

                                        }
                                    }
                                }
                            if((senderId==chatRoom?.senderId && chatRoom.messageNumber>chatRoom.senderLastMessageNumber) || (senderId==chatRoom?.receiverId && chatRoom.messageNumber>chatRoom.receiverLastMessageNumber)) {
                                users.add(chatRoom)
                            }

                        }
                        chatsLiveData.postValue(Result.Success(users))
                    }
                }
            }
        }
    }

    fun deleteChatRooms(documents: List<String>) {
        val database = FirebaseFirestore.getInstance()
        for(id in documents) {
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(id).get().addOnSuccessListener {
                val room = it.toObject(ChatRooms::class.java)
                val map: HashMap<String,Any> = HashMap()
                if(senderId==room?.senderId) {
                    map[Field.senderLastMessageNumber] = room.messageNumber
                } else {
                    map[Field.receiverLastMessageNumber] = room?.messageNumber!!
                }
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(id).update(map)
            }
        }
    }

    private fun fetchSenderDetail() {
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(senderId).addSnapshotListener { value, _ ->
            val sender = value?.toObject(User::class.java)
            senderDetailsLiveData.postValue(Result.Success(sender))
        }
    }


}