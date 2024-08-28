package id.uinjkt.salaamustadz.data.models.quran

import com.google.gson.annotations.SerializedName

class ModelAyat {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("ar")
    lateinit var arab: String

    @SerializedName("idn")
    lateinit var indo: String

    @SerializedName("nomor")
    lateinit var nomor: String
}