package id.uinjkt.salaamustadz.ui.adapter.chat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.chat.ChatRooms
import id.uinjkt.salaamustadz.ui.chat.ChatActivity
import id.uinjkt.salaamustadz.utils.ChatAvailability
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import java.text.SimpleDateFormat
import java.util.Locale

class RoomChatsAdapter(private val context: Context, private val chats: List<ChatRooms>): RecyclerView.Adapter<RoomChatsAdapter.MainViewHolder>() {
    private val selectedItems = HashSet<Int>()
    var liveSelectSet = MutableLiveData<HashSet<Int>>()
    val selectionEnabledLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chatscreen_rv_chats,parent,false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bindData(chats[position])
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    inner class MainViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                if(selectionEnabledLiveData.value==false) {
                    selectionEnabledLiveData.value = true
                    selectedItems.add(bindingAdapterPosition)
                    liveSelectSet.value = selectedItems
                    notifyItemChanged(bindingAdapterPosition)
                }
                true
            }

            itemView.setOnClickListener {
                if (selectionEnabledLiveData.value==true) {
                    if (selectedItems.contains(bindingAdapterPosition)) {
                        selectedItems.remove(bindingAdapterPosition)
                        if(selectedItems.size==0) {
                            selectedItems.clear()
                            selectionEnabledLiveData.value = false
                        }
                    } else {
                        selectedItems.add(bindingAdapterPosition)
                    }
                    liveSelectSet.value = selectedItems
                    notifyItemChanged(bindingAdapterPosition)
                } else {
                    val senderId = chats[bindingAdapterPosition].senderId
                    val receiverId = chats[bindingAdapterPosition].receiverId
                    val chatRoomId = chats[bindingAdapterPosition].id
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra(Constants.KEY_SENDER_ID,senderId)
                    intent.putExtra(Constants.KEY_RECEIVER_ID,receiverId)
                    intent.putExtra(Constants.KEY_CHAT_ROOM_ID,chatRoomId)
                    ContextCompat.startActivity(context, intent, null)
                }
            }
        }

        fun bindData(receiver: ChatRooms) {
            if(selectedItems.contains(bindingAdapterPosition)) {
                itemView.findViewById<CardView>(R.id.chatscreen_rvchat_cv_parent).foreground = ContextCompat.getDrawable(context,R.drawable.selected_chat_receiveforeground)
            } else {
                itemView.findViewById<CardView>(R.id.chatscreen_rvchat_cv_parent).foreground = null
            }
            val senderId: String = FirebaseAuth.getInstance().uid!!
            val profile = itemView.findViewById<CircleImageView>(R.id.chatscreen_rvchat_profileicon)
            val name = itemView.findViewById<TextView>(R.id.chatscreen_rvchat_tv_name)
            val text = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_tv_message)
            val time = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_tv_time)
            val date = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_tv_date)
            val tick = itemView.findViewById<AppCompatImageView>(R.id.chatscreen_rv_chats_tickmark)
            val msgCount = itemView.findViewById<CardView>(R.id.chatscreen_rv_chats_cv_msgcount)
            val unreadCount = itemView.findViewById<TextView>(R.id.chatscreen_rv_chats_mssgcount)
            if (receiver.chatAvailability == ChatAvailability.COMPLETED.status && receiver.firstTextReceiver?.isNotEmpty() == true){
                itemView.findViewById<CardView>(R.id.chatscreen_rvchat_cv_parent).setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_not_available))
            } else {
                itemView.findViewById<CardView>(R.id.chatscreen_rvchat_cv_parent).setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }
            if(receiver.receiverId.equals(senderId)) {
                if (receiver.senderImage.isNullOrEmpty()){
                    profile.setImageResource(R.drawable.profile_user)
                } else {
                    Glide
                        .with(context)
                        .load(StorageUtil.pathToReference(receiver.senderImage.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.profile_user)
                        .into(profile)
                }

                name.text = receiver.senderName
                if(receiver.lastMessageDelStatus?.contains(FirebaseAuth.getInstance().uid!!)!!) {
                    text.text = context.getString(R.string.this_message_hbeen_deleted_for_me)
                } else {
                    if (receiver.lastText?.contains("/images/messages/") == true){
                        text.text = context.getString(R.string.label_photo)
                    }else if (receiver.lastText?.contains("/audio/messages/") == true){
                        text.text = context.getString(R.string.label_voice_message)
                    } else{
                        text.text = receiver.lastText
                    }
                }
                if(receiver.lastTextFrom != senderId) {
                    tick.visibility = View.GONE
                    msgCount.visibility = View.VISIBLE
                } else {
                    tick.visibility = View.VISIBLE
                    msgCount.visibility = View.GONE
                }
                if(receiver.unreadCount<=0) msgCount.visibility = View.GONE
                val t1 = receiver.timestamp?.toDate()
                val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
                val t3 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(t1)
                unreadCount.text = receiver.unreadCount.toString()
                time.text = t2.toString()
                date.text = t3.toString()
            } else {
                if (receiver.receiverImage.isNullOrEmpty()){
                    profile.setImageResource(R.drawable.profile_user)
                } else {
                    Glide
                        .with(context)
                        .load(StorageUtil.pathToReference(receiver.receiverImage.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.profile_user)
                        .into(profile)
                }

                name.text = receiver.receiverName
                if(receiver.lastMessageDelStatus?.contains(FirebaseAuth.getInstance().uid!!)!!) {
                    text.text = context.getString(R.string.this_message_hbeen_deleted)
                } else {
                    if (receiver.lastText?.contains("/images/messages/") == true){
                        text.text = context.getString(R.string.label_photo)
                    }else if (receiver.lastText?.contains("/audio/messages/") == true){
                        text.text = context.getString(R.string.label_voice_message)
                    } else {
                        text.text = receiver.lastText
                    }
                }
                if(receiver.lastTextFrom != senderId) {
                    tick.visibility = View.GONE
                    msgCount.visibility = View.VISIBLE
                } else {
                    tick.visibility = View.VISIBLE
                    msgCount.visibility = View.GONE
                    if (receiver.lastMessageReadStatus.equals("Sent")){
                        tick.setImageResource(R.drawable.ic_single_tick)
                    } else {
                        tick.setImageResource(R.drawable.ic_double_tick)
                    }
                }
                if(receiver.unreadCount<=0) msgCount.visibility = View.GONE
                val t1 = receiver.timestamp?.toDate()
                val t2 = SimpleDateFormat("HH:mm", Locale.getDefault()).format(t1!!)
                val t3 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(t1)
                unreadCount.text = receiver.unreadCount.toString()
                time.text = t2.toString()
                date.text = t3.toString()
            }
        }
    }

    fun getSelectedItems(): List<String> {
        val selectedList = mutableListOf<String>()
        for (position in selectedItems) {
            if (position in chats.indices) {
                selectedList.add(chats[position].id!!)
            }
        }
        selectionEnabledLiveData.value = false
        selectedItems.clear()
        notifyDataSetChanged()
        return selectedList
    }

}