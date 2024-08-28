package id.uinjkt.salaamustadz.data.remote.repository

import id.uinjkt.salaamustadz.data.models.pray.PrayerResponse
import id.uinjkt.salaamustadz.data.remote.api.ApiInterface


class PrayerRepository (private val apiService: ApiInterface) {

    suspend fun fetchData(year: Int, month: Int, city: String, country: String): PrayerResponse {
        return apiService.getScheduleToday(year, month, city, country)
    }
}