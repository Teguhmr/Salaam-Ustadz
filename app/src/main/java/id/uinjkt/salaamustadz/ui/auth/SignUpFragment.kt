package id.uinjkt.salaamustadz.ui.auth

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.hbb20.CountryCodePicker
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.FragmentSignUpBinding
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog
import id.uinjkt.salaamustadz.utils.dialog.VerifyEmailDialog
import id.uinjkt.salaamustadz.utils.emptyDash
import id.uinjkt.salaamustadz.utils.emptyString
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern


class SignUpFragment : Fragment() {
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private var birthDate: String = emptyString()
    private var gender: String? = Constants.KEY_MAN
    private lateinit var phone: String
    private lateinit var cpassword: String
    private lateinit var progressDialog: ProgressDialog
    private lateinit var ccp: CountryCodePicker
    private var phoneNumber: String = emptyDash()
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding as FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(activity)
        ccp = binding.countryCodePicker
        ccp.registerCarrierNumberEditText(binding.edtPhone)

        binding.inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                validateName()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                validateEmail()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                validatePassword()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.edtPhone.doOnTextChanged { text, _, _, _ ->
            phone = text.toString()
             if (phone.isNotEmpty()){
                phoneNumber =  ccp.fullNumberWithPlus
            }
        }

        binding.btnDatePicker.setOnClickListener {
            datePicker()
        }

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


        binding.buttonSignUp.setOnClickListener{
            progressDialog.startProgressDialog()
            if (!validateName() or !validateEmail() or !validatePassword() or !validateConfirmPassword() or !validateCheckTermsPrivacy()) {
                progressDialog.dismissProgressDialog()
            } else {

                val user = User(
                    id = emptyString(),
                    name = name,
                    email = email,
                    password = emptyString(),
                    role = "jamaah",
                    birthDate = birthDate,
                    phone = phoneNumber,
                    createdAt = Timestamp(Date()),
                    updatedAt = Timestamp(Date()),
                    gender = gender)
                registerUser(user)
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }


    }


    private fun registerUser(user: User) {
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.id = auth.currentUser!!.uid
                val fUser = Firebase.auth.currentUser
                if (fUser != null){
                    val profileUpdate = userProfileChangeRequest {
                        displayName = user.name
                    }
                    fUser.updateProfile(profileUpdate).addOnCompleteListener {
                        if (task.isSuccessful) {
                            Timber.tag("UserProfile").d("User profile updated.")
                            fUser.sendEmailVerification().addOnCompleteListener {
                                if (it.isSuccessful){
                                    saveFireStoreData(user.id!!, user)
                                    progressDialog.dismissProgressDialog()

                                }
                            }
                        }
                    }

                }

            } else {
                showToast("Email sudah digunakan!!")
                Timber.tag("ErrorAuth").e(task.exception.toString())
                progressDialog.dismissProgressDialog()
            }
        }


    }

    private fun saveFireStoreData(uid: String, user: User){
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(uid).set(user, SetOptions.merge()).addOnCompleteListener {
            VerifyEmailDialog(requireContext()){findNavController().navigateUp()}.show(parentFragmentManager, "dialog")
        }.addOnFailureListener {
            showToast("Gagal menambahkan data")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("SetTextI18n")
    private fun datePicker(){
        val c = Calendar.getInstance()

        val day = c.get(Calendar.DAY_OF_MONTH)
        val month = c.get(Calendar.MONTH)
        val year = c.get(Calendar.YEAR)

        val datePickerDialog = DatePickerDialog(
            requireContext(), R.style.my_dialog_theme,
            {view, years, monthOfYear, dayOfMoth ->
                c.set(Calendar.YEAR, years)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMoth)
                val myFormat = "dd MMMM yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale("id", "ID"))
                binding.tvChoseDate.text = sdf.format(c.time)

                birthDate = binding.tvChoseDate.text.toString().trim()
                binding.tvChoseDate.error = null
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun validateEmail(): Boolean {
        // jika password tidak valid tampilkan peringatan
        email = binding.inputEmail.text.toString().trim()
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showEmailExistAlert(true)
            false
        } else {
            showEmailExistAlert(false)
            true
        }
    }

    private fun validateName(): Boolean {
        // jika nama kosong
        name = binding.inputName.text.toString().trim()
        //    Boolean encodedImages;
        return if (TextUtils.isEmpty(name)) {
            showNameValid(true)
            false
        } else {
            showNameValid(false)
            true
        }
    }

    private fun validatePassword(): Boolean {
        // jika password < 6 karakter tampilkan peringatan
        password = binding.inputPassword.text.toString().trim()
        return if (password.length < 8) {
            showPasswordMinimalAlert(true)
            false
        }else if (!isValidPassword(password)) {
            showPasswordPatternAlert(true)
            false
        }
        else {
            showPasswordMinimalAlert(false)
            showPasswordPatternAlert(false)
            true
        }
    }

    private fun validateConfirmPassword(): Boolean {
        // jika tidak sama
        cpassword = binding.inputConfirmPassword.text.toString().trim()
        //    Boolean encodedImages;
        return if (cpassword != password) {
            showConfirmPasswordAlert(true)
            false
        } else {
            showConfirmPasswordAlert(false)
            true
        }
    }

    private fun isValidPassword(password: CharSequence): Boolean {
        val pattern: Pattern
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{8,}$"
        pattern = Pattern.compile(passwordPattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }


    private fun showNameValid(isNotValid: Boolean) {
        if (isNotValid) {
            binding.inputName.error = getString(R.string.name_not_valid)
        }
    }

    private fun validateCheckTermsPrivacy(): Boolean {
        return if (!binding.checkTermsPrivacy.isChecked) {
            Toast.makeText(context, resources.getString(R.string.error_terms), Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }

    }

    private fun showEmailExistAlert(isNotValid: Boolean) {
        if (isNotValid) {
            binding.inputEmail.error = getString(R.string.email_not_valid)
        }
    }

    private fun showPasswordMinimalAlert(isNotValid: Boolean) {
        if (isNotValid) {
            binding.inputPassword.error = getString(R.string.password_not_valid)
        }
    }

    private fun showPasswordPatternAlert(isNotValid: Boolean) {
        if (isNotValid) {
            binding.inputPassword.error = getString(R.string.password_not_pattern)
        }
    }

    private fun showConfirmPasswordAlert(isNotValid: Boolean) {
        if (isNotValid) {
            binding.inputConfirmPassword.error = getString(R.string.password_not_same)
        }
    }

    private fun showBirthDateAlert(isNotValid: Boolean) {
        if (isNotValid) {
            binding.tvChoseDate.requestFocus()
            binding.tvChoseDate.error = "Pilih tanggal lahir"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}