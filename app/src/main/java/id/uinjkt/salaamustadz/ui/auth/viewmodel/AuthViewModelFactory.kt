package id.uinjkt.salaamustadz.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.uinjkt.salaamustadz.data.remote.repository.AuthRepository

class AuthViewModelFactory(private val repository: AuthRepository)
    : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
