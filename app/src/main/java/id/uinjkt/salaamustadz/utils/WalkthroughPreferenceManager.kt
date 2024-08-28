package id.uinjkt.salaamustadz.utils

import android.content.Context
import android.content.SharedPreferences

class WalkthroughPreferenceManager(context: Context) {
    private var sharedPref: SharedPreferences

    init {
        sharedPref = context.getSharedPreferences(Constants.KEY_WALKTHROUGH_NAME, Context.MODE_PRIVATE)
    }
    fun putBoolean(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(key,value)
        editor.apply()
    }
    fun getBoolean(key: String, value: Boolean = false): Boolean {
        return sharedPref.getBoolean(key, value)
    }

    fun clear() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

}