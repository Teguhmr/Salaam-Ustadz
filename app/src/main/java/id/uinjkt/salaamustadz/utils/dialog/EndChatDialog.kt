package id.uinjkt.salaamustadz.utils.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogEndChatBinding

class EndChatDialog(private val context: Activity, private val onOkListener:() -> Unit): DialogFragment() {

    private lateinit var binding: DialogEndChatBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEndChatBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        binding.btnNo.setOnClickListener {
           dismiss()
        }

        binding.btnOK.setOnClickListener {
            onOkListener.invoke()
            dismiss()
        }

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}