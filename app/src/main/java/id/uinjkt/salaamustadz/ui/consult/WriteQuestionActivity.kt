package id.uinjkt.salaamustadz.ui.consult

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.models.chat.Chat
import id.uinjkt.salaamustadz.data.models.chat.ChatRooms
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.ActivityWriteQuestionBinding
import id.uinjkt.salaamustadz.ui.chat.ChatActivity
import id.uinjkt.salaamustadz.ui.chat.ChatViewModel
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import id.uinjkt.salaamustadz.utils.capitalizeWords
import id.uinjkt.salaamustadz.utils.dialog.AnonymousNameDialog
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog
import id.uinjkt.salaamustadz.utils.dialog.SendQuestionDialog
import id.uinjkt.salaamustadz.utils.parcelable
import id.uinjkt.salaamustadz.utils.toast
import id.uinjkt.salaamustadz.viewmodelfactory.ChatViewModelFactory
import timber.log.Timber
import java.util.Date
import java.util.Random

class WriteQuestionActivity : BaseActivity<ActivityWriteQuestionBinding>() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var database: FirebaseFirestore
    private lateinit var receiver: User
    private val senderid: String = FirebaseAuth.getInstance().uid!!
    private lateinit var sender: User
    private var senderName: String = ""
    private var imageSender: String? = ""
    private var isCheckedInput: Boolean? = false
    private lateinit var viewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }

    override fun getViewBinding(): ActivityWriteQuestionBinding {
        return ActivityWriteQuestionBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        binding.btnDonate.setOnClickListener {
            openUrlDonate()
        }
    }

    override fun setupUI() {
        receiver = intent.parcelable(Constants.KEY_USTADZ)!!
        binding.checkTermsPrivacy.setOnCheckedChangeListener { _, isChecked ->
            isCheckedInput = isChecked
        }
        progressDialog = ProgressDialog(this)
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(senderid).addSnapshotListener { value, _ ->
            sender = value?.toObject(User::class.java)!!
        }

        val titleQuestionText = binding.titleQuestion.text
        val txtQuestion = binding.txtQuestion.text
        binding.button.setOnClickListener {
            if (titleQuestionText?.isEmpty() == true) {
                applicationContext.toast("Judul tidak boleh kosong")
                return@setOnClickListener
            } else if (txtQuestion?.isEmpty() == true){
                applicationContext.toast("Pertanyaan tidak boleh kosong")
                return@setOnClickListener
            } else {
                SendQuestionDialog(this, onSuccessListener = {
                    progressDialog.startProgressDialog()
                    createChatRoom(titleQuestionText?.trim().toString(),  txtQuestion?.trim().toString())
                }).show(supportFragmentManager, "dialogWriteQuestion")

            }
        }

        binding.txtHideName.setOnClickListener {
            AnonymousNameDialog().show(supportFragmentManager, "PreviewNameAnon")
        }

        binding.backButton.setOnClickListener {
            finish()
        }

    }

    override fun setupAction() {
    }

    override fun setupProcess() {
    }

    override fun setupObserver() {
    }

    private fun openUrlDonate(){
        val uri: Uri = Uri.parse("${sharedPref.getString(Constants.KEY_URL_DONATE)}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("${sharedPref.getString(Constants.KEY_URL_DONATE)}")))
        }
    }
    private fun createChatRoom(titleQuestion: String, textQuestion: String) {
         if (isCheckedInput == true) {
             senderName = "Hamba Allah"
             imageSender = ""
        } else {
             senderName = sender.name.toString()
             imageSender = sender.image
        }
        val role = receiver.role?.capitalizeWords()
        val chatRoom = ChatRooms(
            receiverId = receiver.id,
            receiverName = role + " " + receiver.name,
            receiverImage = receiver.image,
            receiverThoughts = receiver.textStatus,
            timestamp = Timestamp(Date()),
            dateAdded = Timestamp(Date()),
            senderId = sender.id,
            senderName = senderName,
            senderImage = imageSender,
            senderThoughts = sender.textStatus,
            titleQuestionSender = titleQuestion, firstTextSender = textQuestion,
            lastMessageReadStatus = "Sent",
            anonymousSender = isCheckedInput)
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).add(chatRoom).addOnSuccessListener { it3 ->
            val hashmap: HashMap<String,Any> = HashMap()
            hashmap[Field.timestamp] = FieldValue.serverTimestamp()
            hashmap[Field.dateAdded] = FieldValue.serverTimestamp()
            hashmap[Field.id] = it3.id
            it3.update(hashmap)
            sendMessage(sender.id, it3.id, textQuestion)

            Timber.d("Add ChatRoom Successfully")
        }
    }

    private fun sendMessage(senderId: String?, chatRoomId: String, text: String) {
        val chat = Chat("",senderId,text, Timestamp(Date()),0,"text","Sent", false,"",ArrayList(),false,"","",0,"")
        sendChat(chat, senderId!!, chatRoomId)

    }



    private fun sendChat(chat: Chat, senderId:String, chatRoomId: String) {
        viewModel = ViewModelProvider(this, ChatViewModelFactory(senderid, receiver.id.toString(), chatRoomId))[ChatViewModel::class.java]
        if(!receiver.onlineStatus!!) {
            try {
                val notificationId: Int = Random().nextInt()

                viewModel.sendNotification(
                    receiver.fcmToken!!,
                    senderId,
                    chatRoomId,
                    senderName,
                    notificationId.toString(),
                    chat.text.toString(),
                    receiver.fcmToken!!,
                    typeMessage = "text"
                )
            } catch (e: Exception) {
                applicationContext.toast(e.message!!)
            }
        }
        var  unreadCount: Long
        var  messageNumber: Long
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).get().addOnSuccessListener {
            val room: ChatRooms = it.toObject(ChatRooms::class.java)!!
             unreadCount = room.unreadCount + 1
             messageNumber = room.messageNumber + 1
            chat.timestamp =  messageNumber
            val map = HashMap<String,Any>()
            val properties = chat.javaClass.declaredFields
            for (property in properties) {
                property.isAccessible = true
                map[property.name] = property.get(chat)!!
            }
            map[Field.datetime] = FieldValue.serverTimestamp()
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).collection(
                Constants.KEY_COLLECTION_CHAT
            ).add(map).addOnSuccessListener { doc ->
                val hashmap: HashMap<String,Any> = HashMap()
                hashmap[Field.id] = doc.id
                doc.update(hashmap)
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chatRoomId).update(
                    Field.lastText,chat.text,
                    Field.messageNumber, messageNumber, 
                    Field.timestamp, FieldValue.serverTimestamp(),
                    Field.lastTextFrom, senderId,
                    Field.unreadCount, unreadCount,
                    Field.lastMessageDelStatus,ArrayList<String>(),
                    Field.lastMessageId,doc.id)
                toChatActivity(chatRoomId)
            }
        }
    }

    private fun toChatActivity(chatRoomId: String){
        progressDialog.dismissProgressDialog()
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_SENDER_ID, senderid)
        intent.putExtra(Constants.KEY_RECEIVER_ID, receiver.id)
        intent.putExtra(Constants.KEY_CHAT_ROOM_ID, chatRoomId)
        startActivity(intent)
        finish()
    }
}