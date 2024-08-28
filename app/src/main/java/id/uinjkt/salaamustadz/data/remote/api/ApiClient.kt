package id.uinjkt.salaamustadz.data.remote.api

import id.uinjkt.salaamustadz.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ApiClient {
    companion object {
        private var retrofit: Retrofit? = null
        fun getInstance(): Retrofit{
            if(retrofit ==null) {
                retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(
                    ScalarsConverterFactory.create()).build()
            }
            return retrofit!!
        }
        private const val BASE_URL_QURAN = "https://equran.id/api/"
        fun getQuran(): ApiInterface {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_QURAN)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiInterface::class.java)
        }
        private const val BASE_URL_JADWAL_SHOLAT = "http://api.aladhan.com/v1/"
        fun getSchedulePrayer(): ApiInterface {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_JADWAL_SHOLAT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiInterface::class.java)
        }

        private const val BASE_URL_DOA = "https://doa-doa-api-ahmadramadhan.fly.dev/"
        fun getDoaInstance(): ApiInterface {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_DOA)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiInterface::class.java)
        }


    }
}