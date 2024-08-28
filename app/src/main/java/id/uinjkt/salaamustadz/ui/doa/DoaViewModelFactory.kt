package id.uinjkt.salaamustadz.ui.doa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.uinjkt.salaamustadz.data.remote.repository.DoaRepository

class DoaViewModelFactory(private val doaRepository: DoaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoaViewModel::class.java)) {
            return DoaViewModel(doaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
