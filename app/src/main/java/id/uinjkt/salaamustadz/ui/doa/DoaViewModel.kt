package id.uinjkt.salaamustadz.ui.doa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.uinjkt.salaamustadz.data.models.doa.Doa
import id.uinjkt.salaamustadz.data.remote.repository.DoaRepository
import id.uinjkt.salaamustadz.state.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DoaViewModel (private val repository: DoaRepository) : ViewModel() {

    private val _dataState: MutableStateFlow<Result<List<Doa>>> = MutableStateFlow(Result.Loading())
    val dataState: StateFlow<Result<List<Doa>>> = _dataState

    private val _dataDetailState: MutableStateFlow<Result<List<Doa>>> = MutableStateFlow(Result.Loading())
    val dataDetailState: StateFlow<Result<List<Doa>>> = _dataDetailState

    init {
        getDoa()
    }

    private fun getDoa() {
        viewModelScope.launch {
            try {
                val data = repository.fetchData()
                _dataState.value = Result.Success(data)
            } catch (e: Exception) {
                _dataState.value = Result.Error(e.toString())
            }
        }
    }

    fun getDetailDoa(id: String) {
        viewModelScope.launch {
            try {
                val data = repository.fetchDetailData(id)
                _dataDetailState.value = Result.Success(data)
            } catch (e: Exception) {
                _dataDetailState.value = Result.Error(e.toString())
            }
        }
    }
}