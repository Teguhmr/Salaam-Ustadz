package id.uinjkt.salaamustadz.utils.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.uinjkt.salaamustadz.databinding.DialogConfirmBinding
import id.uinjkt.salaamustadz.utils.ConfirmType
import id.uinjkt.salaamustadz.utils.ConfirmType.*

class ConfirmDialog(private val context: Activity, private val type: String, private val onOkListener:() -> Unit): DialogFragment() {

    private lateinit var binding: DialogConfirmBinding
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogConfirmBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        when (ConfirmType.getType(type)){
            CONFIRM_SEND_REVIEW -> {
                binding.tvHeader.text ="Kirim Ulasan?"
                binding.txtBody.text ="Pastikan semua perubahan yang anda tulis telah sesuai. Tetap kirim?"

                binding.btnNo.text = "Tidak"
                binding.btnOK.text = "Ya"
            }

            CONFIRM_SUBMIT_GENDER -> {
                binding.tvHeader.text ="Kirim Data?"
                binding.txtBody.text ="Pastikan data Anda telah sesuai. Tetap kirim?"

                binding.btnNo.text = "Tidak"
                binding.btnOK.text = "Ya"
            }

            CONFIRM_SIGN_OUT -> {
                binding.tvHeader.text ="Tetap Keluar?"
                binding.txtBody.text ="Anda akan keluar dari akun ini. Yakin ingin keluar?"

                binding.btnNo.text = "Tidak"
                binding.btnOK.text = "Ya"
            }

            CONFIRM_UPDATE_APP -> {
                binding.tvHeader.text ="Update Tersedia"
                binding.txtBody.text ="Terdapat pembaharuan aplikasi. Silahkan unduh versi terbaru untuk meningkatkan fitur aplikasi."

                binding.btnNo.text = "Tidak"
                binding.btnOK.text = "Update"
            }
        }

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