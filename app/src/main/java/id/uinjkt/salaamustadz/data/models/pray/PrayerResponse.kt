package id.uinjkt.salaamustadz.data.models.pray

import com.google.gson.annotations.SerializedName

data class PrayerResponse(

	@field:SerializedName("data")
	val data: List<DataPrayer>
)

data class Offset(

	@field:SerializedName("Sunset")
	val sunset: Int? = null,

	@field:SerializedName("Asr")
	val asr: Int? = null,

	@field:SerializedName("Isha")
	val isha: Int? = null,

	@field:SerializedName("Fajr")
	val fajr: Int? = null,

	@field:SerializedName("Dhuhr")
	val dhuhr: Int? = null,

	@field:SerializedName("Maghrib")
	val maghrib: Int? = null,

	@field:SerializedName("Sunrise")
	val sunrise: Int? = null,

	@field:SerializedName("Midnight")
	val midnight: Int? = null,

	@field:SerializedName("Imsak")
	val imsak: Int? = null
)

data class Month(

	@field:SerializedName("number")
	val number: Int,

	@field:SerializedName("en")
	val en: String? = null,

	@field:SerializedName("ar")
	val ar: String? = null
)

data class Date(

	@field:SerializedName("readable")
	val readable: String,

	@field:SerializedName("hijri")
	val hijri: Hijri,

	@field:SerializedName("gregorian")
	val gregorian: Gregorian,

	@field:SerializedName("timestamp")
	val timestamp: String? = null
)

data class DataPrayer(

	@field:SerializedName("date")
	val date: Date,

	@field:SerializedName("meta")
	val meta: Meta? = null,

	@field:SerializedName("timings")
	val timings: Timings
)

data class Params(

	@field:SerializedName("Isha")
	val isha: Any? = null,

	@field:SerializedName("Fajr")
	val fajr: Any? = null
)

data class Location(

	@field:SerializedName("latitude")
	val latitude: Any? = null,

	@field:SerializedName("longitude")
	val longitude: Any? = null
)

data class Weekday(

	@field:SerializedName("en")
	val en: String? = null,

	@field:SerializedName("ar")
	val ar: String? = null
)

data class Designation(

	@field:SerializedName("expanded")
	val expanded: String? = null,

	@field:SerializedName("abbreviated")
	val abbreviated: String? = null
)

data class Method(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("location")
	val location: Location? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("params")
	val params: Params? = null
)

data class Gregorian(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("month")
	val month: Month? = null,

	@field:SerializedName("year")
	val year: String? = null,

	@field:SerializedName("format")
	val format: String? = null,

	@field:SerializedName("weekday")
	val weekday: Weekday? = null,

	@field:SerializedName("designation")
	val designation: Designation? = null,

	@field:SerializedName("day")
	val day: String
)

data class Meta(

	@field:SerializedName("method")
	val method: Method? = null,

	@field:SerializedName("offset")
	val offset: Offset? = null,

	@field:SerializedName("school")
	val school: String? = null,

	@field:SerializedName("timezone")
	val timezone: String? = null,

	@field:SerializedName("midnightMode")
	val midnightMode: String? = null,

	@field:SerializedName("latitude")
	val latitude: Any? = null,

	@field:SerializedName("longitude")
	val longitude: Any? = null,

	@field:SerializedName("latitudeAdjustmentMethod")
	val latitudeAdjustmentMethod: String? = null
)

data class Timings(

	@field:SerializedName("Sunset")
	val sunset: String,

	@field:SerializedName("Asr")
	val asr: String,

	@field:SerializedName("Isha")
	val isha: String,

	@field:SerializedName("Fajr")
	val fajr: String,

	@field:SerializedName("Dhuhr")
	val dhuhr: String,

	@field:SerializedName("Maghrib")
	val maghrib: String,

	@field:SerializedName("Lastthird")
	val lastthird: String,

	@field:SerializedName("Firstthird")
	val firstthird: String,

	@field:SerializedName("Sunrise")
	val sunrise: String,

	@field:SerializedName("Midnight")
	val midnight: String,

	@field:SerializedName("Imsak")
	val imsak: String
)

data class Hijri(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("month")
	val month: Month,

	@field:SerializedName("holidays")
	val holidays: List<Any?>? = null,

	@field:SerializedName("year")
	val year: String,

	@field:SerializedName("format")
	val format: String? = null,

	@field:SerializedName("weekday")
	val weekday: Weekday? = null,

	@field:SerializedName("designation")
	val designation: Designation? = null,

	@field:SerializedName("day")
	val day: String
)
