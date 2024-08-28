package id.uinjkt.salaamustadz.utils.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogPreviewAnonymousNameBinding

class AnonymousNameDialog: DialogFragment() {

    private lateinit var binding: DialogPreviewAnonymousNameBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPreviewAnonymousNameBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        binding.btnOK.setOnClickListener {
            dismiss()
        }

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}