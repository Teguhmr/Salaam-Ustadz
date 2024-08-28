package id.uinjkt.salaamustadz.ui.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.uinjkt.salaamustadz.data.remote.repository.QuranRepository

class QuranViewModelFactory(private val quranRepository: QuranRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurahViewModel::class.java)) {
            return SurahViewModel(quranRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
