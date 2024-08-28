package id.uinjkt.salaamustadz.ui.auth

import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.databinding.ActivityRegisterGenderBinding
import id.uinjkt.salaamustadz.ui.home.MainActivity
import id.uinjkt.salaamustadz.utils.ConfirmType
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import id.uinjkt.salaamustadz.utils.dialog.ConfirmDialog
import id.uinjkt.salaamustadz.utils.toast

class RegisterGenderActivity : BaseActivity<ActivityRegisterGenderBinding>() {
    private var gender: String? = Constants.KEY_MAN
    private val database = FirebaseFirestore.getInstance()

    override fun getViewBinding(): ActivityRegisterGenderBinding {
        return ActivityRegisterGenderBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        setToolbar(binding.toolbar)
        binding.radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonMale -> {
                    gender = getString(R.string.label_man)
                }
                R.id.radioButtonFemale -> {
                    gender = getString(R.string.label_woman)
                }
            }
        }

        binding.buttonSignUp.setOnClickListener {
            ConfirmDialog(this, ConfirmType.CONFIRM_SUBMIT_GENDER.type){
                showLoading()
                database.collection(Constants.KEY_COLLECTION_USER)
                    .document(sharedPref.getString(Constants.KEY_USER_ID).toString())
                    .update(Field.gender, gender).addOnSuccessListener {
                        dismissLoading()
                        sharedPref.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        dismissLoading()
                        toast(it.message.toString())
                    }



            }.show(supportFragmentManager, ConfirmType.CONFIRM_SUBMIT_GENDER.type)
        }
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
    }

    override fun setupObserver() {
    }

}