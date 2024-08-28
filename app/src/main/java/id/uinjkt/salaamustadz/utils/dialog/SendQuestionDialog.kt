package id.uinjkt.salaamustadz.utils.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogSendQuestionBinding

class SendQuestionDialog(private val context: Activity,
                         private val onSuccessListener: () -> Unit
): DialogFragment() {

    private lateinit var binding: DialogSendQuestionBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSendQuestionBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        binding.btnNo.setOnClickListener {
           dismiss()
        }

        binding.btnOK.setOnClickListener {
            onSuccessListener.invoke()
            dismiss()

        }


        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


}