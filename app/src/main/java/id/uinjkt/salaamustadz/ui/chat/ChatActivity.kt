package id.uinjkt.salaamustadz.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.chat.Chat
import id.uinjkt.salaamustadz.data.models.chat.ChatRooms
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.ActivityChatBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.chat.ChatAdapter
import id.uinjkt.salaamustadz.ui.consult.ConsultReviewViewModel
import id.uinjkt.salaamustadz.utils.ChatAvailability
import id.uinjkt.salaamustadz.utils.ChatAvailability.*
import id.uinjkt.salaamustadz.utils.ConfirmType
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.SharedUtils
import id.uinjkt.salaamustadz.utils.SuccessType
import id.uinjkt.salaamustadz.utils.audio.uikit.VoiceSenderDialog
import id.uinjkt.salaamustadz.utils.audio.worker.AudioRecordListener
import id.uinjkt.salaamustadz.utils.capitalizeWords
import id.uinjkt.salaamustadz.utils.dialog.BottomSheetDeleteDialog
import id.uinjkt.salaamustadz.utils.dialog.ConfirmDialog
import id.uinjkt.salaamustadz.utils.dialog.EndChatDialog
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog
import id.uinjkt.salaamustadz.utils.dialog.SendConsultReviewDialog
import id.uinjkt.salaamustadz.utils.dialog.SuccessDialog
import id.uinjkt.salaamustadz.utils.dialog.ViewConsultReviewDialog
import id.uinjkt.salaamustadz.utils.emptyString
import id.uinjkt.salaamustadz.utils.firebase.ConversationListener
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.image.CompressImage
import id.uinjkt.salaamustadz.utils.image.FileUtil
import id.uinjkt.salaamustadz.utils.show
import id.uinjkt.salaamustadz.utils.toast
import id.uinjkt.salaamustadz.viewmodelfactory.ChatViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class ChatActivity : BaseChatActivity(), AudioRecordListener {
    private var _binding: ActivityChatBinding? = null
    private val binding get() = _binding as ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatRoomId: String
    private lateinit var userId: String
    private lateinit var senderId: String
    private lateinit var receiverId: String
    private var imageUrl: String? = null
    private lateinit var adapter: ChatAdapter
    private var senderSet = HashSet<Int>()
    private var receiverSet = HashSet<Int>()
    private var replyPos: Long = 0
    private var replyTextChat: String? = ""
    private var senderDetails: User? = null
    private var receiverDetails: User? = null
    private var chatRooms: ChatRooms? = null
    private lateinit var currentPath: String
    private var getFile: File? = null
    private lateinit var conversationListener: ConversationListener
    private lateinit var progressDialog: ProgressDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        userId = FirebaseAuth.getInstance().uid!!
        val id1 = intent.getStringExtra(Constants.KEY_SENDER_ID)!!
        val id2 = intent.getStringExtra(Constants.KEY_RECEIVER_ID)!!
        chatRoomId = intent.getStringExtra(Constants.KEY_CHAT_ROOM_ID)!!
        if(userId == id1) {
            senderId = id1
            receiverId = id2
        } else {
            senderId = id2
            receiverId = id1
        }
        viewModel = ViewModelProvider(this, ChatViewModelFactory(senderId,receiverId,chatRoomId))[ChatViewModel::class.java]
        viewModel.chatLiveData.observe(this) { state ->
            processChatList(state)
        }
        viewModel.chatRoomsLiveData.observe(this) { state -> //this chatroom
            processReceiverDetails(state)
            chatRooms = state.data
        }
        viewModel.senderDetailLiveData.observe(this) { state ->
            senderDetails = state.data
            processSenderOnChatStatus()
        }

        viewModel.receiverDetailLiveData.observe(this) { state ->
            receiverDetails = state.data
            processReceiverAvailabilityStatus()
            processDetailUser()
        }
        setListeners()
        activityListener()

        binding.chatscreenIvVerticaldots.setOnClickListener {v ->
            showMenu(v, R.menu.option_menu_pop_up)
        }

        cancelNotification()
        conversationListener = ConversationListener(chatRoomId){
            viewModel.updateChatAvailable(COMPLETED.status)
            viewModel.updateEndBy("sistem")
            Timber.tag("finishTime").d("done")
        }

    }

    override fun onResume() {
        super.onResume()
        cancelNotification()
    }
    private fun cancelNotification(){
        if (SharedUtils.getSavedList(this, chatRoomId).isNotEmpty()){
            for (id in SharedUtils.getSavedList(this, chatRoomId)){
                val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(chatRoomId, id)
            }
            SharedUtils.removeSingleValueString(this, chatRoomId)
        }
    }
    private fun showMenuEnd(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Respond to menu item click.
            when (menuItem.itemId) {
                R.id.end_chat -> {
                    EndChatDialog(this) {
                        viewModel.updateChatAvailable(COMPLETED.status)
                        viewModel.updateEndBy("jamaah")
                        viewModel.updateEndTime(Timestamp(Date()))
                    }.show(supportFragmentManager, "EndChatDialog")
                }

                else -> {}
            }

            true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Respond to menu item click.
            when (menuItem.itemId) {
                R.id.option_1 -> {
                    val listOfText = mutableListOf<String>()

                    val list = adapter.getSelectedItems()
                    val separator = "\n"
                    for (chat in list){
                        listOfText.add(chat.text.toString())
                    }
                    val stringText = listOfText.joinToString(separator)
                    Timber.tag("copy").d(stringText)

                    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("message", stringText)
                    clipboard.setPrimaryClip(clip)
                }

                else -> {}
            }

            true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }

    private fun processSenderOnChatStatus() {
        viewModel.updateStatusMessage(senderDetails?.onChatStatus!!)
    }

    private fun activityListener() {
        binding.chatsreenEtWritemessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.chatscreenIvMic.visibility = View.GONE
                binding.chatscreenIvPin.visibility = View.GONE
                if (binding.chatsreenEtWritemessage.text.toString().trim().isEmpty()){
                    binding.chatscreenIvMic.visibility = View.VISIBLE
                    binding.chatscreenIvPin.visibility = View.VISIBLE
                    binding.imgSend.setColorFilter(ContextCompat.getColor(applicationContext, R.color.gray))
//                    viewModel.updateTypingStatus("")
                } else {
                    binding.imgSend.setColorFilter(ContextCompat.getColor(applicationContext, R.color.dark_blue_primary))
//                    viewModel.updateTypingStatus("Typing...")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.chatsreenEtWritemessage.post {
                    binding.chatsreenEtWritemessage.height = (binding.chatsreenEtWritemessage.textSize.toInt()+1) * binding.chatsreenEtWritemessage.lineCount
                }
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListeners() {
        binding.chatscreenBtnSend.setOnClickListener {
            val text = binding.chatsreenEtWritemessage.text.toString().trim()
            if (text.isEmpty()){
                return@setOnClickListener
            }
            sendMessage(text, "text")
            showHeaderDefault()
            binding.replyCv.visibility = View.GONE

            binding.chatsreenEtWritemessage.setText("")
        }
        binding.chatscreenIvBack.setOnClickListener {
            finish()
        }
        binding.chatscreenIvDel.setOnClickListener {
            BottomSheetDeleteDialog(viewModel, senderSet, receiverSet, adapter) {
                showHeaderDefault()
            }.show(supportFragmentManager, "DeleteMessage")
        }
        binding.chatscreenIvReply.setOnClickListener {
            reply()
        }
        binding.replyCancel.setOnClickListener {
            replyCancel()
        }
        binding.chatscreenIvPin.setOnClickListener {
            showAttachmentDialog()
        }
        binding.chatscreenIvMic.setOnClickListener {view ->
            onVoiceRecorder()
        }
        binding.chatscreenIvDots.setOnClickListener {
            showMenuEnd(it, R.menu.end_menu_pop_up)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAttachmentDialog() {
        val bottomSheet = BottomSheetDialog(this, R.style.bottomSheetStyle_attachmentpopup)
        bottomSheet.setContentView(R.layout.attachmentbottomsheet_dialog)
        bottomSheet.show()
        val gallery = bottomSheet.findViewById<CardView>(R.id.attachmentpopup_cv_gallery)
        val camera = bottomSheet.findViewById<CardView>(R.id.attachmentpopup_cv_camera)

        gallery?.setOnClickListener {
            galleryPicker()
            bottomSheet.dismiss()
        }

        camera?.setOnClickListener {
            openCamera()
            bottomSheet.dismiss()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        FileUtil.createTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@ChatActivity,
                "id.uinjkt.salaamustadz",
                it
            )
            currentPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraIntentLauncher.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private val cameraIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPath)
            getFile = myFile

            CompressImage(this@ChatActivity)
                .compressToFileAsFlowable(myFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { file ->
                    StorageUtil.uploadMessageCamera(senderId, file) { imagePath ->
                        Timber.tag("imageMessage").d(imagePath)
                        sendMessage(imagePath, "image")
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun galleryPicker(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePicker.launch(intent)
    }
    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            // Perform cropping operation on the selected image
            selectedImageUri?.let { uri ->
                openCropActivity(uri)
            }
        }
    }

    private fun openCropActivity(imageUri: Uri) {
        // Start the crop activity using the uCrop library
        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(this, R.color.soft_yellow_green))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.hard_yellow_green))

        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        UCrop.of(imageUri, destinationUri)
            .withOptions(options)
            .start(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val croppedImageUri = UCrop.getOutput(data!!)

            CompressImage(this@ChatActivity)
                .compressToFileAsFlowable(FileUtil.from(this, croppedImageUri!!))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { file ->

                    StorageUtil.uploadMessageImage(senderId, Uri.fromFile(file)) { imagePath ->
                        Timber.tag("imageMessage").d(imagePath)
                        sendMessage(imagePath, "image")
                    }
                }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage(text: String, type: String) {
        val messageNumber: Long = 0
        var replyAttached = false
        var replyTo = emptyString()
        var replyBy = emptyString()
        var replyText = emptyString()
        if(binding.replyCv.visibility==View.VISIBLE) {
            replyAttached = true
            replyTo = if(binding.replyName.text.toString()=="Anda") userId
            else receiverId
            replyBy = userId
            replyText = replyTextChat.toString()
        }
        val chat = Chat("",senderId,text, Timestamp(Date()),messageNumber,type,"Sent", false,"",ArrayList(),replyAttached,replyTo,replyBy,replyPos,replyText)
        viewModel.sendMessage(chat)
        if(!receiverDetails?.onlineStatus!!) {
            try {
                val notificationId: Int = Random().nextInt()
                senderDetails?.name = if (chatRooms?.anonymousSender == true){
                    "Hamba Allah"
                } else {
                    senderDetails?.name.toString()
                }
                viewModel.sendNotification(
                    receiverDetails?.fcmToken!!,
                    userId,
                    chatRoomId,
                    senderDetails?.name.toString(),
                    notificationId.toString(),
                    binding.chatsreenEtWritemessage.text.toString(),
                    receiverDetails?.fcmToken!!,
                    typeMessage = type
                )
            } catch (e: Exception) {
                applicationContext.toast(e.message!!)
            }
        }
    }

    private fun processReceiverDetails(state: Result<ChatRooms?>) {
        when(state) {
            is Result.Loading -> {

            }
            is Result.Success -> {
                if (state.data != null) {
                    if(state.data.senderId==userId) {
                        binding.chatscreenTvActivity.text = state.data.receiverActivity
                    } else {
                        if (state.data.senderImage.isNullOrEmpty()){
                            binding.imageProfile.setImageResource(R.drawable.profile_user)
                        } else {
                            Glide
                                .with(this)
                                .load(StorageUtil.pathToReference(state.data.senderImage.toString()))
                                .centerCrop()
                                .placeholder(R.drawable.profile_user)
                                .into(binding.imageProfile)
                        }

                        binding.chatscreenTvName.text = state.data.senderName
                        binding.chatscreenTvActivity.text = state.data.senderActivity
                    }
                    binding.titleQuestion.text = state.data.titleQuestionSender

                    binding.btnCloseTitle.setOnClickListener {
                        binding.layoutTitleQuestion.visibility = View.GONE
                    }

                    when (ChatAvailability.getType(state.data.chatAvailability)){
                        PENDING -> {
                            showBottomChatAvailable("Mohon menunggu balasan, konsultasi akan terbuka sampai ustadz menjawab")
                        }
                        ON_GOING -> {
                            dismissBottomChatAvailable()
                            binding.btnReview.gone()
                        }
                        COMPLETED -> {
                            val t1 = state.data.endTime?.toDate()
                            val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
                            val t3 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(t1)
                            if (state.data.endBy.equals("sistem")) {
                                showBottomChatAvailable("Karena tidak ada percakapan dalam 2x24 jam. \n${state.data.endBy?.capitalizeWords()} menutup sesi ini " +
                                        "pada $t3 $t2.")
                            } else {
                                showBottomChatAvailable("Sesi konsultasi ini telah ditutup oleh ${state.data.endBy} \n" +
                                        "pada $t3 $t2.")
                            }
                            val data = state.data
                            consultReviewViewModel.getReview(chatRoomId)

                            consultReviewViewModel.statusConsultReviewLiveData.observe(this){ result ->
                                when (result){
                                    is Result.Error -> {}
                                    is Result.Loading -> {}
                                    is Result.Success -> {
                                        if (result.data != null){
                                            binding.btnReview.apply {
                                                show()
                                                text = getString(R.string.label_view_review)
                                                setOnClickListener {
                                                    ViewConsultReviewDialog(this@ChatActivity, result.data).show(supportFragmentManager, "ViewConsultReview")
                                                }
                                            }
                                        } else {
                                            binding.btnReview.apply {
                                                show()
                                                text = getString(R.string.label_give_review)
                                                senderDetails?.name = if (chatRooms?.anonymousSender == true){
                                                    "Hamba Allah"
                                                } else {
                                                    senderDetails?.name.toString()
                                                }
                                                setOnClickListener {
                                                    SendConsultReviewDialog(this@ChatActivity){ rating, textReview ->
                                                        ConfirmDialog(this@ChatActivity, ConfirmType.CONFIRM_SEND_REVIEW.type){
                                                            consultReviewViewModel.sendReview(
                                                                chatRoomId,
                                                                rating,
                                                                data.titleQuestionSender.toString(),
                                                                data.firstTextSender.toString(),
                                                                textReview,
                                                                senderId,
                                                                senderDetails?.name.toString(),
                                                                receiverId,
                                                                data.receiverName.toString(),
                                                            )
                                                            consultReviewViewModel.consultReviewLiveData.observe(this@ChatActivity){ result ->
                                                                when (result){
                                                                    is Result.Error -> {
                                                                        toast(result.message.toString())
                                                                    }
                                                                    is Result.Loading -> {
                                                                        progressDialog.startProgressDialog()
                                                                    }
                                                                    is Result.Success -> {
                                                                        progressDialog.dismissProgressDialog()
                                                                        SuccessDialog(this@ChatActivity, SuccessType.SEND_CONSULT_REVIEW.type){
                                                                        }.show(supportFragmentManager, "Success")
                                                                    }
                                                                }
                                                            }
                                                        }.show(supportFragmentManager, "ConfirmDialog")
                                                    }.show(supportFragmentManager, "ConsultReview")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    val data = state.data
                    if (data.endBy?.isEmpty() == true && data.chatAvailability == ON_GOING.status && data.firstTextReceiver?.isNotEmpty() == true){
                        conversationListener.startListening()

                    } else {
                        conversationListener.stopTimer()
                    }

                }
            }
            is Result.Error -> {

            }
        }
    }

    private val consultReviewViewModel: ConsultReviewViewModel by lazy {
        ViewModelProvider(this)[ConsultReviewViewModel::class.java]
    }

    private fun reply() {
        binding.replyCv.visibility = View.VISIBLE
        val selectedItems = adapter.getSelectedItems()
        if(selectedItems[0].fromId==senderId) {
            binding.replyBar.setBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_sender_color))
            binding.replyName.setTextColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_sender_color))
            binding.replyName.text = getString(R.string.label_you)
            replyPos = selectedItems[0].timestamp
            replyTextChat = selectedItems[0].text.toString()
            if (selectedItems[0].type.equals("image")) {
                binding.imgReply.visibility = View.VISIBLE
                binding.replyMsg.text = getString(R.string.label_photo)
                Glide.with(this)
                    .load(StorageUtil.pathToReference(selectedItems[0].text!!))
                    .placeholder(R.drawable.ic_image_black_24dp)
                    .into(binding.imgReply)
            } else if (selectedItems[0].type.equals("audio")) {
                binding.replyMsg.text = getString(R.string.label_voice_message)
                binding.imgReply.visibility = View.GONE
            } else {
                binding.imgReply.visibility = View.GONE
                binding.replyMsg.text = selectedItems[0].text
            }
        } else {
            binding.replyBar.setBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_receiver_color))
            binding.replyName.setTextColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_receiver_color))
            binding.replyName.text = binding.chatscreenTvName.text
            replyPos = selectedItems[0].timestamp
            replyTextChat = selectedItems[0].text.toString()
            if (selectedItems[0].type.equals("image")) {
                binding.imgReply.visibility = View.VISIBLE
                binding.replyMsg.text = getString(R.string.label_photo)
                Glide.with(this)
                    .load(StorageUtil.pathToReference(selectedItems[0].text!!))
                    .placeholder(R.drawable.ic_image_black_24dp)
                    .into(binding.imgReply)
            } else if (selectedItems[0].type.equals("audio")) {
                binding.replyMsg.text = getString(R.string.label_voice_message)
                binding.imgReply.visibility = View.GONE
            } else {
                binding.imgReply.visibility = View.GONE
                binding.replyMsg.text = selectedItems[0].text
            }
        }
        showHeaderDefault()
    }

    private fun replyCancel() {
        binding.replyCv.visibility = View.GONE
        adapter.getSelectedItems()
    }

    private fun processChatList(state: Result<List<Chat>?>) {
        when(state) {
            is Result.Loading -> {

            }
            is Result.Success -> {
                if (state.data != null) {
                    val rv = binding.chatscreenRvChat

                    adapter = ChatAdapter(
                        this, state.data,
                        imageUrl, binding.chatscreenTvName.text.toString(), rv, senderId
                    )
                    val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    if (state.data.size > 8) {
                        lm.stackFromEnd = true
                    }
                    rv.layoutManager = lm
                    rv.adapter = adapter
                    adapter.liveSenderSet.observe(this) { senderState ->
                        senderSet = senderState
                        processUi(state.data)
                    }
                    adapter.liveReceiverSet.observe(this) { receiverState ->
                        receiverSet = receiverState
                        processUi(state.data)
                    }
                    adapter.notifyItemInserted(state.data.size)
                    if (state.data.isNotEmpty()) {
                        rv.smoothScrollToPosition(state.data.size - 1)
                    }
                }
            }
            is Result.Error -> {

            }
        }
    }

    private fun processUi(chats: List<Chat>) {
        if(senderSet.size>0 || receiverSet.size>0) {
            showHeaderAlternate()
            adapter.liveSenderSet.observe(this){
                for (i in it) {
                    Timber.tag("positionAdapter").d("$i")
                    for(n in chats[i].delBy!!){
                        if (chats[i].delFor != "" && n == senderId) {
                            viewGoneDeletedMessage()
                        }

                    }
                    if (chats[i].type.equals("image") || chats[i].type.equals("audio") ){
                        binding.chatscreenIvVerticaldots.visibility = View.GONE
                    }
                }

            }

            adapter.liveReceiverSet.observe(this){
                for (i in it) {
                    Timber.tag("positionAdapter").d("$i")
                    for(n in chats[i].delBy!!){
                        if (chats[i].delFor != "" && n == senderId) {
                            viewGoneDeletedMessage()
                        }
                    }
                    if (chats[i].type.equals("image") || chats[i].type.equals("audio") ){
                        binding.chatscreenIvVerticaldots.visibility = View.GONE
                    }
                }

            }

            if(senderSet.size+receiverSet.size>1) {
                binding.chatscreenIvReply.visibility = View.GONE
            }

        } else {
            showHeaderDefault()
        }
    }

    private fun viewGoneDeletedMessage(){
        binding.chatscreenIvReply.visibility = View.GONE
        binding.chatscreenIvDel.visibility = View.GONE
    }


    private fun processReceiverAvailabilityStatus() {
        if(receiverDetails?.onlineStatus==true) {
            binding.chatscreenCvStatus.setCardBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.schatscreen_color_online))
        } else {
            binding.chatscreenCvStatus.setCardBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.schatscreen_color_offline))
        }
    }

    private fun processDetailUser() {
        imageUrl = receiverDetails?.image
        receiverDetails?.apply {
            binding.chatscreenTvName.text = role?.capitalizeWords().plus(" $name")
        }
        if (receiverDetails?.image.isNullOrEmpty()){
            binding.imageProfile.setImageResource(R.drawable.profile_user)
        } else {
            Glide
                .with(this)
                .load(StorageUtil.pathToReference(receiverDetails?.image.toString()))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(binding.imageProfile)
        }
    }

    private fun showBottomChatAvailable(textNotAvailable: String) {
        binding.bottomChatAvailable.visibility = View.VISIBLE
        binding.txtChatAvailable.text = textNotAvailable
        binding.chatscreenLlEtwritemsg.visibility = View.GONE
        binding.chatscreenBtnSend.visibility = View.GONE
        binding.chatscreenIvDots.isClickable = false
        binding.chatscreenIvDots.setColorFilter(getColor(R.color.soft_yellow_green))
    }

    private fun dismissBottomChatAvailable() {
        binding.bottomChatAvailable.visibility = View.GONE
        binding.chatscreenLlEtwritemsg.visibility = View.VISIBLE
        binding.chatscreenBtnSend.visibility = View.VISIBLE
        binding.chatscreenIvDots.isClickable = true
        binding.chatscreenIvDots.setColorFilter(getColor(R.color.dark_blue_primary))

    }

    private fun showHeaderDefault() {
        binding.chatscreenIvDots.visibility = View.VISIBLE
        binding.imageProfile.visibility = View.VISIBLE
        binding.chatscreenTvActivity.visibility = View.VISIBLE
        binding.chatscreenTvName.visibility = View.VISIBLE
        binding.chatscreenCvStatus.visibility = View.VISIBLE
        binding.chatscreenIvReply.visibility = View.GONE
        binding.chatscreenIvDel.visibility = View.GONE
        binding.chatscreenIvVerticaldots.visibility = View.GONE
    }

    private fun showHeaderAlternate() {
        binding.chatscreenIvDots.visibility = View.GONE
        binding.imageProfile.visibility = View.GONE
        binding.chatscreenTvActivity.visibility = View.GONE
        binding.chatscreenTvName.visibility = View.GONE
        binding.chatscreenCvStatus.visibility = View.GONE
        binding.chatscreenIvReply.visibility = View.VISIBLE
        binding.chatscreenIvDel.visibility = View.VISIBLE
        binding.chatscreenIvVerticaldots.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if(senderSet.size+receiverSet.size>0) {
            adapter.getSelectedItems()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onVoiceRecorder() {
        VoiceSenderDialog(this).show(supportFragmentManager, "VOICE")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAudioReady(audioUri: String?) {
        StorageUtil.uploadMessageVoice(userId, Uri.fromFile(File(audioUri.toString()))){ text ->
            sendMessage(text, "audio")
        }
        Timber.tag("onAudioReady").d(audioUri)
    }

    override fun onRecordFailed(errorMessage: String?) {
        Timber.tag("onRecordedFailed").d(errorMessage)
    }

    override fun onReadyForRecord() {
        Timber.tag("onReadyForRecord").d(onReadyForRecord().toString())
    }
}