package id.uinjkt.salaamustadz.utils.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import id.uinjkt.salaamustadz.R

class ProgressDialog(private val context: Activity?) {
    private var dialog: AlertDialog? = null

    @SuppressLint("InflateParams")
    fun startProgressDialog(setCancelable: Boolean = false) {
        if (context == null || context.isFinishing) {
            // Context is null or the activity is finishing, do not show the dialog
            return
        }
        dismissProgressDialog() // Dismiss any existing dialogs before showing a new one

        val builder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater
        inflater.let {
            builder.setView(inflater.inflate(R.layout.progress_dialog, null))
            dialog = builder.create()
            dialog?.apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setCancelable(setCancelable)
                show()
            }
        }
    }

    fun dismissProgressDialog() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            dialog = null // Set dialog to null after dismissal
        }
    }
}