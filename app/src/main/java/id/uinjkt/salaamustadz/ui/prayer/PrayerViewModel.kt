package id.uinjkt.salaamustadz.ui.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.uinjkt.salaamustadz.data.models.pray.DataPrayer
import id.uinjkt.salaamustadz.data.remote.repository.PrayerRepository
import id.uinjkt.salaamustadz.state.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrayerViewModel (private val repository: PrayerRepository) : ViewModel() {

    private val _dataState: MutableStateFlow<Result<List<DataPrayer>>> = MutableStateFlow(Result.Loading())
    val dataState: StateFlow<Result<List<DataPrayer>>> = _dataState

    fun getPrayerData(year: Int, month: Int, city: String, country: String) {
        viewModelScope.launch {
            try {
                val data = repository.fetchData(year, month, city, country)
                _dataState.value = Result.Success(data.data)
            } catch (e: Exception) {
                _dataState.value = Result.Error(e.toString())
            }
        }
    }
}