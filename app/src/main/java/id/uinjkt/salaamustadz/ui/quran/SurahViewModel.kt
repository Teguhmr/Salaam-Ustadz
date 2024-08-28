package id.uinjkt.salaamustadz.ui.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.uinjkt.salaamustadz.data.remote.repository.QuranRepository
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.ustadzapp.data.models.quran.ModelAyatQuran
import id.uinjkt.ustadzapp.data.models.quran.ModelSurah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SurahViewModel(private val repository: QuranRepository) : ViewModel() {
    private val _dataState: MutableStateFlow<Result<List<ModelSurah>>> = MutableStateFlow(Result.Loading())
    val dataState: StateFlow<Result<List<ModelSurah>>> = _dataState

    private val _dataDetailState: MutableStateFlow<Result<ModelAyatQuran>> = MutableStateFlow(Result.Loading())
    val dataDetailState: StateFlow<Result<ModelAyatQuran>> = _dataDetailState


    init {
        setSurah()
    }
    private fun setSurah() {
        viewModelScope.launch {
            try {
                val data = repository.fetchSurah()
                _dataState.value = Result.Success(data)
            } catch (e: Exception) {
                _dataState.value = Result.Error(e.toString())
            }
        }
    }

    fun setDetailSurah(id: String) {
        viewModelScope.launch {
            try {
                val data = repository.fetchDetailSurah(id)
                _dataDetailState.value = Result.Success(data)
            } catch (e: Exception) {
                _dataDetailState.value = Result.Error(e.toString())
            }
        }
    }

}