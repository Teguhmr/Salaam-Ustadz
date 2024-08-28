package id.uinjkt.salaamustadz.utils.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.DialogSignoutBinding
import id.uinjkt.salaamustadz.ui.auth.AuthActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import id.uinjkt.salaamustadz.utils.PreferenceManager

class SignOutDialog(private val context: Activity): DialogFragment() {

    private lateinit var binding: DialogSignoutBinding
    private lateinit var prefManager: PreferenceManager
    private var docRef: DocumentReference? = null
    private var authID: String? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSignoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)

        authID = FirebaseAuth.getInstance().uid
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()

        if (authID != null) {
            docRef = database.collection(Constants.KEY_COLLECTION_USER).document(authID!!)

        }
        prefManager = PreferenceManager(context)

        binding.btnNo.setOnClickListener {
           dismiss()
        }

        binding.btnOK.setOnClickListener {
            docRef?.update(Field.onlineStatus,false)
            Firebase.auth.signOut()
            prefManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false)
            prefManager.clear()
            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            activity?.finish()
            dismiss()
        }

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}