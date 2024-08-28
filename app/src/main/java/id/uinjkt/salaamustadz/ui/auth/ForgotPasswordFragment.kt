package id.uinjkt.salaamustadz.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.uinjkt.salaamustadz.databinding.FragmentForgotPasswordBinding
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog
import id.uinjkt.salaamustadz.utils.toast
import timber.log.Timber

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding as FragmentForgotPasswordBinding
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(requireActivity())
        binding.buttonSendEmail.setOnClickListener {
            if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches() or binding.inputEmail.text.toString().isEmpty()){
                context?.toast("Masukan email yang valid!")
            } else {
                progressDialog.startProgressDialog()
                Firebase.auth.sendPasswordResetEmail(binding.inputEmail.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val action = ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToEmailSendSuccessFragment()
                            findNavController().navigate(action)
                            Timber.tag("Reset Password").d("Email sent.")
                            progressDialog.dismissProgressDialog()
                        } else {
                            context?.toast("Akun tidak ditemukan.")
                            progressDialog.dismissProgressDialog()
                        }
                    }
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}