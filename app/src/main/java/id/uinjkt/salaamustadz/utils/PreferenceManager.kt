package id.uinjkt.salaamustadz.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private var sharedPref: SharedPreferences

    init {
        sharedPref = context.getSharedPreferences(Constants.KEY_REFERENCE_NAME, Context.MODE_PRIVATE)
    }
    fun putBoolean(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(key,value)
        editor.apply()
    }
    fun getBoolean(key: String, value: Boolean = false): Boolean {
        return sharedPref.getBoolean(key, value)
    }
    fun putString(key: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(key,value)
        editor.apply()
    }
    fun getString(key: String): String? {
        return sharedPref.getString(key,null)
    }

    fun putInt(key: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(key,value)
        editor.apply()
    }
    fun getInt(key: String, defValue: Int = -1): Int {
        return sharedPref.getInt(key,defValue)
    }
    fun clear() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removeSingleValueString(key: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(key)
        editor.apply()
    }

}