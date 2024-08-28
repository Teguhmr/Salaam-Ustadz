package id.uinjkt.salaamustadz.data.remote.repository

import id.uinjkt.salaamustadz.data.remote.api.ApiClient


class DoaRepository {
    suspend fun fetchData() = ApiClient.getDoaInstance().getDoa()
    suspend fun fetchDetailData(id: String) = ApiClient.getDoaInstance().getDetailDoa(id)
}