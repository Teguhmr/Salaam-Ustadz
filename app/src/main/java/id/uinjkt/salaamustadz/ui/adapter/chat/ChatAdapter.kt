package id.uinjkt.salaamustadz.ui.adapter.chat

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.chat.Chat
import id.uinjkt.salaamustadz.ui.image.ImagePreviewActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(private val context: Context, private var chats :List<Chat>, private val imageReceiver: String?,
                  private var receiverName: String, private val recyclerView: RecyclerView,
                  private val senderId: String): RecyclerView.Adapter<ChatAdapter.MainViewHolder>() {
    var senderSet = HashSet<Int>()
    var receiverSet = HashSet<Int>()
    var liveSenderSet = MutableLiveData<HashSet<Int>>()
    var liveReceiverSet = MutableLiveData<HashSet<Int>>()
    var selectionEnabledLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private var currentPlayingPosition = 0
    private var seekBarUpdater: SeekBarUpdater? = null
    private var playingHolder: MainViewHolder? = null
    var mediaPlayer: MediaPlayer? = null
    inner class SeekBarUpdater : Runnable {
        override fun run() {
            val seekBar = playingHolder?.itemView?.findViewById<SeekBar>(R.id.seekbar)
            val currentTimeText = playingHolder?.itemView?.findViewById<TextView>(R.id.txt_play_duration)

            if (null != playingHolder && null != mediaPlayer) {
                seekBar?.progress = mediaPlayer?.currentPosition!!
                val currentDuration = mediaPlayer!!.currentPosition
                currentTimeText?.text = milliSecondToTimer(currentDuration)
                seekBar?.postDelayed(this, 50)
            }
        }
    }

    inner class MainViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        init {
            seekBarUpdater = SeekBarUpdater()
            currentPlayingPosition = -1
            itemView.setOnLongClickListener {
                if(selectionEnabledLiveData.value==false) {
                    selectionEnabledLiveData.value = true
                    if(chats[bindingAdapterPosition].fromId==senderId) {
                        senderSet.add(bindingAdapterPosition)
                        liveSenderSet.value = senderSet

                    } else {
                        receiverSet.add(bindingAdapterPosition)
                        liveReceiverSet.value = receiverSet
                    }
                    notifyItemChanged(bindingAdapterPosition)
                }
                true
            }
            itemView.setOnClickListener {
                if(selectionEnabledLiveData.value==true) {
                    if(chats[bindingAdapterPosition].fromId==senderId) {
                        if(senderSet.contains(bindingAdapterPosition)) {
                            senderSet.remove(bindingAdapterPosition)
                        } else {
                            senderSet.add(bindingAdapterPosition)
                        }
                        liveSenderSet.value = senderSet
                    } else {
                        if(receiverSet.contains(bindingAdapterPosition)) {
                            receiverSet.remove(bindingAdapterPosition)
                        } else {
                            receiverSet.add(bindingAdapterPosition)
                        }
                        liveReceiverSet.value = receiverSet
                    }
                    if(senderSet.size==0 && receiverSet.size==0) {
                        senderSet.clear()
                        receiverSet.clear()
                        liveSenderSet.value = senderSet
                        liveReceiverSet.value = receiverSet
                        selectionEnabledLiveData.value = false
                    }
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
        fun bindDataSent(chat: Chat) {
            val cvSent = itemView.findViewById<CardView>(R.id.sent_reply_cv)
            cvSent.setOnClickListener {
                scrollToPosition(chat.replyPos.toInt()-1)
                changeBackground(chat.replyPos.toInt()-1)
            }
            val barSent = itemView.findViewById<View>(R.id.sent_reply_bar)
            val nameSent = itemView.findViewById<TextView>(R.id.sent_reply_name)
            val msgSent = itemView.findViewById<TextView>(R.id.sent_reply_msg)
            val imgReply = itemView.findViewById<ImageView>(R.id.img_reply)
            if(chat.replyAttached && chat.delFor=="") {
                cvSent.visibility = View.VISIBLE
                if (chat.replyText?.contains("images/messages/") == true){
                    imgReply.visibility = View.VISIBLE
                    msgSent.text = "Foto"
                    Glide.with(context)
                        .load(StorageUtil.pathToReference(chat.replyText!!))
                        .placeholder(R.drawable.ic_image_black_24dp)
                        .into(imgReply)
                } else if (chat.replyText?.contains("audio/messages/") == true) {
                    imgReply.visibility = View.GONE
                    msgSent.text = "Pesan Suara"
                } else {
                    imgReply.visibility = View.GONE
                    msgSent.text = chat.replyText
                }
                if(chat.replyTo==FirebaseAuth.getInstance().uid) {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.text = "Anda"
                } else {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.text = receiverName
                }
            } else {
                cvSent.visibility = View.GONE
            }
            if(senderSet.contains(bindingAdapterPosition)) {
                itemView.findViewById<ConstraintLayout>(R.id.sent_ll_reply).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_sentforeground)
            } else {
                itemView.findViewById<ConstraintLayout>(R.id.sent_ll_reply).foreground = null
            }
            val unreadStatus = itemView.findViewById<ImageView>(R.id.sent_tv_tick)
            if (chat.status == "Sent") {
                unreadStatus.setImageResource(R.drawable.ic_single_tick)
            } else {
                unreadStatus.setImageResource(R.drawable.ic_double_tick)
            }
            val text = itemView.findViewById<TextView>(R.id.sent_tv_text)
            val image = itemView.findViewById<ImageView>(R.id.imageView_message_image)
            val dt = itemView.findViewById<TextView>(R.id.sent_tv_time)
            val audioPlayer = itemView.findViewById<ConstraintLayout>(R.id.layout_playing_recorder)
            val seekBar = itemView.findViewById<SeekBar>(R.id.seekbar)
            val btnPlay = itemView.findViewById<ImageView>(R.id.img_play_button)
            when (chat.type) {
                "text" -> {
                    text.text = chat.text
                    text.visibility = View.VISIBLE
                    image.visibility = View.GONE
                    audioPlayer.visibility = View.GONE
                }
                "image" -> {
                    audioPlayer.visibility = View.GONE
                    if (chat.delBy?.contains(FirebaseAuth.getInstance().uid)!! || chat.delFor=="Everyone"){
                        text.text = chat.text
                        text.visibility = View.VISIBLE
                        image.visibility = View.GONE
                    } else {
                        image.visibility = View.VISIBLE
                        text.visibility = View.GONE
                        Glide.with(context)
                            .load(StorageUtil.pathToReference(chat.text!!))
                            .placeholder(R.drawable.ic_image_black_24dp)
                            .into(image)
                        image.setOnClickListener {
                            val intent = Intent(context, ImagePreviewActivity::class.java)
                            intent.putExtra(Constants.KEY_IMAGE_MESSAGE, chat.text)
                            intent.putExtra(Constants.KEY_NAME_IMAGE_OWNER,"Anda")
                            context.startActivity(intent)
                        }
                        image.setOnLongClickListener {
                            if(selectionEnabledLiveData.value==true||selectionEnabledLiveData.value==false) {
                                selectionEnabledLiveData.value = true
                                if(chats[bindingAdapterPosition].fromId==senderId) {
                                    senderSet.add(bindingAdapterPosition)
                                    liveSenderSet.value = senderSet

                                } else {
                                    receiverSet.add(bindingAdapterPosition)
                                    liveReceiverSet.value = receiverSet
                                }
                                notifyItemChanged(bindingAdapterPosition)
                            }
                            true
                        }
                    }
                }
                "audio" -> {
                    if (chat.delBy?.contains(FirebaseAuth.getInstance().uid)!! || chat.delFor=="Everyone"){
                        text.text = chat.text
                        text.visibility = View.VISIBLE
                        image.visibility = View.GONE
                        audioPlayer.visibility = View.GONE
                    } else {
                        audioPlayer.visibility = View.VISIBLE
                        text.visibility = View.GONE
                        image.visibility = View.GONE

                        StorageUtil.downloadMessageAudioPath(chat.text!!){ uri ->
                            btnPlay.setOnClickListener {
                                if (bindingAdapterPosition == currentPlayingPosition) {
                                    if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                                        mediaPlayer!!.pause()
                                    } else {
                                        if (mediaPlayer != null)
                                            mediaPlayer!!.start()
                                    }
                                } else {
                                    currentPlayingPosition = bindingAdapterPosition
                                    if (mediaPlayer != null) {
                                        if (null != playingHolder) {
                                            updateNonPlayingView(playingHolder!!)
                                        }
                                        mediaPlayer!!.release()
                                    }
                                    playingHolder = this

                                    playSound(uri)
                                }
                                if (mediaPlayer != null)
                                    updatePlayingView()

                            }


                        }


                    }

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                if (mediaPlayer != null && mediaPlayer!!.isPlaying)
                                    mediaPlayer!!.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }

                    })

                }
            }

            val t1 = chat.datetime?.toDate()
            val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
            val t3 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1)
            dt.text = t2
            if(chat.delBy?.contains(FirebaseAuth.getInstance().uid)!! || chat.delFor=="Everyone") {
                text.setTextColor(ContextCompat.getColor(context,R.color.sender_del_text_color))
                text.setTypeface(null,Typeface.ITALIC)
            } else {
                text.setTextColor(ContextCompat.getColor(context,R.color.primary_text))
                text.setTypeface(null,Typeface.NORMAL)
            }
        }
        fun bindDataReceive(chat: Chat) {
            val cvSent = itemView.findViewById<CardView>(R.id.receiver_reply_cv)
            cvSent.setOnClickListener {
                scrollToPosition(chat.replyPos.toInt()-1)
                changeBackground(chat.replyPos.toInt()-1)
            }
            val barSent = itemView.findViewById<View>(R.id.received_reply_bar)
            val nameSent = itemView.findViewById<TextView>(R.id.received_reply_name)
            val msgSent = itemView.findViewById<TextView>(R.id.received_reply_msg)
            val imgReply = itemView.findViewById<ImageView>(R.id.img_reply)
            if(chat.replyAttached && chat.delFor=="") {
                cvSent.visibility = View.VISIBLE
                if (chat.replyText?.contains("images/messages/") == true){
                    imgReply.visibility = View.VISIBLE
                    msgSent.text = "Foto"
                    Glide.with(context)
                        .load(StorageUtil.pathToReference(chat.replyText!!))
                        .placeholder(R.drawable.ic_image_black_24dp)
                        .into(imgReply)
                }else if (chat.replyText?.contains("audio/messages/") == true) {
                    imgReply.visibility = View.GONE
                    msgSent.text = "Pesan Suara"
                } else {
                    imgReply.visibility = View.GONE
                    msgSent.text = chat.replyText
                }
                if(chat.replyTo==FirebaseAuth.getInstance().uid) {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_sender_color))
                    nameSent.text = "Anda"
                } else {
                    barSent.setBackgroundColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.setTextColor(ContextCompat.getColor(context,R.color.reply_receiver_color))
                    nameSent.text = receiverName
                }
            } else {
                cvSent.visibility = View.GONE
            }
            if(receiverSet.contains(bindingAdapterPosition)) {
                itemView.findViewById<ConstraintLayout>(R.id.received_ll_reply).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_receiveforeground)
            } else {
                itemView.findViewById<ConstraintLayout>(R.id.received_ll_reply).foreground = null
            }
            val text = itemView.findViewById<TextView>(R.id.received_tv_text)
            val image = itemView.findViewById<ImageView>(R.id.imageView_message_image)
            val dt = itemView.findViewById<TextView>(R.id.received_tv_datetime)
            val audioPlayer = itemView.findViewById<ConstraintLayout>(R.id.layout_playing_recorder)
            val seekBar = itemView.findViewById<SeekBar>(R.id.seekbar)
            val btnPlay = itemView.findViewById<ImageView>(R.id.img_play_button)
            when (chat.type) {
                "text" -> {
                    text.text = chat.text
                    text.visibility = View.VISIBLE
                    image.visibility = View.GONE
                    audioPlayer.visibility = View.GONE
                }
                "image" -> {
                    if (chat.delBy?.contains(FirebaseAuth.getInstance().uid)!! || chat.delFor=="Everyone"){
                        text.text = chat.text
                        text.visibility = View.VISIBLE
                        image.visibility = View.GONE
                    } else {
                        audioPlayer.visibility = View.GONE
                        image.visibility = View.VISIBLE
                        text.visibility = View.GONE
                        Glide.with(context)
                            .load(StorageUtil.pathToReference(chat.text!!))
                            .placeholder(R.drawable.ic_image_black_24dp)
                            .into(image)
                        image.setOnClickListener {
                            val intent = Intent(context, ImagePreviewActivity::class.java)
                            intent.putExtra(Constants.KEY_IMAGE_MESSAGE, chat.text)
                            intent.putExtra(Constants.KEY_NAME_IMAGE_OWNER, receiverName)
                            context.startActivity(intent)
                        }
                        image.setOnLongClickListener {
                            if(selectionEnabledLiveData.value==true||selectionEnabledLiveData.value==false) {
                                selectionEnabledLiveData.value = true
                                if(chats[bindingAdapterPosition].fromId==senderId) {
                                    senderSet.add(bindingAdapterPosition)
                                    liveSenderSet.value = senderSet

                                } else {
                                    receiverSet.add(bindingAdapterPosition)
                                    liveReceiverSet.value = receiverSet
                                }
                                notifyItemChanged(bindingAdapterPosition)
                            }
                            true
                        }
                    }
                }
                "audio" -> {
                    if (chat.delBy?.contains(FirebaseAuth.getInstance().uid)!! || chat.delFor=="Everyone"){
                        text.text = chat.text
                        text.visibility = View.VISIBLE
                        image.visibility = View.GONE
                        audioPlayer.visibility = View.GONE
                    } else {
                        audioPlayer.visibility = View.VISIBLE
                        text.visibility = View.GONE
                        image.visibility = View.GONE

                        StorageUtil.downloadMessageAudioPath(chat.text!!){ uri ->
                            btnPlay.setOnClickListener {
                                if (bindingAdapterPosition == currentPlayingPosition) {
                                    if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                                        mediaPlayer!!.pause()
                                    } else {
                                        if (mediaPlayer != null)
                                            mediaPlayer!!.start()
                                    }
                                } else {
                                    currentPlayingPosition = bindingAdapterPosition
                                    if (mediaPlayer != null) {
                                        if (null != playingHolder) {
                                            updateNonPlayingView(playingHolder!!)
                                        }
                                        mediaPlayer!!.release()
                                    }
                                    playingHolder = this

                                    playSound(uri)
                                }
                                if (mediaPlayer != null)
                                    updatePlayingView()

                            }


                        }


                    }

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                if (mediaPlayer != null && mediaPlayer!!.isPlaying)
                                    mediaPlayer!!.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }

                    })

                }
            }


            val t1 = chat.datetime?.toDate()
            val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
            val t3 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(t1)
            dt.text = t2
            if(chat.delBy?.contains(FirebaseAuth.getInstance().uid)!! || chat.delFor=="Everyone") {
                text.setTextColor(ContextCompat.getColor(context,R.color.gray_deleted))
                text.setTypeface(null,Typeface.ITALIC)
            } else {
                text.setTextColor(ContextCompat.getColor(context,R.color.primary_text))
                text.setTypeface(null,Typeface.NORMAL)
            }
            val iv = itemView.findViewById<CircleImageView>(R.id.receivec_iv_profile)
            if (imageReceiver.isNullOrEmpty()){
                iv.setImageResource(R.drawable.profile_user)
            }else {
                Glide
                    .with(context)
                    .load(StorageUtil.pathToReference(imageReceiver.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.profile_user)
                    .into(iv)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view: View = if(viewType == VIEW_TYPE_SENT) {
            LayoutInflater.from(context).inflate(R.layout.item_container_sent_messages,parent,false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.item_container_recived_message, parent, false)
        }
        return MainViewHolder(view)
    }

    private fun scrollToPosition(position: Int) {
        recyclerView.smoothScrollToPosition(position)
    }
    private fun changeBackground(position: Int) {
        val data = chats[position]
        data.isSelected = true

        // Notify the adapter that the item at the clicked position has changed
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        if(getItemViewType(position)== VIEW_TYPE_RECEIVER) {
            holder.bindDataReceive(chats[position])
        } else {
            holder.bindDataSent(chats[position])
        }
        if (position == currentPlayingPosition) {
            playingHolder = holder
            updatePlayingView()
        } else {
            updateNonPlayingView(holder)
        }
        val data = chats[position]
        // Update the background of the item view based on the data's isHighlighted property
        if (data.isSelected) {
            // Change the background to the highlighted state (e.g., add a background drawable)
            holder.itemView.foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_foreground)
            Handler(Looper.getMainLooper()).postDelayed({
                holder.itemView.foreground = null
                data.isSelected = false
            }, 500)
        } else {
            holder.itemView.foreground = null
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(FirebaseAuth.getInstance().uid.equals(chats[position].fromId)) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    fun getSelectedItems(): List<Chat> {
        val selectedList = mutableListOf<Chat>()
        for (position in senderSet) {
            if (position in chats.indices) {
                selectedList.add(chats[position])
            }
        }
        for (position in receiverSet) {
            if (position in chats.indices) {
                selectedList.add(chats[position])
            }
        }
        selectionEnabledLiveData.value = false
        senderSet.clear()
        receiverSet.clear()
        liveSenderSet.value = senderSet
        liveReceiverSet.value = receiverSet
        notifyDataSetChanged()
        return selectedList
    }


    private fun playSound(audioResource: Uri) {
        mediaPlayer = MediaPlayer.create(context, audioResource)
        mediaPlayer!!.setOnCompletionListener { releaseMediaPlayer() }
        mediaPlayer!!.start()
    }

    private fun releaseMediaPlayer() {
        if (null != playingHolder) {
            updateNonPlayingView(playingHolder!!)
        }
        mediaPlayer!!.release()
        mediaPlayer = null
        currentPlayingPosition = -1
    }
    private fun updateNonPlayingView(holder: MainViewHolder) {
        val seekbar = holder.itemView.findViewById<SeekBar>(R.id.seekbar)
        seekbar.removeCallbacks(seekBarUpdater)
        seekbar.isEnabled = true //default false
        seekbar.progress = 0
        holder.itemView.findViewById<ImageView>(R.id.img_play_button).setImageResource(R.drawable.ic_play_circle)
    }

    private fun updatePlayingView() {
        val ivPlayPause = playingHolder?.itemView?.findViewById<ImageView>(R.id.img_play_button)
        val seekbar = playingHolder?.itemView?.findViewById<SeekBar>(R.id.seekbar)

        seekbar?.max = mediaPlayer!!.duration
        seekbar?.progress = mediaPlayer!!.currentPosition
        seekbar?.isEnabled = true
        if (mediaPlayer!!.isPlaying) {
            seekbar?.postDelayed(seekBarUpdater, 50)
            ivPlayPause?.setImageResource(R.drawable.ic_pause_circle)
        } else {
            seekbar?.removeCallbacks(seekBarUpdater)
            ivPlayPause?.setImageResource(R.drawable.ic_play_circle)
        }
    }
    private fun milliSecondToTimer(milliSecond: Int): String {
        var timerString = ""
        val secondsString: String

        val hours = milliSecond/(1000*60*60)
        val minutes = (milliSecond%(1000*60*60))/(1000*60)
        val seconds = (milliSecond%(1000*60*60))%(1000*60)/1000

        if (hours>0){
            timerString = "$hours:"
        }
        secondsString = if (seconds<10){
            "0$seconds"
        } else {
            "$seconds"
        }
        timerString = "$timerString$minutes:$secondsString"
        return timerString
    }

    companion object {
        private const val VIEW_TYPE_SENT: Int = 1
        private const val VIEW_TYPE_RECEIVER: Int = 2
    }


}