package id.uinjkt.salaamustadz.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.data.remote.repository.AuthRepository

class AuthViewModel (private val authRepository: AuthRepository): ViewModel() {
    lateinit var authUserLiveData: LiveData<User>
    lateinit var userRole: LiveData<User>

    fun signInWithGoogle(googleAuthCredential: AuthCredential){
        authUserLiveData = authRepository.firebaseSignInWithGoogle(googleAuthCredential)
    }

    fun userRole(authUser: String){
         userRole = authRepository.checkRoleUser(authUser)
    }

}