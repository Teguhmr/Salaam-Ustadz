package id.uinjkt.salaamustadz.ui.prayer

import id.uinjkt.salaamustadz.base.BaseActivity
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.pray.DataPrayer
import id.uinjkt.salaamustadz.data.models.pray.JadwalSholat
import id.uinjkt.salaamustadz.databinding.ActivityPrayerBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.HijriMonth
import id.uinjkt.salaamustadz.utils.NotificationType
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.subtractMinutes
import id.uinjkt.salaamustadz.utils.toast
import io.github.derysudrajat.compassqibla.CompassQibla
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerActivity : BaseActivity<ActivityPrayerBinding>() {
    private val prayerViewModel: PrayerViewModel by viewModel()
    private lateinit var reminderHandler: ReminderScheduler
    private var isAlarmRequestLaunched = false

    override fun getViewBinding(): ActivityPrayerBinding {
        return ActivityPrayerBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        with(binding){
            buttonQibla.setOnClickListener {
                startActivity(Intent(this@PrayerActivity, QiblaActivity::class.java))
            }
        }
    }

    override fun setupUI() {
        setSupportActionBar(binding.toolbar)
        Glide.with(this)
            .load(StorageUtil.pathToReference("/images/bg_mosque.jpg"))
            .placeholder(R.color.soft_yellow_green)
            .into(binding.imgToolbarBg)
        with(binding){
            layoutImsak.setTitle("Imsak")
            layoutSubuh.setTitle("Subuh")
            layoutTerbit.setTitle("Terbit")
            layoutDzuhur.setTitle("Dzuhur")
            layoutAshar.setTitle("Ashar")
            layoutMagrib.setTitle("Maghrib")
            layoutIsya.setTitle("Isya'")
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    override fun setupAction() {
        var isReminderOn = reminderHandler.isRemindersEnabled()
        binding.btnReminder.setOnClickListener {
            if (!isReminderOn){
                setRemindersOn()
                setReminderPrayer()
                reminderHandler.enableReminders()
                toast(getString(R.string.label_reminder_is_on, sharedPref.getInt(Constants.KEY_NOTIFICATION_TIME).toString()))
            } else {
                setRemindersOff()
                reminderHandler.disableReminders()
                cancelReminderPrayer()
                toast(getString(R.string.label_reminder_is_off))
            }
            isReminderOn = !isReminderOn
        }

        if (reminderHandler.isRemindersEnabled()){
            setRemindersOn()
        } else {
            setRemindersOff()
        }

    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isAlarmRequestLaunched){
            if (reminderHandler.isRemindersEnabled()){
                cancelReminderPrayer()
                sharedPref.removeSingleValueString(Constants.KEY_REMINDERS_TIME)
                setReminderPrayer()
            }
            setupObserver()
        }
    }

    private fun dateGregorianAndHijri(dayHijri: String, monthNumberHijri: Int, yearHijri: String) {
        binding.apply {
            val indonesia = Locale("id", "ID")
            val simpleDateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", indonesia)
            val date = simpleDateFormat.format(Date())
            tvDate.text = date

            val hijriDate =
                "$dayHijri ${HijriMonth.getNumber(monthNumberHijri).title} $yearHijri H"
            tvIslamicDate.text = hijriDate
        }
    }

    private fun setReminderPrayer(){
        val getJsonString = sharedPref.getString(Constants.KEY_PRAYER_SCHEDULE)

        if (getJsonString != null){
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            val retrievedMap: Map<String, String> = Gson().fromJson(getJsonString, mapType)
            val remainingTime = sharedPref.getInt(Constants.KEY_NOTIFICATION_TIME)

            for ((prayer, time) in retrievedMap) {
                val reminderMessage =  "$remainingTime menit menuju $prayer pada $time. (${sharedPref.getString(Constants.KEY_CITY)})"
                reminderHandler.scheduleReminderWithNotification(subtractMinutes(time, remainingTime), reminderMessage)
            }
        }
    }
    private fun cancelReminderPrayer(){
        val getJsonString = sharedPref.getString(Constants.KEY_REMINDERS_TIME)

        if (getJsonString != null){
            val mapType = object : TypeToken<ArrayList<Int>>() {}.type
            val retrievedMap: ArrayList<Int> = Gson().fromJson(getJsonString, mapType)

            for (time in retrievedMap) {
                reminderHandler.cancelRemindersNotification(time)
            }
        }
    }

    private fun setRemindersOn(){
        with(binding){
            icReminder.setImageResource(R.drawable.ic_notification)
            icReminder.setColorFilter(getColor(R.color.color_text_orange), PorterDuff.Mode.SRC_IN)
            tvReminder.text = getString(R.string.label_is_on)
            tvReminder.setTextColor(getColor(R.color.color_text_orange))
        }
    }
    private fun setRemindersOff(){
        with(binding){
            icReminder.setImageResource(R.drawable.ic_notification)
            icReminder.setColorFilter(getColor(R.color.secondary_text), PorterDuff.Mode.SRC_IN)
            tvReminder.text = getString(R.string.label_is_off)
            tvReminder.setTextColor(getColor(R.color.secondary_text))
        }
    }

    override fun setupProcess() {
        reminderHandler = ReminderScheduler(this)
        setCurrentLocation()
    }

    @SuppressLint("SimpleDateFormat")
    override fun setupObserver() {
        lifecycleScope.launch {
            prayerViewModel.dataState.collect{ result ->
                when (result){
                    is Result.Error -> {
                        dismissLoading()
                        toast(result.message.toString())
                        Timber.tag("error").e(result.message.toString())
                    }
                    is Result.Loading -> showLoading(true)
                    is Result.Success -> {
                        dismissLoading()

                        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                            if (!isAlarmRequestLaunched && !alarmManager.canScheduleExactAlarms()) {
                                isAlarmRequestLaunched = true
                                launchAlarmRequest()
                            } else if (alarmManager.canScheduleExactAlarms()) {
                                scheduleExactAlarms(result.data)
                            }
                        } else {
                            scheduleExactAlarms(result.data)
                        }


                    }
                }
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun launchAlarmRequest() {
        Intent().apply {
            action = ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            data = Uri.parse("package:$packageName")
        }.also {
            startActivity(it)
        }
    }

    private fun scheduleExactAlarms(data: List<DataPrayer>?) {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        data?.filter {
            it.date.gregorian.day.toInt() == today
        }?.map { prayer ->
            with(binding){
                prayer.apply {
                    layoutImsak.setTime(prayer.timings.imsak)
                    layoutSubuh.setTime(prayer.timings.fajr)
                    layoutTerbit.setTime(prayer.timings.sunrise)
                    layoutDzuhur.setTime(prayer.timings.dhuhr)
                    layoutAshar.setTime(prayer.timings.asr)
                    layoutMagrib.setTime(prayer.timings.maghrib)
                    layoutIsya.setTime(prayer.timings.isha)
                }
            }
            val pray = (
                    JadwalSholat(
                        imsak = extractTime(prayer.timings.imsak),
                        subuh = extractTime(prayer.timings.fajr),
                        terbit = extractTime(prayer.timings.sunrise),
                        dzuhur = extractTime(prayer.timings.dhuhr),
                        ashar = extractTime(prayer.timings.asr),
                        maghrib = extractTime(prayer.timings.maghrib),
                        isya = extractTime(prayer.timings.isha),
                    ))
            dateGregorianAndHijri(
                dayHijri = prayer.date.hijri.day,
                monthNumberHijri = prayer.date.hijri.month.number,
                yearHijri = prayer.date.hijri.year
            )
            getNextPrayerTime(pray)
            Timber.tag("schedule").d(pray.toString())
        }
    }

    private fun extractTime(input: String): String {
        // Assuming the input format is "HH:mm (TimeZone)" or "HH:mm (+Offset)"
        return input.substring(0, 5)
    }


    @SuppressLint("SimpleDateFormat")
    private fun getNextPrayerTime(prayerSchedule: JadwalSholat): String {
        val currentTime = SimpleDateFormat("HH:mm").format(Date())
        val upcomingPrayerTimes: Map<String, String> = mapOf(
            "Imsak" to prayerSchedule.imsak,
            "Shubuh" to prayerSchedule.subuh,
            "Terbit" to prayerSchedule.terbit,
            "Dzuhur" to prayerSchedule.dzuhur,
            "Ashar" to prayerSchedule.ashar,
            "Maghrib" to prayerSchedule.maghrib,
            "Isya" to prayerSchedule.isya
        )

        sharedPref.removeSingleValueString(Constants.KEY_PRAYER_SCHEDULE)
        val jsonString = Gson().toJson(upcomingPrayerTimes)
        sharedPref.putString(Constants.KEY_PRAYER_SCHEDULE, jsonString)

        if (sharedPref.getInt(Constants.KEY_NOTIFICATION_TIME) == -1 ||
            sharedPref.getString(Constants.KEY_NOTIFICATION_TYPE_PRAYER) == null
        ){
            reminderHandler.enableReminders()
            sharedPref.putInt(Constants.KEY_NOTIFICATION_TIME, 15)
            sharedPref.putString(Constants.KEY_NOTIFICATION_TYPE_PRAYER, NotificationType.NOTIFICATION_VIBRATE.title)
        }

        if (reminderHandler.isRemindersEnabled()){
            cancelReminderPrayer()
            sharedPref.removeSingleValueString(Constants.KEY_REMINDERS_TIME)
            setReminderPrayer()
        }

        for (time in upcomingPrayerTimes.values.toList()) {

            if (time > currentTime) {
                with(binding){
                    when (time){
                        prayerSchedule.imsak -> layoutImsak.setBackground()
                        prayerSchedule.subuh -> layoutSubuh.setBackground()
                        prayerSchedule.terbit -> layoutTerbit.setBackground()
                        prayerSchedule.dzuhur -> layoutDzuhur.setBackground()
                        prayerSchedule.ashar -> layoutAshar.setBackground()
                        prayerSchedule.maghrib -> layoutMagrib.setBackground()
                        prayerSchedule.isya -> layoutIsya.setBackground()
                    }
                }
                return time
            }
        }


        return ""
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CompassQibla.Builder(this).onPermissionGranted {
            val intent = intent
            finish()
            startActivity(intent)
        }
    }

    private fun setCurrentLocation() {
        if (isConnectedToInternet(this)) {
            CompassQibla.Builder(this).onGetLocationAddress { address ->
                binding.tvLocation.text = buildString {
                    append(address.locality)
                    append(", ")
                    append(address.subAdminArea ?: address.adminArea)
                }
                sharedPref.putString(Constants.KEY_CITY, address.subAdminArea ?: address.adminArea)
                sharedPref.putString(Constants.KEY_COUNTRY, address.countryName.orEmpty())
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                if (address.subAdminArea != null || address.countryName != null) {
                    prayerViewModel.getPrayerData(
                        year = currentYear,
                        month = currentMonth,
                        city = address.subAdminArea ?: address.adminArea,
                        country = address.countryName
                    )
                } else {
                    toast("Maaf, lokasi belum didukung")
                }
            }.build()
        } else {
            // Handle no internet connection
            toast("No internet connection")
        }
    }

    private fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}