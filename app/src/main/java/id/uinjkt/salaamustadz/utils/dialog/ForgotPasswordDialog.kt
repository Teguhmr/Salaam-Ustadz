package id.uinjkt.salaamustadz.utils.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogForgotPasswordBinding
import id.uinjkt.salaamustadz.utils.toast

class ForgotPasswordDialog (
    private val onSubmitClickListener: (String) -> Unit
): DialogFragment() {

    private lateinit var binding: DialogForgotPasswordBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogForgotPasswordBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        binding.btnSend.setOnClickListener {
            if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text.toString()).matches() or binding.edtEmail.text.toString().isEmpty()){
                context?.toast("Masukan email yang valid!")
            } else {
                onSubmitClickListener.invoke(binding.edtEmail.text.toString())
                dismiss()
            }
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}