package id.uinjkt.salaamustadz.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.utils.image.CompressImage
import id.uinjkt.salaamustadz.utils.image.FileUtil
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

// Function to hide the keyboard
fun EditText.hideKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }

fun String.printMeD() {
    Timber.tag("SalaamUstadz:: ").d(this)
}

fun Context.toast(msg: String){
   Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}

fun Context.toastLong(msg: String){
    Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
}

fun Context.toast(msg: Int){
    Toast.makeText(this,getString(msg),Toast.LENGTH_SHORT).show()
}

fun Context.toastNet(){
    Toast.makeText(this,R.string.err_no_net,Toast.LENGTH_SHORT).show()
}

fun snack(context: Activity,msg: String){
    Snackbar.make(context.findViewById(android.R.id.content),msg,2000).show()
}

fun snackNet(context: Activity){
    Snackbar.make(context.findViewById(android.R.id.content), R.string.err_no_net,2000).show()
}

fun View.gone(){
    this.visibility=View.GONE
}

fun View.show(){
    this.visibility=View.VISIBLE
}


fun View.hide(){
    this.visibility=View.INVISIBLE
}

fun <T>T?.orDefault(value: T): T = this ?: value

fun emptyString(): String {
    return ""
}
fun emptyDash(): String {
    return "-"
}

fun ProgressBar.toggle(show: Boolean){
    if (show)
        this.show()
    else
        this.gone();
}

fun EditText.trim() =
    this.text.toString().trim()

fun getLocaleForIndonesian(): Locale {
    return Locale("id", "ID")
}

fun Char.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this.toString())

fun NavController.isValidDestination(destination: Int): Boolean {
    return destination == this.currentDestination!!.id
}

fun RecyclerView.smoothScrollToPos(position: Int) {
    Handler(Looper.getMainLooper()).postDelayed({
        this.smoothScrollToPosition(position)
    }, 300)
}
fun <T, VH : RecyclerView.ViewHolder> ListAdapter<T, VH>.updateList(list: List<T>?) {
    this.submitList(
        if (list == this.currentList) {
            Timber.v("Same list")
            list.toList()
        } else {
            Timber.v("Not Same list")
            list
        }
    )
}

fun  <T, VH : RecyclerView.ViewHolder> ListAdapter<T,VH>.addRestorePolicy(){
    stateRestorationPolicy =
        RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

fun formatDate(inputDate: String): String {
    // Define the input and output date formats
    val inputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) // Use the appropriate locale for Indonesian
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) // Use the appropriate locale for Indonesian

    try {
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return inputDate // Return the original date if parsing fails
}

@SuppressLint("SimpleDateFormat")
fun getTimeInMillisForSpecificTime(timeString: String): Long {
    val sdf = SimpleDateFormat("HH:mm")
    val calendar = Calendar.getInstance()

    val time = sdf.parse(timeString)
    calendar.time = time as Date

    val now = Calendar.getInstance()
    now.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
    now.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
    now.set(Calendar.SECOND, 0)
    now.set(Calendar.MILLISECOND, 0)

    // Check if the time has already passed today, if so, move to the next day
    if (now.timeInMillis <= System.currentTimeMillis()) {
        now.add(Calendar.DAY_OF_YEAR, 1)
    }

    return now.timeInMillis
}
@SuppressLint("SimpleDateFormat")
fun subtractMinutes(time: String, minutesToSubtract: Int): String {
    val sdf = SimpleDateFormat("HH:mm")
    val date = sdf.parse(time)
    val calendar = Calendar.getInstance()
    calendar.time = date as Date
    calendar.add(Calendar.MINUTE, -minutesToSubtract)
    return sdf.format(calendar.time)
}

fun compressImageToByteArray(context: Context, uri: Uri): Flowable<ByteArray> {
    val outputStream = ByteArrayOutputStream()

    return CompressImage(context)
        .compressToFileAsFlowable(FileUtil.from(context, uri))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map { file ->
            val bitmapImage = BitmapFactory.decodeFile(file.absolutePath)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.toByteArray()
        }


}
fun isValidPassword(password: CharSequence): Boolean {
    val pattern: Pattern
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{8,}$"
    pattern = Pattern.compile(passwordPattern)
    val matcher: Matcher = pattern.matcher(password)
    return matcher.matches()
}

fun <T> List<T>.chunk(size: Int): List<List<T>> {
    return this.chunked(size)
}
