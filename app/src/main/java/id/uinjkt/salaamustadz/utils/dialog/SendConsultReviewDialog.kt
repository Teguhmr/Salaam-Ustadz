package id.uinjkt.salaamustadz.utils.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogSendConsultReviewBinding
import id.uinjkt.salaamustadz.utils.toast

class SendConsultReviewDialog(private val context: Activity,
                              private val onSuccessListener: (rating: Float, textReview: String) -> Unit
): DialogFragment() {

    private lateinit var binding: DialogSendConsultReviewBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSendConsultReviewBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        binding.btnCancel.setOnClickListener {
           dismiss()
        }

        binding.btnOK.setOnClickListener {
            if (binding.appCompatRatingBar.rating == 0F || binding.edtReview.text.toString().isEmpty()){
                context.toast("Rating dan ulasan tidak boleh kosong")
                return@setOnClickListener
            }
            onSuccessListener.invoke(binding.appCompatRatingBar.rating, binding.edtReview.text.toString())
            dismiss()
        }


        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


}