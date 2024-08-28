package id.uinjkt.salaamustadz.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.remote.repository.AuthRepository
import id.uinjkt.salaamustadz.databinding.FragmentAuthBinding
import id.uinjkt.salaamustadz.ui.auth.viewmodel.AuthViewModel
import id.uinjkt.salaamustadz.ui.auth.viewmodel.AuthViewModelFactory
import id.uinjkt.salaamustadz.ui.home.MainActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog
import id.uinjkt.salaamustadz.utils.toast
import timber.log.Timber

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding as FragmentAuthBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(requireContext()))
    }
    private lateinit var prefManager: PreferenceManager
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignIn.setOnClickListener { v ->
            val action = AuthFragmentDirections.actionAuthFragmentToSignInFragment()
            findNavController().navigate(action)
        }

        binding.btnSignUp.setOnClickListener { v ->
            val action = AuthFragmentDirections.actionAuthFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        oneTapClient = Identity.getSignInClient(requireContext())
        prefManager = PreferenceManager(requireContext())
        progressDialog = ProgressDialog(requireActivity())

        initGoogleSignIn()

//        onTapSignInGoogle()

        binding.btnSignUpWithGoogle.setOnClickListener {
            signIn()
        }
    }

//    private fun onTapSignInGoogle() {
//        oneTapClient.beginSignIn(signInRequest)
//            .addOnSuccessListener{result ->
//                try {
//                    startIntentSenderForResult(result.pendingIntent.intentSender, REQ_ONE_TAP,
//                        null, 0, 0, 0, null)
//                }catch (e: IntentSender.SendIntentException) {
//                    Timber.e("Couldn't start One Tap UI: ${e.localizedMessage}")
//                }
//            }
//            .addOnFailureListener { e ->
//                Timber.d(e.localizedMessage)
//            }
//    }

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

    // [START signin]
    fun signIn() {
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
                    progressDialog.dismissProgressDialog()
                    Timber.w("Google sign in failed", e)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
        const val RC_SIGN_IN = 100  // Can be any integer unique to the Activity
    }

}