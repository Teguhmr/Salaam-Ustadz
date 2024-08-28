package id.uinjkt.salaamustadz.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.remote.repository.AuthRepository
import id.uinjkt.salaamustadz.databinding.FragmentSignInBinding
import id.uinjkt.salaamustadz.ui.auth.AuthFragment.Companion.RC_SIGN_IN
import id.uinjkt.salaamustadz.ui.auth.viewmodel.AuthViewModel
import id.uinjkt.salaamustadz.ui.auth.viewmodel.AuthViewModelFactory
import id.uinjkt.salaamustadz.ui.home.MainActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog
import id.uinjkt.salaamustadz.utils.toast
import timber.log.Timber

class SignInFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var prefManager: PreferenceManager
    private lateinit var progressDialog: ProgressDialog
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding as FragmentSignInBinding

    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(requireContext()))
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())
        progressDialog = ProgressDialog(activity)

        binding.buttonSignIn.setOnClickListener {
            when {
                binding.inputEmail.text.toString().isEmpty() -> {
                    binding.inputEmail.error = resources.getString(R.string.email_not_valid)
                }
                binding.inputPassword.text.toString().isEmpty() -> {
                    binding.inputPassword.error = "Password kosong"
                }
                else -> {
                    progressDialog.startProgressDialog()
                    signInUser(binding.inputEmail.text.toString().trim(), binding.inputPassword.text.toString().trim())
                }
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        initGoogleSignIn()

        binding.btnSignUpWithGoogle.setOnClickListener {
            signIn()
        }

        binding.txtForgotPassword.setOnClickListener {
            val action = SignInFragmentDirections.actionSignInFragmentToForgotPasswordFragment()
            findNavController().navigate(action)

        }


    }

    private fun signInUser(email: String, password: String) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                if (auth.currentUser!!.isEmailVerified){
                    viewModel.userRole(auth.currentUser!!.uid)
                    goToMainActivity()
                    progressDialog.dismissProgressDialog()
                } else {
                    Timber.e(task.exception?.localizedMessage)
                    showToast("Login gagal, silahkan verifikasi email!")
                    progressDialog.dismissProgressDialog()
                }

            } else {
                showToast("Email atau password salah")
                progressDialog.dismissProgressDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initGoogleSignIn() {
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .setAutoSelectEnabled(false)
            .build()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

    }
    private fun signIn() {
        googleSignInClient.signOut()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN -> {
                progressDialog.startProgressDialog()
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Timber.d("firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    showToast("Masuk Gagal: $e")
                    progressDialog.dismissProgressDialog()
                    Timber.w("Google sign in failed:: $e", e)
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModel.signInWithGoogle(credential)
        viewModel.authUserLiveData.observe(viewLifecycleOwner) { user ->
            viewModel.userRole(user.id!!)
            progressDialog.dismissProgressDialog()
            goToMainActivity()
        }

    }

    private fun goToMainActivity(){
        viewModel.userRole.observe(viewLifecycleOwner) {roleUser ->
            prefManager.putString(Constants.KEY_USER_ID, roleUser.id!!)
            prefManager.putString(Constants.KEY_ROLE, roleUser.role!!)

            if (roleUser.role.equals("jamaah") && !roleUser.gender.isNullOrEmpty()){
                context?.toast("Masuk sebagai ${roleUser.email}")
                prefManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                activity?.finish()
            } else if (roleUser.role.equals("jamaah") && roleUser.gender.isNullOrEmpty()) {
                prefManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false)
                startActivity(Intent(requireActivity(), RegisterGenderActivity::class.java))
            } else {
                Firebase.auth.signOut()
                googleSignInClient.signOut()
                prefManager.clear()
                context?.toast("Tidak bisa login! Akun berstatus ${roleUser.role}")
            }

        }

    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}