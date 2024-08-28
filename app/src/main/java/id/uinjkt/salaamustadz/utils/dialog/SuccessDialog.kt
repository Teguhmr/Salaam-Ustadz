package id.uinjkt.salaamustadz.utils.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogSuccessBinding
import id.uinjkt.salaamustadz.utils.SuccessType
import id.uinjkt.salaamustadz.utils.SuccessType.*

class SuccessDialog(private val context: Context, private val typeDialog: String, private val onOkListener:() -> Unit): DialogFragment() {

    private lateinit var binding: DialogSuccessBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSuccessBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        when(SuccessType.getType(typeDialog)){
            SEND_ARTICLE -> {
                binding.tvHeader.text = "Terkirim!"
                binding.txtBody.text = "Terima kasih atas artikel yang Anda tulis. Silahkan cek artikel ada pada halaman artikel saya"
            }
            UPDATE_ARTICLE -> {
                binding.tvHeader.text = "Tersimpan!"
                binding.txtBody.text = "Artikel Anda telah tersimpan!"
            }
            UPDATE_PROFILE -> {
                binding.tvHeader.text = "Tersimpan!"
                binding.txtBody.text = "Profil Anda telah tersimpan!"
            }
            CHANGE_PASSWORD -> {
                binding.tvHeader.text = "Tersimpan!"
                binding.txtBody.text = "Password Anda telah diubah!"
            }
            SEND_CONSULT_REVIEW -> {
                binding.tvHeader.text = "Terkirim!"
                binding.txtBody.text = "Terima kasih atas ulasan yang Anda tulis."
            }
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