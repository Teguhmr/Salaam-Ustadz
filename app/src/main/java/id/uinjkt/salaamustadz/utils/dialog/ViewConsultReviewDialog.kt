package id.uinjkt.salaamustadz.utils.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.data.models.chat.ConsultReview
import id.uinjkt.salaamustadz.databinding.DialogViewConsultReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewConsultReviewDialog(private val context: Activity, private val consultReview: ConsultReview?): DialogFragment() {

    private lateinit var binding: DialogViewConsultReviewBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogViewConsultReviewBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        binding.btnCancel.setOnClickListener {
           dismiss()
        }

        val data = consultReview as ConsultReview

        binding.appCompatRatingBar.rating = data.rating
        binding.txtReview.text = data.textReview
        val t1 = data.createdAt?.toDate()
        val t3 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(t1 as Date)
        binding.txtDate.text = t3


        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


}