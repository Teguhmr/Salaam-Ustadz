package id.uinjkt.ustadzapp.data.models.quran

import com.google.gson.annotations.SerializedName
import id.uinjkt.salaamustadz.data.models.quran.ModelAyat

data class ModelAyatQuran(

    @field:SerializedName("nama")
	val nama: String? = null,

    @field:SerializedName("ayat")
	val ayat: ArrayList<ModelAyat>,

    @field:SerializedName("nama_latin")
	val namaLatin: String? = null,

    @field:SerializedName("jumlah_ayat")
	val jumlahAyat: Int? = null,

    @field:SerializedName("tempat_turun")
	val tempatTurun: String,

    @field:SerializedName("arti")
	val arti: String? = null,

    @field:SerializedName("deskripsi")
	val deskripsi: String? = null,

    @field:SerializedName("audio")
	val audio: String? = null,

    @field:SerializedName("nomor")
	val nomor: Int? = null,

    @field:SerializedName("status")
	val status: Boolean? = null
)
