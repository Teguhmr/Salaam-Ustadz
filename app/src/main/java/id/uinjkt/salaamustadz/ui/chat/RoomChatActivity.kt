package id.uinjkt.salaamustadz.ui.chat

import android.view.View
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.models.chat.ChatRooms
import id.uinjkt.salaamustadz.databinding.ActivityRoomChatBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.chat.RoomChatsAdapter
import id.uinjkt.salaamustadz.utils.ChatAvailability.ALL
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.toast
import id.uinjkt.salaamustadz.viewmodelfactory.RoomChatsViewModelFactory

class RoomChatActivity : BaseActivity<ActivityRoomChatBinding>() {
    private lateinit var viewModel: RoomChatsViewModel
    private val senderid: String = FirebaseAuth.getInstance().uid!!
    private var adapter: RoomChatsAdapter? = null
    private var roomSet = HashSet<Int>()
    private var listData: List<ChatRooms>? = emptyList()
    private lateinit var chipAdapter: ChipAdapter
    override fun getViewBinding(): ActivityRoomChatBinding {
        return ActivityRoomChatBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        sharedPref.removeSingleValueString(Constants.KEY_CHAT_AVAILABLE)
        setListeners()
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
        viewModel = ViewModelProvider(this, RoomChatsViewModelFactory(senderid,""))[RoomChatsViewModel::class.java]


        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun setupObserver() {
        initChipAdapter()

        viewModel.chatLiveData.observe(this) { state ->
            when(state) {
                is Result.Loading -> {

                }
                is Result.Success -> {
                    listData = state.data
                    val chatAvailabilities: List<Int> = listData?.mapNotNull { it.chatAvailability }?.distinct() ?: emptyList()
                    val defaultChatAvailability = chatAvailabilities.minOrNull() ?: 0

                    chipAdapter.setChips(listOf("Menunggu", "Berlangsung", "Selesai", "Semua"), defaultChatAvailability){status ->
                        processChatList(state.data, status)
                    }
                    processChatList(state.data, defaultChatAvailability)
                }
                is Result.Error -> {
                    toast(state.message.toString())
                }
            }
        }
    }


    private fun setListeners() {
        binding.chatscreenCvOptiondel.setOnClickListener {
            val bottomSheet = BottomSheetDialog(this, R.style.bottomSheetStyle_accountdetail)
            bottomSheet.setContentView(R.layout.chatroomscreen_delete_dialog)
            bottomSheet.show()
            val btnNo = bottomSheet.findViewById<CardView>(R.id.chatroom_cv_delno)
            val btnYes = bottomSheet.findViewById<CardView>(R.id.chatroom_cv_delyes)
            btnNo?.setOnClickListener {
                bottomSheet.dismiss()
            }
            btnYes?.setOnClickListener {
                val documents = adapter?.getSelectedItems()
                viewModel.deleteChatRooms(documents!!)
                bottomSheet.dismiss()
            }
        }
        binding.chatscreenCvOptioncancel.setOnClickListener {
            adapter?.getSelectedItems()
        }

    }

    private fun initChipAdapter() {
        chipAdapter = ChipAdapter(
            this,
            binding.chipGroup,
            listOf("Menunggu", "Berlangsung", "Selesai", "Semua"),
            0 // Default chat availability
        )
    }

    private fun processChatList(data: List<ChatRooms>?, chatAvailability: Int) {
        val filteredList = if (chatAvailability == ALL.status){
            data
        }else{
            data?.filter { chatRoom ->
                chatRoom.chatAvailability == chatAvailability
            }
        }

        if (filteredList.isNullOrEmpty()) {
            binding.bgNothingMsg.visibility = View.VISIBLE
        } else {
            binding.bgNothingMsg.visibility = View.GONE
            // Use filteredList for further processing (e.g., set adapter, update UI, etc.)
            adapter = RoomChatsAdapter(this, filteredList)
            val rv = binding.chatscreenRvChats
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
            rv.setHasFixedSize(true)
            rv.adapter = adapter
            adapter?.selectionEnabledLiveData?.observe(this) { state ->
                if(state==true) {
                    binding.chatscreenCvOptionbar.visibility = View.VISIBLE
                } else {
                    binding.chatscreenCvOptionbar.visibility = View.GONE
                }
            }
            adapter?.liveSelectSet?.observe(this){ selectSet ->
                roomSet = selectSet
            }
            adapter?.notifyDataSetChanged()
        }

    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(roomSet.size>0) {
            adapter?.getSelectedItems()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

}