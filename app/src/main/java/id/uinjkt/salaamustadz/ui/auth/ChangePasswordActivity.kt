package id.uinjkt.salaamustadz.ui.auth

import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityChangePasswordBinding
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.SuccessType
import id.uinjkt.salaamustadz.utils.dialog.SuccessDialog
import id.uinjkt.salaamustadz.utils.emptyString
import id.uinjkt.salaamustadz.utils.firebase.FireStoreUtil
import id.uinjkt.salaamustadz.utils.isValidPassword
import id.uinjkt.salaamustadz.utils.toast

class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {

    private lateinit var preferenceManager: PreferenceManager
    override fun getViewBinding(): ActivityChangePasswordBinding {
        return ActivityChangePasswordBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        preferenceManager = PreferenceManager(this)
        binding.backButton.setOnClickListener{
            finish()
        }
    }

    override fun setupAction() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {

            val email = preferenceManager.getString(Constants.KEY_EMAIL).toString()
            var currentPassword = emptyString()
            var confirmPassword = emptyString()
            var newPassword = emptyString()

            binding.inputOldPassword.doOnTextChanged { text, _, _, _ ->
                currentPassword = text.toString()
            }

            binding.confirmNewPassword.doOnTextChanged { text, _, _, _ ->
                confirmPassword = text.toString()
            }
            binding.inputNewPassword.doOnTextChanged { text, _, _, _ ->
                newPassword = text.toString()
            }

            binding.buttonSendPassword.setOnClickListener {
                if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    if (validatePassword()) {
                        if (newPassword == confirmPassword) {
                            showLoading()
                            val credential: AuthCredential = EmailAuthProvider.getCredential(email, currentPassword)
                            // Re-authenticate the user (in case their account is protected with a password)

                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    // Re-authentication successful, now change the password
                                    user.updatePassword(newPassword)
                                    FireStoreUtil.updateNewPassword(user.uid)
                                    SuccessDialog(this, SuccessType.CHANGE_PASSWORD.type) {
                                        finish()
                                    }.show(supportFragmentManager, "successUpdateProfileDialog")
                                    dismissLoading()
                                }
                                .addOnFailureListener { e ->
                                    toast(e.message.toString())
                                    dismissLoading()
                                }
                        } else {
                            toast("Kata sandi baru tidak sama")
                        }
                    }
                } else {
                    toast("Kata sandi baru tidak boleh kosong")
                    return@setOnClickListener
                }
            }


        }
    }

    override fun setupProcess() {
    }

    override fun setupObserver() {
    }

    private fun validatePassword(): Boolean {
        val password = binding.inputNewPassword.text.toString().trim()
        return if (password.length < 8) {
            toast("Sandi minimal 8 karakter")
            false
        }else if (!isValidPassword(password)) {
            toast("Kata sandi harus terdiri dari huruf dan angka")
            false
        } else {
            true
        }
    }

}