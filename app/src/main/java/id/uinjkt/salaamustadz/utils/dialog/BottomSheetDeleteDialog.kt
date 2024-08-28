package id.uinjkt.salaamustadz.utils.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.LongpressChatDialogBinding
import id.uinjkt.salaamustadz.ui.adapter.chat.ChatAdapter
import id.uinjkt.salaamustadz.ui.chat.ChatViewModel

class BottomSheetDeleteDialog(private val viewModel: ChatViewModel,
                              private val senderSet: HashSet<Int>,
                              private val receiverSet: HashSet<Int>,
                              private val adapter: ChatAdapter,
                              private val onYesClickListener: () -> Unit
): BottomSheetDialogFragment() {

    private var _binding: LongpressChatDialogBinding? = null
    private val binding get() = _binding as LongpressChatDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LongpressChatDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.bottomSheetStyle_longpressdelchat)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDeleteDialog()
    }
    private fun showDeleteDialog() {
        val ll_first = binding.chatscreeDelChatLlFirst
        val ll_second = binding.chatscreeDelChatLlSecond
        val delForEve = binding.chatscreeLlDelForEve
        val delForMe = binding.chatscreeLlDelForMe
        val no = binding.longpressDelChatNo
        val yes = binding.longpressDelChatYes
        val text = binding.longpressDelChatTvtop
        var flag: String
        if(receiverSet.size>0) {
            ll_first.visibility = View.VISIBLE
            ll_second.visibility = View.GONE
            delForEve.visibility = View.GONE
            delForMe.visibility = View.VISIBLE
            no.visibility = View.GONE
            yes.visibility = View.GONE
            text.visibility = View.GONE
        } else if(senderSet.size>0) {
            ll_first.visibility = View.VISIBLE
            ll_second.visibility = View.GONE
            delForEve.visibility = View.VISIBLE
            delForMe.visibility = View.VISIBLE
            no.visibility = View.GONE
            yes.visibility = View.GONE
            text.visibility = View.GONE
        }
        delForEve.setOnClickListener {
            flag = "Everyone"
            ll_first.visibility = View.GONE
            ll_second.visibility = View.VISIBLE
            delForEve.visibility = View.GONE
            delForMe.visibility = View.GONE
            no.visibility = View.VISIBLE
            yes.visibility = View.VISIBLE
            text.visibility = View.VISIBLE
            yes.setOnClickListener {

                val list = adapter.getSelectedItems()
                for(chat in list) {
                    viewModel.deleteMessage(chat.id!!,flag)
                }
                onYesClickListener.invoke()
                dismiss()
            }
        }
        delForMe.setOnClickListener {
            flag = "Me"
            ll_first.visibility = View.GONE
            ll_second.visibility = View.VISIBLE
            delForEve.visibility = View.GONE
            delForMe.visibility = View.GONE
            no.visibility = View.VISIBLE
            yes.visibility = View.VISIBLE
            text.visibility = View.VISIBLE
            yes.setOnClickListener {
                val list = adapter.getSelectedItems()
                for(chat in list) {
                    viewModel.deleteMessage(chat.id!!,flag)
                }
                onYesClickListener.invoke()
                dismiss()
            }
        }
        no.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}