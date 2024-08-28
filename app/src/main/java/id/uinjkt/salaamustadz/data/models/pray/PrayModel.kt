package id.uinjkt.salaamustadz.data.models.pray

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrayModel(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("data")
    val data: DataSholat,

    ) : Parcelable

@Parcelize
data class DataSholat(
    @SerializedName("id")
    val id: String,

    @SerializedName("lokasi")
    val lokasi: String,

    @SerializedName("daerah")
    val daerah: String,

    @SerializedName("status")
    val koordinat: DataKoordinat,

    @SerializedName("jadwal")
    val jadwal: JadwalSholat

    ) : Parcelable

@Parcelize
data class DataKoordinat(
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("lintang")
    val lintang: String,
    @SerializedName("bujur")
    val bujur: String,
) : Parcelable


@Parcelize
data class JadwalSholat(

    @SerializedName("imsak")
    val imsak: String,

    @SerializedName("subuh")
    val subuh: String,

    @SerializedName("terbit")
    val terbit: String,

    @SerializedName("dzuhur")
    val dzuhur: String,

    @SerializedName("ashar")
    val ashar: String,

    @SerializedName("maghrib")
    val maghrib: String,

    @SerializedName("isya")
    val isya: String,

) : Parcelable

@Parcelize
data class AllCities(
    @SerializedName("id")
    val id: String,

    @SerializedName("lokasi")
    val location: String
): Parcelable


