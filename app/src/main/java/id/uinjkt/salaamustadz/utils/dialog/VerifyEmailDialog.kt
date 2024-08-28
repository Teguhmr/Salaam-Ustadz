package id.uinjkt.salaamustadz.utils.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogEmailVerificationBinding

class VerifyEmailDialog(private val context: Context, private val onSuccessListener:() -> Unit): DialogFragment() {

    private lateinit var binding: DialogEmailVerificationBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEmailVerificationBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        binding.btnOK.setOnClickListener {
           onSuccessListener.invoke()
           dismiss()
        }

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}