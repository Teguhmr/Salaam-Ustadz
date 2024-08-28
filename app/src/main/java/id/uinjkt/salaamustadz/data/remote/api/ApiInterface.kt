package id.uinjkt.salaamustadz.data.remote.api

import id.uinjkt.salaamustadz.data.models.doa.Doa
import id.uinjkt.salaamustadz.data.models.pray.PrayerResponse
import id.uinjkt.ustadzapp.data.models.quran.ModelAyatQuran
import id.uinjkt.ustadzapp.data.models.quran.ModelSurah
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    @GET("surat")
    suspend fun getListSurah(): ArrayList<ModelSurah>

    @GET("surat/{nomor}")
    suspend fun getDetailSurah(
        @Path("nomor") nomor: String
    ): ModelAyatQuran

    @Headers("Content-Type: application/json")
    @GET("calendarByCity/{year}/{month}")
    suspend fun getScheduleToday(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("city") city: String,
        @Query("country") country: String
    ): PrayerResponse

    @GET("api")
    suspend fun getDoa(): List<Doa>

    @GET("api/{id}")
    suspend fun getDetailDoa(
        @Path("id") id: String
    ): List<Doa>
}