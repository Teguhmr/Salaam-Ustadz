package id.uinjkt.salaamustadz.data.remote.repository

import id.uinjkt.salaamustadz.data.remote.api.ApiClient


class QuranRepository {
    suspend fun fetchSurah() = ApiClient.getQuran().getListSurah()
    suspend fun fetchDetailSurah(id: String) = ApiClient.getQuran().getDetailSurah(id)
}